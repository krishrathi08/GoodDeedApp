package eu.tutorials.gooddeedproject.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.LightGrayBackground
import eu.tutorials.gooddeedproject.ui.theme.PrimaryBlueText
import androidx.navigation.NavType
import androidx.navigation.navArgument

// Defines all the possible screens in our main app navigation
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Community : Screen("community")
    object Profile : Screen("profile")
    object CreatePost : Screen("create_post")
    object AccountDetails : Screen("account_details")
    object AllBadges : Screen("all_badges")

    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: Int) = "event_details/$eventId"
    }
}

data class BottomBarItem(val screen: Screen, val title: String, @DrawableRes val iconRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            val title = when (currentRoute) {
                Screen.Home.route -> "Home"
                Screen.Explore.route -> "Explore"
                Screen.Community.route -> "Community"
                Screen.Profile.route -> "Profile"
                else -> ""
            }
            // Only show the top bar on the main bottom navigation screens
            if (title.isNotEmpty()) {
                AppTopAppBar(title = title, navController = navController, authViewModel = authViewModel)
            }
        },
        bottomBar = { AppBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            if (currentRoute == Screen.Community.route) {
                CommunityFAB(onClick = { navController.navigate(Screen.CreatePost.route) })
            }
        },
        containerColor = LightGrayBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            BottomNavGraph(navController = navController, authViewModel = authViewModel)
        }
    }
}

@Composable
fun AppTopAppBar(title: String, navController: NavController, authViewModel: AuthViewModel) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_withouttext),
                contentDescription = "App Logo",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Gray
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Account Details") },
                    onClick = {
                        navController.navigate(Screen.AccountDetails.route)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Log Out") },
                    onClick = {
                        authViewModel.signOut()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomBarItem(Screen.Home, "Home", R.drawable.ic_home),
        BottomBarItem(Screen.Explore, "Explore", R.drawable.ic_explore),
        BottomBarItem(Screen.Community, "Community", R.drawable.ic_community),
        BottomBarItem(Screen.Profile, "Profile", R.drawable.ic_profile)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
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
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    selectedIconColor = PrimaryBlueText,
                    selectedTextColor = PrimaryBlueText,
                    indicatorColor = LightGrayBackground
                ),
            )
        }
    }
}

@Composable
fun CommunityFAB(onClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            kotlinx.coroutines.delay(3000)
            isExpanded = false
        }
    }

    ExtendedFloatingActionButton(
        onClick = {
            if (isExpanded) {
                onClick()
            } else {
                isExpanded = true
            }
        },
        text = { AnimatedVisibility(visible = isExpanded) { Text("New Post") } },
        icon = { Icon(Icons.Default.Add, contentDescription = "New Post") },
        containerColor = BlueButtonColor,
        contentColor = Color.White,
        expanded = isExpanded,
    )
}

@Composable
fun BottomNavGraph(navController: androidx.navigation.NavHostController, authViewModel: AuthViewModel) {
    val communityViewModel: CommunityViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) { HomeScreenContent(navController) }
        composable(route = Screen.Explore.route) { ExploreScreen(navController) }
        composable(route = Screen.Community.route) { CommunityScreen(authViewModel = authViewModel, communityViewModel = communityViewModel) }
        composable(route = Screen.Profile.route) { ProfileScreen(navController = navController, authViewModel = authViewModel) }
        composable(route = Screen.CreatePost.route) { CreatePostScreen(navController = navController, viewModel = communityViewModel) }
        composable(route = Screen.AccountDetails.route) { AccountDetailsScreen(navController = navController, authViewModel = authViewModel) }
        composable(route = Screen.AllBadges.route) { AllBadgesScreen(navController = navController) }
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            if (eventId != null) {
                EventDetailsScreen(eventId = eventId, navController = navController)
            }
        }
    }
}