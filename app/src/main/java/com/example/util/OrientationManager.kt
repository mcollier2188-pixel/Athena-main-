package com.example.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppOrientation(val title: String, val requestedOrientation: Int) {
    LANDSCAPE("Landscape (Native Default)", ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
    PORTRAIT("Portrait (90° Rotated)", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
}

/**
 * OrientationManager
 * Manages native orientation with Landscape default, 90° portrait toggle, and persistent preference.
 */
class OrientationManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentOrientation = MutableStateFlow(loadSavedOrientation())
    val currentOrientation: StateFlow<AppOrientation> = _currentOrientation.asStateFlow()

    private fun loadSavedOrientation(): AppOrientation {
        val name = prefs.getString(KEY_ORIENTATION, AppOrientation.LANDSCAPE.name)
        return try {
            AppOrientation.valueOf(name ?: AppOrientation.LANDSCAPE.name)
        } catch (_: Exception) {
            AppOrientation.LANDSCAPE
        }
    }

    fun applyToActivity(activity: Activity) {
        val orientation = _currentOrientation.value
        activity.requestedOrientation = orientation.requestedOrientation
    }

    fun setOrientation(activity: Activity?, orientation: AppOrientation) {
        _currentOrientation.value = orientation
        prefs.edit().putString(KEY_ORIENTATION, orientation.name).apply()
        activity?.requestedOrientation = orientation.requestedOrientation
    }

    /**
     * Toggles 90 degrees between Native Landscape and Portrait
     */
    fun toggle90Degrees(activity: Activity?): AppOrientation {
        val next = if (_currentOrientation.value == AppOrientation.LANDSCAPE) {
            AppOrientation.PORTRAIT
        } else {
            AppOrientation.LANDSCAPE
        }
        setOrientation(activity, next)
        return next
    }

    companion object {
        private const val PREFS_NAME = "athena_orientation_prefs"
        private const val KEY_ORIENTATION = "app_screen_orientation"

        @Volatile
        private var instance: OrientationManager? = null

        fun getInstance(context: Context): OrientationManager {
            return instance ?: synchronized(this) {
                instance ?: OrientationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
