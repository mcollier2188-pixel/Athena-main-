package com.example.launcher

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Represents an installed Android application discovered on the device.
 */
data class LauncherAppItem(
    val label: String,
    val packageName: String,
    val activityName: String,
    val isSystemApp: Boolean = false,
    val isFavorite: Boolean = false,
    val installTime: Long = 0L,
    val iconBitmap: ImageBitmap? = null
)

/**
 * Filter mode for Installed Apps Drawer
 */
enum class AppDrawerFilter(val label: String) {
    ALL("All Apps"),
    FAVORITES("Favorites"),
    SYSTEM("System"),
    USER("User Apps")
}

/**
 * Display layout style for App Drawer
 */
enum class AppDrawerLayoutMode(val label: String) {
    GRID("Grid View"),
    LIST("List View")
}
