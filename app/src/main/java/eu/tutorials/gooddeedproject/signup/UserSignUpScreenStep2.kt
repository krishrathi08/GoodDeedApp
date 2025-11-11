package eu.tutorials.gooddeedproject.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSignUpScreenStep2(
    authViewModel: AuthViewModel,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit   // ✅ this will take you to Home
) {
    val signUpState by authViewModel.userSignUpState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedInterest by remember { mutableStateOf("Interests") }

    // ✅ On signup success → set userType = USER, then navigate to Home
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
                        docRef.set(mapOf("userType" to "USER"), SetOptions.merge()).await()
                    }

                    Toast.makeText(context, "Sign up successful — Welcome!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Signed up but failed to set userType: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            authViewModel.resetAuthState()
            onSignUpClick() // ✅ go straight to Home
        }
    }

    // ✅ Show auth errors
    LaunchedEffect(authState.error) {
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
                    modifier = Modifier.size(180.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Additional Details",
                    color = PrimaryBlueText,
                    fontSize = 24.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Step 2 of 2", color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(24.dp))

                InterestsDropdown(
                    selectedInterest = selectedInterest,
                    onInterestSelected = { selectedInterest = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Your Availability", modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onBackground)
                MultiSelectChipGroup(
                    options = listOf("Weekdays", "Weekends", "Evenings"),
                    selectedOptions = signUpState.availability,
                    onSelectionChanged = { authViewModel.onUserSignUpInfoChanged(availability = it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your Skills", modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onBackground)
                MultiSelectChipGroup(
                    options = listOf("Teaching", "Medical", "Driving", "Management"),
                    selectedOptions = signUpState.skills,
                    onSelectionChanged = { authViewModel.onUserSignUpInfoChanged(skills = it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuthTextField(
                    value = signUpState.dob,
                    onValueChange = { authViewModel.onUserSignUpInfoChanged(dob = it) },
                    label = "Date of Birth (DD/MM/YYYY)"
                )
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
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { authViewModel.signUpWithDetails(password, "USER") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsDropdown(
    selectedInterest: String,
    onInterestSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interests = listOf(
        "Environmental Protection",
        "Animal Welfare",
        "Community Health",
        "Education for Children",
        "Disaster Relief"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedInterest,
            onValueChange = {},
            readOnly = true,
            label = { Text("Interests") },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlueText,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            interests.forEach { interest ->
                DropdownMenuItem(
                    text = { Text(interest) },
                    onClick = {
                        onInterestSelected(interest)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            val isSelected = selectedOptions.contains(option)
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = selectedOptions.toMutableSet()
                    if (isSelected) {
                        newSelection.remove(option)
                    } else {
                        newSelection.add(option)
                    }
                    onSelectionChanged(newSelection)
                },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlueText,
                    selectedLabelColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
