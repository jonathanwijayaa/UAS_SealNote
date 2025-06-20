// path: app/src/main/java/com/example/sealnote/view/TrashScreen.kt

package com.example.sealnote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sealnote.data.ThemeOption // <-- TAMBAHKAN IMPORT INI
import com.example.sealnote.model.Notes
import com.example.sealnote.ui.theme.SealnoteTheme
import com.example.sealnote.util.SortOption
import com.example.sealnote.viewmodel.TrashViewModel
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun TrashRoute(
    navController: NavHostController,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val trashedNotes by viewModel.trashedNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    TrashScreen(
        currentRoute = currentRoute,
        trashedNotes = trashedNotes,
        searchQuery = searchQuery,
        sortOption = sortOption,
        snackbarHostState = snackbarHostState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortOptionChange = viewModel::onSortOptionChange,
        onRestoreNote = viewModel::restoreNote,
        onPermanentlyDeleteNote = viewModel::deletePermanently,
        onNavigate = { route ->
            if (currentRoute != route) {
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        onNavigateToCalculator = {
            navController.navigate("stealthCalculator") {
                popUpTo("homepage") { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    currentRoute: String?,
    trashedNotes: List<Notes>,
    searchQuery: String,
    sortOption: SortOption,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (SortOption) -> Unit,
    onRestoreNote: (String) -> Unit,
    onPermanentlyDeleteNote: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateToCalculator: () -> Unit
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
                Text("SealNote Menu", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp))
                menuItems.forEach { (route, details) ->
                    val (label, icon) = details
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = { scope.launch { drawerState.close() }; onNavigate(route) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Calculate, "Back to Calculator") },
                    label = { Text("Back to Calculator") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToCalculator() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (isSearchActive) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text("Search in trash...", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else {
                            Text("Trash")
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = { isSearchActive = false; onSearchQueryChange("") }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        } else {
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
                                Icon(Icons.Default.Search, "Search Trash")
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
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                Text(
                    text = "Items in trash are automatically deleted after 30 days.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trashedNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if(searchQuery.isNotEmpty()) "No results found" else "Trash is empty.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trashedNotes, key = { it.id }) { note ->
                            TrashNoteCard(
                                note = note,
                                onRestore = { onRestoreNote(note.id) },
                                onPermanentlyDelete = { onPermanentlyDeleteNote(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrashNoteCard(
    note: Notes,
    onRestore: () -> Unit,
    onPermanentlyDelete: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box {
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, "More Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Restore", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { onRestore(); isMenuExpanded = false },
                            leadingIcon = { Icon(Icons.Outlined.Restore, "Restore", tint = MaterialTheme.colorScheme.onSurface) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete forever", color = MaterialTheme.colorScheme.error) },
                            onClick = { onPermanentlyDelete(); isMenuExpanded = false },
                            leadingIcon = { Icon(Icons.Outlined.DeleteForever, "Delete Forever", tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
            Text(
                text = note.content,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = "Deleted: ${note.updatedAt.toRelativeTimeString()}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun Date?.toRelativeTimeString(): String {
    if (this == null) return "a moment ago"
    val now = System.currentTimeMillis()
    val diff = now - this.time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    if (seconds < 60) return "just now"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    if (minutes < 60) return "$minutes min ago"
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    if (hours < 24) return "$hours hours ago"
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return "$days days ago"
}

// --- FUNGSI PREVIEW YANG DIPERBAIKI ---
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TrashScreenPreview() {
    // Data sampel dibuat di sini
    val sampleNotes = List(5) { index ->
        Notes(
            id = "note_$index",
            title = "Judul Catatan Dihapus $index",
            content = "Ini adalah cuplikan singkat dari konten catatan yang telah dihapus...",
            updatedAt = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(index.toLong()))
        )
    }

    // Panggil `remember` di level atas dari composable
    val snackbarHostState = remember { SnackbarHostState() }

    // Panggil SealnoteTheme dengan parameter `themeOption` yang baru
    SealnoteTheme(themeOption = ThemeOption.DARK) {
        TrashScreen(
            currentRoute = "trash",
            trashedNotes = sampleNotes,
            searchQuery = "",
            sortOption = SortOption.BY_DATE_DESC,
            // Teruskan state yang sudah di-"remember"
            snackbarHostState = snackbarHostState,
            onSearchQueryChange = {},
            onSortOptionChange = {},
            onRestoreNote = {},
            onPermanentlyDeleteNote = {},
            onNavigate = {},
            onNavigateToCalculator = {}
        )
    }
}