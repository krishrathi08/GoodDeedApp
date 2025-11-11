package eu.tutorials.gooddeedproject.organizer

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.home.CreatePostScreen
import eu.tutorials.gooddeedproject.ui.theme.LightGrayBackground
import eu.tutorials.gooddeedproject.ui.theme.OrangeButtonColor

// Data models and Sealed class for this screen
sealed class OrganizerScreen(val route: String) {
    object Dashboard : OrganizerScreen("dashboard")
    object Events : OrganizerScreen("events")
    object Volunteers : OrganizerScreen("volunteers")
    object Profile : OrganizerScreen("profile")
}

data class OrganizerBottomBarItem(val screen: OrganizerScreen, val title: String, @DrawableRes val iconRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainOrganizerScreen(
    mainNavController: NavHostController,
    authViewModel: AuthViewModel,
    organizerViewModel: OrganizerViewModel
) {
    val innerNavController = rememberNavController()
    val organizerViewModel: OrganizerViewModel = viewModel()

    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTitle = when (currentRoute) {
        OrganizerScreen.Dashboard.route -> "Dashboard"
        OrganizerScreen.Events.route -> "My Events"
        OrganizerScreen.Volunteers.route -> "Volunteers"
        OrganizerScreen.Profile.route -> "Profile"
        "notifications" -> "Notifications"
        else -> "Organizer"
    }
    val unreadCount by organizerViewModel.unreadNotificationCount.collectAsState()

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = currentTitle,
                unreadNotificationCount = unreadCount,
                onNotificationClick = {
                    innerNavController.navigate("notifications")
                },
                onLogoutClick = {
                    authViewModel.signOut()
                }
            )
        },
        bottomBar = { OrganizerBottomNavigationBar(navController = innerNavController) },
        containerColor = LightGrayBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            OrganizerNavGraph(
                innerNavController = innerNavController,
                mainNavController = mainNavController,
                authViewModel = authViewModel,
                organizerViewModel = organizerViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    unreadNotificationCount: Int,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Your Logo and Title
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_withouttext),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(36.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            // Notification Icon with Badge
            BadgedBox(
                badge = {
                    if (unreadNotificationCount > 0) {
                        Badge { Text("$unreadNotificationCount") }
                    }
                }
            ) {
                IconButton(onClick = onNotificationClick) {
                       Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Log Out") },
                        onClick = {
                            showMenu = false
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrganizerBottomNavigationBar(navController: NavController) {
    val items = listOf(
        OrganizerBottomBarItem(OrganizerScreen.Dashboard, "Dashboard", R.drawable.ic_home),
        OrganizerBottomBarItem(OrganizerScreen.Events, "Events", R.drawable.ic_explore),
        OrganizerBottomBarItem(OrganizerScreen.Volunteers, "Volunteers", R.drawable.ic_community),
        OrganizerBottomBarItem(OrganizerScreen.Profile, "Profile", R.drawable.ic_profile)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            NavigationBarItem(
                label = { Text(text = item.title) },
                icon = { Icon(painter = painterResource(id = item.iconRes), contentDescription = item.title, modifier = Modifier.size(28.dp)) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = OrangeButtonColor,
                    selectedTextColor = OrangeButtonColor,
                    indicatorColor = LightGrayBackground
                ),
            )
        }
    }
}

@Composable
fun OrganizerNavGraph(
    innerNavController: NavHostController,
    mainNavController: NavHostController,
    authViewModel: AuthViewModel,
    organizerViewModel: OrganizerViewModel
) {
    NavHost(
        navController = innerNavController,
        startDestination = OrganizerScreen.Dashboard.route
    ) {
        composable(route = OrganizerScreen.Dashboard.route) {
            OrganizerDashboardScreen(
                navController = mainNavController,
                authViewModel = authViewModel,
                viewModel = organizerViewModel
            )
        }
        composable(route = OrganizerScreen.Events.route) {
            OrganizerEventsScreen(
                navController = innerNavController,
                viewModel = organizerViewModel
            )
        }
        composable(route = "create_event") {
            CreateEventScreen(
                navController = innerNavController,
                viewModel = organizerViewModel
            )
        }
        composable(route = OrganizerScreen.Volunteers.route) {
            EventListForVolunteers(
                viewModel = organizerViewModel,
                onEventClick = { eventId ->
                    // Navigate to the shared volunteer list screen
                    innerNavController.navigate(RegisteredScreen.VolunteerList.createRoute(eventId))
                }
            )
        }
        composable(route = OrganizerScreen.Profile.route) {
            OrganizerProfileScreen(
                navController = innerNavController, // Use innerNavController for this
                viewModel = organizerViewModel
            )
        }
        composable(route = OrganizerScreen.Volunteers.route) {
            VolunteersTabScreen(
                mainNavController = mainNavController,
                innerNavController = innerNavController,
                organizerViewModel = organizerViewModel
            )
        }
        composable("create_post") {
            CreatePostScreen(
                navController = innerNavController,
                communityViewModel = viewModel()
            )
        }
        composable("notifications") {
            NotificationsScreen(viewModel = organizerViewModel)
        }
        composable("edit_organizer_profile") {
            EditOrganizerProfileScreen(
                navController = innerNavController,
                viewModel = organizerViewModel
            )
        }
        composable("notifications") {
            NotificationsScreen(viewModel = organizerViewModel)
        }
        composable(
            route = "manage_event/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            ManageEventScreen(
                eventId = backStackEntry.arguments?.getString("eventId") ?: "",
                navController = innerNavController, // Use innerNavController
                viewModel = organizerViewModel
            )
        }
        composable(route = RegisteredScreen.VolunteerList.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            VolunteerListForEvent(
                eventId = eventId,
                viewModel = organizerViewModel,
                onBack = { innerNavController.popBackStack() }
            )
        }
        composable(
            route = "edit_event/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EditEventScreen( // We'll create this screen next
                eventId = backStackEntry.arguments?.getString("eventId") ?: "",
                navController = innerNavController,
                viewModel = organizerViewModel
            )
        }
    }
}