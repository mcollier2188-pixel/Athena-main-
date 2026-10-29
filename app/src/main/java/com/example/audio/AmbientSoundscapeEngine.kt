package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class AmbientSoundLayer(val title: String, val subtitle: String, val iconLabel: String) {
    DEEP_EARTH("Deep Earth Infrasound", "Sub-bedrock resonant rumble (18-35 Hz)", "🌍"),
    GRANITE_WIND("Granite Wind", "Atmospheric resonant breeze across stones", "💨"),
    PINK_NOISE("Pink Acoustic Blanket", "Harmonious 1/f spectral balance", "🌊"),
    CAVE_WATERS("Sacred Cave Waters", "Gentle rhythmic drops in limestone caverns", "💧")
}

data class AmbientLayerState(
    val layer: AmbientSoundLayer,
    val isEnabled: Boolean = false,
    val volume: Float = 0.5f
)

/**
 * State and real-time AudioTrack synthesizer for multi-layer ambient soundscapes.
 * Provides physical audio streaming for deep earth infrasound, granite wind,
 * pink noise, and cave waters with real-time volume mixing and timer management.
 */
class AmbientSoundscapeEngine {
    private val tag = "AmbientSoundscape"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _layers = MutableStateFlow(
        AmbientSoundLayer.values().map {
            AmbientLayerState(
                layer = it,
                isEnabled = it == AmbientSoundLayer.DEEP_EARTH,
                volume = 0.6f
            )
        }
    )
    val layers: StateFlow<List<AmbientLayerState>> = _layers.asStateFlow()

    private val _isEngineActive = MutableStateFlow(false)
    val isEngineActive: StateFlow<Boolean> = _isEngineActive.asStateFlow()

    // Sleep timer state in remaining seconds (0 = timer off)
    private val _timerRemainingSeconds = MutableStateFlow(0)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(0)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private var playbackJob: Job? = null
    private var activeAudioTrack: AudioTrack? = null
    private val sampleRate = 44100
    private val bufferFrames = 1024

    fun toggleLayer(layer: AmbientSoundLayer) {
        val current = _layers.value.toMutableList()
        val index = current.indexOfFirst { it.layer == layer }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(isEnabled = !old.isEnabled)
            _layers.value = current
        }
    }

    fun setLayerVolume(layer: AmbientSoundLayer, volume: Float) {
        val current = _layers.value.toMutableList()
        val index = current.indexOfFirst { it.layer == layer }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(volume = volume.coerceIn(0f, 1f))
            _layers.value = current
        }
    }

    fun setTimer(minutes: Int) {
        val seconds = minutes * 60
        _timerTotalSeconds.value = seconds
        _timerRemainingSeconds.value = seconds
    }

    fun cancelTimer() {
        _timerTotalSeconds.value = 0
        _timerRemainingSeconds.value = 0
    }

    fun tickTimerOneSecond(): Boolean {
        val rem = _timerRemainingSeconds.value
        if (rem <= 0) return true
        val next = rem - 1
        _timerRemainingSeconds.value = next
        if (next <= 0) {
            stopSoundscape()
            return true
        }
        return false
    }

    @Synchronized
    fun startSoundscape() {
        _isEngineActive.value = true
        playbackJob?.cancel()
        playbackJob = coroutineScope.launch {
            streamAmbientLoop()
        }
    }

    @Synchronized
    fun stopSoundscape() {
        _isEngineActive.value = false
        _timerRemainingSeconds.value = 0
        _timerTotalSeconds.value = 0
        playbackJob?.cancel()
        playbackJob = null

        try {
            activeAudioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
        } catch (e: Throwable) {
            Log.d(tag, "AudioTrack release note: ${e.message}")
        } finally {
            activeAudioTrack = null
        }
    }

    private fun streamAmbientLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) return

        val bufferSize = maxOf(minBuf * 2, bufferFrames * 4)
        val track = try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Throwable) {
            Log.w(tag, "Ambient AudioTrack init error: ${e.message}")
            return
        }

        activeAudioTrack = track

        try {
            track.play()
        } catch (e: Throwable) {
            return
        }

        val shortBuffer = ShortArray(bufferFrames)
        val random = Random(42)
        var phaseEarth = 0.0
        var phaseWind = 0.0
        var phaseWater = 0.0
        var pinkB0 = 0.0
        var pinkB1 = 0.0
        var pinkB2 = 0.0

        val twoPi = 2.0 * PI

        while (_isEngineActive.value && coroutineScope.coroutineContext.isActive) {
            val layerStates = _layers.value

            val earthLayer = layerStates.find { it.layer == AmbientSoundLayer.DEEP_EARTH }
            val windLayer = layerStates.find { it.layer == AmbientSoundLayer.GRANITE_WIND }
            val pinkLayer = layerStates.find { it.layer == AmbientSoundLayer.PINK_NOISE }
            val waterLayer = layerStates.find { it.layer == AmbientSoundLayer.CAVE_WATERS }

            val earthGain = if (earthLayer?.isEnabled == true) earthLayer.volume else 0f
            val windGain = if (windLayer?.isEnabled == true) windLayer.volume else 0f
            val pinkGain = if (pinkLayer?.isEnabled == true) pinkLayer.volume else 0f
            val waterGain = if (waterLayer?.isEnabled == true) waterLayer.volume else 0f

            for (i in 0 until bufferFrames) {
                var sample = 0.0

                // 1. Deep Earth rumble (harmonic warm sine at ~27.4 Hz and ~54.8 Hz)
                if (earthGain > 0f) {
                    val earthWave = 0.7 * sin(phaseEarth) + 0.3 * sin(2.0 * phaseEarth)
                    sample += earthWave * earthGain * 0.45
                    phaseEarth = (phaseEarth + twoPi * 27.4 / sampleRate) % twoPi
                }

                // 2. Granite Wind (filtered gentle noise with slow swell)
                if (windGain > 0f) {
                    val white = (random.nextDouble() * 2.0 - 1.0)
                    val swell = 0.6 + 0.4 * sin(phaseWind)
                    sample += white * swell * windGain * 0.12
                    phaseWind = (phaseWind + twoPi * 0.2 / sampleRate) % twoPi
                }

                // 3. Pink Noise (Paul Kellet's filter approximation)
                if (pinkGain > 0f) {
                    val white = (random.nextDouble() * 2.0 - 1.0)
                    pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                    pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                    pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                    val pink = pinkB0 + pinkB1 + pinkB2 + white * 0.5362
                    sample += pink * 0.1 * pinkGain * 0.25
                }

                // 4. Sacred Cave Waters (rhythmic gentle acoustic droplets)
                if (waterGain > 0f) {
                    val drop = sin(phaseWater) * sin(phaseWater * 1.5)
                    sample += drop * waterGain * 0.20
                    phaseWater = (phaseWater + twoPi * 380.0 / sampleRate) % twoPi
                }

                shortBuffer[i] = (sample * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
            }

            try {
                val written = track.write(shortBuffer, 0, bufferFrames)
                if (written < 0) break
            } catch (e: Throwable) {
                break
            }
        }

        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.release()
        } catch (e: Throwable) {
            // Ignore
        } finally {
            if (activeAudioTrack === track) {
                activeAudioTrack = null
            }
        }
    }

    companion object {
        val SHARED = AmbientSoundscapeEngine()
    }
}
