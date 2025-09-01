package eu.tutorials.gooddeedproject.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.yourpackage.home.CommunityViewModel
import com.yourpackage.home.Post
import com.yourpackage.home.PostType
import com.yourpackage.home.Story
import com.yourpackage.home.sampleStories
import eu.tutorials.gooddeedproject.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: CommunityViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
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
            StoriesSection(stories = sampleStories)
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
                onLikeClicked = { viewModel.onLikeClicked(post) },
                onCommentClicked = {
                    selectedPostForComment = post
                    showCommentSheet = true
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCommentSheet && selectedPostForComment != null) {
        CommentBottomSheet(
            post = selectedPostForComment!!,
            onDismiss = { showCommentSheet = false }
        )
    }
}

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
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .border(2.dp, color = PrimaryBlueText, shape = CircleShape),
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
    onCommentClicked: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = post.user.profilePicRes),
                contentDescription = "Profile picture of ${post.user.userName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
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
        }
        Spacer(modifier = Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(16.dp)) {
            AsyncImage(
                model = post.postImageUri ?: post.postImageRes,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.caption,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        PostActions(post = post, onLikeClicked = onLikeClicked, onCommentClicked = onCommentClicked)
    }
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
        Text(text = "${post.commentCount}", color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(post: Post, onDismiss: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add a comment for ${post.user.userName}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Write your comment...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch { modalBottomSheetState.hide() }.invokeOnCompletion {
                        if (!modalBottomSheetState.isVisible) {
                            onDismiss()
                        }
                    }
                    Toast.makeText(context, "Comment Posted!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Post")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    GoodDeedProjectTheme {
        CommunityScreen()
    }
}