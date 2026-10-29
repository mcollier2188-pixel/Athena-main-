package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.data.MultimediaProjectEntity
import com.example.data.MultimediaProjectRepository
import com.example.data.WorkspaceDatabase
import com.example.data.WorkspaceItem
import com.example.data.firestore.FirestoreNote
import com.example.data.firestore.FirestoreWorkspaceRepository
import com.example.gateway.GoogleDriveSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WorkspaceViewModel(
    application: Application,
    private val firestoreRepository: FirestoreWorkspaceRepository? = null,
    private val currentUserId: String? = null
) : AndroidViewModel(application) {
    private val dao = WorkspaceDatabase.getDatabase(application).workspaceDao()
    private val projectRepo = ProjectRepository(WorkspaceDatabase.getDatabase(application).projectDao())
    private val multimediaProjectRepo = MultimediaProjectRepository(WorkspaceDatabase.getDatabase(application).multimediaProjectDao())

    val projects: StateFlow<List<ProjectEntity>> = projectRepo.allActiveProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjectsWithArchived: StateFlow<List<ProjectEntity>> = projectRepo.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val multimediaProjects: StateFlow<List<MultimediaProjectEntity>> = multimediaProjectRepo.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMultimediaProjects: StateFlow<List<MultimediaProjectEntity>> = multimediaProjectRepo.favoriteProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createMultimediaProject(
        title: String,
        description: String = "",
        mediaType: String = "VIDEO",
        filePath: String,
        thumbnailPath: String? = null,
        fileSizeBytes: Long = 0L,
        durationSeconds: Float = 0.0f,
        mimeType: String = "video/mp4",
        resolution: String? = null,
        aspectRatio: String = "16:9",
        tags: String = ""
    ) {
        viewModelScope.launch {
            val project = MultimediaProjectEntity(
                title = title,
                description = description,
                mediaType = mediaType,
                filePath = filePath,
                thumbnailPath = thumbnailPath,
                fileSizeBytes = fileSizeBytes,
                durationSeconds = durationSeconds,
                mimeType = mimeType,
                resolution = resolution,
                aspectRatio = aspectRatio,
                tags = tags
            )
            multimediaProjectRepo.saveProject(project)
        }
    }

    fun updateMultimediaProject(project: MultimediaProjectEntity) {
        viewModelScope.launch {
            multimediaProjectRepo.updateProject(project.copy(modifiedAt = System.currentTimeMillis()))
        }
    }

    fun deleteMultimediaProject(project: MultimediaProjectEntity) {
        viewModelScope.launch {
            multimediaProjectRepo.deleteProject(project)
        }
    }

    fun deleteMultimediaProjectById(id: String) {
        viewModelScope.launch {
            multimediaProjectRepo.deleteProjectById(id)
        }
    }

    fun toggleMultimediaProjectFavorite(project: MultimediaProjectEntity) {
        viewModelScope.launch {
            multimediaProjectRepo.updateProject(project.copy(isFavorite = !project.isFavorite, modifiedAt = System.currentTimeMillis()))
        }
    }

    fun createProject(title: String, description: String = "", colorTagHex: String = "#00F0FF") {
        viewModelScope.launch {
            projectRepo.createProject(title = title, description = description, colorTagHex = colorTagHex)
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepo.updateProject(project)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepo.deleteProject(project)
        }
    }

    fun deleteProjectById(id: String) {
        viewModelScope.launch {
            projectRepo.deleteProjectById(id)
        }
    }

    val currentFolderId = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")
    val isTrashView = MutableStateFlow(false)

    // Selection mode state
    val isSelectionMode = MutableStateFlow(false)
    val selectedItemIds = MutableStateFlow<Set<String>>(emptySet())

    val allFolders: StateFlow<List<WorkspaceItem>> = dao.getAllLive()
        .combine(MutableStateFlow(Unit)) { items, _ ->
            items.filter { it.kind == "folder" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allItems: StateFlow<List<WorkspaceItem>> = dao.getAllLive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentItems: StateFlow<List<WorkspaceItem>> = combine(
        currentFolderId,
        searchQuery,
        isTrashView
    ) { folderId, query, isTrash ->
        Triple(folderId, query, isTrash)
    }.flatMapLatest { (folderId, query, isTrash) ->
        when {
            isTrash -> dao.getTrashItems()
            query.isNotBlank() -> dao.searchItems(query.trim())
            else -> dao.getItemsInFolder(folderId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Real-time synchronization with Firestore when authenticated
        if (currentUserId != null && firestoreRepository != null) {
            viewModelScope.launch {
                try {
                    firestoreRepository.observeNotes(currentUserId).collect { remoteNotes ->
                        remoteNotes.forEach { remote ->
                            val local = dao.getItemById(remote.id)
                            if (local == null || local.modifiedTimestamp < System.currentTimeMillis() - 1000) {
                                dao.insertItem(
                                    WorkspaceItem(
                                        id = remote.id,
                                        parentId = remote.parentId,
                                        kind = remote.kind,
                                        name = remote.name,
                                        text = remote.text,
                                        tags = remote.tags,
                                        isPinned = remote.isPinned,
                                        modifiedTimestamp = System.currentTimeMillis(),
                                        size = remote.text.length.toLong()
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Handled gracefully offline
                }
            }
        }
    }

    fun navigateToFolder(folderId: String?) {
        currentFolderId.value = folderId
        searchQuery.value = ""
        isTrashView.value = false
        clearSelection()
    }

    fun toggleTrashView() {
        isTrashView.value = !isTrashView.value
        clearSelection()
    }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun toggleSelectionMode() {
        isSelectionMode.value = !isSelectionMode.value
        if (!isSelectionMode.value) {
            clearSelection()
        }
    }

    fun toggleSelectItem(id: String) {
        val current = selectedItemIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        selectedItemIds.value = current
    }

    fun clearSelection() {
        selectedItemIds.value = emptySet()
    }

    fun createNote(name: String = "Untitled note", initialText: String = "", tags: String = ""): String {
        val newId = UUID.randomUUID().toString()
        val note = WorkspaceItem(
            id = newId,
            parentId = currentFolderId.value,
            kind = "note",
            name = name,
            text = initialText,
            tags = tags,
            modifiedTimestamp = System.currentTimeMillis(),
            size = initialText.length.toLong()
        )
        viewModelScope.launch {
            dao.insertItem(note)
            if (currentUserId != null && firestoreRepository != null) {
                try {
                    firestoreRepository.saveNote(
                        currentUserId,
                        FirestoreNote(
                            id = newId,
                            userId = currentUserId,
                            name = name,
                            text = initialText,
                            tags = tags,
                            kind = "note",
                            parentId = currentFolderId.value
                        )
                    )
                } catch (e: Exception) {
                    // Local saved, Firestore sync will retry or error handled
                }
            }
            GoogleDriveSyncManager.onNoteCreatedOrUpdated(getApplication(), viewModelScope)
        }
        return newId
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        val newId = UUID.randomUUID().toString()
        val folder = WorkspaceItem(
            id = newId,
            parentId = currentFolderId.value,
            kind = "folder",
            name = name.trim(),
            modifiedTimestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            dao.insertItem(folder)
            if (currentUserId != null && firestoreRepository != null) {
                try {
                    firestoreRepository.saveNote(
                        currentUserId,
                        FirestoreNote(
                            id = newId,
                            userId = currentUserId,
                            name = name.trim(),
                            kind = "folder",
                            parentId = currentFolderId.value
                        )
                    )
                } catch (e: Exception) {
                    // Local saved
                }
            }
            GoogleDriveSyncManager.onNoteCreatedOrUpdated(getApplication(), viewModelScope)
        }
    }

    fun updateItem(item: WorkspaceItem) {
        viewModelScope.launch {
            dao.updateItem(item.copy(modifiedTimestamp = System.currentTimeMillis(), size = item.text.length.toLong()))
            if (currentUserId != null && firestoreRepository != null) {
                try {
                    firestoreRepository.saveNote(
                        currentUserId,
                        FirestoreNote(
                            id = item.id,
                            userId = currentUserId,
                            name = item.name,
                            text = item.text,
                            tags = item.tags,
                            kind = item.kind,
                            parentId = item.parentId,
                            isPinned = item.isPinned
                        )
                    )
                } catch (e: Exception) {
                    // Handled
                }
            }
            GoogleDriveSyncManager.onNoteCreatedOrUpdated(getApplication(), viewModelScope)
        }
    }

    fun togglePin(item: WorkspaceItem) {
        val updated = item.copy(isPinned = !item.isPinned)
        viewModelScope.launch {
            dao.updateItem(updated)
            if (currentUserId != null && firestoreRepository != null) {
                try {
                    firestoreRepository.saveNote(
                        currentUserId,
                        FirestoreNote(
                            id = updated.id,
                            userId = currentUserId,
                            name = updated.name,
                            text = updated.text,
                            tags = updated.tags,
                            kind = updated.kind,
                            parentId = updated.parentId,
                            isPinned = updated.isPinned
                        )
                    )
                } catch (e: Exception) {
                    // Handled
                }
            }
        }
    }

    fun moveToTrash(id: String) {
        viewModelScope.launch {
            dao.moveToTrash(id)
        }
    }

    fun restoreItem(id: String) {
        viewModelScope.launch {
            dao.restoreItem(id)
        }
    }

    fun deletePermanently(id: String) {
        viewModelScope.launch {
            dao.deletePermanently(id)
            if (currentUserId != null && firestoreRepository != null) {
                try {
                    firestoreRepository.deleteNote(currentUserId, id)
                } catch (e: Exception) {
                    // Handled
                }
            }
        }
    }

    fun deleteSelectedPermanently() {
        val ids = selectedItemIds.value.toList()
        viewModelScope.launch {
            dao.deletePermanently(ids)
            if (currentUserId != null && firestoreRepository != null) {
                ids.forEach { id ->
                    try {
                        firestoreRepository.deleteNote(currentUserId, id)
                    } catch (e: Exception) {
                        // Handled
                    }
                }
            }
            clearSelection()
        }
    }

    fun moveSelectedToTrash() {
        val ids = selectedItemIds.value.toList()
        viewModelScope.launch {
            ids.forEach { dao.moveToTrash(it) }
            clearSelection()
        }
    }

    fun moveSelectedToFolder(targetFolderId: String?) {
        val ids = selectedItemIds.value.toList()
        viewModelScope.launch {
            ids.forEach { id ->
                val item = dao.getItemById(id)
                if (item != null) {
                    val updated = item.copy(parentId = targetFolderId)
                    dao.updateItem(updated)
                    if (currentUserId != null && firestoreRepository != null) {
                        try {
                            firestoreRepository.saveNote(
                                currentUserId,
                                FirestoreNote(
                                    id = updated.id,
                                    userId = currentUserId,
                                    name = updated.name,
                                    text = updated.text,
                                    tags = updated.tags,
                                    kind = updated.kind,
                                    parentId = updated.parentId,
                                    isPinned = updated.isPinned
                                )
                            )
                        } catch (e: Exception) {
                            // Handled
                        }
                    }
                }
            }
            clearSelection()
        }
    }

    fun togglePinSelected() {
        val ids = selectedItemIds.value.toList()
        viewModelScope.launch {
            ids.forEach { id ->
                val item = dao.getItemById(id)
                if (item != null) {
                    val updated = item.copy(isPinned = !item.isPinned)
                    dao.updateItem(updated)
                    if (currentUserId != null && firestoreRepository != null) {
                        try {
                            firestoreRepository.saveNote(
                                currentUserId,
                                FirestoreNote(
                                    id = updated.id,
                                    userId = currentUserId,
                                    name = updated.name,
                                    text = updated.text,
                                    tags = updated.tags,
                                    kind = updated.kind,
                                    parentId = updated.parentId,
                                    isPinned = updated.isPinned
                                )
                            )
                        } catch (e: Exception) {
                            // Handled
                        }
                    }
                }
            }
            clearSelection()
        }
    }

    // =========================================================
    // Echo Projects Integration
    // =========================================================

    /**
     * Creates or locates the 'Echo Projects' folder and adds a project note into it.
     */
    fun addEchoProjectToWorkspace(
        name: String,
        content: String,
        tags: String = "echo, saga, echo-verse",
        onComplete: (String, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val all = dao.getAll()
            val echoFolder = all.find { it.kind == "folder" && (it.name.contains("Echo Projects", ignoreCase = true) || it.name.contains("Echo Cinema", ignoreCase = true)) }
            val folderId = if (echoFolder != null) {
                echoFolder.id
            } else {
                val newFolderId = UUID.randomUUID().toString()
                dao.insertItem(
                    WorkspaceItem(
                        id = newFolderId,
                        parentId = null,
                        kind = "folder",
                        name = "🎬 Echo Projects",
                        tags = "echo, visual, scripts",
                        modifiedTimestamp = System.currentTimeMillis()
                    )
                )
                newFolderId
            }

            val noteId = UUID.randomUUID().toString()
            val note = WorkspaceItem(
                id = noteId,
                parentId = folderId,
                kind = "note",
                name = name,
                text = content,
                tags = tags,
                modifiedTimestamp = System.currentTimeMillis(),
                size = content.length.toLong()
            )
            dao.insertItem(note)
            onComplete(folderId, noteId)
        }
    }

    /**
     * Imports all primary Echo production projects into Room Workspace.
     */
    fun importAllEchoProjects(onComplete: (Int, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val all = dao.getAll()
            val echoFolder = all.find { it.kind == "folder" && (it.name.contains("Echo Projects", ignoreCase = true) || it.name.contains("Echo Cinema", ignoreCase = true)) }
            val folderId = if (echoFolder != null) {
                echoFolder.id
            } else {
                val newFolderId = UUID.randomUUID().toString()
                dao.insertItem(
                    WorkspaceItem(
                        id = newFolderId,
                        parentId = null,
                        kind = "folder",
                        name = "🎬 Echo Projects",
                        tags = "echo, visual, scripts",
                        modifiedTimestamp = System.currentTimeMillis()
                    )
                )
                newFolderId
            }

            val cinemaItems = listOf(
                Pair(
                    "The Conductor — Frequency Saga (Seasons 1–6)",
                    """# The Conductor — Frequency Saga (85.2 Hz)
6 Seasons • 30 Episodes + Christmas Special + Feature Film Finale

## Premise:
Everything in existence hums at a frequency. Most people never hear it. Mara, Ruth, Iris, and Elara do.
Not as a sound — as a message. As a connection. The Conductor has tended this song longer than memory.
Now the song is changing, and it needs new voices to carry it forward.

## Trilogy One: The Awakening
• S1: The Call (The First Echo, Answered, The Silence That Speaks, Crossing the Line, More Than One)
• S2: The Network (The Weaving, Gaps and Silences, The Anchor, Echoes of Doubt, Harmony in the Noise)
• S3: The Harmony (Many Voices One Song, The Threshold, What Hides in the Spaces, The Conductor, The Silence Speaks)

## Trilogy Two: The Growing Light
• S4: The Growing Light (The Other Side of the Door, The Unfinished Song, Those Who Stayed, The Bridge Between, A New Frequency)
• S5: The Broken Path (The Echo That Turned, The Worlds That Said No, Doubt Has a Voice, When Voices Drift Apart, The Quiet Rebuild)
• S6: The Great Song (The Frequency of Purpose, The Silence's Origin, The Song That Was Forgotten, The Invitation, The Great Song)
"""
                ),
                Pair(
                    "Solo Runs — Conductor, Mara, Elara, Ruth, Silencer",
                    """# Echo Verse: Character Solo Runs (Complete 4-Issue Sets)

### 1. The Conductor (🎺)
• Issue 1: The First Note — Hearing what no one else could.
• Issue 2: The Long Night — Centuries of silence, carrying the song alone.
• Issue 3: The Visitor — The only other person who understood.
• Issue 4: The Handover — Passing the song to Mara.

### 2. Mara (🌊)
• Issue 1: The Quiet Child — Growing up tuned differently.
• Issue 2: The Crack — First encounter with The Silencer.
• Issue 3: The Conductor — Connecting without words.
• Issue 4: The Choice — Singing aloud at Portsmouth harbour.

### 3. Elara (🛡️)
• Issue 1: The Guardian — Standing between song and silence.
• Issue 2: The Mistake — The weight she carries.
• Issue 3: The Offer — Refusing the Silencer's terms.
• Issue 4: The Bridge — The vital watcher on the perimeter.

### 4. Ruth (🤍)
• Issue 1: The Best Friend — The ordinary extraordinary anchor.
• Issue 2: The Barrier — Staying when the frequency went dark.
• Issue 3: The Amplifier — Belief as the strongest signal.
• Issue 4: The Baton — Realizing she was leading all along.

### 5. The Silencer (⚫)
• Issue 1: The First Silence — A light that deemed the world too loud.
• Issue 2: The Argument — Why harmony and silence parted.
• Issue 3: The Cost — The hidden price of isolation.
• Issue 4: The Hand He Pulled Back — The refused invitation.
"""
                ),
                Pair(
                    "The Songwriter's Notebook Trilogy",
                    """# The Songwriter's Notebook — Trilogy
A quiet story of human connection, buskers, handwritten melodies, and ripples that return.

### Book 1: The Complete Story
Eli leaves a worn leather notebook on a park bench.
1. Eli — I Lost My Voice But I Found The Words
2. Clara — The Music Doesn't Leave When The Voice Is Gone
3. Jamie — You Don't Have To Carry It All At Once
4. Maya — Home Isn't Always Where You Arrived
5. Peter — The Quiet Carpenter's Rhythm

### Book 2: The Notebook Travels On
The circle widens across new towns and hands.
• Lila, Ray, Zara, Jesse, and Eli's return.

### Book 3: The Song Goes On
The ripple comes full circle to Portsmouth.
• Frank, Poppy, Arthur, Liv, and Maya's community choir.
"""
                ),
                Pair(
                    "Echo Series Bible & Lore",
                    """# Echo — Series Bible & Production Guide
Carrier Node: 85.200 Hz (Master 2x Harmonic of 42.6 Hz Bedrock Waveguide)

## Fundamental Lore:
1. Every voice has a resonance frequency.
2. Connection doesn't require uniform pitch — it requires harmonic cooperation.
3. The Silence is not an enemy to be destroyed, but a lonely voice invited to re-join the chord.
4. Solent Envelope: 11.74-second tidal cycle breathing beneath every scene.
"""
                )
            )

            var addedCount = 0
            cinemaItems.forEach { (title, body) ->
                val noteId = UUID.randomUUID().toString()
                dao.insertItem(
                    WorkspaceItem(
                        id = noteId,
                        parentId = folderId,
                        kind = "note",
                        name = title,
                        text = body.trimIndent(),
                        tags = "echo, saga, script",
                        modifiedTimestamp = System.currentTimeMillis(),
                        size = body.length.toLong()
                    )
                )
                addedCount++
            }
            onComplete(addedCount, folderId)
        }
    }

    /**
     * Uploads and stores an imported file directly into the 'Echo Projects' Room workspace folder.
     */
    fun uploadFileToEchoWorkspace(
        fileName: String,
        mimeType: String,
        fileSizeBytes: Long,
        textContent: String,
        uriString: String? = null,
        onComplete: (String, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val all = dao.getAll()
            val echoFolder = all.find { it.kind == "folder" && (it.name.contains("Echo Projects", ignoreCase = true) || it.name.contains("Echo Cinema", ignoreCase = true)) }
            val folderId = if (echoFolder != null) {
                echoFolder.id
            } else {
                val newFolderId = UUID.randomUUID().toString()
                dao.insertItem(
                    WorkspaceItem(
                        id = newFolderId,
                        parentId = null,
                        kind = "folder",
                        name = "🎬 Echo Projects",
                        tags = "echo, visual, media, uploads",
                        modifiedTimestamp = System.currentTimeMillis()
                    )
                )
                newFolderId
            }

            val fileKind = when {
                mimeType.startsWith("text/") || fileName.endsWith(".txt") || fileName.endsWith(".md") -> "note"
                else -> "file"
            }

            val tagCategory = when {
                mimeType.startsWith("video/") -> "echo, video, media"
                mimeType.startsWith("image/") -> "echo, image, visual"
                mimeType.contains("pdf") -> "echo, pdf, document"
                mimeType.contains("word") || fileName.endsWith(".docx") || fileName.endsWith(".doc") -> "echo, word, script"
                else -> "echo, document, upload"
            }

            val newId = UUID.randomUUID().toString()
            val item = WorkspaceItem(
                id = newId,
                parentId = folderId,
                kind = fileKind,
                name = fileName,
                text = textContent,
                fileType = mimeType,
                tags = tagCategory,
                modifiedTimestamp = System.currentTimeMillis(),
                size = if (fileSizeBytes > 0) fileSizeBytes else textContent.length.toLong()
            )
            dao.insertItem(item)
            onComplete(folderId, newId)
        }
    }
}

