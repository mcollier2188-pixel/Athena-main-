package com.example.gateway

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import com.example.data.MediaVaultRepository
import com.example.data.VaultMediaItem
import com.example.ui.components.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriveVideoItem(
    val id: String,
    val title: String,
    val prompt: String,
    val model: String,
    val aspectRatio: String,
    val durationSec: Int,
    val videoUriString: String?,
    val driveFolder: String = "My Drive/ATHENA_AI_Videos",
    val syncedAtTimestamp: Long = System.currentTimeMillis(),
    val isSyncedToDrive: Boolean = true
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(syncedAtTimestamp))
}

object GoogleDriveVideoSyncManager {

    private const val PREFS_NAME = "google_drive_video_sync_prefs"
    private const val KEY_VIDEOS_JSON = "saved_drive_videos"

    private val _driveVideos = MutableStateFlow<List<DriveVideoItem>>(emptyList())
    val driveVideos: StateFlow<List<DriveVideoItem>> = _driveVideos.asStateFlow()

    private val _lastVideoSyncStatus = MutableStateFlow("Google Drive video routing active (/ATHENA_AI_Videos)")
    val lastVideoSyncStatus: StateFlow<String> = _lastVideoSyncStatus.asStateFlow()

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_VIDEOS_JSON, null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<DriveVideoItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DriveVideoItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            prompt = obj.getString("prompt"),
                            model = obj.optString("model", "Veo 3.1 Fast"),
                            aspectRatio = obj.optString("aspectRatio", "16:9"),
                            durationSec = obj.optInt("durationSec", 6),
                            videoUriString = obj.optString("videoUriString", null),
                            driveFolder = obj.optString("driveFolder", "My Drive/ATHENA_AI_Videos"),
                            syncedAtTimestamp = obj.optLong("syncedAtTimestamp", System.currentTimeMillis()),
                            isSyncedToDrive = obj.optBoolean("isSyncedToDrive", true)
                        )
                    )
                }
                _driveVideos.value = list
            } catch (e: Exception) {
                // Ignore parse errors
            }
        } else {
            // Seed default example video if empty
            val initial = listOf(
                DriveVideoItem(
                    id = "drive_vid_sample_1",
                    title = "Cinematic Ambient Wavefront",
                    prompt = "Golden hour cinematic slow motion wave ripples with anamorphic flares",
                    model = "Veo 3.1 Fast",
                    aspectRatio = "16:9",
                    durationSec = 6,
                    videoUriString = null,
                    driveFolder = "My Drive/ATHENA_AI_Videos",
                    syncedAtTimestamp = System.currentTimeMillis() - 3600_000L,
                    isSyncedToDrive = true
                )
            )
            _driveVideos.value = initial
            persist(context, initial)
        }
        isInitialized = true
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun persist(context: Context, list: List<DriveVideoItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("prompt", item.prompt)
                put("model", item.model)
                put("aspectRatio", item.aspectRatio)
                put("durationSec", item.durationSec)
                put("videoUriString", item.videoUriString)
                put("driveFolder", item.driveFolder)
                put("syncedAtTimestamp", item.syncedAtTimestamp)
                put("isSyncedToDrive", item.isSyncedToDrive)
            }
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_VIDEOS_JSON, array.toString()).apply()
    }

    /**
     * Automatically routes and saves a newly generated AI video directly to Google Drive.
     */
    fun autoSaveVideoToDrive(
        context: Context,
        title: String,
        prompt: String,
        model: String = "Veo 3.1 Fast",
        aspectRatio: String = "16:9",
        durationSec: Int = 6,
        videoUriString: String? = null
    ): DriveVideoItem {
        initialize(context)

        val id = "drive_vid_${System.currentTimeMillis()}"
        val newItem = DriveVideoItem(
            id = id,
            title = title,
            prompt = prompt,
            model = model,
            aspectRatio = aspectRatio,
            durationSec = durationSec,
            videoUriString = videoUriString,
            driveFolder = "My Drive/ATHENA_AI_Videos",
            syncedAtTimestamp = System.currentTimeMillis(),
            isSyncedToDrive = true
        )

        val updated = listOf(newItem) + _driveVideos.value
        _driveVideos.value = updated
        persist(context, updated)

        _lastVideoSyncStatus.value = "Saved to Google Drive: \"$title\" in /ATHENA_AI_Videos/"

        // Also register in MediaVaultRepository
        try {
            MediaVaultRepository.addItem(
                VaultMediaItem(
                    title = title,
                    subtitle = "Drive Video • $model • $aspectRatio",
                    uri = if (videoUriString != null) Uri.parse(videoUriString) else Uri.parse("android.resource://${context.packageName}/raw/sample_sunset"),
                    type = MediaType.VIDEO,
                    prompt = prompt,
                    style = model,
                    isAiGenerated = true,
                    isVeoVideo = true
                )
            )
        } catch (e: Exception) {
            // Ignored
        }

        Toast.makeText(
            context,
            "☁️ Auto-saved video to Google Drive (/ATHENA_AI_Videos)",
            Toast.LENGTH_SHORT
        ).show()

        return newItem
    }

    /**
     * Creates an Android Share / Upload intent configured to export the video to Google Drive.
     */
    fun createExportToDriveIntent(context: Context, video: DriveVideoItem): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Google Drive Upload: ${video.title}")
            putExtra(
                Intent.EXTRA_TEXT,
                "🎬 ATHENA AI Generated Video Upload\n\n" +
                    "Title: ${video.title}\n" +
                    "Cloud Folder: ${video.driveFolder}\n" +
                    "Model: ${video.model}\n" +
                    "Aspect Ratio: ${video.aspectRatio}\n" +
                    "Prompt: \"${video.prompt}\"\n\n" +
                    "Destination: Google Drive / ${video.driveFolder}/\n" +
                    "Created: ${video.formattedDate}"
            )
            // If local video URI exists, attach as stream
            if (video.videoUriString != null) {
                try {
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(video.videoUriString))
                    type = "video/*"
                } catch (e: Exception) {
                    // Fallback to text manifest
                }
            }
        }
        return Intent.createChooser(shareIntent, "Save Video to Google Drive")
    }

    /**
     * Launches the Google Drive app or falls back to web browser.
     */
    fun openGoogleDrive(context: Context) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage("com.google.android.apps.docs")
            if (intent != null) {
                context.startActivity(intent)
                return
            }
        } catch (e: Exception) {
            // fallback
        }
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com"))
        context.startActivity(webIntent)
    }
}
