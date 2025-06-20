package com.example.sealnote.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNotesViewModel : ViewModel() {
    // StateFlow untuk Title
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    // StateFlow untuk Notes
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    // StateFlow untuk Tanggal Dibuat (contoh)
    private val _createdDate = MutableStateFlow(getCurrentDate())
    val createdDate: StateFlow<String> = _createdDate.asStateFlow()

    // StateFlow untuk Tanggal Terakhir Diubah (contoh)
    private val _lastChangedDate = MutableStateFlow(getCurrentDateTime())
    val lastChangedDate: StateFlow<String> = _lastChangedDate.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        _lastChangedDate.value = getCurrentDateTime() // Update last changed on edit
    }

    fun onNotesChange(newNotes: String) {
        _notes.value = newNotes
        _lastChangedDate.value = getCurrentDateTime() // Update last changed on edit
    }

    fun saveNote() {
        val currentTitle = _title.value
        val currentNotes = _notes.value
        // TODO: Implementasikan logika penyimpanan ke database atau repository
        println("Saving Note: Title='$currentTitle', Notes='$currentNotes'")
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return "Created: ${sdf.format(Date())}"
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh.mma", Locale.getDefault())
        return "Last Changed : ${sdf.format(Date())}"
    }
}
