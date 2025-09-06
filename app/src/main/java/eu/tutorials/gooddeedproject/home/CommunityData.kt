package eu.tutorials.gooddeedproject.home

import android.net.Uri
import androidx.annotation.DrawableRes

enum class PostType {
    USER, NGO
}

data class Story(
    val userId: String, // ADD THIS UNIQUE ID
    val userName: String,
    @DrawableRes val profilePicRes: Int
)

// ADD THIS NEW DATA CLASS
data class Comment(
    val user: Story,
    val text: String
)

data class Post(
    val id: Int,
    val user: Story,
    val postedFrom: String,
    val caption: String,
    val initialLikes: Int,
    val type: PostType,
    val comments: List<Comment>, // CHANGED from commentCount to a List
    @DrawableRes val postImageRes: Int? = null,
    val postImageUri: Uri? = null,
    val isLiked: Boolean = false,
    val likes: Int = initialLikes
)