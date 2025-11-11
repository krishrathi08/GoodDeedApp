package eu.tutorials.gooddeedproject.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(viewModel: OrganizerViewModel) {
    val notifications by viewModel.notifications.collectAsState()

    // Mark notifications as read when this screen is viewed
    LaunchedEffect(Unit) {
        viewModel.markNotificationsAsRead()
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(notifications, key = { it.id }) { notification ->
            NotificationItem(notification = notification)
            Divider()
        }
    }
}

@Composable
fun NotificationItem(notification: OrganizerViewModel.Notification) {
    val fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
    val color = if (notification.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = notification.message,
            fontWeight = fontWeight,
            color = color
        )
    }
}