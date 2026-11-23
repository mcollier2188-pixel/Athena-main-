package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
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
        AmbientSoundLayer.entries.map {
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
    private var timerJob: Job? = null
    private val sampleRate = 44100
    private val bufferFrames = 1024

    fun toggleLayer(layer: AmbientSoundLayer) {
        _layers.update { list ->
            list.map { if (it.layer == layer) it.copy(isEnabled = !it.isEnabled) else it }
        }
    }

    fun setLayerVolume(layer: AmbientSoundLayer, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _layers.update { list ->
            list.map { if (it.layer == layer) it.copy(volume = clamped) else it }
        }
    }

    @Synchronized
    fun setTimer(minutes: Int) {
        val seconds = (minutes * 60).coerceAtLeast(0)
        _timerTotalSeconds.value = seconds
        _timerRemainingSeconds.value = seconds
        timerJob?.cancel()
        if (seconds > 0) {
            timerJob = coroutineScope.launch {
                while (isActive && _timerRemainingSeconds.value > 0) {
                    delay(1000)
                    if (tickTimerOneSecond()) break
                }
            }
        }
    }

    @Synchronized
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
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
        if (_isEngineActive.value && playbackJob?.isActive == true) {
            return
        }
        _isEngineActive.value = true
        playbackJob?.cancel()
        playbackJob = coroutineScope.launch {
            streamAmbientLoop()
        }
    }

    @Synchronized
    fun stopSoundscape() {
        _isEngineActive.value = false
        timerJob?.cancel()
        timerJob = null
        _timerRemainingSeconds.value = 0
        _timerTotalSeconds.value = 0
        playbackJob?.cancel()
        playbackJob = null
    }

    fun close() {
        stopSoundscape()
        if (this !== SHARED) {
            coroutineScope.cancel()
        }
    }

    private suspend fun streamAmbientLoop() {
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

        try {
            track.play()

            val shortBuffer = ShortArray(bufferFrames)
            val random = Random(42)
            var phaseEarth = 0.0
            var phaseWind = 0.0
            var pinkB0 = 0.0
            var pinkB1 = 0.0
            var pinkB2 = 0.0
            var pinkB3 = 0.0
            var pinkB4 = 0.0
            var pinkB5 = 0.0
            var pinkB6 = 0.0

            // Cave water droplet state
            var dropletSamplesSinceTrigger = sampleRate
            var nextDropletIntervalSamples = (sampleRate * 0.65).toInt()
            var dropletFreqHz = 620.0
            var dropletPhase = 0.0

            val twoPi = 2.0 * PI

            while (_isEngineActive.value && currentCoroutineContext().isActive) {
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

                    // 3. Pink Noise (Paul Kellet's complete 7-pole 1/f filter)
                    if (pinkGain > 0f) {
                        val white = (random.nextDouble() * 2.0 - 1.0)
                        pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                        pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                        pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                        pinkB3 = 0.86650 * pinkB3 + white * 0.3104856
                        pinkB4 = 0.55000 * pinkB4 + white * 0.5329522
                        pinkB5 = -0.7616 * pinkB5 - white * 0.0168980
                        val pink = pinkB0 + pinkB1 + pinkB2 + pinkB3 + pinkB4 + pinkB5 + pinkB6 + white * 0.5362
                        pinkB6 = white * 0.115926
                        sample += pink * 0.05 * pinkGain * 0.35
                    }

                    // 4. Sacred Cave Waters (intermittent exponentially decaying resonant droplets)
                    if (waterGain > 0f) {
                        dropletSamplesSinceTrigger++
                        if (dropletSamplesSinceTrigger >= nextDropletIntervalSamples) {
                            dropletSamplesSinceTrigger = 0
                            nextDropletIntervalSamples = (sampleRate * (0.35 + random.nextDouble() * 1.1)).toInt()
                            dropletFreqHz = 480.0 + random.nextDouble() * 440.0
                            dropletPhase = 0.0
                        }
                        val tSec = dropletSamplesSinceTrigger.toDouble() / sampleRate
                        if (tSec < 0.18) {
                            val env = exp(-tSec * 28.0)
                            val pitchBend = dropletFreqHz * (1.0 + 0.35 * (1.0 - env))
                            dropletPhase = (dropletPhase + twoPi * pitchBend / sampleRate) % twoPi
                            sample += sin(dropletPhase) * env * waterGain * 0.28
                        }
                    }

                    shortBuffer[i] = (sample * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
                }

                val written = track.write(shortBuffer, 0, bufferFrames)
                if (written < 0) break
            }
        } catch (e: Throwable) {
            Log.d(tag, "Ambient stream ended: ${e.message}")
        } finally {
            runCatching {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
            }
            runCatching { track.release() }
        }
    }

    companion object {
        val SHARED = AmbientSoundscapeEngine()
    }
}
