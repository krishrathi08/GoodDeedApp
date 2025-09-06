package eu.tutorials.gooddeedproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourpackage.signup.SignUpScreen
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.home.MainScreen
import eu.tutorials.gooddeedproject.signin.OrganizerSignInScreen
import eu.tutorials.gooddeedproject.signin.UserSignInScreen
import eu.tutorials.gooddeedproject.signup.OrganizerSignUpScreen
import eu.tutorials.gooddeedproject.signup.UserSignUpScreenStep1
import eu.tutorials.gooddeedproject.signup.UserSignUpScreenStep2
import eu.tutorials.gooddeedproject.ui.theme.GoodDeedProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoodDeedProjectTheme {
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()

                if (currentUser != null) {
                    // If user is logged in, show the main app with home, explore, etc.
                    MainScreen(authViewModel = authViewModel)
                } else {
                    // If user is not logged in, show the authentication flow
                    AuthNavigation(authViewModel)
                }
            }
        }
    }
}

@Composable
fun AuthNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "selection") {
        composable("selection") {
            SignUpScreen(
                onUserSignUpClick = { navController.navigate("user_signup_step1") },
                onOrganizerSignUpClick = { navController.navigate("organizer_signup") }
            )
        }
        composable("user_signup_step1") {
            UserSignUpScreenStep1(
                authViewModel = authViewModel,
                onNextClick = { navController.navigate("user_signup_step2") }
            )
        }
        composable("user_signup_step2") {
            UserSignUpScreenStep2(
                authViewModel = authViewModel,
                onSignUpClick = {}, // Success is handled by the MainActivity state change
                onSignInClick = { navController.navigate("user_signin") }
            )
        }
        composable("organizer_signup") {
            OrganizerSignUpScreen(
                authViewModel = authViewModel,
                onSignInClick = { navController.navigate("organizer_signin") }
            )
        }
        composable("user_signin") {
            UserSignInScreen(
                authViewModel = authViewModel,
                onSignUpClick = { navController.navigate("user_signup_step1") }
            )
        }
        composable("organizer_signin") {
            OrganizerSignInScreen(
                authViewModel = authViewModel,
                onSignUpClick = { navController.navigate("organizer_signup") }
            )
        }
    }
}