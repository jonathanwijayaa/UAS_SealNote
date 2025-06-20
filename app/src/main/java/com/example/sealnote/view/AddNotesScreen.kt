package com.example.sealnote.view

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sealnote.ui.theme.SealnoteTheme // Pastikan import ini ada
import com.example.sealnote.viewmodel.AddEditNoteViewModel
import com.example.sealnote.viewmodel.UiEvent

@Composable
fun AddEditNoteRoute(
    onBack: () -> Unit,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    // Mengambil state dari ViewModel
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    // Mengambil status bookmark dari ViewModel
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    // Mengambil status secret dari ViewModel
    val isSecret by viewModel.isSecret.collectAsStateWithLifecycle()


    val snackbarHostState = remember { SnackbarHostState() }

    // State untuk URI gambar, dikelola di UI level
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        // TODO: Anda perlu logika untuk meng-upload URI ini ke Firebase Storage
        // dan menyimpan URL-nya di dalam dokumen catatan Anda.
    }

    // Launcher untuk meminta izin kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Camera permission granted. Feature under development.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    // Mendengarkan event dari ViewModel (misal: "Catatan Disimpan" atau "Error")
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.NoteSaved -> {
                    // Jika event NoteSaved diterima, panggil onBack untuk kembali
                    onBack()
                }
            }
        }
    }

    AddEditNoteScreen(
        title = title,
        content = content,
        isBookmarked = isBookmarked, // Teruskan status bookmark
        isSecret = isSecret, // Teruskan status secret
        imageUri = imageUri,
        onTitleChange = { viewModel.title.value = it },
        onContentChange = { viewModel.content.value = it },
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSaveClick = viewModel::onSaveNoteClick,
        onCameraClick = {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onGalleryClick = {
            imagePickerLauncher.launch("image/*")
        },
        // Tambahkan fungsi toggle bookmark ke ViewModel
        onToggleBookmark = viewModel::toggleBookmarkStatus,
        // Tambahkan fungsi toggle secret ke ViewModel
        onToggleSecret = viewModel::toggleSecretStatus
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    title: String,
    content: String,
    isBookmarked: Boolean, // Tambahkan parameter untuk status bookmark
    isSecret: Boolean, // Tambahkan parameter untuk status secret
    imageUri: Uri?,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaveClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onToggleBookmark: () -> Unit, // Tambahkan callback untuk toggle bookmark
    onToggleSecret: () -> Unit // Tambahkan callback untuk toggle secret
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (title.isEmpty()) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // --- ICON BOOKMARK ---
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else LocalContentColor.current
                        )
                    }
                    // --- ICON SECRET ---
                    IconButton(onClick = onToggleSecret) {
                        Icon(
                            imageVector = if (isSecret) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = if (isSecret) "Make Public" else "Make Secret",
                            tint = if (isSecret) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }

                    // Tombol save (tetap di sini)
                    IconButton(onClick = onSaveClick) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note")
                    }
                },
                modifier = Modifier.shadow(2.dp) // Shadow yang lebih halus
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = "Open Camera")
                    }
                    IconButton(onClick = onGalleryClick) {
                        Icon(Icons.Outlined.Image, contentDescription = "Open Gallery")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Text field untuk Judul
            CustomTextField(
                value = title,
                onValueChange = onTitleChange,
                hint = "Title",
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Menampilkan gambar jika ada
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Text field untuk Konten
            CustomTextField(
                value = content,
                onValueChange = onContentChange,
                hint = "Start writing your note here...",
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                minLines = 15 // Beri ruang lebih banyak untuk menulis
            )
        }
    }
}

// Composable terpisah untuk TextField agar lebih bersih
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    textStyle: TextStyle = LocalTextStyle.current
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, style = textStyle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = modifier,
        minLines = minLines,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AddEditNoteScreenPreview() {
    SealnoteTheme {
        AddEditNoteScreen(
            title = "My Awesome Note",
            content = "This is the content of the note.",
            isBookmarked = true, // Contoh untuk preview
            isSecret = false, // Contoh untuk preview
            imageUri = null,
            onTitleChange = {},
            onContentChange = {},
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onSaveClick = {},
            onCameraClick = {},
            onGalleryClick = {},
            onToggleBookmark = {},
            onToggleSecret = {}
        )
    }
}