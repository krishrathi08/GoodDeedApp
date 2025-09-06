package eu.tutorials.gooddeedproject.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.GoodDeedProjectTheme
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(authViewModel: AuthViewModel, communityViewModel: CommunityViewModel) {
    val posts by communityViewModel.posts.collectAsState()
    val stories by communityViewModel.stories.collectAsState()
    val currentUserId = authViewModel.currentUserId
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostForComment by remember { mutableStateOf<Post?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Community Feed", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        item {
            StoriesSection(stories = stories)
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Posts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        items(posts, key = { it.id }) { post: Post ->
            PostCard(
                post = post,
                onLikeClicked = { communityViewModel.onLikeClicked(post) },
                onCommentClicked = {
                    selectedPostForComment = post
                    showCommentSheet = true
                },
                authViewModel = authViewModel,
                communityViewModel = communityViewModel
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCommentSheet && selectedPostForComment != null) {
        CommentBottomSheet(
            postId = selectedPostForComment!!.id,
            viewModel = communityViewModel,
            currentUserId = currentUserId ?: "",
            onDismiss = { showCommentSheet = false }
        )
    }
}

// THIS IS THE NEW, CORRECTED COMMENT BOTTOM SHEET
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postId: Int,
    viewModel: CommunityViewModel,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    var commentText by remember { mutableStateOf("") }
    var isAddingComment by remember { mutableStateOf(false) }
    val posts by viewModel.posts.collectAsState()
    val post = posts.find { it.id == postId } ?: return

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        containerColor = Color.White
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.White,
            floatingActionButton = {
                if (!isAddingComment) {
                    FloatingActionButton(
                        onClick = { isAddingComment = true },
                        containerColor = BlueButtonColor
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Comment", tint = Color.White)
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(visible = isAddingComment) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                            viewModel.addComment(post.id, commentText)
                            commentText = ""
                            isAddingComment = false
                        }) {
                            Text("Post")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Comments", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(post.comments) { comment ->
                        CommentItem(comment = comment, currentUserId = currentUserId)
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}


// All other composables are unchanged and correct
@Composable
fun StoriesSection(stories: List<Story>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stories) { story ->
            StoryItem(story = story)
        }
    }
}

@Composable
fun StoryItem(story: Story) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = story.profilePicRes),
            contentDescription = "Profile picture of ${story.userName}",
            modifier = Modifier.size(70.dp).clip(CircleShape).border(2.dp, color = PrimaryBlueText, shape = CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = story.userName, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun PostCard(
    post: Post,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    // Add ViewModels as parameters to handle user-specific logic
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel
) {
    val currentUserId = authViewModel.currentUserId
    var showPostMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Post Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = post.user.profilePicRes),
                contentDescription = "Profile picture of ${post.user.userName}",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (post.type == PostType.NGO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = post.user.userName, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlueText.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "NGO",
                            color = PrimaryBlueText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                Column {
                    Text(text = post.user.userName, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(
                        text = buildAnnotatedString {
                            append("Posted From ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryBlueText)) {
                                append(post.postedFrom)
                            }
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            // Show the three-dots menu ONLY if the post belongs to the current user
            if (post.user.userId == currentUserId) {
                Box {
                    IconButton(onClick = { showPostMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Post options")
                    }
                    DropdownMenu(
                        expanded = showPostMenu,
                        onDismissRequest = { showPostMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                // TODO: Navigate to an Edit Post screen
                                showPostMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                communityViewModel.deletePost(post.id)
                                showPostMenu = false
                            }
                        )
                    }
                }
            }
        }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(shape = RoundedCornerShape(16.dp)) {
            AsyncImage(
                model = post.postImageUri ?: post.postImageRes,
                contentDescription = "Post image",
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PostActions(post = post, onLikeClicked = onLikeClicked, onCommentClicked = onCommentClicked)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.caption,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
}

@Composable
fun PostActions(post: Post, onLikeClicked: () -> Unit, onCommentClicked: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconToggleButton(checked = post.isLiked, onCheckedChange = { onLikeClicked() }) {
            Icon(
                imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (post.isLiked) Color.Red else Color.Black
            )
        }
        Text(text = "${post.likes}", color = Color.Black, fontWeight = FontWeight.Bold)
        IconButton(onClick = onCommentClicked) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                tint = Color.Black
            )
        }
        Text(text = "${post.comments.size}", color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CommentItem(comment: Comment, currentUserId: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Image(
            painter = painterResource(id = comment.user.profilePicRes),
            contentDescription = "Profile picture of ${comment.user.userName}",
            modifier = Modifier.size(32.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            val displayName = if (comment.user.userId == currentUserId) "You" else comment.user.userName
            Text(text = displayName, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
            Text(text = comment.text, color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    GoodDeedProjectTheme {
        val authViewModel: AuthViewModel = viewModel()
        val communityViewModel: CommunityViewModel = viewModel()
        CommunityScreen(authViewModel = authViewModel, communityViewModel = communityViewModel)
    }
}