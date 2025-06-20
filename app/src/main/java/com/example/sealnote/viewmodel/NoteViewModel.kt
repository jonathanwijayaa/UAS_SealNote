package com.example.sealnote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sealnote.model.Notes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NoteViewModel : ViewModel() {

    private val _notes = MutableLiveData<List<Notes>>()
    val notes: LiveData<List<Notes>> get() = _notes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Fetch notes for the logged-in user
    fun fetchNotes() {
        val userId = getCurrentUserId()
        if (userId == null) {
            _error.value = "User not logged in."
            return
        }

        db.collection("users").document(userId).collection("notes")
            .orderBy("title", Query.Direction.ASCENDING) // Or by a timestamp
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Failed to fetch notes: ${e.message}"
                    return@addSnapshotListener
                }

                val noteList = snapshot?.documents?.mapNotNull {
                    it.toObject(Notes::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _notes.value = noteList
            }
    }

    // Add or Update a note
    fun saveNote(title: String, content: String, noteId: String? = null) {
        val userId = getCurrentUserId()
        if (userId == null) {
            _error.value = "Cannot save note. User not logged in."
            return
        }

        if (title.isEmpty() || content.isEmpty()) {
            _error.value = "Title and content cannot be empty."
            return
        }

        val note = Notes(
            userId = userId,
            title = title,
            content = content
        )

        val collection = db.collection("users").document(userId).collection("notes")

        if (noteId == null) {
            // Add new note
            collection.add(note).addOnFailureListener { e ->
                _error.value = "Failed to add note: ${e.message}"
            }
        } else {
            // Update existing note
            collection.document(noteId).set(note).addOnFailureListener { e ->
                _error.value = "Failed to update note: ${e.message}"
            }
        }
    }
}