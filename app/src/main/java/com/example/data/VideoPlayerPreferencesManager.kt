package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages video playback and rotation preferences persisted across app sessions.
 */
object VideoPlayerPreferencesManager {
    private const val PREFS_NAME = "video_player_prefs"
    private const val KEY_DOUBLE_TAP_ROTATE_ENABLED = "double_tap_rotate_enabled"
    private const val KEY_DEFAULT_ORIENTATION_DEGREES = "default_orientation_degrees"
    private const val KEY_GESTURE_HINT_BADGES_VISIBLE = "gesture_hint_badges_visible"

    private var sharedPreferences: SharedPreferences? = null

    private val _doubleTapRotateEnabled = MutableStateFlow(true)
    val doubleTapRotateEnabled: StateFlow<Boolean> = _doubleTapRotateEnabled.asStateFlow()

    private val _defaultOrientationDegrees = MutableStateFlow(0f)
    val defaultOrientationDegrees: StateFlow<Float> = _defaultOrientationDegrees.asStateFlow()

    private val _gestureHintBadgesVisible = MutableStateFlow(true)
    val gestureHintBadgesVisible: StateFlow<Boolean> = _gestureHintBadgesVisible.asStateFlow()

    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences = prefs
            _doubleTapRotateEnabled.value = prefs.getBoolean(KEY_DOUBLE_TAP_ROTATE_ENABLED, true)
            _defaultOrientationDegrees.value = prefs.getFloat(KEY_DEFAULT_ORIENTATION_DEGREES, 0f)
            _gestureHintBadgesVisible.value = prefs.getBoolean(KEY_GESTURE_HINT_BADGES_VISIBLE, true)
        }
    }

    fun setDoubleTapRotateEnabled(enabled: Boolean) {
        _doubleTapRotateEnabled.value = enabled
        sharedPreferences?.edit()?.putBoolean(KEY_DOUBLE_TAP_ROTATE_ENABLED, enabled)?.apply()
    }

    fun setDefaultOrientationDegrees(degrees: Float) {
        _defaultOrientationDegrees.value = degrees
        sharedPreferences?.edit()?.putFloat(KEY_DEFAULT_ORIENTATION_DEGREES, degrees)?.apply()
    }

    fun setGestureHintBadgesVisible(visible: Boolean) {
        _gestureHintBadgesVisible.value = visible
        sharedPreferences?.edit()?.putBoolean(KEY_GESTURE_HINT_BADGES_VISIBLE, visible)?.apply()
    }
}
