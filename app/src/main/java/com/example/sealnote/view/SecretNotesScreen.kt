package com.example.sealnote.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sealnote.model.Notes
import com.example.sealnote.util.SortOption
import com.example.sealnote.viewmodel.SecretNotesViewModel
import kotlinx.coroutines.launch


@Composable
fun SecretNotesRoute(
    navController: NavHostController,
    viewModel: SecretNotesViewModel = hiltViewModel()
) {
    val secretNotes by viewModel.secretNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Ambil fungsi dari ViewModel yang akan digunakan di UI
    val onDeleteNote: (String) -> Unit = viewModel::trashNote
    // Sudah benar: onToggleSecret hanya perlu noteId
    val onToggleSecret: (String,Boolean) -> Unit = viewModel::toggleSecretStatus
    // Sudah benar: onToggleBookmark hanya perlu noteId
    val onToggleBookmark: (String,Boolean) -> Unit = viewModel::toggleBookmarkStatus

    SecretNotesScreen(
        currentRoute = currentRoute,
        notes = secretNotes,
        searchQuery = searchQuery,
        sortOption = sortOption,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortOptionChange = viewModel::onSortOptionChange,
        onNoteClick = { noteId ->
            navController.navigate("add_edit_note_screen/$noteId")
        },
        onNavigateToAddNote = {
            navController.navigate("add_edit_note_screen/null?isSecret=true")
        },
        onNavigate = { route ->
            // Cegah navigasi ke halaman yang sama
            if (currentRoute != route) {
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        onNavigateToCalculator = {
            navController.navigate("stealthCalculator") {
                popUpTo("homepage") { inclusive = true }
            }
        },
        // Teruskan fungsi aksi ke UI
        onDeleteClick = onDeleteNote,
        // PERBAIKAN DI SINI: onToggleSecretClick sekarang hanya perlu String
        onToggleSecretClick = onToggleSecret,
        // PERBAIKAN DI SINI: onBookmarkClick sekarang hanya perlu String
        onBookmarkClick = onToggleBookmark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretNotesScreen(
    currentRoute: String?,
    notes: List<Notes>,
    searchQuery: String,
    sortOption: SortOption,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (SortOption) -> Unit,
    onNoteClick: (String) -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateToCalculator: () -> Unit,
    onDeleteClick: (String) -> Unit,
    // PERBAIKAN DI SINI: Ubah parameter onToggleSecretClick menjadi hanya String
    onToggleSecretClick: (String, Boolean) -> Unit,
    // PERBAIKAN DI SINI: Ubah parameter onBookmarkClick menjadi hanya String
    onBookmarkClick: (String, Boolean) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val menuItems = listOf(
        "homepage" to ("All Notes" to Icons.Default.Home),
        "bookmarks" to ("Bookmarks" to Icons.Default.BookmarkBorder),
        "secretNotes" to ("Secret Notes" to Icons.Default.Lock), // Rute ini akan terpilih
        "trash" to ("Trash" to Icons.Default.Delete),
        "settings" to ("Settings" to Icons.Default.Settings),
        "profile" to ("Profile" to Icons.Default.Person)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("SealNote Menu", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp))
                menuItems.forEach { (route, details) ->
                    val (label, icon) = details
                    // Logika selected yang disederhanakan dan diperbaiki
                    val selected = currentRoute == route
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = selected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigate(route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Calculate, "Back to Calculator") },
                    label = { Text("Back to Calculator") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCalculator()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (isSearchActive) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text("Search secret notes...", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else {
                            Text("Secret Notes")
                        }
                    },
                    // --- PERUBAHAN UTAMA DI SINI ---
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = { isSearchActive = false; onSearchQueryChange("") }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        } else {
                            // Mengganti ArrowBack dengan ikon Menu untuk membuka drawer
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Open Menu")
                            }
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, "Clear Search")
                                }
                            }
                        } else {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, "Search Secret Notes")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onNavigateToAddNote) {
                    Icon(Icons.Default.Add, contentDescription = "Add Secret Note")
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        TextButton(onClick = { isSortMenuExpanded = true }) {
                            Text(sortOption.displayName)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort Options")
                        }
                        DropdownMenu(expanded = isSortMenuExpanded, onDismissRequest = { isSortMenuExpanded = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(text = { Text(option.displayName) }, onClick = {
                                    onSortOptionChange(option)
                                    isSortMenuExpanded = false
                                })
                            }
                        }
                    }
                }
                HorizontalDivider()
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if(searchQuery.isNotEmpty()) "No results found" else "No secret notes yet. Tap '+' to add one!")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onEditClick = { onNoteClick(note.id) },
                                onDeleteClick = { onDeleteClick(note.id) },
                                // PERBAIKAN DI SINI: Panggil onToggleSecretClick tanpa parameter boolean
                                onToggleSecretClick = { onToggleSecretClick(note.id, note.secret) },
                                // PERBAIKAN DI SINI: Panggil onBookmarkClick tanpa parameter boolean
                                onBookmarkClick = { onBookmarkClick(note.id, note.bookmarked) }
                            )
                        }
                    }
                }
            }
        }
    }
}