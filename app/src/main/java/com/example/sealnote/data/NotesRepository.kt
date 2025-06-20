package com.example.sealnote.data

import com.example.sealnote.model.Notes
import com.example.sealnote.model.User // <-- PASTIKAN IMPORT INI ADA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun getUserId(): String? = auth.currentUser?.uid

    private fun getUserNotesCollection() = getUserId()?.let {
        firestore.collection("users").document(it).collection("notes")
    }

    // --- FUNGSI UNTUK NOTES (SUDAH ADA) ---
    // ... (semua fungsi getNotes, saveNote, trashNote, dll. tetap di sini)
    // ... (saya tidak akan menuliskannya lagi untuk keringkasan)
    fun getAllNotes(): Flow<List<Notes>> {
        val collection = getUserNotesCollection() ?: return flowOf(emptyList())
        return collection
            .whereEqualTo("trashed", false)
            .whereEqualTo("secret", false) // <-- TAMBAHKAN INI
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Notes>() } }
    }

    fun getSecretNotes(): Flow<List<Notes>> {
        val collection = getUserNotesCollection() ?: return flowOf(emptyList())
        return collection
            .whereEqualTo("secret", true)
            .whereEqualTo("trashed", false)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Notes>() } }
    }

    fun getTrashedNotes(): Flow<List<Notes>> {
        val collection = getUserNotesCollection() ?: return flowOf(emptyList())
        return collection.whereEqualTo("trashed", true)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Notes>() } }
    }

    fun getBookmarkedNotes(): Flow<List<Notes>> {
        val collection = getUserNotesCollection() ?: return flowOf(emptyList())
        return collection
            .whereEqualTo("bookmarked", true)
            .whereEqualTo("secret", false)
            .whereEqualTo("trashed", false)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Notes>() } }
    }

    fun getNoteById(noteId: String): Flow<Notes?> {
        val collection = getUserNotesCollection() ?: return flowOf(null)
        return collection.document(noteId)
            .snapshots()
            .map { it.toObject<Notes>() }
    }

    suspend fun saveNote(noteId: String?, title: String, content: String, isSecret: Boolean) {
        val userId = getUserId() ?: throw Exception("User is not logged in.")
        val collection = firestore.collection("users").document(userId).collection("notes")
        val currentTime = Date()

        if (noteId == null) {
            val newNoteDocument = collection.document()
            val newNote = Notes(
                id = newNoteDocument.id,
                userId = userId,
                title = title,
                content = content,
                createdAt = currentTime,
                updatedAt = currentTime,
                bookmarked = false,
                secret = isSecret,
                trashed = false
            )
            newNoteDocument.set(newNote).await()
        } else {
            collection.document(noteId).update(
                mapOf(
                    "title" to title,
                    "content" to content,
                    "secret" to isSecret,
                    "updatedAt" to currentTime
                )
            ).await()
        }
    }

    suspend fun toggleSecretStatus(noteId: String, isSecret: Boolean) {
        getUserNotesCollection()?.document(noteId)?.update(
            mapOf(
                "secret" to isSecret,
                "updatedAt" to Date()
            )
        )?.await()
    }

    suspend fun toggleBookmarkStatus(noteId: String, isBookmarked: Boolean) {
        getUserNotesCollection()?.document(noteId)?.update(
            mapOf(
                "bookmarked" to isBookmarked,
                "updatedAt" to Date()
            )
        )?.await()
    }

    suspend fun trashNote(noteId: String) {
        val collection = getUserNotesCollection() ?: return

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val expireTime = calendar.time

        collection.document(noteId).update(
            mapOf(
                "trashed" to true,
                "expireAt" to expireTime,
                "updatedAt" to Date()
            )
        ).await()
    }

    suspend fun restoreNoteFromTrash(noteId: String) {
        val collection = getUserNotesCollection() ?: return
        collection.document(noteId).update(
            mapOf(
                "trashed" to false,
                "expireAt" to null,
                "updatedAt" to Date()
            )
        ).await()
    }

    suspend fun deleteNotePermanently(noteId: String) {
        getUserNotesCollection()?.document(noteId)?.delete()?.await()
    }

    // --- FUNGSI-FUNGSI BARU UNTUK PROFIL PENGGUNA ---

    /**
     * Mengambil data profil pengguna dari Firestore berdasarkan userId.
     */
    suspend fun getUserProfile(userId: String): User? {
        return try {
            firestore.collection("users").document(userId)
                .get()
                .await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            // Sebaiknya log error di sini
            null
        }
    }

    /**
     * Memperbarui nama lengkap pengguna di Firestore.
     */
    suspend fun updateUserProfile(userId: String, newName: String) {
        firestore.collection("users").document(userId)
            .update("fullName", newName)
            .await()
    }

    /**
     * Memperbarui hash password di Firestore.
     */
    suspend fun updateUserPasswordHash(userId: String, newHash: String) {
        firestore.collection("users").document(userId)
            .update("passwordHash", newHash)
            .await()
    }
}