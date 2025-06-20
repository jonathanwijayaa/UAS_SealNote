package com.example.sealnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    private val auth = FirebaseAuth.getInstance()

    // Check if a user is already logged in
    val isLoggedIn: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = auth.currentUser != null
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginResult.value = LoginResult.Error("Email and Password must be filled!")
            return
        }

        _loginResult.value = LoginResult.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _loginResult.value = LoginResult.Success
            }
            .addOnFailureListener { e ->
                _loginResult.value = LoginResult.Error("Login failed: ${e.message}")
            }
    }

    fun logout() {
        auth.signOut()
    }

    sealed class LoginResult {
        object Loading: LoginResult()
        object Success : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}