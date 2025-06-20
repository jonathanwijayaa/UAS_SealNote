package com.example.sealnote.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName // <-- 1. TAMBAHKAN IMPORT INI
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notes(
    @DocumentId
    val id: String = "",

    // --- 2. PERBAIKI PEMETAAN FIELD DENGAN ANOTASI ---
    // Anotasi @PropertyName memberitahu Firestore untuk memetakan field "userID"
    // dari dokumen ke properti "userId" di kelas Kotlin ini.
    @get:PropertyName("userID") @set:PropertyName("userID")
    var userId: String = "",
    // ----------------------------------------------------

    val title: String = "",
    val content: String = "",
    val bookmarked: Boolean = false,
    val trashed: Boolean = false,
    val secret: Boolean = false,

    @ServerTimestamp
    val createdAt: Date? = null,

    @ServerTimestamp
    val updatedAt: Date? = null,

    val expireAt: Date? = null
)