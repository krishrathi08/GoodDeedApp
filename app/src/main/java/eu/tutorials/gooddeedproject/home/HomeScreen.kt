package eu.tutorials.gooddeedproject.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel


// Sample Data with IDs
val sampleUpcomingEvents = listOf(
    UpcomingEvent(1, "Beach Cleanup Day", "Juhu Beach", "07/09/25, 9:00 AM", R.drawable.beach_cleanup),
    UpcomingEvent(2, "Emergency Relief", "Disaster Site", "Ongoing", R.drawable.emergency_relief)
)
val sampleSuggestedEvents = listOf(
    SuggestedEvent(3, "Help Kids Read", "Central Library", R.drawable.help_kids_read),
    SuggestedEvent(4, "Animal Rescue", "City Animal Shelter", R.drawable.animal_rescue)
)

@Composable
fun HomeScreenContent(navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profile by profileViewModel.profile.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeHeader(name = profile.name)
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            StatsSection()
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            SectionHeader(title = "Your Upcoming Events", onSeeAllClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(sampleUpcomingEvents) { event ->
            UpcomingEventCard(
                event = event,
                onClick = { navController.navigate(Screen.EventDetails.createRoute(event.id)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
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
        items(sampleSuggestedEvents) { event ->
            SuggestedEventCard(
                event = event,
                onClick = { navController.navigate(Screen.EventDetails.createRoute(event.id)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_time,
            value = "76",
            label = "Volunteer Hours"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_calendar,
            value = "12",
            label = "Events Attended"
        )
        StatCard(
            modifier = Modifier.weight(1f),
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
fun UpcomingEventCard(event: UpcomingEvent, onClick: () -> Unit) {
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
fun SuggestedEventCard(event: SuggestedEvent, onClick: () -> Unit) {
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
                modifier = Modifier.fillMaxWidth().height(150.dp),
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
fun HomeScreenPreview() {
    GoodDeedProjectTheme {
        HomeScreenContent(navController = rememberNavController())
    }
}