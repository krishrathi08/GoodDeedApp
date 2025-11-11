package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes

enum class PostType {
    USER, NGO
}

// Add default values to all properties
// In your models file (e.g., Post.kt)
data class Story(
    val userId: String = "",
    val userName: String = "",
    val profilePicUrl: String = ""
)

// Add default values to all properties
data class Comment(
    val id: String = "",
    val user: Story = Story(),
    val text: String = "",
    val timestamp: Long = 0L
)

data class Post(
    val id: String = "",
    val user: Story = Story(),
    val caption: String = "",
    val postImageUrl: String = "",
    val type: PostType = PostType.USER,
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val taggedEventId: String? = null
)

// This data is for UI state only, not for Firestore
data class PostWithComments(
    val post: Post,
    val comments: List<Comment> = emptyList(),
    val isLiked: Boolean = false // Track like state locally
)