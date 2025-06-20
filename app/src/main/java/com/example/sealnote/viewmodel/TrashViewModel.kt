// path: app/src/main/java/com/example/sealnote/viewmodel/TrashViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.NotesRepository
import com.example.sealnote.model.Notes
import com.example.sealnote.util.SortOption // Pastikan import ini ada
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    // State untuk Search & Sort
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.BY_DATE_DESC) // Default urutkan berdasarkan tanggal
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // StateFlow utama yang sekarang cerdas: mengambil, memfilter, dan mengurutkan.
    val trashedNotes: StateFlow<List<Notes>> =
        combine(
            repository.getTrashedNotes(), // Sumber utama tetap catatan yang di-trash
            _searchQuery,
            _sortOption
        ) { trashedNotes, query, sort ->
            val filteredNotes = if (query.isBlank()) {
                trashedNotes
            } else {
                trashedNotes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true)
                }
            }
            when (sort) {
                SortOption.BY_DATE_DESC -> filteredNotes.sortedByDescending { it.updatedAt }
                SortOption.BY_DATE_ASC -> filteredNotes.sortedBy { it.updatedAt }
                SortOption.BY_TITLE_ASC -> filteredNotes.sortedBy { it.title }
                SortOption.BY_TITLE_DESC -> filteredNotes.sortedByDescending { it.title }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Fungsi untuk mengubah state dari UI
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOptionChange(newSortOption: SortOption) {
        _sortOption.value = newSortOption
    }

    // Fungsi aksi ini sudah benar dan dipertahankan
    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            try {
                repository.restoreNoteFromTrash(noteId)
                _eventFlow.emit("Catatan berhasil dipulihkan.")
            } catch (e: Exception) {
                _eventFlow.emit("Gagal memulihkan catatan.")
            }
        }
    }

    fun deletePermanently(noteId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotePermanently(noteId)
                _eventFlow.emit("Catatan dihapus permanen.")
            } catch (e: Exception) {
                _eventFlow.emit("Gagal menghapus catatan.")
            }
        }
    }
}