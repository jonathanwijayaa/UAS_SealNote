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
import com.example.sealnote.viewmodel.HomepageViewModel
import kotlinx.coroutines.launch

// Pastikan import NoteCard dari filenya sendiri
// import com.example.sealnote.view.NoteCard // Contoh import jika NoteCard di file terpisah

@Composable
fun HomepageRoute(
    navController: NavHostController,
    viewModel: HomepageViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Referensi fungsi dari ViewModel, yang sekarang hanya menerima String noteId
    val onDeleteNote: (String) -> Unit = viewModel::trashNote
    val onToggleSecret: (String, Boolean) -> Unit = viewModel::toggleSecretStatus
    val onToggleBookmark: (String, Boolean) -> Unit = viewModel::toggleBookmarkStatus

    HomepageScreen(
        currentRoute = currentRoute,
        notes = notes,
        searchQuery = searchQuery,
        sortOption = sortOption,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortOptionChange = viewModel::onSortOptionChange,
        onNoteClick = { noteId ->
            navController.navigate("add_edit_note_screen/$noteId")
        },
        onDeleteNoteClick = onDeleteNote,
        // HANYA meneruskan referensi fungsi. VM akan menangani logikanya.
        onToggleSecretClick = onToggleSecret,
        // Navigasi untuk tombol FAB ditambahkan kembali
        onNavigateToAddNote = {
            navController.navigate("add_edit_note_screen/null")
        },
        onNavigate = { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
        },
        onNavigateToCalculator = {
            navController.navigate("stealthCalculator") {
                popUpTo("homepage") { inclusive = true }
            }
        },
        // HANYA meneruskan referensi fungsi. VM akan menangani logikanya.
        onBookmarkClick = onToggleBookmark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageScreen(
    currentRoute: String?,
    notes: List<Notes>,
    searchQuery: String,
    sortOption: SortOption,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (SortOption) -> Unit,
    onNoteClick: (String) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    onNavigateToAddNote: () -> Unit, // Parameter ditambahkan kembali
    // PERBAIKAN DI SINI: Ubah signature agar hanya menerima String noteId
    onToggleSecretClick: (String, Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateToCalculator: () -> Unit,
    // PERBAIKAN DI SINI: Ubah signature agar hanya menerima String noteId
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
        "secretNotes" to ("Secret Notes" to Icons.Default.Lock),
        "trash" to ("Trash" to Icons.Default.Delete),
        "settings" to ("Settings" to Icons.Default.Settings),
        "profile" to ("Profile" to Icons.Default.Person)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "SealNote Menu",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                )

                menuItems.forEach { (route, details) ->
                    val (label, icon) = details
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != route) {
                                onNavigate(route)
                            }
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
                                            Text("Search notes...", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        } else {
                            Text("All Notes")
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                isSearchActive = false
                                onSearchQueryChange("")
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu")
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
                                Icon(Icons.Default.Search, "Search Notes")
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
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        TextButton(onClick = { isSortMenuExpanded = true }) {
                            Text(sortOption.displayName)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort Options")
                        }
                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        onSortOptionChange(option)
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()

                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if(searchQuery.isNotEmpty()) "No results found" else "No notes yet. Tap '+' to add one!")
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
                                onDeleteClick = { onDeleteNoteClick(note.id) },
                                // PERBAIKAN UTAMA DI SINI: Hanya meneruskan note.id
                                onToggleSecretClick = { onToggleSecretClick(note.id, note.secret) },
                                onBookmarkClick = { onBookmarkClick(note.id, note.bookmarked) }
                            )
                        }
                    }
                }
            }
        }
    }
}