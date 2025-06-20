// path: app/src/main/java/com/example/sealnote/viewmodel/SignUpViewModel.kt

package com.example.sealnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sealnote.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest // <-- IMPORT INI

class SignUpViewModel : ViewModel() {

    private val _signUpResult = MutableLiveData<SignUpResult>()
    val signUpResult: LiveData<SignUpResult> get() = _signUpResult

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- FUNGSI BARU UNTUK HASHING ---
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        // Konversi byte array ke dalam format hex string
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun signUp(fullName: String, email: String, password: String, confirmPassword: String) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _signUpResult.value = SignUpResult.Error("Please fill in all fields.")
            return
        }
        if (password != confirmPassword) {
            _signUpResult.value = SignUpResult.Error("Passwords do not match.")
            return
        }

        _signUpResult.value = SignUpResult.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {

                    // --- PERUBAHAN DI SINI ---
                    // Buat hash dari password sebelum disimpan
                    val hashedPassword = hashPassword(password)

                    val user = User(
                        id = firebaseUser.uid,
                        fullName = fullName,
                        email = email,
                        // Simpan hash, BUKAN password asli
                        passwordHash = hashedPassword
                    )
                    // -------------------------

                    // Save user details to Firestore
                    db.collection("users").document(firebaseUser.uid)
                        .set(user)
                        .addOnSuccessListener {
                            _signUpResult.value = SignUpResult.Success
                        }
                        .addOnFailureListener { e ->
                            _signUpResult.value = SignUpResult.Error("Failed to save user data: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                _signUpResult.value = SignUpResult.Error("Sign up failed: ${e.message}")
            }
    }

    sealed class SignUpResult {
        object Loading : SignUpResult()
        object Success : SignUpResult()
        data class Error(val message: String) : SignUpResult()
    }
}