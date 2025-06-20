// path: app/src/main/java/com/example/sealnote/viewmodel/AuthViewModel.kt

package com.example.sealnote.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Enum baru untuk merepresentasikan tipe autentikator yang diminta
enum class AuthenticatorType {
    FINGERPRINT,
    FACE
}

sealed class BiometricAuthState {
    object Idle : BiometricAuthState()
    object Success : BiometricAuthState()
    data class Error(val message: String) : BiometricAuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _authState = MutableStateFlow<BiometricAuthState>(BiometricAuthState.Idle)
    val authState = _authState.asStateFlow()

    fun canAuthenticate(context: Context, type: AuthenticatorType): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = when (type) {
            // Fingerprint harus selalu kuat (Kelas 3)
            AuthenticatorType.FINGERPRINT -> BIOMETRIC_STRONG
            // Untuk Face, kita izinkan kuat atau lemah agar lebih banyak perangkat didukung
            AuthenticatorType.FACE -> BIOMETRIC_STRONG or BIOMETRIC_WEAK
        }
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Fungsi ini sekarang menerima tipe yang diinginkan
    fun startAuthentication(activity: FragmentActivity, type: AuthenticatorType) {
        if (!canAuthenticate(activity, type)) {
            _authState.value = BiometricAuthState.Error("This biometric type is not available.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                _authState.value = BiometricAuthState.Error(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _authState.value = BiometricAuthState.Success
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        // Tentukan level authenticator berdasarkan pilihan pengguna
        val authenticators = when (type) {
            AuthenticatorType.FINGERPRINT -> BIOMETRIC_STRONG
            AuthenticatorType.FACE -> BIOMETRIC_STRONG or BIOMETRIC_WEAK
        }

        // Sesuaikan judul prompt
        val title = if (type == AuthenticatorType.FINGERPRINT) {
            "Fingerprint Authentication"
        } else {
            "Face Authentication"
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Unlock your secret notes")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(authenticators) // Gunakan authenticator yang sudah ditentukan
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun resetAuthState() {
        _authState.value = BiometricAuthState.Idle
    }
}