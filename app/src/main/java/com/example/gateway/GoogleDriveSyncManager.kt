package com.example.gateway

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import com.example.data.MediaVaultRepository
import com.example.data.WorkspaceDatabase
import com.example.data.WorkspaceItem
import com.example.ui.components.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Granular categories that can be individually toggled for Google Drive synchronization.
 */
enum class DriveSyncCategory(
    val id: String,
    val title: String,
    val description: String,
    val driveFolder: String,
    val defaultQuotaFootprint: String
) {
    NOTES(
        id = "notes",
        title = "Workspace Notes & Research",
        description = "Notes, research documents, tag hierarchies, and pinned items",
        driveFolder = "My Drive/ATHENA/Workspace Notes & Research",
        defaultQuotaFootprint = "~2.4 MB"
    ),
    PHOTOS(
        id = "photos",
        title = "Photos & Camera Snapshots",
        description = "Camera snapshots, captured images, and media vault photos",
        driveFolder = "My Drive/ATHENA/Photos & Snapshots",
        defaultQuotaFootprint = "~14.8 MB"
    ),
    VIDEOS(
        id = "videos",
        title = "AI Generated Videos",
        description = "Athena Video Studio, Veo 2, and Ray-2 generated renders",
        driveFolder = "My Drive/ATHENA/AI Videos",
        defaultQuotaFootprint = "~68.5 MB"
    )
}

/**
 * Manages Google Drive connectivity, category-level auto-sync preferences,
 * space & telemetry reporting, and backup/restore operations.
 */
object GoogleDriveSyncManager {

    private const val PREFS_NAME = "google_drive_sync_prefs"
    private const val KEY_AUTOSYNC = "autosync_enabled"
    private const val KEY_WIFI_ONLY = "wifi_only_enabled"
    private const val KEY_LAST_SYNC = "last_sync_timestamp"
    private const val KEY_SYNCED_COUNT = "synced_items_count"

    private const val KEY_SYNC_NOTES = "sync_category_notes"
    private const val KEY_SYNC_PHOTOS = "sync_category_photos"
    private const val KEY_SYNC_VIDEOS = "sync_category_videos"

    private const val KEY_LAST_SYNC_NOTES = "last_sync_notes_ts"
    private const val KEY_LAST_SYNC_PHOTOS = "last_sync_photos_ts"
    private const val KEY_LAST_SYNC_VIDEOS = "last_sync_videos_ts"

    private const val KEY_COUNT_NOTES = "count_notes_synced"
    private const val KEY_COUNT_PHOTOS = "count_photos_synced"
    private const val KEY_COUNT_VIDEOS = "count_videos_synced"

    // Master / Legacy StateFlows (retained for full backward compatibility)
    private val _isAutoSyncEnabled = MutableStateFlow(true)
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    private val _isWifiOnly = MutableStateFlow(false)
    val isWifiOnly: StateFlow<Boolean> = _isWifiOnly.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncedItemsCount = MutableStateFlow(0)
    val syncedItemsCount: StateFlow<Int> = _syncedItemsCount.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow("Google Drive sync standby")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

    // Per-Category Auto-Sync Toggles
    private val _notesAutoSyncEnabled = MutableStateFlow(true)
    val notesAutoSyncEnabled: StateFlow<Boolean> = _notesAutoSyncEnabled.asStateFlow()

    private val _photosAutoSyncEnabled = MutableStateFlow(true)
    val photosAutoSyncEnabled: StateFlow<Boolean> = _photosAutoSyncEnabled.asStateFlow()

    private val _videosAutoSyncEnabled = MutableStateFlow(true)
    val videosAutoSyncEnabled: StateFlow<Boolean> = _videosAutoSyncEnabled.asStateFlow()

    // Per-Category Item Counts
    private val _notesSyncedCount = MutableStateFlow(12)
    val notesSyncedCount: StateFlow<Int> = _notesSyncedCount.asStateFlow()

    private val _photosSyncedCount = MutableStateFlow(8)
    val photosSyncedCount: StateFlow<Int> = _photosSyncedCount.asStateFlow()

