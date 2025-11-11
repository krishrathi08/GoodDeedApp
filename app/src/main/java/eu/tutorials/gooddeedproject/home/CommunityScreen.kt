package eu.tutorials.gooddeedproject.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(authViewModel: AuthViewModel, communityViewModel: CommunityViewModel) {
    val posts by communityViewModel.posts.collectAsState()
    val currentUserId = authViewModel.currentUserId
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostForComment by remember { mutableStateOf<PostWithComments?>(null) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Community Feed", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

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

    if (showCommentSheet && selectedPostForComment != null) {
        CommentBottomSheet(
            postWithComments = selectedPostForComment!!,
            viewModel = communityViewModel,
            onDismiss = { showCommentSheet = false },
            currentUserId = currentUserId
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postWithComments: PostWithComments,
    viewModel: CommunityViewModel,
    currentUserId: String?,
    onDismiss: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    val areCommentsLoading by viewModel.areCommentsLoading.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            Text("Comments", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (areCommentsLoading && postWithComments.comments.isEmpty()) {
                    CircularProgressIndicator()
                } else if (postWithComments.comments.isEmpty()) {
                    Text("No comments yet. Be the first to comment!")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(postWithComments.comments, key = { it.id }) { comment ->
                            CommentItem(
                                comment = comment
                            )
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Write a comment...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    viewModel.addComment(postWithComments.post.id, commentText)
                    commentText = ""
                }) {
                    Text("Post")
                }
            }
        }
    }
}


@Composable
fun PostCard(
    postWithComments: PostWithComments,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    currentUserId: String?,
    communityViewModel: CommunityViewModel,
    onImageClick: (String) -> Unit
) {
    val post = postWithComments.post
    var showPostMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.user.profilePicUrl,
                placeholder = painterResource(id = R.drawable.profile_krish),
                error = painterResource(id = R.drawable.profile_krish),
                contentDescription = "Profile picture of ${post.user.userName}",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = post.user.userName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (post.user.userId == currentUserId) {
                Box {
                    IconButton(onClick = { showPostMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Post options")
                    }
                    DropdownMenu(expanded = showPostMenu,
                        onDismissRequest = { showPostMenu = false }) {
                        DropdownMenuItem(text = { Text("Delete") },
                            onClick = {
                            communityViewModel.deletePost(post)
                            showPostMenu = false
                        })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(shape = RoundedCornerShape(16.dp)) {
            AsyncImage(
                model = post.postImageUrl,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onImageClick(post.postImageUrl) },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PostActions(postWithComments = postWithComments, onLikeClicked = onLikeClicked, onCommentClicked = onCommentClicked)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.caption,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun PostActions(postWithComments: PostWithComments, onLikeClicked: () -> Unit, onCommentClicked: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconToggleButton(checked = postWithComments.isLiked, onCheckedChange = { onLikeClicked() }) {
            Icon(
                imageVector = if (postWithComments.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (postWithComments.isLiked) Color.Red else MaterialTheme.colorScheme.onBackground
            )
        }
        Text(text = "${postWithComments.post.likes}", fontWeight = FontWeight.Bold)
        IconButton(onClick = onCommentClicked) {
            Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(text = "${postWithComments.comments.size}", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CommentItem(
    comment: Comment
) {
    Row(modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = comment.user.profilePicUrl,
            placeholder = painterResource(id = R.drawable.profile_krish),
            error = painterResource(id = R.drawable.profile_krish),
            contentDescription = "Profile picture of ${comment.user.userName}",
            modifier = Modifier.size(32.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = comment.user.userName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            Text(text = comment.text, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        }
    }
}

