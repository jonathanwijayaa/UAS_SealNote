package com.example.sealnote.model

// To hold user information
data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val passwordHash: String = ""
)