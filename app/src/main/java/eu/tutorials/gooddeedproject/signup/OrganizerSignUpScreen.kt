package eu.tutorials.gooddeedproject.signup

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.*

@Composable
fun OrganizerSignUpScreen(
    authViewModel: AuthViewModel,
    onSignInClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        authState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            authViewModel.resetAuthState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.width(180.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProfileImagePicker(
                    imageUri = imageUri,
                    onClick = { /* Photo picker logic here */ }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Register As Organizer",
                    color = OrangeButtonColor,
                    fontSize = 24.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                AuthTextField(value = name, onValueChange = { name = it }, label = "Name")
                Spacer(modifier = Modifier.height(16.dp))
                AuthTextField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(16.dp))
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuthTextField(value = bio, onValueChange = { bio = it }, label = "Bio", minLines = 3)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { authViewModel.signUp(email, password) }, // Calls ViewModel
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeButtonColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Sign Up", fontSize = 18.sp, fontFamily = poppinsFontFamily, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                SignInPrompt(onSignInClick = onSignInClick, linkColor = OrangeButtonColor)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        if (authState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}