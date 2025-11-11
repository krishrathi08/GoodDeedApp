package eu.tutorials.gooddeedproject.organizer

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    navController: NavController,
    viewModel: OrganizerViewModel
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // 1. Find the event to edit
    val event by remember(eventId) {
        derivedStateOf {
            (viewModel.upcomingEvents.value + viewModel.completedEvents.value).find { it.id == eventId }
        }
    }

    // 2. Form fields ke liye State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var durationInHours by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Nayi selected image
    var existingImageUrl by remember { mutableStateOf<String?>(null) } // Purani image
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // 3. Jab event load ho, form ko uski details se bharo
    LaunchedEffect(event) {
        event?.let {
            title = it.title
            description = it.description
            location = it.location
            time = it.time
            category = it.category
            durationInHours = it.durationInHours.toString()
            existingImageUrl = it.imageUrl
            selectedDateMillis = it.date
        }
    }

    // Date Picker Logic
    val calendar = Calendar.getInstance()
    selectedDateMillis?.let { calendar.timeInMillis = it }
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

    val formattedDate by remember(selectedDateMillis) {
        derivedStateOf {
            selectedDateMillis?.let {
                val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                sdf.format(it)
            } ?: ""
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (event == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Picker (nayi image ya purani image dikhayega)
                AsyncImage(
                    model = imageUri ?: existingImageUrl,
                    contentDescription = "Event Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.logo)
                )

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())

                // Date Picker
                Box(modifier = Modifier.clickable(onClick = { datePickerDialog.show() }, indication = null, interactionSource = remember { MutableInteractionSource() })) {
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        label = { Text("Event Date") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Select Date") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }

                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (e.g., 10:00 AM)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = durationInHours, onValueChange = { durationInHours = it }, label = { Text("Duration in Hours") }, modifier = Modifier.fillMaxWidth())

                // Save Button
                Button(
                    onClick = {
                        val duration = durationInHours.toIntOrNull() ?: 0
                        if (title.isNotBlank() && selectedDateMillis != null) {
                            isLoading = true
                            viewModel.updateEvent(
                                event = event!!,
                                title = title,
                                description = description,
                                location = location,
                                date = selectedDateMillis!!,
                                time = time,
                                category = category,
                                durationInHours = duration,
                                newImageUri = imageUri
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Event Updated!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Update failed.", Toast.LENGTH_SHORT).show()
                                }
                                isLoading = false
                            }
                        } else {
                            Toast.makeText(context, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Save Changes")
                }
            }
        }
    }
}