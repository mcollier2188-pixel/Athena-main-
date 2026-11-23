package com.example.system.update

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.MainActivity
import java.io.File
import kotlin.system.exitProcess

/**
 * Metadata for detected or staged update packages.
 */
data class UpdatePackageInfo(
    val file: File,
    val title: String,
    val versionString: String,
    val fileSizeBytes: Long,
    val modifiedAt: Long,
    val isValidApk: Boolean
)

/**
 * State snapshot saved before update or reboot to ensure seamless recovery.
 */
data class UpdateRecoveryInfo(
    val lastScreen: String,
    val timestamp: Long,
    val updateTitle: String
)

/**
 * ATHENA System Update & OTA Recovery Manager.
 * Handles:
 * 1. "Install via USB" / "Install Unknown Apps" permission auditing & settings navigation.
 * 2. Discovery of local APK update packages across files, cache, and downloads.
 * 3. FileProvider-based in-app package installer intent launching.
 * 4. State recovery persistence and clean application restart/reboot.
 */
object AthenaUpdateManager {

    private const val TAG = "AthenaUpdateManager"
    private const val PREFS_NAME = "athena_update_prefs"
    private const val KEY_PENDING_RECOVERY = "has_pending_recovery"
    private const val KEY_RECOVERY_SCREEN = "recovery_screen"
    private const val KEY_RECOVERY_TIME = "recovery_timestamp"
    private const val KEY_RECOVERY_TITLE = "recovery_title"

