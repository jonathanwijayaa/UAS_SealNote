// path: app/src/main/java/com/example/sealnote/viewmodel/BookmarksViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.NotesRepository
import com.example.sealnote.model.Notes
import com.example.sealnote.util.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    // --- STATE UNTUK SEARCH DAN SORT ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.BY_DATE_DESC) // Default sort
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    // ------------------------------------

    // --- StateFlow untuk catatan yang sudah di-bookmark, difilter, dan diurutkan ---
    val bookmarkedNotes: StateFlow<List<Notes>> =
        combine(
            repository.getBookmarkedNotes(), // Sumber utama: catatan yang di-bookmark
            _searchQuery,
            _sortOption
        ) { allBookmarkedNotes, query, sort ->
            // 1. Filter berdasarkan query pencarian
            val filteredNotes = if (query.isBlank()) {
                allBookmarkedNotes
            } else {
                allBookmarkedNotes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true)
                }
            }
            // 2. Urutkan hasil filter
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

    // --- Fungsi untuk mengubah state ---
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOptionChange(newSortOption: SortOption) {
        _sortOption.value = newSortOption
    }

    // --- Fungsi Aksi pada Note (opsional, tapi bagus untuk konsistensi) ---
    fun trashNote(noteId: String) {
        viewModelScope.launch {
            try {
                repository.trashNote(noteId)
                _eventFlow.emit("Catatan dipindahkan ke sampah.")
            } catch (e: Exception) {
                _eventFlow.emit("Gagal memindahkan catatan.")
            }
        }
    }

    fun toggleSecretStatus(noteId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleSecretStatus(noteId, !currentStatus)
                val message = if (!currentStatus) "Catatan ditambahkan ke rahasia" else "Catatan dihapus dari rahasia"
                _eventFlow.emit(message)
            } catch (e: Exception) {
                _eventFlow.emit("Gagal mengubah status catatan.")
            }
        }
    }

    // --- FUNGSI BOOKMARK YANG DIPERBAIKI ---
    fun toggleBookmarkStatus(noteId: String, currentBookmarkStatus: Boolean) {
        viewModelScope.launch {
            try {
                // Gunakan currentBookmarkStatus yang diterima dari UI
                val newBookmarkStatus = !currentBookmarkStatus
                repository.toggleBookmarkStatus(noteId, newBookmarkStatus)

                val message = if (newBookmarkStatus) "Catatan ditambahkan ke bookmark." else "Catatan dihapus dari bookmark."
                _eventFlow.emit(message)

            } catch (e: Exception) {
                _eventFlow.emit("Gagal mengubah status bookmark catatan: ${e.localizedMessage}")
            }
        }
    }
}