package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes

data class ExploreEvent(
    val category: String,
    val title: String,
    val location: String,
    val dateTime: String,
    @DrawableRes val imageRes: Int
)