package eu.tutorials.gooddeedproject.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.ui.theme.*
import eu.tutorials.gooddeedproject.utils.getLatLngForLocation
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExploreScreen(navController: NavController) {
    val eventsViewModel: EventsViewModel = viewModel()
    val allSuggestedEvents by eventsViewModel.suggestedEvents.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val upcomingEvents by eventsViewModel.upcomingEvents.collectAsState()
    val suggestedEvents by eventsViewModel.suggestedEvents.collectAsState()

    val allAvailableEvents = remember(upcomingEvents, suggestedEvents) {
        (upcomingEvents + suggestedEvents).distinctBy { it.id } // Use distinctBy to avoid duplicates
    }

    val filteredEvents = remember(selectedFilter, allAvailableEvents) {
        if (selectedFilter == "All") {
            allAvailableEvents
        } else {
            allAvailableEvents.filter { event ->
                event.category.contains(selectedFilter, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Explore Opportunities", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar()
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { newFilter -> selectedFilter = newFilter }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Text("View From Map", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            // ✅ 3. Pass the combined & filtered list to the MapView
            MapView(events = filteredEvents, navController = navController)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text("All Available Events", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Events For You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(filteredEvents, key = { it.id }) { event ->
            ExploreEventCard(
                event = event,
                onClick = { navController.navigate("event_details/${event.id}") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MapView(events: List<Event>, navController: NavController) {
    val bhopalCenter = LatLng(23.2599, 77.4126)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bhopalCenter, 12f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            events.forEach { event ->
                val coordinates = getLatLngForLocation(event.location)
                if (coordinates != null) {
                    Marker(
                        state = MarkerState(position = coordinates),
                        title = event.title,
                        snippet = event.location,
                        onInfoWindowClick = {
                            navController.navigate("event_details/${event.id}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf("") }
    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search Events", color = MaterialTheme.colorScheme.onSurface) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PrimaryBlueText,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val filters = listOf("All", "Environment", "Education", "Animal Welfare", "Health", "Community Development")
        items(filters) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlueText,
                    selectedLabelColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun ExploreEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = event.category.uppercase(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.location, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    // Combine date and time
                    Text(text = "${formatEventDate(event.date)}, ${event.time}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor)
                ) {
                    Text("Details")
                }
            }
        }
    }
}

// Helper function to format the timestamp
private fun formatEventDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}