package eu.tutorials.gooddeedproject.organizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.home.Event
import eu.tutorials.gooddeedproject.ui.theme.OrangeButtonColor
import java.text.SimpleDateFormat
import java.util.*

data class ActionItem(
    val title: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: OrganizerViewModel = viewModel()
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val organizerProfile by viewModel.organizerProfile.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val eventsOrganizedCount by viewModel.eventsOrganizedCount.collectAsState()

    val totalVolunteerHours by viewModel.totalVolunteerHours.collectAsState()

    LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text("Welcome back,", fontSize = 24.sp)
                    Text(organizerProfile?.name ?: "Organizer", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        item {
            StatsSection(
                eventsOrganized = eventsOrganizedCount,
                totalVolunteerHours = totalVolunteerHours,
                badgesEarned = 6
            )
        }
            item {
                UpcomingEventsSection(upcomingEvents = upcomingEvents)
            }
            item {
                AnalyticsSection()
            }
        }
    }


// ALL YOUR OTHER COMPOSABLES REMAIN EXACTLY THE SAME
@Composable
fun StatsSection(eventsOrganized: Int, totalVolunteerHours: Int, badgesEarned: Int) { // ✅ FIX: Signature updated
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), value = totalVolunteerHours.toString(), label = "Total Volunteer Hours", iconRes = R.drawable.ic_time)
        StatCard(modifier = Modifier.weight(1f), value = eventsOrganized.toString(), label = "Events Organized", iconRes = R.drawable.ic_calendar)
        StatCard(modifier = Modifier.weight(1f), value = badgesEarned.toString(), label = "Badges Earned", iconRes = R.drawable.ic_trophy)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, iconRes: Int) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = OrangeButtonColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun UpcomingEventsSection(upcomingEvents: List<Event>) { // ✅ FIX: Signature updated
    Column {
        Text("Upcoming Events", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (upcomingEvents.isEmpty()) {
            Text("You have no upcoming events.", color = MaterialTheme.colorScheme.onSurface)
        } else {
            upcomingEvents.forEach { event ->
                UpcomingEventCard(event = event)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun UpcomingEventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(event.title, fontWeight = FontWeight.Bold, color = OrangeButtonColor)
                Text(event.location, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(formatEventDate(event.date), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun AnalyticsSection() {
    val data = mapOf("This Week" to 60.9f, "Last Week" to 39.1f)
    val colors = listOf(OrangeButtonColor, Color.LightGray)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Analytics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    data.values.forEachIndexed { index, value ->
                        val sweepAngle = (value / 100f) * 360f
                        drawArc(
                            color = colors[index],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 30f)
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("60.9%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("This Week", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Volunteer Hours", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun formatEventDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}