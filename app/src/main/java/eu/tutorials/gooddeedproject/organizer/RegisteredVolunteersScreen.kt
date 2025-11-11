package eu.tutorials.gooddeedproject.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.models.UserProfile

sealed class RegisteredScreen(val route: String) {
    object EventList : RegisteredScreen("event_list")
    object VolunteerList : RegisteredScreen("volunteer_list/{eventId}") {
        fun createRoute(eventId: String) = "volunteer_list/$eventId"
    }
}

@Composable
fun RegisteredVolunteersScreen(organizerViewModel: OrganizerViewModel, innerNavController: NavHostController) {
    EventListForVolunteers(
        viewModel = organizerViewModel,
        onEventClick = { eventId ->
            innerNavController.navigate(RegisteredScreen.VolunteerList.createRoute(eventId))
        }
    )
}

@Composable
fun EventListForVolunteers(viewModel: OrganizerViewModel, onEventClick: (String) -> Unit) {
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Select an event to see registered volunteers:", style = MaterialTheme.typography.titleMedium)
        }
        items(upcomingEvents, key = { it.id }) { event ->
            OrganizerEventCard(
                event = event,
                modifier = Modifier.clickable { onEventClick(event.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerListForEvent(eventId: String, viewModel: OrganizerViewModel, onBack: () -> Unit) {
    val allEvents by (viewModel.upcomingEvents.collectAsState())
    val event = remember(eventId, allEvents) { allEvents.find { it.id == eventId } }

    val volunteersMap by viewModel.volunteersForEvent.collectAsState()
    val volunteers = volunteersMap[eventId]

    LaunchedEffect(event) {
        if (event != null && volunteers == null) {
            viewModel.fetchVolunteersForEvent(event.id, event.registeredUserIds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Volunteers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (volunteers == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (volunteers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No volunteers have registered for this event yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(volunteers, key = { it.uid }) { volunteer ->
                    VolunteerCard(volunteer = volunteer)
                }
            }
        }
    }
}

@Composable
fun VolunteerCard(volunteer: UserProfile) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = volunteer.profilePicUrl,
                contentDescription = volunteer.name,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.profile_krish)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(volunteer.name, fontWeight = FontWeight.Bold)
                Text(volunteer.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}