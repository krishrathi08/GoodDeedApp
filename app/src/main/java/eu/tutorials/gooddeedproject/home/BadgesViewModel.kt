package eu.tutorials.gooddeedproject.home // Or your viewmodels package

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.models.Badge
import eu.tutorials.gooddeedproject.models.BadgeId
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BadgesViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // ✅ STEP 1: DEFINE ALL POSSIBLE BADGES (THE RULES)
    private val allPossibleBadges = listOf(
        Badge(BadgeId.FIRST_STEP, "First Step", "Completed your first event", R.drawable.badge_3_months),
        Badge(BadgeId.COMMUNITY_HERO, "Community Hero", "Completed 5 events", R.drawable.badge_community_hero),
        Badge(BadgeId.ECO_WARRIOR, "Eco Warrior", "Completed 2 environmental events", R.drawable.badge_eco_warrior),
        Badge(BadgeId.HEALTH_CHAMPION, "Health Champion", "Completed 2 health-related events", R.drawable.badge_50_hours),
        Badge(BadgeId.ANIMAL_FRIEND, "Animal Friend", "Completed 2 animal welfare events", R.drawable.badge_volunteer_of_week)
    )

    private val _badges = MutableStateFlow<List<Badge>>(allPossibleBadges)
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    init {
        evaluateBadges()
    }

    private fun evaluateBadges() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            try {
                // ✅ STEP 2: GET USER'S COMPLETED EVENTS
                val userProfile = db.collection("users").document(user.uid).get().await().toObject<UserProfile>()
                val completedEventIds = userProfile?.participatedEventIds ?: emptyList()

                if (completedEventIds.isEmpty()) {
                    _badges.value = allPossibleBadges // No events, no earned badges
                    return@launch
                }

                // Fetch details of only the completed events
                val completedEvents = db.collection("events")
                    .whereIn("id", completedEventIds)
                    .get().await().toObjects(Event::class.java)

                // ✅ STEP 3: APPLY THE RULES
                val earnedBadgeIds = mutableSetOf<BadgeId>()

                // Rule: First Step
                if (completedEvents.isNotEmpty()) earnedBadgeIds.add(BadgeId.FIRST_STEP)
                // Rule: Community Hero
                if (completedEvents.size >= 5) earnedBadgeIds.add(BadgeId.COMMUNITY_HERO)
                // Rule: Eco Warrior
                if (completedEvents.count { it.category.equals("Environment", ignoreCase = true) } >= 2) earnedBadgeIds.add(BadgeId.ECO_WARRIOR)
                // Rule: Health Champion
                if (completedEvents.count { it.category.equals("Health", ignoreCase = true) } >= 2) earnedBadgeIds.add(BadgeId.HEALTH_CHAMPION)
                // Rule: Animal Friend
                if (completedEvents.count { it.category.equals("Animal Welfare", ignoreCase = true) } >= 2) earnedBadgeIds.add(BadgeId.ANIMAL_FRIEND)

                // ✅ STEP 4: CREATE THE FINAL LIST FOR THE UI
                _badges.value = allPossibleBadges.map { badge ->
                    badge.copy(isEarned = earnedBadgeIds.contains(badge.id))
                }

            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Error evaluating badges", e)
                _badges.value = allPossibleBadges // On error, show default state
            }
        }
    }
}