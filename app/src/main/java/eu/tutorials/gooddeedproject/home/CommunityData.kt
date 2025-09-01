package com.yourpackage.home

import android.net.Uri
import androidx.annotation.DrawableRes

// Add this enum to define the types of posts
enum class PostType {
    USER, NGO
}

data class Story(
    val userName: String,
    @DrawableRes val profilePicRes: Int
)

data class Post(
    val id: Int,
    val user: Story, // For NGOs, this will hold the NGO's name and logo
    val postedFrom: String,
    val caption: String,
    val initialLikes: Int,
    val commentCount: Int,
    val type: PostType, // Add this type field
    @DrawableRes val postImageRes: Int? = null,
    val postImageUri: Uri? = null,
    val isLiked: Boolean = false,
    val likes: Int = initialLikes
)