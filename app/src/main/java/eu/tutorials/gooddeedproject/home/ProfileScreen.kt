package eu.tutorials.gooddeedproject.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.models.Badge
import eu.tutorials.gooddeedproject.models.UserProfile
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(navController: NavController) {
    // 1. Get all three ViewModels
    val profileViewModel: ProfileViewModel = viewModel()
    val eventsViewModel: EventsViewModel = viewModel()
    val badgesViewModel: BadgesViewModel = viewModel()

    val userProfile by profileViewModel.profile.collectAsState()
    val badges by badgesViewModel.badges.collectAsState()
    val completedEvents by eventsViewModel.completedEvents.collectAsState()

    val totalHours by eventsViewModel.userTotalHours.collectAsState()

    var showCompletedEventSheet by remember { mutableStateOf(false) }
    var selectedCompletedEvent by remember { mutableStateOf<Event?>(null) }

    if (showCompletedEventSheet && selectedCompletedEvent != null) {
        CompletedEventActionsBottomSheet(
            event = selectedCompletedEvent!!,
            onDismiss = { showCompletedEventSheet = false },
            onNavigate = { route ->
                navController.navigate(route)
                showCompletedEventSheet = false
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (userProfile == null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ProfileHeader(
                    profile = userProfile!!,
                    eventsAttendedCount = completedEvents.size,
                    totalHours = totalHours
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            BadgeCollection(badges = badges, navController = navController)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Completed Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(completedEvents, key = { it.id }) { event ->
            CompletedEventCard(
                event = event,
                onClick = {
                    selectedCompletedEvent = event
                    showCompletedEventSheet = true
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedEventActionsBottomSheet(
    event: Event,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            ActionRow(
                icon = Icons.Default.Create,
                text = "Write a Post",
                onClick = { onNavigate("create_post?eventId=${event.id}") }
            )
            ActionRow(
                icon = Icons.Default.Info,
                text = "View Event Details",
                onClick = { onNavigate("event_details/${event.id}") }
            )
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile, eventsAttendedCount: Int, totalHours: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = profile.profilePicUrl,
                contentDescription = "Profile Picture",
                placeholder = painterResource(id = R.drawable.profile_krish),
                error = painterResource(id = R.drawable.profile_krish),
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Joined ${formatJoinDate(profile.joinDate)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(value = totalHours.toString(), label = "Total Hours")
                StatItem(value = eventsAttendedCount.toString(), label = "Events")
                StatItem(value = profile.ngosHelped.toString(), label = "NGOs Helped")
            }
        }
    }
}

@Composable
fun CompletedEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // ✅ Make the card clickable
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                Text(text = "${formatJoinDate(event.date)}, ${event.location}", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

fun formatJoinDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val date = Date(timestamp)
    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun BadgeCollection(badges: List<eu.tutorials.gooddeedproject.models.Badge>, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(), // Removed padding
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Badge Collection",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (badges.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(badges) { badge ->
                        BadgeItem(badge = badge)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    val alpha = if (badge.isEarned) 1f else 0.3f // Full color if earned, faded if not

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Image(
            painter = painterResource(id = badge.imageRes),
            contentDescription = badge.name,
            modifier = Modifier
                .size(80.dp)
                .alpha(alpha) // Apply the alpha here
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.name,
            fontSize = 14.sp,
            fontWeight = if (badge.isEarned) FontWeight.Bold else FontWeight.Normal,
            color = if (badge.isEarned) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        // Optionally, show description if not earned
        if (!badge.isEarned) {
            Text(
                text = "(Locked)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}