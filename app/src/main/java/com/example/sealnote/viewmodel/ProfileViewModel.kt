// path: app/src/main/java/com/example/sealnote/viewmodel/ProfileViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sealnote.data.NotesRepository
import com.example.sealnote.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isEditing: Boolean = false,
    val editedName: String = "",
    val editedPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val triggerBiometric: Boolean = false,// State untuk memicu prompt biometrik
    val isSignedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: NotesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in.") }
                return@launch
            }

            try {
                val user = repository.getUserProfile(userId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        editedName = user?.fullName ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // --- Manajemen State UI ---
    fun onNameChange(newName: String) { _uiState.update { it.copy(editedName = newName) } }
    fun onPasswordChange(newPass: String) { _uiState.update { it.copy(editedPassword = newPass) } }

    fun onEditToggle() {
        val currentIsEditing = _uiState.value.isEditing
        if (currentIsEditing) {
            // Saat "Cancel" diklik, reset semua perubahan
            _uiState.update {
                it.copy(
                    isEditing = false,
                    isPasswordVisible = false, // Sembunyikan lagi passwordnya
                    editedName = it.user?.fullName ?: "",
                    editedPassword = ""
                )
            }
        } else {
            // Masuk mode edit
            _uiState.update { it.copy(isEditing = true) }
        }
    }

    // --- Logika Biometrik ---
    fun onTogglePasswordVisibilityRequest() {
        if (_uiState.value.isPasswordVisible) {
            // Jika sudah terlihat, langsung sembunyikan tanpa biometrik
            _uiState.update { it.copy(isPasswordVisible = false) }
        } else {
            // Jika tersembunyi, minta UI untuk memicu prompt biometrik
            _uiState.update { it.copy(triggerBiometric = true) }
        }
    }

    fun onBiometricAuthResult(success: Boolean) {
        _uiState.update { it.copy(triggerBiometric = false) } // Reset pemicu
        if (success) {
            _uiState.update { it.copy(isPasswordVisible = true) } // Tampilkan password jika berhasil
        }
    }

    // --- Logika Penyimpanan ---
    fun onSaveChanges() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val newName = _uiState.value.editedName
            val newPassword = _uiState.value.editedPassword

            if (newName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Name cannot be empty.") }
                return@launch
            }

            var somethingChanged = false

            // 1. Update nama jika berubah
            if (newName != _uiState.value.user?.fullName) {
                try {
                    repository.updateUserProfile(user.uid, newName)
                    somethingChanged = true
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = "Failed to update name.") }
                    return@launch
                }
            }

            // 2. Update password jika diisi
            if (newPassword.isNotBlank()) {
                if (newPassword.length < 6) {
                    _uiState.update { it.copy(errorMessage = "New password must be at least 6 characters.") }
                    return@launch
                }
                try {
                    user.updatePassword(newPassword).await()
                    // Update hash di Firestore (contoh)
                    repository.updateUserPasswordHash(user.uid, hashString(newPassword))
                    somethingChanged = true
                } catch (e: Exception) {
                    val errorMsg = when (e) {
                        is FirebaseAuthRecentLoginRequiredException -> "This is a sensitive action and requires recent authentication. Please sign out and sign in again before changing your password."
                        else -> "Failed to update password: ${e.localizedMessage}"
                    }
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                    return@launch
                }
            }

            // Jika ada perubahan, tampilkan pesan sukses
            if (somethingChanged) {
                _uiState.update { it.copy(infoMessage = "Profile updated successfully!") }
            }

            // Keluar dari mode edit dan muat ulang data
            _uiState.update { it.copy(isEditing = false, isPasswordVisible = false) }
            loadUserProfile()
        }
    }

    // --- FUNGSI SIGN OUT ---
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut() // Melakukan sign out dari Firebase
                _uiState.update { it.copy(isSignedOut = true, infoMessage = "Successfully signed out.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to sign out: ${e.localizedMessage}") }
            }
        }
    }

    // Fungsi untuk mereset isSignedOut setelah navigasi dilakukan
    fun onSignOutNavigated() {
        _uiState.update { it.copy(isSignedOut = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun hashString(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}
