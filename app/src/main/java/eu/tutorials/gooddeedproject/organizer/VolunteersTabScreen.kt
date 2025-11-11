package eu.tutorials.gooddeedproject.organizer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController

@Composable
fun VolunteersTabScreen(
    mainNavController: NavHostController,
    innerNavController: NavHostController,
    organizerViewModel: OrganizerViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Registered", "Feed")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> RegisteredVolunteersScreen(organizerViewModel = organizerViewModel,
                innerNavController = innerNavController)
            1 -> OrganizerFeedScreen(mainNavController = mainNavController,
                innerNavController = innerNavController)

        }
    }
}