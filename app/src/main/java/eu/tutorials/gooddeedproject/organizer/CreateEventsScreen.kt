package eu.tutorials.gooddeedproject.organizer

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: OrganizerViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var durationInHours by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val formattedDate by remember(selectedDateMillis) {
        derivedStateOf {
            selectedDateMillis?.let {
                val sdf = SimpleDateFormat("dd MMMM, yyyY", Locale.getDefault())
                sdf.format(it)
            } ?: ""
        }
    }

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Date Picker Dialog Logic
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDateMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())

            // ✅ THE FIX: Wrap the TextField in a Box and make the Box clickable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { datePickerDialog.show() },
                        // This removes the click ripple effect
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    label = { Text("Event Date") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Select Date") },
                    modifier = Modifier.fillMaxWidth(),
                    // We disable the TextField so it can't be focused, allowing the Box to handle all clicks
                    enabled = false,
                    // We must override the disabled colors so it looks enabled
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }

            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (e.g., 10:00 AM - 2:00 PM)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g., Environment)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = durationInHours, onValueChange = { durationInHours = it }, label = { Text("Duration in Hours (e.g., 4)") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = { photoPickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (imageUri == null) "Select Image" else "Image Selected!")
            }

            Button(
                onClick = {
                    val duration = durationInHours.toIntOrNull() ?: 0
                    if (title.isNotBlank() && imageUri != null && selectedDateMillis != null && duration > 0) {
                        isLoading = true
                        viewModel.createEvent(title, description, category, location, selectedDateMillis!!, time, duration, imageUri!!) { success ->
                            if (success) {
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to create event.", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields and select an image/date.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Create Event")
            }
        }
    }
}