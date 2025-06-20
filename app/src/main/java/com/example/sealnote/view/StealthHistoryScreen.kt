package com.example.sealnote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.lifecycle.viewmodel.compose.viewModel // Import untuk ViewModel
import androidx.navigation.NavHostController // Import NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import kotlinx.coroutines.launch // Untuk CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

import com.example.sealnote.viewmodel.CalculatorHistoryViewModel // Import History ViewModel

// Definisi Warna dari XML
val HistoryScreenBackground = Color(0xFF152332)
val HistoryItemExpressionColor = Color(0xFF8090A6)
val HistoryItemResultColor = Color.White
val HistoryItemDividerColor = Color(0xFF2A2F3A)

// Data class untuk setiap entri riwayat (pastikan ini di tingkat atas file atau di folder model)
data class CalculationHistoryEntry(
    val id: String, // Untuk key jika menggunakan LazyColumn
    val expression: String,
    val result: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StealthHistoryScreen(
    navController: NavHostController, // Tambahkan NavHostController
    historyViewModel: CalculatorHistoryViewModel = viewModel() // Inject History ViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text("Mode Kalkulator", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                NavigationDrawerItem(
                    label = { Text("Kalkulator Standar") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("stealthCalculator") {
                            popUpTo("stealthHistory") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Kalkulator Ilmiah") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("stealthScientific") {
                            popUpTo("stealthHistory") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Riwayat Kalkulasi") },
                    selected = true, // Ini adalah halaman saat ini
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Tidak perlu navigasi, karena sudah di halaman ini
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Bersihkan Riwayat") },
                    selected = false,
                    onClick = {
                        historyViewModel.clearHistory()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Riwayat Kalkulasi",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HistoryScreenBackground, // Sesuaikan dengan background riwayat
                        titleContentColor = HistoryItemResultColor // Warna teks judul
                    )
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Penting untuk apply padding dari Scaffold
                color = HistoryScreenBackground
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Gunakan LazyColumn untuk riwayat yang efisien
                    // Karena `offset` dan `height` yang hardcoded dari XML lama,
                    // mungkin perlu penyesuaian jika ingin responsif.
                    // Untuk menjaga kesesuaian visual dengan preview, saya akan biarkan offset-nya.
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 0.dp) // Sesuaikan offset setelah TopAppBar
                            .fillMaxSize() // Gunakan fillMaxSize dan biarkan scroll mengisi sisanya
                            .background(HistoryScreenBackground)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (historyViewModel.historyEntries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(), // Untuk membuat teks di tengah jika kosong
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada riwayat kalkulasi.",
                                    color = HistoryItemExpressionColor,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            historyViewModel.historyEntries.forEach { entry ->
                                HistoryItemView(entry = entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemView(entry: CalculationHistoryEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HistoryScreenBackground)
            .padding(vertical = 15.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp - 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = entry.expression,
                    color = HistoryItemExpressionColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )
                Text(
                    text = entry.result,
                    color = HistoryItemResultColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        HorizontalDivider(
            color = HistoryItemDividerColor,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, widthDp = 416, heightDp = 891)
@Composable
fun CalculationHistoryScreenPreview() {
    val sampleHistory = listOf(
        CalculationHistoryEntry("id1", "25.000 - 5.000", "5.000"),
        CalculationHistoryEntry("id2", "50.000 - 15.000", "15.000"),
        CalculationHistoryEntry("id3", "100.000 - 32.500", "32.500"),
        CalculationHistoryEntry("id4", "75.000 - 10.000", "10.000"),
        CalculationHistoryEntry("id5", "150.000 - 45.000", "45.000"),
        CalculationHistoryEntry("id6", "30.000 - 7.500", "7.500"),
    )
    MaterialTheme {
        // Untuk preview, inisialisasi ViewModel dengan data dummy jika diperlukan
        val dummyHistoryViewModel: CalculatorHistoryViewModel = viewModel()
        dummyHistoryViewModel.historyEntries.addAll(sampleHistory) // Tambahkan data dummy
        StealthHistoryScreen(navController = rememberNavController(), historyViewModel = dummyHistoryViewModel)
    }
}