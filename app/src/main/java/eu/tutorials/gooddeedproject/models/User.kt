package eu.tutorials.gooddeedproject.models

// This is the final, correct data class
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePicUrl: String = "",
    val bio: String = "",
    val joinDate: Long = 0L,
    val phone: String = "",
    val city: String = "",
    val availability: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val userType: String = "USER",

    val participatedEventIds: List<String> = emptyList(),
    val upcomingEventIds: List<String> = emptyList(),

    // Stats are now normal properties, not TODO() blocks
    val totalHours: Int = 0,
    val eventsAttended: Int = 0,
    val ngosHelped: Int = 0
)