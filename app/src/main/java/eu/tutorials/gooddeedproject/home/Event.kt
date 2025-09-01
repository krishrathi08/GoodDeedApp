package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes

data class UpcomingEvent(
    val title: String,
    val location: String,
    val dateTime: String,
    @DrawableRes val imageRes: Int
)

data class SuggestedEvent(
    val title: String,
    val location: String,
    @DrawableRes val imageRes: Int
)