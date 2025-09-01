package com.yourpackage.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import eu.tutorials.gooddeedproject.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- Sample Data ---
// This sample data is used to populate the feed initially.
// In a real app, you would fetch this data from a backend server.
val sampleStories = listOf(
    Story("Your Story", R.drawable.profile_krish),
    Story("Akshat", R.drawable.profile_akshat),
    Story("Prakhar", R.drawable.profile_prakhar),
    Story("Kartik", R.drawable.profile_kartik),
    Story("Vansh", R.drawable.profile_vansh),
    Story("Project Clean Beach", R.drawable.beach_cleanup)
)

val samplePosts = listOf(
    Post(
        id = 2,
        user = sampleStories[5],
        postedFrom = "",
        caption = "Waves of change start with a single action! A massive thank you to every volunteer who joined us today. Together, we removed hundreds of kilos of trash from Dumas Beach, leaving nothing but our footprints. Our coast is breathing a little easier because of you!",
        initialLikes = 25,
        commentCount = 3,
        type = PostType.NGO,
        postImageRes = R.drawable.beach_cleanup_post
    ),
    Post(
        id = 3,
        user = sampleStories[4],
        postedFrom = "Community Kitchen Help",
        caption = "Making a difference, one plate at a time! Had a truly rewarding day at the community kitchen, connecting with people and ensuring warm meals reach those who need them most. The smiles make all the effort worthwhile.",
        initialLikes = 34,
        commentCount = 4,
        type = PostType.USER,
        postImageRes = R.drawable.community_kitchen_post
    ),
    Post(
        id = 1,
        user = sampleStories[1],
        postedFrom = "Animal Rescue",
        postImageRes = R.drawable.post_image_dog_rescue,
        caption = "Every rescue is a journey of trust. Today, we helped a scared little dog find its way for safety. It's moments like these that fuel my passion for animal welfare. So grateful to be part of its second chance.",
        initialLikes = 48,
        commentCount = 9,
        type = PostType.USER
    )
)


class CommunityViewModel : ViewModel() {

    // This private state holds the list of posts.
    private val _posts = MutableStateFlow(samplePosts)
    // This is the public, read-only version that the UI observes for changes.
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    fun onLikeClicked(post: Post) {
        _posts.update { currentPosts ->
            currentPosts.map {
                if (it.id == post.id) {
                    it.copy(
                        isLiked = !it.isLiked,
                        likes = if (it.isLiked) it.likes - 1 else it.likes + 1
                    )
                } else {
                    it
                }
            }
        }
    }

    fun addPost(caption: String, imageUri: Uri?) {
        // A new post must have an image.
        if (imageUri == null) return

        val newPost = Post(
            id = (_posts.value.maxOfOrNull { it.id } ?: 0) + 1, // Create a new unique ID
            user = sampleStories[0], // Assumes the current user is "Your Story"
            postedFrom = "New Post",
            postImageUri = imageUri, // Use the selected image URI
            caption = caption,
            initialLikes = 0,
            commentCount = 0,
            type = PostType.USER // New posts are always from the USER
        )

        // Add the new post to the top of the list
        _posts.update { currentPosts ->
            listOf(newPost) + currentPosts
        }
    }
}