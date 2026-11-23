package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FirestoreWorkspaceRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        fun createOrNull(context: Context): FirestoreWorkspaceRepository? {
            val appContext = context.applicationContext
            if (FirebaseApp.getApps(appContext).isEmpty()) return null
            return try {
                val dbId = appContext.getString(R.string.firestore_database_id)
                FirestoreWorkspaceRepository(
                    db = FirebaseFirestore.getInstance(dbId),
                    auth = FirebaseAuth.getInstance()
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private fun requireUserId(userId: String?): String {
        val currentUid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated for Firestore operations")
        val targetUid = userId?.takeIf { it.isNotBlank() } ?: currentUid
        require(targetUid == currentUid) { "Cannot perform Firestore operations for another user UID" }
        return currentUid
    }

    // 1. Observe notes and folders in real-time
    fun observeNotes(userId: String): Flow<List<FirestoreNote>> {
        val uid = requireUserId(userId)
        return db.collection("users").document(uid).collection("notes")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreNote::class.java)?.copy(id = doc.id)
                }
            }
    }

    // 2. Fetch single note
    suspend fun getNote(userId: String, noteId: String): FirestoreNote? {
        val uid = requireUserId(userId)
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(FirestoreNote::class.java)?.copy(id = snapshot.id)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }

    // 3. Upsert note
    suspend fun saveNote(userId: String, note: FirestoreNote) {
        val uid = requireUserId(userId)
        require(note.id.isNotBlank()) { "Note ID must not be blank" }
        val safeName = note.name.trim().ifEmpty { "Untitled note" }.take(120)
        val docRef = db.collection("users").document(uid).collection("notes").document(note.id)
        try {
            val payload = mutableMapOf<String, Any?>(
                "id" to note.id,
                "userId" to uid,
                "name" to safeName,
                "text" to note.text.take(100000),
                "tags" to note.tags.take(200),
                "kind" to note.kind,
                "parentId" to (note.parentId?.take(100) ?: FieldValue.delete()),
                "isPinned" to note.isPinned,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (note.createdAt != null) {
                payload["createdAt"] = note.createdAt
            }

            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // 4. Delete note
    suspend fun deleteNote(userId: String, noteId: String) {
        val uid = requireUserId(userId)
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // 5. Save AI creation / Dola artifact
    suspend fun saveAiCreation(userId: String, creation: FirestoreAiCreation) {
        val uid = requireUserId(userId)
        require(creation.id.isNotBlank()) { "Creation ID must not be blank" }
        val safePrompt = creation.prompt.trim().ifEmpty { "AI Generation" }.take(2000)
        val safeModel = creation.modelUsed.trim().ifEmpty { "gemini-2.5-flash" }.take(80)
        val docRef = db.collection("users").document(uid).collection("generations").document(creation.id)
        try {
            val payload = mapOf(
                "id" to creation.id,
                "userId" to uid,
                "type" to creation.type,
                "prompt" to safePrompt,
                "resultData" to creation.resultData.take(500000),
                "modelUsed" to safeModel,
                "createdAt" to (creation.createdAt ?: FieldValue.serverTimestamp())
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // 6. Observe AI creations
    fun observeAiCreations(userId: String): Flow<List<FirestoreAiCreation>> {
        val uid = requireUserId(userId)
        return db.collection("users").document(uid).collection("generations")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreAiCreation::class.java)?.copy(id = doc.id)
                }
            }
    }

    // 7. Profile sync
    suspend fun saveProfile(userId: String, profile: FirestoreUserProfile) {
        val uid = requireUserId(userId)
        val docRef = db.collection("users").document(uid).collection("profile").document("current")
        try {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to profile.displayName.take(100),
                "email" to profile.email.take(100),
                "favoriteFrequency" to profile.favoriteFrequency,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
