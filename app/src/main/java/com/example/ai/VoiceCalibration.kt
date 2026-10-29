package com.example.ai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VoiceCalibrationSettings(
    val pitch: Float = 1.05f,
    val rate: Float = 0.95f,
    val volume: Float = 1.0f
)

/**
 * Voice Calibration singleton for ATHENA suite / Dola module.
 * Persists and coordinates pitch, speech rate, and audio volume settings
 * across all speech-enabled modules in the ATHENA suite (Ask Dola, Voice Journal,
 * Perspective Weaver, Genius Council, Lost Journal, and Workspace).
 */
class VoiceCalibration private constructor(private val appContext: Context) {

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<VoiceCalibrationSettings> = _settings.asStateFlow()

    val pitch: Float
        get() = _settings.value.pitch

    val rate: Float
        get() = _settings.value.rate

    val volume: Float
        get() = _settings.value.volume

    private fun loadSettings(): VoiceCalibrationSettings {
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = sp.getFloat(KEY_PITCH, DEFAULT_PITCH).coerceIn(MIN_PITCH, MAX_PITCH)
        val r = sp.getFloat(KEY_RATE, DEFAULT_RATE).coerceIn(MIN_RATE, MAX_RATE)
        val v = sp.getFloat(KEY_VOLUME, DEFAULT_VOLUME).coerceIn(MIN_VOLUME, MAX_VOLUME)
        return VoiceCalibrationSettings(pitch = p, rate = r, volume = v)
    }

    /**
     * Updates and saves the voice calibration parameters across all speech modules.
     */
    fun calibrate(pitch: Float, rate: Float, volume: Float) {
        val safePitch = pitch.coerceIn(MIN_PITCH, MAX_PITCH)
        val safeRate = rate.coerceIn(MIN_RATE, MAX_RATE)
        val safeVolume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)

        prefs.edit()
            .putFloat(KEY_PITCH, safePitch)
            .putFloat(KEY_RATE, safeRate)
            .putFloat(KEY_VOLUME, safeVolume)
            .apply()

        // Also update legacy dola_voice_prefs for backward compatibility
        try {
            appContext.getSharedPreferences("dola_voice_prefs", Context.MODE_PRIVATE)
                .edit()
                .putFloat("global_speech_rate", safeRate)
                .putFloat("global_speech_pitch", safePitch)
                .putFloat("global_speech_volume", safeVolume)
                .apply()
        } catch (_: Throwable) {}

        _settings.value = VoiceCalibrationSettings(
            pitch = safePitch,
            rate = safeRate,
            volume = safeVolume
        )
    }

    /**
     * Resets calibration to the ATHENA Dola optimal acoustic defaults.
     */
    fun resetToDefaults() {
        calibrate(
            pitch = DEFAULT_PITCH,
            rate = DEFAULT_RATE,
            volume = DEFAULT_VOLUME
        )
    }

    companion object {
        const val PREFS_NAME = "dola_voice_calibration_prefs"
        const val KEY_PITCH = "calibrated_pitch"
        const val KEY_RATE = "calibrated_rate"
        const val KEY_VOLUME = "calibrated_volume"

        const val DEFAULT_PITCH = 1.05f
        const val DEFAULT_RATE = 0.95f
        const val DEFAULT_VOLUME = 1.0f

        const val MIN_PITCH = 0.5f
        const val MAX_PITCH = 2.0f
        const val MIN_RATE = 0.5f
        const val MAX_RATE = 2.0f
        const val MIN_VOLUME = 0.0f
        const val MAX_VOLUME = 1.0f

        @Volatile
        private var instance: VoiceCalibration? = null

        fun getInstance(context: Context): VoiceCalibration {
            return instance ?: synchronized(this) {
                instance ?: VoiceCalibration(context.applicationContext).also { instance = it }
            }
        }
    }
}
