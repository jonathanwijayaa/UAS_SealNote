// path: app/src/main/java/com/example/sealnote/viewmodel/SecretNotesViewModel.kt

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
class SecretNotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    // 1. State untuk menampung query pencarian dari UI
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 2. State untuk menampung opsi pengurutan dari UI
    private val _sortOption = MutableStateFlow(SortOption.BY_DATE_DESC) // Default sort
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // 3. StateFlow utama yang sekarang "pintar"
    // Ia akan menggabungkan data asli dengan state search dan sort
    val secretNotes: StateFlow<List<Notes>> =
        combine(
            repository.getSecretNotes(), // Sumber data utama: catatan rahasia
            _searchQuery,
            _sortOption
        ) { allSecretNotes, query, sort ->
            // Pertama, filter berdasarkan query pencarian
            val filteredNotes = if (query.isBlank()) {
                allSecretNotes
            } else {
                allSecretNotes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true)
                }
            }
            // Kedua, urutkan hasil catatan yang sudah difilter
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

    // Event flow untuk mengirim pesan ke UI (misalnya untuk Toast/Snackbar)
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()


    // 4. Fungsi publik yang akan dipanggil oleh UI untuk mengubah state
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOptionChange(newSortOption: SortOption) {
        _sortOption.value = newSortOption
    }

    // 5. Fungsi aksi untuk NoteCard (opsional, tapi penting untuk fungsionalitas penuh)
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

    // Fungsi ini akan berguna untuk membatalkan status rahasia dari dalam halaman ini
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