package eu.tutorials.gooddeedproject.organizer

import android.app.Notification
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.models.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

// Data model for dashboard action items

class OrganizerViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    // StateFlows for all the data
    private val _organizerProfile = MutableStateFlow<UserProfile?>(null)
    val organizerProfile: StateFlow<UserProfile?> = _organizerProfile.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents.asStateFlow()

    private val _completedEvents = MutableStateFlow<List<Event>>(emptyList())
    val completedEvents: StateFlow<List<Event>> = _completedEvents.asStateFlow()

    private val _volunteersForEvent = MutableStateFlow<Map<String, List<UserProfile>>>(emptyMap())
    val volunteersForEvent: StateFlow<Map<String, List<UserProfile>>> = _volunteersForEvent.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // StateFlows for Stats
    val eventsOrganizedCount: StateFlow<Int> = _completedEvents.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVolunteerHours: StateFlow<Int> = _completedEvents.map { events ->
        events.sumOf { it.registeredUserIds.size * it.durationInHours }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadNotificationCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Sample data (can be made live later)
    val actionItems = listOf(
        ActionItem("3 New Volunteer Requests"),
        ActionItem("Approve hours for 'Food Drive'")
    )

    // Listeners that we need to be able to cancel
    private var profileListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null

    init {
        // ✅ THE FIX: Listen to auth changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User logged IN -> Start new listeners for this user
                loadOrganizerData(user.uid)
            } else {
                // User logged OUT -> Stop old listeners and clear all data
                cancelAllListeners()
                _organizerProfile.value = null
                _upcomingEvents.value = emptyList()
                _completedEvents.value = emptyList()
                _volunteersForEvent.value = emptyMap()
                _notifications.value = emptyList()
            }
        }
    }

    private fun loadOrganizerData(userId: String) {
        // Cancel any old listeners before starting new ones
        cancelAllListeners()

        // Listener for the organizer's profile data
        profileListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    _organizerProfile.value = snapshot.toObject(UserProfile::class.java)
                } else if (error != null) { Log.e("OrganizerViewModel", "Error listening to profile", error) }
            }

        // Listener for the organizer's events
        eventsListener = db.collection("events")
            .whereEqualTo("organizerId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val allMyEvents = snapshot.toObjects(Event::class.java)
                    val currentTime = System.currentTimeMillis()
                    _upcomingEvents.value = allMyEvents.filter { it.date >= currentTime }.sortedBy { it.date }
                    _completedEvents.value = allMyEvents.filter { it.date < currentTime }
                } else if (error != null) { Log.e("OrganizerViewModel", "Error listening to events", error) }
            }

        // Listener for notifications
        notificationsListener = db.collection("notifications")
            .whereEqualTo("organizerId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    _notifications.value = snapshot.toObjects(Notification::class.java)
                } else if (error != null) { Log.e("OrganizerViewModel", "Error listening to notifications", error) }
            }
    }

    private fun cancelAllListeners() {
        profileListener?.remove()
        eventsListener?.remove()
        notificationsListener?.remove()
    }

    // This is called when the ViewModel is finally destroyed
    override fun onCleared() {
        super.onCleared()
        cancelAllListeners()
    }

    // --- All other functions (createEvent, deleteEvent, etc.) remain the same ---

    fun fetchVolunteersForEvent(eventId: String, volunteerIds: List<String>) {
        if (volunteerIds.isEmpty()) {
            _volunteersForEvent.update { it + (eventId to emptyList()) }
            return
        }
        if (_volunteersForEvent.value.containsKey(eventId)) return

        viewModelScope.launch {
            try {
                val volunteers = db.collection("users")
                    .whereIn("uid", volunteerIds)
                    .get().await().toObjects(UserProfile::class.java)
                _volunteersForEvent.update { it + (eventId to volunteers) }
            } catch (e: Exception) {
                Log.e("OrganizerViewModel", "Error fetching volunteers", e)
            }
        }
    }

    fun createEvent(
        title: String, description: String, category: String, location: String,
        date: Long, time: String, durationInHours: Int, imageUri: Uri, onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            val organizer = _organizerProfile.value
            if (user == null || organizer == null) {
                onComplete(false); return@launch
            }

            try {
                val imageRef = storage.reference.child("event_images/${UUID.randomUUID()}")
                val downloadUrl = imageRef.putFile(imageUri).await().storage.downloadUrl.await().toString()
                val eventId = db.collection("events").document().id

                val newEvent = Event(
                    id = eventId, organizerId = user.uid, organizerName = organizer.name,
                    organizerLogoUrl = organizer.profilePicUrl, title = title,
                    description = description, category = category, location = location,
                    date = date, time = time, imageUrl = downloadUrl,
                    durationInHours = durationInHours
                )

                db.collection("events").document(eventId).set(newEvent).await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("OrganizerViewModel", "Error creating event", e)
                onComplete(false)
            }
        }
    }

    fun updateEvent(
        event: Event, // Purana event object
        // Nayi details:
        title: String,
        description: String,
        location: String,
        date: Long,
        time: String,
        category: String,
        durationInHours: Int,
        newImageUri: Uri?, // Nayi image, agar select ki hai
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var imageUrl = event.imageUrl // Default mein purani image URL rakho

                // 1. Agar user ne nayi image select ki hai, toh use upload karo
                if (newImageUri != null) {
                    // Hum event ki ID ko image ka naam de sakte hain taaki woh unique rahe
                    val imageRef = storage.reference.child("event_images/${event.id}")
                    imageUrl = imageRef.putFile(newImageUri).await()
                        .storage.downloadUrl.await().toString()
                }

                // 2. Fields ka map banao jo update karni hain
                val updates = mapOf(
                    "title" to title,
                    "description" to description,
                    "location" to location,
                    "date" to date,
                    "time" to time,
                    "category" to category,
                    "durationInHours" to durationInHours,
                    "imageUrl" to imageUrl
                )

                // 3. Firestore document ko update karo
                db.collection("events").document(event.id).update(updates).await()
                onComplete(true) // Success

            } catch (e: Exception) {
                Log.e("OrganizerViewModel", "Error updating event", e)
                onComplete(false) // Failure
            }
        }
    }

    fun deleteEvent(event: Event, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("events").document(event.id).delete().await()
                if (event.imageUrl.isNotBlank()) {
                    storage.getReferenceFromUrl(event.imageUrl).delete().await()
                }
                onComplete(true)
            } catch (e: Exception) {
                Log.e("OrganizerViewModel", "Error deleting event", e)
                onComplete(false)
            }
        }
    }

    fun updateProfile(
        name: String, phone: String, city: String, bio: String,
        newLogoUri: Uri?, onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run { onComplete(false); return@launch }
            try {
                var imageUrl = organizerProfile.value?.profilePicUrl ?: ""
                if (newLogoUri != null) {
                    val imageRef = storage.reference.child("profile_pics/$userId")
                    imageUrl = imageRef.putFile(newLogoUri).await().storage.downloadUrl.await().toString()
                }
                val updates = mapOf(
                    "name" to name, "phone" to phone, "city" to city,
                    "bio" to bio, "profilePicUrl" to imageUrl
                )
                db.collection("users").document(userId).update(updates).await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("OrganizerViewModel", "Error updating profile", e)
                onComplete(false)
            }
        }
    }

    data class Notification(
        val id: String = "",
        val organizerId: String = "",
        val message: String = "",
        val type: String = "",
        val isRead: Boolean = false,
        val timestamp: Long = 0L
    )

    fun markNotificationsAsRead() {
        val userId = auth.currentUser?.uid ?: return
        _notifications.update { currentNotifications ->
            currentNotifications.map { it.copy(isRead = true) }
        }
        viewModelScope.launch {
            try {
                val unreadDocs = db.collection("notifications")
                    .whereEqualTo("organizerId", userId)
                    .whereEqualTo("isRead", false)
                    .get().await()
                val batch = db.batch()
                unreadDocs.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            } catch (e: Exception) {
                Log.e("OrganizerViewModel", "Error marking notifications as read", e)
            }
        }
    }
}