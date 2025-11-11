package eu.tutorials.gooddeedproject.models

import androidx.annotation.DrawableRes

enum class BadgeId {
    FIRST_STEP,
    COMMUNITY_HERO,
    ECO_WARRIOR,
    HEALTH_CHAMPION,
    ANIMAL_FRIEND
}

data class Badge(
    val id: BadgeId,
    val name: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val isEarned: Boolean = false
)