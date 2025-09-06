package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data model for the user's profile information
data class UserProfile(
    val uid: String = "",
    val name: String = "User Name",
    val joinDate: String = "",
    val profilePicUrl: String = "", // This will hold the URL from Firebase Storage
    val totalHours: Int = 0,
    val eventsAttended: Int = 0,
    val ngosHelped: Int = 0
)

// Data model for a single badge
data class Badge(
    @DrawableRes val imageRes: Int
)

// Data model for a completed event entry
data class CompletedEvent(
    val title: String,
    val details: String,
    @DrawableRes val imageRes: Int
)

// --- Sample Data (for badges and completed events) ---
val sampleBadges = listOf(
    Badge(R.drawable.badge_50_hours),
    Badge(R.drawable.badge_volunteer_of_week),
    Badge(R.drawable.badge_community_hero),
    Badge(R.drawable.badge_eco_warrior),
    Badge(R.drawable.badge_3_months)
)

val sampleCompletedEvents = listOf(
    CompletedEvent("Health On Wheels", "August 10, 2025 - 5 Hours", R.drawable.event_health_on_wheels),
    CompletedEvent("Sanitary Pad Distribution", "August 6, 2025 - 3 Hours", R.drawable.event_sanitary_pad),
    CompletedEvent("Community Sports Day", "August 01, 2025 - 8 Hours", R.drawable.event_sports_day)
)


class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // StateFlow to hold the dynamic user profile data
    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    // StateFlows for static badge and event data
    private val _badges = MutableStateFlow(sampleBadges)
    val badges: StateFlow<List<Badge>> = _badges

    private val _completedEvents = MutableStateFlow(sampleCompletedEvents)
    val completedEvents: StateFlow<List<CompletedEvent>> = _completedEvents

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                try {
                    val document = db.collection("users").document(userId).get().await()
                    if (document.exists()) {
                        val name = document.getString("name") ?: "No Name"
                        val joinTimestamp = document.getLong("joinDate") ?: System.currentTimeMillis()
                        val formattedDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(joinTimestamp))

                        // Update the profile StateFlow with data from Firestore
                        _profile.value = _profile.value.copy(
                            uid = userId,
                            name = name,
                            joinDate = "Joined $formattedDate"
                            // TODO: Fetch profilePicUrl, hours, events, and ngos from Firestore
                        )
                    }
                } catch (e: Exception) {
                    // Handle any errors, e.g., by logging or showing a message to the user
                }
            }
        }
    }
}