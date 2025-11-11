package eu.tutorials.gooddeedproject.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.*
import kotlinx.coroutines.tasks.await

@Composable
fun OrganizerSignUpScreenStep2(
    authViewModel: AuthViewModel,
    onSignUpClick: () -> Unit
) {
    val signUpState by authViewModel.organizerSignUpState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ✅ Observe signup success
    LaunchedEffect(authState.success) {
        if (authState.success) {
            val uid = authViewModel.currentUserId
            if (uid != null) {
                try {
                    val db = Firebase.firestore
                    val docRef = db.collection("users").document(uid)
                    val snapshot = docRef.get().await()
                    val existingType = snapshot.getString("userType")

                    if (existingType == null) {
                        docRef.set(mapOf("userType" to "ORGANIZER"), SetOptions.merge()).await()
                    }

                    Toast.makeText(context, "Welcome Organizer!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Signed up but failed to set userType: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            // ✅ Reset state and navigate to Organizer dashboard (not back to sign-in)
            authViewModel.resetAuthState()
            onSignUpClick()
        }
    }

    // Show auth errors
    LaunchedEffect(authState.error) {
        authState.error?.let { err ->
            Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
            authViewModel.resetAuthState()
        }
    }

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
                modifier = Modifier.size(180.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Additional Details",
                color = OrangeButtonColor,
                fontSize = 24.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 2 of 2", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                value = signUpState.regId,
                onValueChange = { authViewModel.onOrganizerSignUpInfoChanged(regId = it) },
                label = "NGO Registration ID"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = signUpState.phone,
                onValueChange = { authViewModel.onOrganizerSignUpInfoChanged(phone = it) },
                label = "Contact Number",
                keyboardType = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = signUpState.city,
                onValueChange = { authViewModel.onOrganizerSignUpInfoChanged(city = it) },
                label = "City"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = signUpState.bio,
                onValueChange = { authViewModel.onOrganizerSignUpInfoChanged(bio = it) },
                label = "Mission / Bio",
                minLines = 3
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { authViewModel.signUpWithDetails(password, "ORGANIZER") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeButtonColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Sign Up", fontSize = 18.sp, fontFamily = poppinsFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (authState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
