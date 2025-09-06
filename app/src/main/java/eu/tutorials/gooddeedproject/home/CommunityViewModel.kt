package eu.tutorials.gooddeedproject.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.gooddeedproject.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

val CURRENT_USER = Story(userId = "user_krish", userName = "Krish Rathi", profilePicRes = R.drawable.profile_krish)


val sampleStories = listOf(
    // We will programmatically add the current user's story first.
    Story("user_akshat", "Akshat", R.drawable.profile_akshat),
    Story("user_prakhar", "Prakhar", R.drawable.profile_prakhar),
    Story("user_kartik", "Kartik", R.drawable.profile_kartik),
    Story("user_vansh", "Vansh", R.drawable.profile_vansh),
    Story("ngo_clean_beach", "Project Clean Beach", R.drawable.beach_cleanup_post)
)

val samplePosts = listOf(
    Post(
        id = 2,
        user = sampleStories[4],
        postedFrom = "",
        caption = "Waves of change start with a single action! A massive thank you to every volunteer who joined us today. Together, we removed hundreds of kilos of trash from Dumas Beach, leaving nothing but our footprints. Our coast is breathing a little easier because of you!",
        initialLikes = 25,
        type = PostType.NGO,
        postImageRes = R.drawable.beach_cleanup_post,
        comments = listOf(
            Comment(sampleStories[3], "Amazing work by the team!"),
            Comment(sampleStories[2], "So inspiring to see this.")
        )
    ),
    Post(
        id = 3,
        user = sampleStories[3],
        postedFrom = "Community Kitchen Help",
        caption = "Making a difference, one plate at a time! Had a truly rewarding day at the community kitchen, connecting with people and ensuring warm meals reach those who need them most. The smiles make all the effort worthwhile.",
        initialLikes = 34,
        type = PostType.USER,
        postImageRes = R.drawable.community_kitchen_post,
        comments = listOf(
            Comment(sampleStories[1], "Great initiative, Vansh!")
        )
    ),
    Post(
        id = 1,
        user = sampleStories[0],
        postedFrom = "Animal Rescue",
        postImageRes = R.drawable.post_image_dog_rescue,
        caption = "Every rescue is a journey of trust. Today, we helped a scared little dog find its way to safety. It's moments like these that fuel my passion for animal welfare. So grateful to be part of its second chance.",
        initialLikes = 48,
        type = PostType.USER,
        comments = listOf(
            Comment(sampleStories[0], "So heartwarming!"),
            Comment(sampleStories[4], "You're a hero, Akshat!")
        )
    )
)


class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val _posts = MutableStateFlow(samplePosts)
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow(listOf(CURRENT_USER.copy(userName = "Your Story")) + sampleStories)
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    fun deletePost(postId: Int) {
        _posts.update { currentPosts ->
            currentPosts.filterNot { it.id == postId }
        }
    }

    fun onLikeClicked(postToUpdate: Post) {
        _posts.update { currentPosts ->
            currentPosts.map { post ->
                if (post.id == postToUpdate.id) {
                    post.copy(
                        isLiked = !post.isLiked,
                        likes = if (post.isLiked) post.likes - 1 else post.likes + 1
                    )
                } else {
                    post
                }
            }
        }
    }

    fun addPost(caption: String, imageUri: Uri?) {
        if (imageUri == null) return
        val newPost = Post(
            id = (_posts.value.maxOfOrNull { it.id } ?: 0) + 1,
            user = CURRENT_USER,
            postedFrom = "New Post",
            postImageUri = imageUri,
            caption = caption,
            initialLikes = 0,
            comments = emptyList(),
            type = PostType.USER
        )
        _posts.update { currentPosts -> listOf(newPost) + currentPosts }
    }

    // ADD THIS NEW FUNCTION
    fun addComment(postId: Int, commentText: String) {
        if (commentText.isBlank()) return

        val newComment = Comment(
            user = CURRENT_USER, // New comments are always from the current user
            text = commentText
        )

        _posts.update { currentPosts ->
            currentPosts.map { post ->
                if (post.id == postId) {
                    // Adds the new comment to the beginning of the list
                    post.copy(comments = listOf(newComment) + post.comments)
                } else {
                    post
                }
            }
        }
    }
}