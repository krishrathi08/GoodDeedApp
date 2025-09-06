package eu.tutorials.gooddeedproject.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
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
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.ui.theme.*

// Sample Data with IDs
val sampleExploreEvents = listOf(
    ExploreEvent(1, "Social Support", "Community Kitchen Help", "Mahakal Anna Kshetra, Bhopal", "Daily, 12:00 PM - 2:00 PM", R.drawable.community_kitchen),
    ExploreEvent(2, "Environmental Action", "Clean Shipra Drive", "Ram Ghat, Bhopal", "September 7, 2025 - 8:00 AM", R.drawable.shipra_drive),
    ExploreEvent(3, "Animal Welfare", "Dog Walking Day", "City Animal Shelter", "August 24, 2025 - 9:00 AM", R.drawable.dog_walking)
)

@Composable
fun ExploreScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredEvents = remember(selectedFilter, sampleExploreEvents) {
        if (selectedFilter == "All") {
            sampleExploreEvents
        } else {
            sampleExploreEvents.filter {
                it.category.contains(selectedFilter, ignoreCase = true)
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
            Text(
                text = "Explore Opportunities",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            SearchBar()
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { newFilter ->
                    selectedFilter = newFilter
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Text(
                text = "View From Map",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            MapView()
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            Text(
                text = "Events For You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(filteredEvents) { event ->
            ExploreEventCard(
                event = event,
                onClick = { navController.navigate(Screen.EventDetails.createRoute(event.id)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
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
        placeholder = { Text("Search Events", color = Color.Gray) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PrimaryBlueText,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
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
        val filters = listOf("All", "Environment", "Education", "Animal", "Social")
        items(filters) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlueText,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun MapView() {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                Toast.makeText(context, "Location permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.map_placeholder),
            contentDescription = "View From Map",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
        )
    }
}

@Composable
fun ExploreEventCard(event: ExploreEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            onClick = onClick
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = event.imageRes),
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = event.category.uppercase(), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.location, fontSize = 14.sp, color = Color.DarkGray)
                    Text(text = event.dateTime, fontSize = 14.sp, color = Color.Gray)
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

@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview() {
    GoodDeedProjectTheme {
        ExploreScreen(navController = rememberNavController())
    }
}