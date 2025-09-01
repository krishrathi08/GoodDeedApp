package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.GoodDeedProjectTheme
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText

// --- Sample Data ---
val sampleUpcomingEvents = listOf(
    UpcomingEvent("Beach Cleanup Day", "Juhu Beach", "07/09/25, 9:00 AM", R.drawable.beach_cleanup),
    UpcomingEvent("Emergency Relief", "Juhu Beach", "07/09/25, 9:00 AM", R.drawable.emergency_relief)
)
val sampleSuggestedEvents = listOf(
    SuggestedEvent("Help Kids Read", "Central Library", R.drawable.help_kids_read),
    SuggestedEvent("Animal Rescue", "Animal Shelter", R.drawable.animal_rescue)
)

// RENAMED from HomeScreen
@Composable
fun HomeScreenContent() {
    // REMOVED Scaffold wrapper
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Welcome Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeHeader(name = "Krish")
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Stats Section
        item {
            StatsSection()
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Upcoming Events Section
        item {
            SectionHeader(title = "Your Upcoming Events", onSeeAllClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(sampleUpcomingEvents.size) { index ->
            UpcomingEventCard(event = sampleUpcomingEvents[index])
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Suggested For You Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Suggested For You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(sampleSuggestedEvents.size) { index ->
            SuggestedEventCard(event = sampleSuggestedEvents[index])
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// REMOVED HomeTopAppBar and AppBottomNavigationBar from this file

@Composable
fun WelcomeHeader(name: String) {
    Column {
        Text(
            text = "Welcome back, $name!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Ready to make a difference today?",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun StatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        // Use spacedBy for consistent spacing between items
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f), // This card takes 1/3 of the space
            iconRes = R.drawable.ic_time,
            value = "76",
            label = "Volunteer Hours"
        )
        StatCard(
            modifier = Modifier.weight(1f), // This card takes 1/3 of the space
            iconRes = R.drawable.ic_calendar,
            value = "12",
            label = "Events Attended"
        )
        StatCard(
            modifier = Modifier.weight(1f), // This card takes 1/3 of the space
            iconRes = R.drawable.ic_trophy,
            value = "6",
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = PrimaryBlueText,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        TextButton(onClick = onSeeAllClick) {
            Text(text = "See All", color = PrimaryBlueText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UpcomingEventCard(event: UpcomingEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = event.imageRes),
                contentDescription = event.title,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                Text(text = event.location, fontSize = 14.sp, color = Color.DarkGray)
                Text(text = event.dateTime, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SuggestedEventCard(event: SuggestedEvent) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                    Text(text = event.location, fontSize = 14.sp, color = Color.DarkGray)
                }
                Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor)) {
                    Text("Details")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GoodDeedProjectTheme {
        HomeScreenContent()
    }
}