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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
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
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.GoodDeedProjectTheme
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText
import androidx.compose.runtime.remember

val sampleExploreEvents = listOf(
    ExploreEvent("Social Support", "Community Kitchen Help", "Mahakal Anna Kshetra, Bhopal", "Daily, 12:00 PM - 2:00 PM", R.drawable.community_kitchen),
    ExploreEvent("Environmental Action", "Clean Shipra Drive", "Ram Ghat, Bhopal", "September 7, 2025 - 8:00 AM", R.drawable.shipra_drive),
    ExploreEvent("Animal Welfare", "Dog Walking Day", "City Animal Shelter", "August 24, 2025 - 9:00 AM", R.drawable.dog_walking)
)


@Composable
fun ExploreScreen() {
    // 1. ADD STATE FOR THE SELECTED FILTER
    var selectedFilter by remember { mutableStateOf("All") }

    // 2. CREATE A FILTERED LIST BASED ON THE SELECTED STATE
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
        // ... (Header and Search Bar are unchanged) ...
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

        // 3. UPDATE THE FILTER CHIPS CALL
        item {
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { newFilter ->
                    selectedFilter = newFilter
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ... (Map View is unchanged) ...
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
        // 4. DISPLAY THE FILTERED LIST
        items(filteredEvents.size) { index ->
            ExploreEventCard(event = filteredEvents[index])
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
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
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
        // The filter text should match the start of the category text
        val filters = listOf("All", "Environment", "Education", "Animal", "Social")
        items(filters.size) { index ->
            val filter = filters[index]
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
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                Toast.makeText(context, "Location permission granted!", Toast.LENGTH_SHORT).show()
                // TODO: Add logic to fetch and display events based on location
            } else if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                Toast.makeText(context, "Approximate location permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Card(
        // The modifier on the Card itself doesn't change
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        // We apply the corrected clickable modifier to the Image inside the Card
        Image(
            painter = painterResource(id = R.drawable.map_placeholder),
            contentDescription = "View From Map",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(), // This provides the ripple effect
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
fun ExploreEventCard(event: ExploreEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            // THIS IS THE MODIFIED PART
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically // Aligns text and button vertically
            ) {
                // This Column holds all the text and takes up all available space
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.category.uppercase(),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.location,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = event.dateTime,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Spacer to ensure some distance between text and button
                Spacer(modifier = Modifier.width(8.dp))

                // The Button is now at the end of the Row
                Button(
                    onClick = { /*TODO*/ },
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
        ExploreScreen()
    }
}