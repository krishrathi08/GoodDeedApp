package eu.tutorials.gooddeedproject.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R

@Composable
fun OrganizerProfileScreen(
    navController: NavController,
    viewModel: OrganizerViewModel
) {
    val organizerProfile by viewModel.organizerProfile.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("edit_organizer_profile") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
            }
        }
    ) { paddingValues ->
        if (organizerProfile == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val profile = organizerProfile!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = profile.profilePicUrl,
                    contentDescription = "Organizer Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.logo)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(profile.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                ProfileInfoSection(profile = profile)
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(profile: eu.tutorials.gooddeedproject.models.UserProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileInfoRow(icon = Icons.Default.Email, title = "Email", subtitle = profile.email)
        ProfileInfoRow(icon = Icons.Default.Phone, title = "Phone", subtitle = profile.phone)
        ProfileInfoRow(icon = Icons.Default.Business, title = "City", subtitle = profile.city)
        // You can add more fields like Bio, Categories here if they exist in your UserProfile model
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}