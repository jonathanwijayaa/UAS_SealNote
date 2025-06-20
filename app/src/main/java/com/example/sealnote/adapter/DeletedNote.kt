package com.example.sealnote.model

// Data class untuk catatan yang dihapus
data class DeletedNote(
    val id: String,
    val title: String,
    val contentSnippet: String,
    val deletionDate: String // Harus berisi teks lengkap seperti "Deleted date : 22 Mei 2022"
)