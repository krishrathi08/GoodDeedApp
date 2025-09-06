package eu.tutorials.gooddeedproject.home

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profile by profileViewModel.profile.collectAsState()
    val badges by profileViewModel.badges.collectAsState()
    val completedEvents by profileViewModel.completedEvents.collectAsState()
    val signUpState by authViewModel.signUpState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileHeader(profile = profile, profilePicUri = signUpState.profilePicUri)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            BadgeCollection(
                badges = badges,
                navController = navController
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Completed Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(completedEvents) { event ->
            CompletedEventCard(event = event)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile, profilePicUri: Uri?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = profile.profilePicUrl.ifEmpty { profilePicUri ?: R.drawable.profile_krish },
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Display the user's actual name
            Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = profile.joinDate, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(value = profile.totalHours.toString(), label = "Total Hours")
                StatItem(value = profile.eventsAttended.toString(), label = "Events")
                StatItem(value = profile.ngosHelped.toString(), label = "NGOs Helped")
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun BadgeCollection(badges: List<Badge>, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            // Center content horizontally within the column
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Badge Collection" title remains aligned to the start
            Text(
                text = "Badge Collection",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start) // Keep title aligned left
            )
            Spacer(modifier = Modifier.height(16.dp))

            // LazyRow for badges
            LazyRow(
                modifier = Modifier.fillMaxWidth(), // Fill width to center items within it
                horizontalArrangement = Arrangement.Center, // Center badges horizontally
                verticalAlignment = Alignment.CenterVertically // Center badges vertically if they had different heights
            ) {
                // We use .take(2) here just for the preview on the profile page
                items(badges.take(2)) { badge ->
                    Image(
                        painter = painterResource(id = badge.imageRes),
                        contentDescription = "Badge",
                        modifier = Modifier.size(150.dp) // Increased size for badges
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(Screen.AllBadges.route) },
                colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor),
                // The Button is already centered because its parent Column has horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("See All")
            }
        }
    }
}

@Composable
fun CompletedEventCard(event: CompletedEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueText)
                Text(text = event.details, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}