    /**
     * Checks if the app is authorized to request package installations directly (Android 8.0+).
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens the device settings screen allowing the user to authorize ATHENA to install unknown packages.
     */
    fun openUnknownAppSourcesSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open unknown app sources settings", e)
            runCatching {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        }
    }

    /**
     * Opens Android Developer Options to toggle "Install via USB" or "Verify apps over USB".
     */
    fun openDeveloperOptions(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Developer options not available on device", e)
            runCatching {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        }
    }

    /**
     * Imports a user-selected APK content URI into internal staging (`apk_builds/`) so it can be
     * verified and installed via FileProvider under Android 11-16 Scoped Storage.
     */
    fun importApkFromUri(context: Context, uri: Uri, displayName: String? = null): File? {
        return try {
            val buildsDir = File(context.filesDir, "apk_builds").apply { mkdirs() }
            val rawName = displayName?.takeIf { it.isNotBlank() } ?: "athena_imported_update.apk"
            val sanitized = rawName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .let { if (it.endsWith(".apk", ignoreCase = true)) it else "$it.apk" }
            val targetFile = File(buildsDir, sanitized)
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            stream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.length() > 0L) targetFile else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import APK from URI", e)
            null
        }
    }

    /**
     * Discovers all available valid APK update packages located in app storage or staging folders.
     */
    fun detectAvailableUpdatePackages(context: Context): List<UpdatePackageInfo> {
        val candidates = LinkedHashSet<File>()

        // 1. App apk_builds directory
        val buildsDir = File(context.filesDir, "apk_builds")
        if (buildsDir.exists()) {
            candidates.addAll(buildsDir.listFiles { f -> f.extension.equals("apk", ignoreCase = true) }?.toList() ?: emptyList())
        }

        // 2. App exports directory
        val exportsDir = File(context.filesDir, "exports")
        if (exportsDir.exists()) {
            candidates.addAll(exportsDir.listFiles { f -> f.extension.equals("apk", ignoreCase = true) }?.toList() ?: emptyList())
        }

        // 3. Cache exports directory
        val cacheDir = File(context.cacheDir, "exports")
        if (cacheDir.exists()) {
            candidates.addAll(cacheDir.listFiles { f -> f.extension.equals("apk", ignoreCase = true) }?.toList() ?: emptyList())
        }

        // 4. Download directory (legacy / broad storage access if readable)
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && downloadsDir.exists()) {
                candidates.addAll(downloadsDir.listFiles { f ->
                    f.name.contains("athena", ignoreCase = true) && f.extension.equals("apk", ignoreCase = true)
                }?.toList() ?: emptyList())
            }
        } catch (e: Throwable) {
            Log.d(TAG, "Notice reading public downloads: ${e.message}")
        }

        val pm = context.packageManager
        return candidates.mapNotNull { file ->
            if (!file.exists() || file.length() <= 0L) return@mapNotNull null
            val archiveInfo = pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_ACTIVITIES)
            val isValid = archiveInfo != null
            if (!isValid) return@mapNotNull null

            archiveInfo?.applicationInfo?.apply {
                sourceDir = file.absolutePath
                publicSourceDir = file.absolutePath
            }
            val versionStr = archiveInfo?.versionName?.let { "v$it" } ?: "v${BuildConfig.VERSION_NAME}-OTA"
            val title = archiveInfo?.applicationInfo?.loadLabel(pm)?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: file.nameWithoutExtension

            UpdatePackageInfo(
                file = file,
                title = title,
                versionString = versionStr,
                fileSizeBytes = file.length(),
                modifiedAt = file.lastModified(),
                isValidApk = true
            )
        }.sortedByDescending { it.modifiedAt }
    }

    /**
     * Launches the native Android package installer for the chosen APK file.
     * Automatically stages external files into internal `apk_builds/` if outside FileProvider roots.
     */
    fun triggerPackageInstall(context: Context, apkFile: File, currentScreen: String = "Cockpit"): Boolean {
        if (!apkFile.exists() || apkFile.length() <= 0L) return false

        // 1. Arm recovery snapshot synchronously
        saveStateForUpdateRecovery(context, currentScreen, apkFile.name)

        return try {
            val internalFilesRoot = context.filesDir.canonicalPath
            val internalCacheRoot = context.cacheDir.canonicalPath
            val canonicalApkPath = apkFile.canonicalPath

            val providerReadyFile = if (
                canonicalApkPath.startsWith(internalFilesRoot) ||
                canonicalApkPath.startsWith(internalCacheRoot)
            ) {
                apkFile
            } else {
                val stagedDir = File(context.filesDir, "apk_builds").apply { mkdirs() }
                val stagedCopy = File(stagedDir, apkFile.name)
                apkFile.copyTo(stagedCopy, overwrite = true)
                stagedCopy
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                providerReadyFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                clipData = ClipData.newRawUri(providerReadyFile.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            false
        }
    }

    /**
     * Persists a recovery checkpoint synchronously (`commit()`) before updating or restarting.
     */
    fun saveStateForUpdateRecovery(context: Context, currentScreen: String, updateTitle: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_PENDING_RECOVERY, true)
            .putString(KEY_RECOVERY_SCREEN, currentScreen)
            .putLong(KEY_RECOVERY_TIME, System.currentTimeMillis())
            .putString(KEY_RECOVERY_TITLE, updateTitle)
            .commit()
    }

    /**
     * Checks whether an update recovery is pending and clears the flag upon consumption.
     */
    fun checkAndConsumeUpdateRecovery(context: Context): UpdateRecoveryInfo? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isPending = prefs.getBoolean(KEY_PENDING_RECOVERY, false)
        if (!isPending) return null

        val screen = prefs.getString(KEY_RECOVERY_SCREEN, "Cockpit") ?: "Cockpit"
        val time = prefs.getLong(KEY_RECOVERY_TIME, System.currentTimeMillis())
        val title = prefs.getString(KEY_RECOVERY_TITLE, "System Update") ?: "System Update"

        prefs.edit().putBoolean(KEY_PENDING_RECOVERY, false).apply()

        return UpdateRecoveryInfo(
            lastScreen = screen,
            timestamp = time,
            updateTitle = title
        )
    }

    /**
     * Cleanly restarts ATHENA and auto-relaunches MainActivity without triggering Android 10-16
     * Background Activity Launch (BAL) AlarmManager blocks.
     */
    fun triggerCleanAppRestart(context: Context, currentScreen: String = "Cockpit") {
        saveStateForUpdateRecovery(context, currentScreen, "Clean System Restart")

        try {
            val componentName = ComponentName(context, MainActivity::class.java)
            val restartIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(restartIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching restart activity task", e)
        }

        Runtime.getRuntime().exit(0)
        exitProcess(0)
    }
}
