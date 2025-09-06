package eu.tutorials.gooddeedproject.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.tutorials.gooddeedproject.auth.AuthViewModel
import eu.tutorials.gooddeedproject.signup.AuthTextField
import eu.tutorials.gooddeedproject.signup.MultiSelectChipGroup
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val signUpState by authViewModel.signUpState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // In a real app, you would save these details to Firestore here.
                        // For now, it just goes back.
                        navController.popBackStack()
                    }) {
                        Text("Save", color = BlueButtonColor, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Each field is now an OutlinedTextField, allowing the user to edit the value.
            // The value is read from the AuthViewModel and updated using onSignUpInfoChanged.

            AuthTextField(
                value = signUpState.name,
                onValueChange = { authViewModel.onSignUpInfoChanged(name = it) },
                label = "Name"
            )

            AuthTextField(
                value = signUpState.email,
                onValueChange = { authViewModel.onSignUpInfoChanged(email = it) },
                label = "Email",
                keyboardType = KeyboardType.Email
            )

            AuthTextField(
                value = signUpState.phone,
                onValueChange = { authViewModel.onSignUpInfoChanged(phone = it) },
                label = "Phone Number",
                keyboardType = KeyboardType.Phone
            )

            AuthTextField(
                value = signUpState.city,
                onValueChange = { authViewModel.onSignUpInfoChanged(city = it) },
                label = "City"
            )

            AuthTextField(
                value = signUpState.dob,
                onValueChange = { authViewModel.onSignUpInfoChanged(dob = it) },
                label = "Date of Birth (DD/MM/YYYY)"
            )

            Text("Your Availability", color = MaterialTheme.colorScheme.onBackground)
            MultiSelectChipGroup(
                options = listOf("Weekdays", "Weekends", "Evenings"),
                selectedOptions = signUpState.availability,
                onSelectionChanged = { authViewModel.onSignUpInfoChanged(availability = it) }
            )

            Text("Your Skills", color = MaterialTheme.colorScheme.onBackground)
            MultiSelectChipGroup(
                options = listOf("Teaching", "Medical", "Driving", "Management"),
                selectedOptions = signUpState.skills,
                onSelectionChanged = { authViewModel.onSignUpInfoChanged(skills = it) }
            )
        }
    }
}