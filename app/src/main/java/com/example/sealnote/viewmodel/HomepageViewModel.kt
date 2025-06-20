// path: app/src/main/java/com/example/sealnote/viewmodel/HomepageViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.NotesRepository
import com.example.sealnote.model.Notes
import com.example.sealnote.util.SortOption
import com.google.firebase.auth.FirebaseAuth // <-- 1. IMPORT FIREBASE AUTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomepageViewModel @Inject constructor(
    private val repository: NotesRepository,
    // --- 2. TAMBAHKAN FIREBASE AUTH DI CONSTRUCTOR ---
    private val auth: FirebaseAuth
) : ViewModel() {

    // --- STATE UNTUK SEARCH DAN SORT ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.BY_DATE_DESC) // Default sort
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    // ------------------------------------

    val notes: StateFlow<List<Notes>> =
        combine(
            repository.getAllNotes(),
            _searchQuery,
            _sortOption
        ) { allNotes, query, sort ->
            val filteredNotes = if (query.isBlank()) {
                allNotes
            } else {
                allNotes.filter { note ->
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

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOptionChange(newSortOption: SortOption) {
        _sortOption.value = newSortOption
    }

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

    // --- 3. TAMBAHKAN FUNGSI LOGOUT BARU ---
    fun logout() {
        auth.signOut()
    }

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
    // ---------------------------------------
}