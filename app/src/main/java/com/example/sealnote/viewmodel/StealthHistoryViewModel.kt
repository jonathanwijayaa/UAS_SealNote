package com.example.sealnote.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.sealnote.view.CalculationHistoryEntry

class CalculatorHistoryViewModel : ViewModel() {
    // Gunakan mutableStateListOf agar perubahan secara otomatis memicu recomposition
    val historyEntries = mutableStateListOf<CalculationHistoryEntry>()

    fun addHistoryEntry(expression: String, result: String) {
        // Tambahkan ke bagian atas daftar untuk menampilkan yang terbaru terlebih dahulu
        historyEntries.add(0, CalculationHistoryEntry(
            id = System.currentTimeMillis().toString(), // ID unik sederhana
            expression = expression,
            result = result
        ))
        // Opsional: Batasi ukuran riwayat, misal 100 entri
        if (historyEntries.size > 100) {
            historyEntries.removeLast()
        }
    }

    fun clearHistory() {
        historyEntries.clear()
    }
}