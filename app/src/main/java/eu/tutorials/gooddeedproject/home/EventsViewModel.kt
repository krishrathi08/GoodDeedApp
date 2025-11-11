package eu.tutorials.gooddeedproject.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.models.UserProfile
import eu.tutorials.gooddeedproject.organizer.OrganizerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EventsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(60_000) // 60 seconds
        }
    }

    private val _registeredUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val registeredUsers: StateFlow<List<UserProfile>> = _registeredUsers

    val completedEvents: StateFlow<List<Event>> = combine(_userProfile, _allEvents, ticker) { user, events, _ ->
        val participatedIds = user?.participatedEventIds ?: emptyList()
        val upcomingIds = user?.upcomingEventIds ?: emptyList()
        val userEventIds = (participatedIds + upcomingIds).toSet()
        val currentTime = System.currentTimeMillis()
        events.filter { event -> event.date < currentTime && userEventIds.contains(event.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingEvents: StateFlow<List<Event>> = combine(_userProfile, _allEvents, ticker) { user, events, _ ->
        val upcomingIds = user?.upcomingEventIds ?: emptyList()
        val currentTime = System.currentTimeMillis()
        events.filter { event -> event.date >= currentTime && upcomingIds.contains(event.id) }
            .sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestedEvents: StateFlow<List<Event>> = combine(_userProfile, _allEvents, ticker) { user, events, _ ->
        val userEventIds = (user?.participatedEventIds ?: emptyList()) + (user?.upcomingEventIds ?: emptyList())
        val currentTime = System.currentTimeMillis()
        events.filter { event ->
            event.date >= currentTime && !userEventIds.contains(event.id)
        }.sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTotalHours: StateFlow<Int> = completedEvents.map { events ->
        // Sums up the 'durationInHours' for all events in the completed list
        events.sumOf { it.durationInHours }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        listenToDataChanges()
    }

    private fun listenToDataChanges() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                _userProfile.value = null
                _allEvents.value = emptyList()
                return@addAuthStateListener
            }

            db.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        _userProfile.value = snapshot.toObject<UserProfile>()
                    } else if (error != null) {
                        Log.e("EventsViewModel", "Error listening to user profile", error)
                    }
                }

            db.collection("events").orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        _allEvents.value = snapshot.toObjects(Event::class.java)
                    } else if (error != null) {
                        Log.e("EventsViewModel", "Error listening to events", error)
                    }
                }
        }
    }

    fun registerForEvent(event: Event) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _snackbarMessage.emit("You must be logged in to register.")
                return@launch
            }

            try {
                val eventRef = db.collection("events").document(event.id)
                val userRef = db.collection("users").document(userId)

                // ✅ We get the user profile's current value here
                val currentUserProfile = _userProfile.value

                // ✅ All batch operations must be inside this block
                db.runBatch { batch ->
                    // 1. Update the event
                    batch.update(eventRef, "registeredUserIds", FieldValue.arrayUnion(userId))
                    // 2. Update the user
                    batch.update(userRef, "upcomingEventIds", FieldValue.arrayUnion(event.id))

                    // 3. Create and set the notification within the same batch
                    val notificationId = db.collection("notifications").document().id
                    val notificationMessage = "${currentUserProfile?.name ?: "A new user"} registered for your event: ${event.title}"
                    val notification = OrganizerViewModel.Notification(
                        id = notificationId,
                        organizerId = event.organizerId,
                        message = notificationMessage,
                        type = "NEW_VOLUNTEER",
                        timestamp = System.currentTimeMillis()
                    )
                    batch.set(db.collection("notifications").document(notificationId), notification)

                }.await() // The await() should be after the block

                _snackbarMessage.emit("Successfully registered for ${event.title}!")

            } catch (e: Exception) {
                _snackbarMessage.emit("Registration failed: ${e.message}")
                Log.e("EventsViewModel", "Registration failed", e)
            }
        }
    }

    fun fetchRegisteredUsers(event: Event) {
        if (event.registeredUserIds.isEmpty()) {
            _registeredUsers.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                // This query fetches all user profiles whose UID is in the event's list
                val usersSnapshot = db.collection("users")
                    .whereIn("uid", event.registeredUserIds)
                    .get()
                    .await()

                _registeredUsers.value = usersSnapshot.toObjects(UserProfile::class.java)
            } catch (e: Exception) {
                Log.e("EventsViewModel", "Error fetching registered users", e)
                _registeredUsers.value = emptyList() // Clear on error
            }
        }
    }
}