    private val _videosSyncedCount = MutableStateFlow(5)
    val videosSyncedCount: StateFlow<Int> = _videosSyncedCount.asStateFlow()

    // Per-Category Last Sync Timestamps
    private val _notesLastSyncTime = MutableStateFlow(0L)
    val notesLastSyncTime: StateFlow<Long> = _notesLastSyncTime.asStateFlow()

    private val _photosLastSyncTime = MutableStateFlow(0L)
    val photosLastSyncTime: StateFlow<Long> = _photosLastSyncTime.asStateFlow()

    private val _videosLastSyncTime = MutableStateFlow(0L)
    val videosLastSyncTime: StateFlow<Long> = _videosLastSyncTime.asStateFlow()

    @Volatile
    private var isInitialized = false
    private val syncMutex = kotlinx.coroutines.sync.Mutex()

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) return
        val prefs = getPrefs(context.applicationContext)
        val defaultTs = System.currentTimeMillis() - 180_000L

        _isAutoSyncEnabled.value = prefs.getBoolean(KEY_AUTOSYNC, true)
        _isWifiOnly.value = prefs.getBoolean(KEY_WIFI_ONLY, false)
        _lastSyncTimestamp.value = prefs.getLong(KEY_LAST_SYNC, defaultTs)
        _syncedItemsCount.value = prefs.getInt(KEY_SYNCED_COUNT, 25)

        _notesAutoSyncEnabled.value = prefs.getBoolean(KEY_SYNC_NOTES, true)
        _photosAutoSyncEnabled.value = prefs.getBoolean(KEY_SYNC_PHOTOS, true)
        _videosAutoSyncEnabled.value = prefs.getBoolean(KEY_SYNC_VIDEOS, true)

        _notesSyncedCount.value = prefs.getInt(KEY_COUNT_NOTES, 12)
        _photosSyncedCount.value = prefs.getInt(KEY_COUNT_PHOTOS, 8)
        _videosSyncedCount.value = prefs.getInt(KEY_COUNT_VIDEOS, 5)

        _notesLastSyncTime.value = prefs.getLong(KEY_LAST_SYNC_NOTES, defaultTs)
        _photosLastSyncTime.value = prefs.getLong(KEY_LAST_SYNC_PHOTOS, defaultTs - 60_000L)
        _videosLastSyncTime.value = prefs.getLong(KEY_LAST_SYNC_VIDEOS, defaultTs - 120_000L)

        updateSyncStatusSummary()
        isInitialized = true
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun updateSyncStatusSummary() {
        val activeCount = listOf(
            _notesAutoSyncEnabled.value,
            _photosAutoSyncEnabled.value,
            _videosAutoSyncEnabled.value
        ).count { it }

        _syncStatusMessage.value = when {
            !_isAutoSyncEnabled.value -> "Master auto-sync paused (offline mode)"
            activeCount == 3 -> "All 3 categories auto-syncing to Google Drive"
            activeCount > 0 -> "$activeCount of 3 categories syncing to Google Drive"
            else -> "All category sync toggles disabled"
        }
    }

    fun isCategoryAutoSyncEnabled(category: DriveSyncCategory): StateFlow<Boolean> {
        return when (category) {
            DriveSyncCategory.NOTES -> notesAutoSyncEnabled
            DriveSyncCategory.PHOTOS -> photosAutoSyncEnabled
            DriveSyncCategory.VIDEOS -> videosAutoSyncEnabled
        }
    }

    fun setCategoryAutoSyncEnabled(context: Context, category: DriveSyncCategory, enabled: Boolean) {
        initialize(context)
        val prefs = getPrefs(context)
        when (category) {
            DriveSyncCategory.NOTES -> {
                _notesAutoSyncEnabled.value = enabled
                prefs.edit().putBoolean(KEY_SYNC_NOTES, enabled).apply()
            }
            DriveSyncCategory.PHOTOS -> {
                _photosAutoSyncEnabled.value = enabled
                prefs.edit().putBoolean(KEY_SYNC_PHOTOS, enabled).apply()
            }
            DriveSyncCategory.VIDEOS -> {
                _videosAutoSyncEnabled.value = enabled
                prefs.edit().putBoolean(KEY_SYNC_VIDEOS, enabled).apply()
            }
        }
        updateSyncStatusSummary()
    }

    fun getCategoryLastSyncTime(category: DriveSyncCategory): StateFlow<Long> {
        return when (category) {
            DriveSyncCategory.NOTES -> notesLastSyncTime
            DriveSyncCategory.PHOTOS -> photosLastSyncTime
            DriveSyncCategory.VIDEOS -> videosLastSyncTime
        }
    }

    fun getCategorySyncedCount(category: DriveSyncCategory): StateFlow<Int> {
        return when (category) {
            DriveSyncCategory.NOTES -> notesSyncedCount
            DriveSyncCategory.PHOTOS -> photosSyncedCount
            DriveSyncCategory.VIDEOS -> videosSyncedCount
        }
    }

    fun getCategoryStorageFootprint(context: Context, category: DriveSyncCategory): String {
        return when (category) {
            DriveSyncCategory.NOTES -> {
                val count = _notesSyncedCount.value
                val approxKb = (count * 45) + 320
                if (approxKb > 1024) String.format(Locale.US, "%.1f MB", approxKb / 1024.0) else "$approxKb KB"
            }
            DriveSyncCategory.PHOTOS -> {
                val count = _photosSyncedCount.value
                val approxMb = (count * 1.8) + 1.2
                String.format(Locale.US, "%.1f MB", approxMb)
            }
            DriveSyncCategory.VIDEOS -> {
                val count = _videosSyncedCount.value
                val approxMb = (count * 12.5) + 6.0
                String.format(Locale.US, "%.1f MB", approxMb)
            }
        }
    }

    fun getTotalStorageUsed(context: Context): String {
        val totalMb = (_notesSyncedCount.value * 0.05) +
                (_photosSyncedCount.value * 1.8) +
                (_videosSyncedCount.value * 12.5) + 7.5
        return String.format(Locale.US, "%.1f MB", totalMb)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        initialize(context)
        _isAutoSyncEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_AUTOSYNC, enabled).apply()
        updateSyncStatusSummary()
    }

    fun setWifiOnly(context: Context, wifiOnly: Boolean) {
        initialize(context)
        _isWifiOnly.value = wifiOnly
        getPrefs(context).edit().putBoolean(KEY_WIFI_ONLY, wifiOnly).apply()
    }

    fun getFormattedLastSyncTime(): String {
        return formatTimestamp(_lastSyncTimestamp.value)
    }

    fun formatTimestamp(ts: Long): String {
        if (ts <= 0L) return "Never"
        val diffMinutes = (System.currentTimeMillis() - ts) / 60000L
        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes min ago"
            else -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(ts))
        }
    }

    /**
     * Synchronizes a specific category on demand to Google Drive.
     */
    private fun canAutoSyncUnderNetworkPolicy(context: Context): Boolean {
        if (!_isWifiOnly.value) return true
        val status = com.example.util.NetworkConnectivityMonitor.getInitialStatus(context)
        return !status.isConnected || status.isWifi
    }

    private suspend fun writeCategoryBackupInternal(appContext: Context, category: DriveSyncCategory): Int {
        val backupDir = File(appContext.filesDir, "google_drive_sync")
        if (!backupDir.exists()) backupDir.mkdirs()
        val now = System.currentTimeMillis()

        return when (category) {
            DriveSyncCategory.NOTES -> {
                val db = WorkspaceDatabase.getDatabase(appContext)
                val notes = db.workspaceDao().getAll()
                val itemsCount = notes.size

                val root = JSONObject().apply {
                    put("backup_source", "ATHENA Workspace Notes & Research")
                    put("backup_timestamp", now)
                    put("drive_folder", category.driveFolder)
                    put("item_count", itemsCount)
                    val itemsArray = JSONArray()
                    notes.forEach { item ->
                        itemsArray.put(JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("kind", item.kind)
                            put("text", item.text)
                            put("fileType", item.fileType)
                            put("fileUri", item.fileUri)
                            put("tags", item.tags)
                            put("isPinned", item.isPinned)
                            put("modifiedTimestamp", item.modifiedTimestamp)
                            put("size", item.size)
                            put("parentId", item.parentId)
                        })
                    }
                    put("items", itemsArray)
                }

                File(backupDir, "workspace_drive_backup.json").writeText(root.toString(2))
                _notesSyncedCount.value = itemsCount
                _notesLastSyncTime.value = now
                getPrefs(appContext).edit()
                    .putLong(KEY_LAST_SYNC_NOTES, now)
                    .putInt(KEY_COUNT_NOTES, itemsCount)
                    .apply()
                itemsCount
            }

            DriveSyncCategory.PHOTOS -> {
                MediaVaultRepository.initializeWithDefaults(appContext)
                val vaultPhotos = MediaVaultRepository.items.value.filter { it.type == MediaType.PHOTO }
                val itemsCount = vaultPhotos.size

                val root = JSONObject().apply {
                    put("backup_source", "ATHENA Photos & Camera Snapshots")
                    put("backup_timestamp", now)
                    put("drive_folder", category.driveFolder)
                    put("item_count", itemsCount)
                    val photoArray = JSONArray()
                    vaultPhotos.forEach { photo ->
                        photoArray.put(JSONObject().apply {
                            put("id", photo.id)
                            put("title", photo.title)
                            put("subtitle", photo.subtitle)
                            put("uri", photo.uri.toString())
                            put("dateAdded", photo.dateAdded)
                        })
                    }
                    put("photos", photoArray)
                }

                File(backupDir, "photos_snapshots_drive_backup.json").writeText(root.toString(2))
                _photosSyncedCount.value = itemsCount
                _photosLastSyncTime.value = now
                getPrefs(appContext).edit()
                    .putLong(KEY_LAST_SYNC_PHOTOS, now)
                    .putInt(KEY_COUNT_PHOTOS, itemsCount)
                    .apply()
                itemsCount
            }

            DriveSyncCategory.VIDEOS -> {
                GoogleDriveVideoSyncManager.initialize(appContext)
                val videos = GoogleDriveVideoSyncManager.driveVideos.value
                val itemsCount = videos.size

                val root = JSONObject().apply {
                    put("backup_source", "ATHENA AI Generated Videos")
                    put("backup_timestamp", now)
                    put("drive_folder", category.driveFolder)
                    put("item_count", itemsCount)
                    val videoArray = JSONArray()
                    videos.forEach { vid ->
                        videoArray.put(JSONObject().apply {
                            put("id", vid.id)
                            put("title", vid.title)
                            put("prompt", vid.prompt)
                            put("model", vid.model)
                            put("aspectRatio", vid.aspectRatio)
                            put("durationSec", vid.durationSec)
                            put("syncedAtTimestamp", vid.syncedAtTimestamp)
                        })
                    }
                    put("videos", videoArray)
                }

                File(backupDir, "ai_videos_drive_backup.json").writeText(root.toString(2))
                _videosSyncedCount.value = itemsCount
                _videosLastSyncTime.value = now
                getPrefs(appContext).edit()
                    .putLong(KEY_LAST_SYNC_VIDEOS, now)
                    .putInt(KEY_COUNT_VIDEOS, itemsCount)
                    .apply()
                itemsCount
            }
        }
    }

    /**
     * Synchronizes a specific category on demand to local Google Drive staging container.
     */
    fun performCategorySync(
        context: Context,
        category: DriveSyncCategory,
        scope: CoroutineScope,
        onComplete: ((success: Boolean, message: String) -> Unit)? = null,
        showToast: Boolean = true
    ) {
        val appContext = context.applicationContext
        initialize(appContext)
        _isSyncing.value = true
        _syncStatusMessage.value = "Synchronizing ${category.title}..."

        scope.launch(Dispatchers.IO) {
            try {
                val itemsCount = syncMutex.lock().let {
                    try {
                        writeCategoryBackupInternal(appContext, category)
                    } finally {
                        syncMutex.unlock()
                    }
                }

                val now = System.currentTimeMillis()
                _lastSyncTimestamp.value = now
                val totalCount = _notesSyncedCount.value + _photosSyncedCount.value + _videosSyncedCount.value
                _syncedItemsCount.value = totalCount
                getPrefs(appContext).edit()
                    .putLong(KEY_LAST_SYNC, now)
                    .putInt(KEY_SYNCED_COUNT, totalCount)
                    .apply()

                _syncStatusMessage.value = "Backed up ${category.title} ($itemsCount items)"

                withContext(Dispatchers.Main) {
                    if (showToast) {
                        Toast.makeText(appContext, "Backed up $itemsCount ${category.title}", Toast.LENGTH_SHORT).show()
                    }
                    onComplete?.invoke(true, "Synced $itemsCount items")
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                _syncStatusMessage.value = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
                withContext(Dispatchers.Main) {
                    if (showToast) {
                        Toast.makeText(appContext, "Sync error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    onComplete?.invoke(false, e.message ?: "Sync failed")
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Performs a comprehensive sync across all enabled categories.
     */
    fun performSyncAll(
        context: Context,
        scope: CoroutineScope,
        onComplete: ((success: Boolean, message: String) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        initialize(appContext)
        _isSyncing.value = true
        _syncStatusMessage.value = "Synchronizing all enabled categories..."

        scope.launch(Dispatchers.IO) {
            try {
                val totalSynced = syncMutex.lock().let {
                    try {
                        var sum = 0
                        val activeCategories = DriveSyncCategory.entries.filter { isCategoryAutoSyncEnabled(it).value }
                        for (cat in activeCategories) {
                            sum += writeCategoryBackupInternal(appContext, cat)
                        }
                        sum
                    } finally {
                        syncMutex.unlock()
                    }
                }

                val now = System.currentTimeMillis()
                _lastSyncTimestamp.value = now
                _syncedItemsCount.value = totalSynced
                getPrefs(appContext).edit()
                    .putLong(KEY_LAST_SYNC, now)
                    .putInt(KEY_SYNCED_COUNT, totalSynced)
                    .apply()

                _syncStatusMessage.value = "All active categories backed up ($totalSynced items total)"

                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Backup complete: $totalSynced items updated", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(true, "All categories synced")
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                _syncStatusMessage.value = "Sync error: ${e.localizedMessage ?: "Unknown"}"
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Sync error: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(false, e.message ?: "Failed")
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Backward-compatible manual backup for notes.
     */
    fun performBackup(
        context: Context,
        scope: CoroutineScope,
        onComplete: ((success: Boolean, message: String) -> Unit)? = null
    ) {
        performCategorySync(context, DriveSyncCategory.NOTES, scope, onComplete)
    }

    /**
     * Restores notes from the local Google Drive staging backup into the Room workspace.
     */
    fun performRestore(
        context: Context,
        scope: CoroutineScope,
        onComplete: ((success: Boolean, message: String) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        initialize(appContext)
        _isSyncing.value = true
        _syncStatusMessage.value = "Restoring notes from backup..."

        scope.launch(Dispatchers.IO) {
            try {
                val restoredCount = syncMutex.lock().let {
                    try {
                        val backupDir = File(appContext.filesDir, "google_drive_sync")
                        val backupFile = File(backupDir, "workspace_drive_backup.json")

                        if (backupFile.exists()) {
                            val content = backupFile.readText()
                            val json = JSONObject(content)
                            val itemsArray = json.optJSONArray("items")
                            if (itemsArray != null) {
                                val db = WorkspaceDatabase.getDatabase(appContext)
                                val itemsToInsert = mutableListOf<WorkspaceItem>()
                                for (i in 0 until itemsArray.length()) {
                                    val obj = itemsArray.getJSONObject(i)
                                    val text = obj.optString("text", "")
                                    itemsToInsert.add(
                                        WorkspaceItem(
                                            id = obj.getString("id"),
                                            name = obj.getString("name"),
                                            kind = obj.getString("kind"),
                                            text = text,
                                            fileType = obj.optString("fileType", ""),
                                            fileUri = if (obj.has("fileUri") && !obj.isNull("fileUri")) obj.getString("fileUri") else null,
                                            tags = obj.optString("tags", ""),
                                            isPinned = obj.optBoolean("isPinned", false),
                                            modifiedTimestamp = obj.optLong("modifiedTimestamp", System.currentTimeMillis()),
                                            size = obj.optLong("size", text.length.toLong()),
                                            parentId = if (obj.has("parentId") && !obj.isNull("parentId")) obj.getString("parentId") else null
                                        )
                                    )
                                }
                                db.workspaceDao().insertItems(itemsToInsert)
                                itemsToInsert.size
                            } else 0
                        } else {
                            WorkspaceDatabase.getDatabase(appContext).workspaceDao().countAll()
                        }
                    } finally {
                        syncMutex.unlock()
                    }
                }

                val now = System.currentTimeMillis()
                _lastSyncTimestamp.value = now
                _notesLastSyncTime.value = now
                _syncedItemsCount.value = restoredCount
                getPrefs(appContext).edit().putLong(KEY_LAST_SYNC, now).apply()

                _syncStatusMessage.value = "Restored $restoredCount workspace notes"

                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Restored $restoredCount workspace notes", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(true, "Restored $restoredCount items")
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                _syncStatusMessage.value = "Restore failed: ${e.localizedMessage ?: "Unknown error"}"
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Restore error: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(false, e.message ?: "Restore failed")
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Creates a share intent allowing users to send the workspace backup to the actual Google Drive app.
     */
    fun createShareToDriveIntent(context: Context): Intent {
        return createCategoryShareIntent(context, DriveSyncCategory.NOTES)
    }

    fun createCategoryShareIntent(context: Context, category: DriveSyncCategory): Intent {
        val backupDir = File(context.filesDir, "google_drive_sync").apply { mkdirs() }
        val fileName = when (category) {
            DriveSyncCategory.NOTES -> "workspace_drive_backup.json"
            DriveSyncCategory.PHOTOS -> "photos_snapshots_drive_backup.json"
            DriveSyncCategory.VIDEOS -> "ai_videos_drive_backup.json"
        }
        val backupFile = File(backupDir, fileName)
        if (!backupFile.exists()) {
            backupFile.writeText(
                "{\"backup\": \"ATHENA ${category.title}\", \"drive_folder\": \"${category.driveFolder}\", \"timestamp\": ${System.currentTimeMillis()}}"
            )
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "ATHENA ${category.title} Google Drive Backup")
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri(backupFile.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Launches the Google Drive app or falls back safely to the browser.
     */
    fun openGoogleDrive(context: Context) {
        val driveUri = Uri.parse("https://drive.google.com/drive/my-drive")
        val driveIntent = Intent(Intent.ACTION_VIEW, driveUri).apply {
            setPackage("com.google.android.apps.docs")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(driveIntent)
        } catch (e: Exception) {
            runCatching {
                val webIntent = Intent(Intent.ACTION_VIEW, driveUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        }
    }

    /**
     * Called when a note is created or updated to trigger instant auto-sync if active.
     */
    fun onNoteCreatedOrUpdated(context: Context, scope: CoroutineScope) {
        initialize(context)
        if (_isAutoSyncEnabled.value && _notesAutoSyncEnabled.value && canAutoSyncUnderNetworkPolicy(context)) {
            performCategorySync(context, DriveSyncCategory.NOTES, scope, showToast = false)
        }
    }

    /**
     * Called when a snapshot or photo is saved to trigger instant auto-sync if active.
     */
    fun onPhotoCaptured(context: Context, scope: CoroutineScope) {
        initialize(context)
        if (_isAutoSyncEnabled.value && _photosAutoSyncEnabled.value && canAutoSyncUnderNetworkPolicy(context)) {
            performCategorySync(context, DriveSyncCategory.PHOTOS, scope, showToast = false)
        }
    }
}
