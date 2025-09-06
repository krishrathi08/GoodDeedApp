package eu.tutorials.gooddeedproject.signup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.*

@Composable
fun UserSignUpScreenStep1(
    authViewModel: AuthViewModel,
    onNextClick: () -> Unit
) {
    val signUpState by authViewModel.signUpState.collectAsState()
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- PHOTO PICKER LAUNCHER ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // When an image is selected, update the ViewModel
            if (uri != null) {
                authViewModel.onSignUpInfoChanged(profilePicUri = uri)
            }
        }
    )

    // --- PERMISSION LAUNCHER ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
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
                modifier = Modifier.width(180.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // The ProfileImagePicker now gets its URI from the ViewModel
            ProfileImagePicker(
                imageUri = signUpState.profilePicUri,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Register As User",
                color = PrimaryBlueText,
                fontSize = 24.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 1 of 2", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                value = signUpState.name,
                onValueChange = { authViewModel.onSignUpInfoChanged(name = it) },
                label = "Name"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = signUpState.email,
                onValueChange = { authViewModel.onSignUpInfoChanged(email = it) },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNextClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Next", fontSize = 18.sp, fontFamily = poppinsFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    }
}