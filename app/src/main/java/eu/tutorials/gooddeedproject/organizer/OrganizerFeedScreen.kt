package eu.tutorials.gooddeedproject.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.home.*

@Composable
fun OrganizerFeedScreen(
    mainNavController: NavHostController,
    innerNavController: NavHostController,
    communityViewModel: CommunityViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val posts by communityViewModel.posts.collectAsState()
    val currentUserId = authViewModel.currentUserId

    // State for Comment Sheet and Image Zoom
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostForComment by remember { mutableStateOf<PostWithComments?>(null) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { innerNavController.navigate("create_post") }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = paddingValues) {
                items(posts, key = { it.post.id }) { postWithComments ->
                    PostCard(
                        postWithComments = postWithComments,
                        onLikeClicked = { communityViewModel.onLikeClicked(postWithComments.post) },
                        onCommentClicked = {
                            communityViewModel.listenForComments(postWithComments.post.id)
                            selectedPostForComment = postWithComments
                            showCommentSheet = true
                        },
                        currentUserId = currentUserId,
                        communityViewModel = communityViewModel,
                        onImageClick = { imageUrl ->
                            zoomedImageUrl = imageUrl
                        }
                    )
                }
            }
        }

        // Dialog for zoomed image
        if (zoomedImageUrl != null) {
            Dialog(onDismissRequest = { zoomedImageUrl = null }) {
                AsyncImage(
                    model = zoomedImageUrl,
                    contentDescription = "Zoomed Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { zoomedImageUrl = null }
                )
            }
        }

        // Bottom sheet for comments
        if (showCommentSheet && selectedPostForComment != null) {
            CommentBottomSheet(
                postWithComments = selectedPostForComment!!,
                viewModel = communityViewModel,
                currentUserId = currentUserId,
                onDismiss = { showCommentSheet = false }
            )
        }
    }
}