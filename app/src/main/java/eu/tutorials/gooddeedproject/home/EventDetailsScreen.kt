package eu.tutorials.gooddeedproject.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController,
    eventsViewModel: EventsViewModel = viewModel()
) {
    // Collect all event lists from the single EventsViewModel
    val upcomingEvents by eventsViewModel.upcomingEvents.collectAsState()
    val suggestedEvents by eventsViewModel.suggestedEvents.collectAsState()
    val completedEvents by eventsViewModel.completedEvents.collectAsState()

    // Find the specific event from the collected lists
    val event = remember(eventId, upcomingEvents, suggestedEvents, completedEvents) {
        (upcomingEvents + suggestedEvents + completedEvents).find { it.id == eventId }
    }

    val isRegistered = upcomingEvents.any { it.id == eventId }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventsViewModel) {
        eventsViewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Handle loading and not-found states
        if (event == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                // If event is not found, show a message. Otherwise, it's just loading.
                Text("Loading event...")
            }
            return@Scaffold
        }

        // Once the event is not null, show the details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Event Header Image",
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(event.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = event.organizerLogoUrl,
                        contentDescription = "NGO Logo",
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        placeholder = painterResource(id = R.drawable.logo)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.organizerName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                InfoRow(icon = Icons.Default.CalendarToday, text = formatEventDate(event.date))
                InfoRow(icon = Icons.Default.Schedule, text = event.time)
                InfoRow(icon = Icons.Default.LocationOn, text = event.location)
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text(event.description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { eventsViewModel.registerForEvent(event) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegistered) MaterialTheme.colorScheme.onSurface else BlueButtonColor
                    ),
                    enabled = !isRegistered
                ) {
                    if (isRegistered) {
                        Text("You are Registered!", fontSize = 18.sp)
                    } else {
                        Text("Volunteer Now", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

// Helper composables
@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatEventDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}