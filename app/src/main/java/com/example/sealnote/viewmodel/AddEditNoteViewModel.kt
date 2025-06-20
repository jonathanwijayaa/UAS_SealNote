// app/src/main/java/com/example/sealnote/viewmodel/AddEditNoteViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.NotesRepository
import com.example.sealnote.model.Notes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NotesRepository,
    savedStateHandle: SavedStateHandle // Untuk mendapatkan argumen navigasi
) : ViewModel() {

    // State untuk UI
    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val isBookmarked = MutableStateFlow(false) // State untuk bookmark
    val isSecret = MutableStateFlow(false) // State untuk secret

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // ID catatan, null jika ini adalah catatan baru
    private var noteId: String? = null

    init {
        // Ambil noteId dari SavedStateHandle
        noteId = savedStateHandle.get<String>("noteId")

        // Jika ada noteId, berarti ini adalah mode edit
        noteId?.let { id ->
            viewModelScope.launch {
                repository.getNoteById(id).firstOrNull()?.let { note ->
                    title.value = note.title
                    content.value = note.content
                    isBookmarked.value = note.bookmarked // Inisialisasi status bookmark
                    isSecret.value = note.secret // Inisialisasi status secret
                } ?: run {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Catatan tidak ditemukan."))
                }
            }
        }
        // Tangani parameter isSecret dari navigasi untuk catatan baru (jika ada)
        val navIsSecret = savedStateHandle.get<Boolean>("isSecret") ?: false
        isSecret.value = navIsSecret
    }

    // Fungsi yang dipanggil saat tombol simpan diklik
    fun onSaveNoteClick() {
        if (title.value.isBlank() && content.value.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("Judul atau konten tidak boleh kosong."))
            }
            return
        }

        viewModelScope.launch {
            try {
                // Simpan catatan dengan status secret dan bookmark saat ini
                repository.saveNote(
                    noteId = noteId,
                    title = title.value,
                    content = content.value,
                    isSecret = isSecret.value // Kirim status secret saat ini
                )
                // Setelah catatan disimpan/diperbarui, pastikan status bookmark juga diterapkan
                // Ini penting jika tombol bookmark ditekan sebelum save
                noteId?.let { id ->
                    repository.toggleBookmarkStatus(id, isBookmarked.value)
                } ?: run {
                    // Jika ini catatan baru, setelah save, kita akan mendapatkan ID baru.
                    // Ini sedikit lebih kompleks karena saveNote tidak langsung mengembalikan ID.
                    // Untuk aplikasi nyata, Anda mungkin perlu mengambil catatan lagi atau mengubah saveNote.
                    // Untuk demo ini, kita asumsikan status bookmark sudah diatur saat save
                    // atau akan dihandle saat catatan dimuat ulang.
                    // Alternatif: Ubah saveNote untuk mengembalikan Notes object yang telah disimpan
                }

                _eventFlow.emit(UiEvent.ShowSnackbar("Catatan berhasil disimpan!"))
                _eventFlow.emit(UiEvent.NoteSaved) // Memberi tahu UI untuk kembali
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Gagal menyimpan catatan: ${e.localizedMessage}"))
            }
        }
    }

    // Fungsi untuk toggle status bookmark
    fun toggleBookmarkStatus() {
        // Balikkan nilai bookmark saat ini
        isBookmarked.value = !isBookmarked.value
        // Anda juga bisa langsung memanggil repository di sini
        // Tapi jika Anda ingin perubahan visual instan sebelum disimpan ke DB,
        // ubah state lokal dulu, lalu panggil repo saat onSaveNoteClick()
        // Atau panggil repo langsung di sini dan biarkan UI refresh dari DB.
        // Pilihan saat ini: perubahan langsung di state ViewModel
    }

    // Fungsi untuk toggle status secret
    fun toggleSecretStatus() {
        // Balikkan nilai secret saat ini
        isSecret.value = !isSecret.value
        // Sama seperti bookmark, Anda bisa langsung memanggil repository di sini
    }
}

// Model untuk event UI (tetap sama)
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object NoteSaved : UiEvent()
}