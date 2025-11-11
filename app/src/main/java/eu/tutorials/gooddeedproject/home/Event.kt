package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val id: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val organizerLogoUrl: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val date: Long = 0L,
    val time: String = "",
    val imageUrl: String = "",
    val durationInHours: Int = 0,
    val registeredUserIds: List<String> = emptyList()
) : Parcelable


