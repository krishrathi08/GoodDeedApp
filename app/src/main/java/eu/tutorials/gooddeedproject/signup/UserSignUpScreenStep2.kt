package eu.tutorials.gooddeedproject.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSignUpScreenStep2(
    authViewModel: AuthViewModel,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val signUpState by authViewModel.signUpState.collectAsState()
    val password by remember { mutableStateOf("") } // We need a password field here as well for final submission
    val authState by authViewModel.authState.collectAsState()

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

            AuthTextField(
                value = signUpState.phone,
                onValueChange = { authViewModel.onSignUpInfoChanged(phone = it) },
                label = "Phone Number",
                keyboardType = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = signUpState.city,
                onValueChange = { authViewModel.onSignUpInfoChanged(city = it) },
                label = "City"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Availability", modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onBackground)
            MultiSelectChipGroup(
                options = listOf("Weekdays", "Weekends", "Evenings"),
                selectedOptions = signUpState.availability,
                onSelectionChanged = { authViewModel.onSignUpInfoChanged(availability = it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Skills", modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onBackground)
            MultiSelectChipGroup(
                options = listOf("Teaching", "Medical", "Driving", "Management"),
                selectedOptions = signUpState.skills,
                onSelectionChanged = { authViewModel.onSignUpInfoChanged(skills = it) }
            )

            // You'd typically use a Date Picker dialog for this, but a text field is simpler for now
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                value = signUpState.dob,
                onValueChange = { authViewModel.onSignUpInfoChanged(dob = it) },
                label = "Date of Birth (DD/MM/YYYY)"
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { authViewModel.signUpWithDetails(password) }, // Use the new function
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Sign Up", fontSize = 18.sp, fontFamily = poppinsFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            SignInPrompt(onSignInClick = onSignInClick, linkColor = PrimaryBlueText)
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
        items(options.size) { index ->
            val option = options[index]
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
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}