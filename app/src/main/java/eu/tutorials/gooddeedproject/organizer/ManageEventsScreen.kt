package eu.tutorials.gooddeedproject.organizer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.gooddeedproject.home.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEventScreen(
    eventId: String,
    navController: NavController,
    viewModel: OrganizerViewModel
) {
    // Find the event from the ViewModel's list
    val event by remember(eventId) {
        derivedStateOf {
            (viewModel.upcomingEvents.value + viewModel.completedEvents.value).find { it.id == eventId }
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Event", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (event == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Event not found or loading...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(event!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                // Button to see volunteers
                Button(
                    onClick = { navController.navigate(RegisteredScreen.VolunteerList.createRoute(event!!.id)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("View Registered Volunteers (${event!!.registeredUserIds.size})")
                }

                // Button to edit event
                OutlinedButton(
                    onClick = {navController.navigate("edit_event/${event!!.id}")},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Edit Event Details")
                }

                // Button to delete event
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onError)
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Delete Event")
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Delete
    if (showDeleteDialog && event != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to delete '${event!!.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        viewModel.deleteEvent(event!!) { success ->
                            if (success) {
                                Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to delete event.", Toast.LENGTH_SHORT).show()
                            }
                            isDeleting = false
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}