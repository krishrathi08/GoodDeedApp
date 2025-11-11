package eu.tutorials.gooddeedproject.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.*
import java.text.SimpleDateFormat
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.models.UserProfile
import eu.tutorials.gooddeedproject.utils.getLatLngForLocation
import java.util.*


@Composable
fun HomeScreenContent(navController: NavController, authViewModel: AuthViewModel) {
    // 1. Get instances of both ViewModels
    val eventsViewModel: EventsViewModel = viewModel()
    val userProfile by authViewModel.userProfile.collectAsState()

    // 2. Collect the live data lists from EventsViewModel
    val upcomingEvents by eventsViewModel.upcomingEvents.collectAsState()
    val suggestedEvents by eventsViewModel.suggestedEvents.collectAsState()
    val completedEvents by eventsViewModel.completedEvents.collectAsState()

    val totalHours by eventsViewModel.userTotalHours.collectAsState()

    var showActionsSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    // State for the "Who's Going" bottom sheet
    var showWhosGoingSheet by remember { mutableStateOf(false) }
    val registeredUsers by eventsViewModel.registeredUsers.collectAsState()

    val context = LocalContext.current

    if (showActionsSheet && selectedEvent != null) {
        EventActionsBottomSheet(
            event = selectedEvent!!,
            onDismiss = { showActionsSheet = false },
            onNavigate = { route ->
                navController.navigate(route)
                showActionsSheet = false
            },
            onShare = { event ->
                shareEvent(context, event)
                showActionsSheet = false
            },
            onGetDirections = { event ->
                openDirections(context, event.location)
                showActionsSheet = false
            },
            onWhosGoing = { event ->
                eventsViewModel.fetchRegisteredUsers(event)
                showActionsSheet = false
                showWhosGoingSheet = true
            }
        )
    }

    if (showWhosGoingSheet) {
        RegisteredUsersSheet(
            users = registeredUsers,
            onDismiss = { showWhosGoingSheet = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeHeader(name = userProfile?.name ?: "User")
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            StatsSection(
                eventsAttended = completedEvents.size,
                volunteerHours = totalHours
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Upcoming Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // 4. Use the live data list for upcoming events
        items(upcomingEvents, key = { it.id }) { event ->
            EventCard(
                event = event,
                onClick = {
                    selectedEvent = event
                    showActionsSheet = true
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Suggested For You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(suggestedEvents, key = { it.id }) { event ->
            EventCard(
                event = event,
                onClick = { navController.navigate("event_details/${event.id}")  }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo) // A fallback image
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                Text(text = event.location, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(text = formatEventDate(event.date), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
private fun formatEventDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun WelcomeHeader(name: String) {
    Column {
        Text(
            text = "Welcome back, $name!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ready to make a difference today?",
            fontSize = 16.sp
        )
    }
}

@Composable
fun StatsSection(eventsAttended: Int, volunteerHours: Int) { // ✅ Signature updated
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_time,
            value = volunteerHours.toString(), // ✅ Use live data
            label = "Volunteer Hours"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_calendar,
            value = eventsAttended.toString(),
            label = "Events Attended"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_trophy,
            value = "6", // This is still static
            label = "Badges Earned"
        )
    }
}


@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = PrimaryBlueText,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventActionsBottomSheet(
    event: Event,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onShare: (Event) -> Unit,
    onGetDirections: (Event) -> Unit,
    onWhosGoing: (Event) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            Divider()
            ActionRow(icon = Icons.Default.Info, text = "View Details", onClick = { onNavigate("event_details/${event.id}") })
            ActionRow(icon = Icons.Default.Share, text = "Share Event", onClick = { onShare(event) })
            ActionRow(icon = Icons.Default.Groups, text = "See Who's Going", onClick = { onWhosGoing(event) })
            ActionRow(icon = Icons.Default.Place, text = "Get Directions", onClick = { onGetDirections(event) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisteredUsersSheet(users: List<UserProfile>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Who's Going", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            Divider()
            if (users.isEmpty()) {
                Text("No one else has registered yet.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(users) { user ->
                        UserItem(user)
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserProfile) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        AsyncImage(
            model = user.profilePicUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(40.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.profile_krish)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(user.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = onSeeAllClick) {
            Text(text = "See All", color = PrimaryBlueText, fontWeight = FontWeight.Bold)
        }
    }
}

private fun openDirections(context: Context, locationName: String) {
    val coordinates = getLatLngForLocation(locationName)
    val query = coordinates?.let { "${it.latitude},${it.longitude}" } ?: locationName
    val gmmIntentUri = Uri.parse("google.navigation:q=$query")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    }
}

private fun shareEvent(context: Context, event: Event) {
    val shareText = """
        Let's make a difference!
        Join me for the event: "${event.title}"
        Organized by: ${event.organizerName}
        Date: ${formatEventDate(event.date)}
        
        Find more details on the GoodDeed App!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Join me for a Good Deed!")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Event"))
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GoodDeedProjectTheme {
        HomeScreenContent(navController = rememberNavController(), authViewModel = viewModel())
    }
}