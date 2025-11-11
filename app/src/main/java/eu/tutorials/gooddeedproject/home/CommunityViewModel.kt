package eu.tutorials.gooddeedproject.home

import android.net.Uri
import android.util.Log
import androidx.compose.ui.input.key.Key.Companion.Notification
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.organizer.OrganizerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommunityViewModel() : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _posts = MutableStateFlow<List<PostWithComments>>(emptyList())
    val posts: StateFlow<List<PostWithComments>> = _posts.asStateFlow()

    private val _areCommentsLoading = MutableStateFlow(false)
    val areCommentsLoading: StateFlow<Boolean> = _areCommentsLoading.asStateFlow()

    private var currentUserProfile: Story? = null

    init {
        viewModelScope.launch {
            fetchCurrentUserProfile()
            listenForPosts()
        }
    }

    private suspend fun fetchCurrentUserProfile() {
        val user = auth.currentUser ?: return
        try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            if (userDoc.exists()) {
                // ✅ THE FIX: Fetch 'profilePicUrl' (String) from the user document
                currentUserProfile = Story(
                    userId = user.uid,
                    userName = userDoc.getString("name") ?: "You",
                    profilePicUrl = userDoc.getString("profilePicUrl") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching current user profile", e)
        }
    }

    private fun listenForPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { return@addSnapshotListener }
                if (snapshot != null) {
                    val firestorePosts = snapshot.documents.mapNotNull { it.toObject<Post>() }
                    _posts.update {
                        firestorePosts.map { post ->
                            // Keep existing comments and like state if already present
                            val existing = it.find { p -> p.post.id == post.id }
                            PostWithComments(
                                post = post,
                                comments = existing?.comments ?: emptyList(),
                                isLiked = existing?.isLiked ?: false
                            )
                        }
                    }
                }
            }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                // Step A: Firestore se post document ko delete karo
                db.collection("posts").document(post.id).delete().await()

                // Step B: Agar post mein image hai, toh use Storage se delete karo
                if (post.postImageUrl.isNotBlank()) {
                    Firebase.storage.getReferenceFromUrl(post.postImageUrl).delete().await()
                }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error deleting post", e)
                // Yahan aap user ko error dikha sakte ho agar zaroori ho
            }
        }
    }

    fun onLikeClicked(postToUpdate: Post) {
        _posts.update { currentPosts ->
            currentPosts.map { postWithComments ->
                if (postWithComments.post.id == postToUpdate.id) {
                    // Determine the new like state and count
                    val newIsLiked = !postWithComments.isLiked
                    val newLikeCount = if (newIsLiked) {
                        postWithComments.post.likes + 1
                    } else {
                        postWithComments.post.likes - 1
                    }

                    // Update the post in Firestore (so the like is permanent)
                    val postRef = db.collection("posts").document(postToUpdate.id)
                    db.runBatch { batch ->
                        // Use FieldValue.increment to safely handle multiple likes at once
                        batch.update(postRef, "likes", newLikeCount)
                        // TODO: You would also save which users liked which post here
                    }

                    // Return the updated state for the UI to show immediately
                    postWithComments.copy(
                        isLiked = newIsLiked,
                        post = postWithComments.post.copy(likes = newLikeCount)
                    )

                } else {
                    postWithComments
                }
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        val user = currentUserProfile ?: return
        if (commentText.isBlank()) return

        val comment = Comment(
            user = user,
            text = commentText,
            timestamp = System.currentTimeMillis()
        )
        db.collection("posts").document(postId)
            .collection("comments")
            .add(comment)
        // Listener will auto-update UI
    }

    fun listenForComments(postId: String) {
        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Show oldest comments first
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Comment>()?.copy(id = doc.id)
                    }
                    _posts.update { currentPosts ->
                        currentPosts.map { postWithComments ->
                            if (postWithComments.post.id == postId) {
                                postWithComments.copy(comments = comments)
                            } else {
                                postWithComments
                            }
                        }
                    }
                }
                _areCommentsLoading.value = false
            }
    }

    fun addPost(caption: String, imageUri: Uri?, taggedEventId: String?) {
        viewModelScope.launch {
            val user = currentUserProfile ?: return@launch
            if (imageUri == null) return@launch

            try {
                // Step 1: Image upload pehle karo
                val imageRef = storage.reference.child("post_images/${UUID.randomUUID()}")
                val downloadUrl = imageRef.putFile(imageUri).await()
                    .storage.downloadUrl.await().toString()

                // Step 2: Post aur Notification objects taiyaar karo
                val postId = db.collection("posts").document().id
                val newPost = Post(
                    id = postId,
                    user = user,
                    caption = caption,
                    postImageUrl = downloadUrl,
                    timestamp = System.currentTimeMillis(),
                    taggedEventId = taggedEventId
                )

                // Step 3: Batch write shuru karo
                val batch = db.batch()

                // Post ko batch mein add karo
                val postRef = db.collection("posts").document(postId)
                batch.set(postRef, newPost)

                // Agar event tagged hai, toh notification ko bhi batch mein add karo
                if (taggedEventId != null) {
                    val eventDoc = db.collection("events").document(taggedEventId).get().await()
                    val organizerId = eventDoc.getString("organizerId")
                    if (organizerId != null) {
                        val notificationMessage = "${user.userName} tagged your event in a post."
                        val notificationId = db.collection("notifications").document().id
                        val notification = OrganizerViewModel.Notification( // ✅ Typo fixed
                            id = notificationId,
                            organizerId = organizerId,
                            message = notificationMessage,
                            type = "POST_TAG",
                            timestamp = System.currentTimeMillis()
                        )
                        val notificationRef = db.collection("notifications").document(notificationId)
                        batch.set(notificationRef, notification)
                    }
                }

                // Step 4: Batch ko commit karo (dono cheezein ek saath save hongi)
                batch.commit().await()

            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error adding post", e)
            }
        }
    }
}