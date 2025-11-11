package eu.tutorials.gooddeedproject.home

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.LightGrayBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel,
    eventsViewModel: EventsViewModel = viewModel(),
    taggedEventId: String? = null // ✅ YAHAN PARAMETER ADD KIYA GAYA HAI
) {
    var captionText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    var showEventSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val completedEvents by eventsViewModel.completedEvents.collectAsState()

    // Yeh logic automatically event ko select kar lega agar ID pass hui hai
    LaunchedEffect(taggedEventId, completedEvents) {
        if (taggedEventId != null && completedEvents.isNotEmpty()) {
            selectedEvent = completedEvents.find { it.id == taggedEventId }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )
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

    if (showEventSheet) {
        EventSelectionBottomSheet(
            events = completedEvents,
            onDismiss = { showEventSheet = false },
            onEventSelected = { event ->
                selectedEvent = event
                showEventSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (imageUri != null) {
                            communityViewModel.addPost(captionText, imageUri, selectedEvent?.id)
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Post", color = BlueButtonColor, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = LightGrayBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected post image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Tap to add a photo", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = captionText,
                onValueChange = { captionText = it },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                placeholder = { Text("Write a caption...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedEvent == null) {
                OutlinedButton(
                    onClick = { showEventSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tag Event")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tag an Event")
                }
            } else {
                InputChip(
                    selected = true,
                    onClick = { showEventSheet = true },
                    label = { Text(selectedEvent!!.title) },
                    trailingIcon = {
                        IconButton(onClick = { selectedEvent = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove tag")
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSelectionBottomSheet(
    events: List<Event>,
    onDismiss: () -> Unit,
    onEventSelected: (Event) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                "Tag a Completed Event",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(events, key = { it.id }) { event ->
                    Text(
                        text = event.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventSelected(event) }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}