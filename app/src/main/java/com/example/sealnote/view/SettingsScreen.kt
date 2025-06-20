// path: app/src/main/java/com/example/sealnote/view/SettingsScreen.kt

package com.example.sealnote.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sealnote.data.ThemeOption
import com.example.sealnote.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * Composable "Cerdas" (Smart Composable) untuk halaman Pengaturan.
 * Menghubungkan ViewModel dengan UI dan menangani navigasi.
 */
@Composable
fun SettingsRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Mengambil state yang dibutuhkan dari ViewModel
    val themeOption by viewModel.themeOption.collectAsStateWithLifecycle()

    // Mengambil state rute saat ini dari NavController
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Menghubungkan state dan event ke UI
    SettingsScreen(
        currentRoute = currentRoute,
        currentThemeOption = themeOption,
        onThemeChange = viewModel::onThemeOptionSelected,
        onNavigate = { route ->
            // Mencegah navigasi ke halaman yang sama
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

/**
 * Composable "Bodoh" (Dumb Composable) yang hanya menampilkan UI Pengaturan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentRoute: String?,
    currentThemeOption: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateToCalculator: () -> Unit,
) {
    // State untuk mengelola drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Daftar menu yang konsisten dengan halaman lain
    val menuItems = listOf(
        "homepage" to ("All Notes" to Icons.Default.Home),
        "bookmarks" to ("Bookmarks" to Icons.Default.BookmarkBorder),
        "secretNotes" to ("Secret Notes" to Icons.Default.Lock),
        "trash" to ("Trash" to Icons.Default.Delete),
        "settings" to ("Settings" to Icons.Default.Settings),
        "profile" to ("Profile" to Icons.Default.Person)
    )

    // Membungkus Scaffold dengan ModalNavigationDrawer
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
                        selected = currentRoute == route, // Akan menyorot "Settings"
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
                    title = { Text("Settings") },
                    // --- PERUBAHAN UTAMA DI SINI ---
                    // Mengganti tombol kembali dengan ikon menu untuk membuka drawer
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ThemeSettingItem(
                    currentThemeOption = currentThemeOption,
                    onOptionSelected = onThemeChange
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

/**
 * Composable spesifik untuk menampilkan dan mengubah pengaturan tema.
 * (Tidak ada perubahan di sini)
 */
@Composable
fun ThemeSettingItem(
    currentThemeOption: ThemeOption,
    onOptionSelected: (ThemeOption) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val currentThemeName = when (currentThemeOption) {
        ThemeOption.LIGHT -> "Light"
        ThemeOption.DARK -> "Dark"
        ThemeOption.SYSTEM -> "System default"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isMenuExpanded = true }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = currentThemeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Theme")
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                ThemeOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.replaceFirstChar { it.titlecase() }) },
                        onClick = {
                            onOptionSelected(option)
                            isMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}