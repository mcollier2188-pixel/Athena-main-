package com.example.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sovereign Manager for Android Launcher Capabilities, Installed Apps Discovery,
 * and Default Home Application Configuration.
 */
class LauncherAppsManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _installedApps = MutableStateFlow<List<LauncherAppItem>>(emptyList())
    val installedApps: StateFlow<List<LauncherAppItem>> = _installedApps.asStateFlow()

    private val _favoriteApps = MutableStateFlow<List<LauncherAppItem>>(emptyList())
    val favoriteApps: StateFlow<List<LauncherAppItem>> = _favoriteApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val favoriteSet = mutableSetOf<String>()

    init {
        loadSavedFavorites()
        checkDefaultLauncherStatus()
        refreshInstalledApps()
    }

    private fun loadSavedFavorites() {
        val saved = prefs.getStringSet(KEY_FAVORITES, null)
        if (saved != null) {
            favoriteSet.clear()
            favoriteSet.addAll(saved)
        } else {
            // Default recommended favorites if empty
            favoriteSet.addAll(setOf("com.android.chrome", "com.google.android.youtube", "com.android.camera", "com.android.settings"))
        }
    }

    private fun saveFavorites() {
        prefs.edit().putStringSet(KEY_FAVORITES, favoriteSet.toSet()).apply()
        updateFavoritesList()
    }

    fun checkDefaultLauncherStatus() {
        try {
            val pm = context.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            val currentDefaultPkg = resolveInfo?.activityInfo?.packageName
            _isDefaultLauncher.value = (currentDefaultPkg == context.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking default launcher status: ${e.message}")
            _isDefaultLauncher.value = false
        }
    }

    fun refreshInstalledApps() {
        scope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                queryInstalledLauncherApps()
            }
            _installedApps.value = apps
            updateFavoritesList()
            _isLoading.value = false
            checkDefaultLauncherStatus()
        }
    }

    private fun queryInstalledLauncherApps(): List<LauncherAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            0
        }

        val resolveInfos: List<ResolveInfo> = try {
            pm.queryIntentActivities(mainIntent, flags)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query launcher activities: ${e.message}")
            emptyList()
        }

        val appList = mutableListOf<LauncherAppItem>()

        for (resolveInfo in resolveInfos) {
            try {
                val pkgName = resolveInfo.activityInfo.packageName
                val actName = resolveInfo.activityInfo.name
                val label = resolveInfo.loadLabel(pm)?.toString() ?: pkgName
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val iconBitmap = try {
                    val drawable = resolveInfo.loadIcon(pm)
                    drawableToImageBitmap(drawable)
                } catch (e: Exception) {
                    null
                }

                val installTime = try {
                    pm.getPackageInfo(pkgName, 0).firstInstallTime
                } catch (e: Exception) {
                    0L
                }

                val item = LauncherAppItem(
                    label = label,
                    packageName = pkgName,
                    activityName = actName,
                    isSystemApp = isSystem,
                    isFavorite = favoriteSet.contains(pkgName),
                    installTime = installTime,
                    iconBitmap = iconBitmap
                )
                appList.add(item)
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing app info: ${e.message}")
            }
        }

        // Sort alphabetically by label
        return appList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private fun updateFavoritesList() {
        val currentAll = _installedApps.value
        val updated = currentAll.map { app ->
            app.copy(isFavorite = favoriteSet.contains(app.packageName))
        }
        _installedApps.value = updated
        _favoriteApps.value = updated.filter { it.isFavorite }
    }

    fun toggleFavorite(packageName: String) {
        if (favoriteSet.contains(packageName)) {
            favoriteSet.remove(packageName)
        } else {
            favoriteSet.add(packageName)
        }
        saveFavorites()
    }

    fun isFavorite(packageName: String): Boolean = favoriteSet.contains(packageName)

    fun launchApp(targetContext: Context, packageName: String): Boolean {
        return try {
            val pm = targetContext.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                targetContext.startActivity(intent)
                true
            } else {
                Toast.makeText(targetContext, "Unable to launch $packageName", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app $packageName: ${e.message}")
            Toast.makeText(targetContext, "Launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppInfo(targetContext: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            targetContext.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app details for $packageName: ${e.message}")
            Toast.makeText(targetContext, "Unable to open app info", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDefaultLauncherPrompt(targetContext: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                targetContext.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                targetContext.startActivity(intent)
            }
        } catch (e: Exception) {
            try {
                // Secondary fallback: prompt chooser for Home category
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(homeIntent, "Select Default Home Launcher").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                targetContext.startActivity(chooser)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed to launch home settings: ${fallbackEx.message}")
                Toast.makeText(targetContext, "Open Settings -> Apps -> Default apps to set ATHENA as Home", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap? {
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(width.coerceIn(48, 192), height.coerceIn(48, 192), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.w(TAG, "Drawable conversion failed: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "LauncherAppsManager"
        private const val PREFS_NAME = "athena_launcher_prefs"
        private const val KEY_FAVORITES = "key_pinned_favorites"

        @Volatile
        private var INSTANCE: LauncherAppsManager? = null

        fun getInstance(context: Context): LauncherAppsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LauncherAppsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
