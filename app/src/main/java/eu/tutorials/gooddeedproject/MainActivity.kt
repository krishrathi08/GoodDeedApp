package eu.tutorials.gooddeedproject

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourpackage.signup.SignUpScreen
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.home.MainScreen
import eu.tutorials.gooddeedproject.organizer.MainOrganizerScreen
import eu.tutorials.gooddeedproject.organizer.OrganizerViewModel
import eu.tutorials.gooddeedproject.signin.OrganizerSignInScreen
import eu.tutorials.gooddeedproject.signin.UserSignInScreen
import eu.tutorials.gooddeedproject.signup.OrganizerSignUpScreenStep1
import eu.tutorials.gooddeedproject.signup.OrganizerSignUpScreenStep2
import eu.tutorials.gooddeedproject.signup.UserSignUpScreenStep1
import eu.tutorials.gooddeedproject.signup.UserSignUpScreenStep2
import eu.tutorials.gooddeedproject.ui.theme.GoodDeedProjectTheme

enum class UserType {
    USER, ORGANIZER, UNKNOWN, LOADING
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContent {
                GoodDeedProjectTheme {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()


                    val currentUser by authViewModel.currentUser.collectAsState()
                    val userProfile by authViewModel.userProfile.collectAsState()

                    if (currentUser != null) {
                        if (userProfile == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            when (userProfile?.userType) {
                                "USER" -> MainScreen(authViewModel = authViewModel)
                                "ORGANIZER" -> MainOrganizerScreen(
                                    mainNavController = navController,
                                    authViewModel = authViewModel,
                                    organizerViewModel = OrganizerViewModel()
                                )
                                else -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Error: User role is invalid. Please contact support.")
                                    }
                                }
                            }
                        }
                    } else {
                        AuthNavigation(navController = navController, authViewModel = authViewModel)
                    }
                }
            }
    }
}

@Composable
fun AuthNavigation(authViewModel: AuthViewModel, navController: NavHostController) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // Listen to sign-in success here to navigate
    LaunchedEffect(authState.success) {
        if (authState.success) {
            authViewModel.resetAuthState()
        }
    }

    NavHost(navController = navController, startDestination = "selection") {
        composable("selection") {
            SignUpScreen(
                onUserSignUpClick = { navController.navigate("user_signup_step1") },
                onOrganizerSignUpClick = { navController.navigate("organizer_signup_step1") }
            )
        }
        // User sign-up flow
        composable("user_signup_step1") {
            UserSignUpScreenStep1(
                authViewModel = authViewModel,
                onNextClick = { navController.navigate("user_signup_step2") },
                onSignInClick = { navController.navigate("user_signin") }
            )
        }
        composable("user_signup_step2") {
            UserSignUpScreenStep2(
                authViewModel = authViewModel,
                onSignInClick = { navController.navigate("user_signin") },
                onSignUpClick = {
                    // Sign-up logic is in ViewModel, success will trigger state change
                }
            )
        }
        // Organizer sign-up flow
        composable("organizer_signup_step1") {
            OrganizerSignUpScreenStep1(
                authViewModel = authViewModel,
                onNextClick = { navController.navigate("organizer_signup_step2") },
                onSignInClick = { navController.navigate("organizer_signin") }
            )
        }
        composable("organizer_signup_step2") {
            OrganizerSignUpScreenStep2(
                authViewModel = authViewModel,
                onSignUpClick = {
                    // Sign-up logic is in ViewModel, success will trigger state change
                }
            )
        }
        // Sign-in screens
        composable("user_signin") {
            UserSignInScreen(
                authViewModel = authViewModel,
                onSignUpClick = { navController.navigate("user_signup_step1") }
            )
        }
        composable("organizer_signin") {
            OrganizerSignInScreen(
                authViewModel = authViewModel,
                onSignUpClick = { navController.navigate("organizer_signup_step1") }
            )
        }
    }
}