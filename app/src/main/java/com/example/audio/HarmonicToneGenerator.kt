package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
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
import kotlin.math.PI
import kotlin.math.sin

enum class WaveformTimbre {
    PURE_SINE,
    BEDROCK_HARMONIC,
    TECTONIC_PULSE
}

enum class SoundEffectType {
    TAP,
    CHIME,
    HARMONIC_PING,
    ALERT,
    SUCCESS,
    FINGER_RELEASE,
    SUB_BASS_PULSE
}

/**
 * Real-time Acoustic Sound Synthesis Engine.
 * Streams dynamic 16-bit PCM audio via Android AudioTrack on a dedicated background coroutine.
 * Supports continuous frequency synthesis (pure sine, bedrock harmonic, tectonic pulse),
 * octave multiplication, tidal LFO tremolo, and tactile cybernetic UI sound effects.
 */
class HarmonicToneGenerator {
    private val tag = "HarmonicToneGenerator"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isPlayingState = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlayingState.asStateFlow()

    private val _isMutedState = MutableStateFlow(false)
    val isMutedState: StateFlow<Boolean> = _isMutedState.asStateFlow()

    private val _volumeState = MutableStateFlow(0.85f)
    val volumeState: StateFlow<Float> = _volumeState.asStateFlow()

    private val _isButtonSoundEnabledState = MutableStateFlow(true)
    val isButtonSoundEnabledState: StateFlow<Boolean> = _isButtonSoundEnabledState.asStateFlow()

    @Volatile
    var isButtonSoundEnabled: Boolean = true
        private set

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var activeFrequencies: List<Double> = listOf(42.6)
        private set

    @Volatile
    var octaveMultiplier: Double = 8.0
        private set

    @Volatile
    var isTidalLfoEnabled: Boolean = false
        private set

    @Volatile
    var volume: Float = 0.85f
        private set

    @Volatile
    var isMuted: Boolean = false
        private set

    @Volatile
    var timbre: WaveformTimbre = WaveformTimbre.PURE_SINE
        private set

    @Volatile
    var hardwareCalibrationOffsetHz: Double = 0.0
        private set

    private var playbackJob: Job? = null
    private var activeAudioTrack: AudioTrack? = null

    private val sampleRate = 44100
    private val bufferFrames = 1024

    fun setOctaveMultiplier(mult: Double) {
        octaveMultiplier = mult.coerceAtLeast(0.1)
    }

    fun setHardwareCalibrationOffset(offsetHz: Double) {
        hardwareCalibrationOffsetHz = offsetHz
    }

    fun setTidalLfo(enabled: Boolean) {
        isTidalLfoEnabled = enabled
    }

    fun setVolumeLevel(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        volume = clamped
        _volumeState.value = clamped
    }

    fun setButtonSoundEnabled(enabled: Boolean) {
        isButtonSoundEnabled = enabled
        _isButtonSoundEnabledState.value = enabled
    }

    fun toggleButtonSound() {
        setButtonSoundEnabled(!isButtonSoundEnabled)
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        _isMutedState.value = muted
    }

    fun toggleMute() {
        setMuted(!isMuted)
    }

    fun setTimbreMode(mode: WaveformTimbre) {
        timbre = mode
    }

    fun updateFrequencies(frequencies: List<Double>) {
        activeFrequencies = if (frequencies.isEmpty()) listOf(42.6) else frequencies
    }

    @Synchronized
    fun start(
        frequencies: List<Double> = activeFrequencies,
        mult: Double = octaveMultiplier,
        tidalLfo: Boolean = isTidalLfoEnabled,
        timbreMode: WaveformTimbre = timbre
    ) {
        activeFrequencies = if (frequencies.isEmpty()) listOf(42.6) else frequencies
        octaveMultiplier = mult.coerceAtLeast(0.1)
        isTidalLfoEnabled = tidalLfo
        timbre = timbreMode

        isPlaying = true
        _isPlayingState.value = true

        playbackJob?.cancel()
        playbackJob = coroutineScope.launch {
            streamAudioLoop()
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        _isPlayingState.value = false
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
            Log.w(tag, "AudioTrack release notice: ${e.message}")
        } finally {
            activeAudioTrack = null
        }
    }

    /**
     * Plays a brief, crisp tactile UI sound chime (tap, confirmation, Solfeggio bell, alert).
     */
    fun playChime(effect: SoundEffectType = SoundEffectType.CHIME) {
        if (isMuted) return
        if (effect == SoundEffectType.TAP && !isButtonSoundEnabled) return
        coroutineScope.launch {
            try {
                when (effect) {
                    SoundEffectType.TAP -> synthesizeShortTone(frequencyHz = 880.0, durationMs = 35, peakVolume = 0.25f)
                    SoundEffectType.CHIME -> synthesizeShortTone(frequencyHz = 528.0, durationMs = 180, peakVolume = 0.45f)
                    SoundEffectType.HARMONIC_PING -> synthesizeShortTone(frequencyHz = 432.0, durationMs = 220, peakVolume = 0.40f)
                    SoundEffectType.ALERT -> {
                        synthesizeShortTone(frequencyHz = 660.0, durationMs = 80, peakVolume = 0.35f)
                        synthesizeShortTone(frequencyHz = 990.0, durationMs = 120, peakVolume = 0.35f)
                    }
                    SoundEffectType.SUCCESS -> {
                        synthesizeShortTone(frequencyHz = 432.0, durationMs = 70, peakVolume = 0.30f)
                        synthesizeShortTone(frequencyHz = 528.0, durationMs = 70, peakVolume = 0.35f)
                        synthesizeShortTone(frequencyHz = 648.0, durationMs = 140, peakVolume = 0.40f)
                    }
                    SoundEffectType.FINGER_RELEASE -> {
                        synthesizeShortTone(frequencyHz = 1620.0, durationMs = 30, peakVolume = 0.70f)
                    }
                    SoundEffectType.SUB_BASS_PULSE -> {
                        synthesizeShortTone(frequencyHz = 42.6, durationMs = 2400, peakVolume = 0.75f)
                    }
                }
            } catch (e: Throwable) {
                Log.d(tag, "Chime generation ignored: ${e.message}")
            }
        }
    }

    /**
     * Plays a specific frequency blip for previewing or acoustic testing.
     */
    fun playFeedbackTone(frequencyHz: Double, durationMs: Long = 180, peakVolume: Float = 0.4f) {
        if (isMuted) return
        coroutineScope.launch {
            synthesizeShortTone(frequencyHz = frequencyHz, durationMs = durationMs, peakVolume = peakVolume)
        }
    }

    /**
     * Synthesizes and loops a continuous tone at specified frequency and volume level.
     */
    fun playContinuousTone(frequencyHz: Double = 42.6, volume: Float = 0.20f) {
        setVolumeLevel(volume)
        start(frequencies = listOf(frequencyHz))
    }

    private fun synthesizeShortTone(frequencyHz: Double, durationMs: Long, peakVolume: Float) {
        try {
            val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(128)
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBuf, totalSamples * 2)

            val track = createAudioTrack(bufferSize) ?: return
            val shortBuffer = ShortArray(totalSamples)

            val twoPi = 2.0 * PI
            val effectiveFreq = frequencyHz.coerceIn(20.0, 18000.0)
            val radPerSample = twoPi * effectiveFreq / sampleRate

            val attackSamples = (totalSamples * 0.1).toInt().coerceAtLeast(1)
            val decaySamples = (totalSamples * 0.8).toInt().coerceAtLeast(1)

            for (i in 0 until totalSamples) {
                // Envelope window to prevent audio clicks
                val envelope = when {
                    i < attackSamples -> i.toFloat() / attackSamples
                    i > (totalSamples - decaySamples) -> {
                        val decayProgress = (totalSamples - i).toFloat() / decaySamples
                        decayProgress * decayProgress
                    }
                    else -> 1.0f
                }
                val sampleValue = sin(i * radPerSample) * envelope * peakVolume * (if (isMuted) 0f else volume)
                shortBuffer[i] = (sampleValue * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
            }

            track.play()
            track.write(shortBuffer, 0, totalSamples)
            // Short delay to let hardware finish playing
            Thread.sleep(durationMs + 20)
            track.stop()
            track.release()
        } catch (e: Throwable) {
            Log.d(tag, "Short tone failed: ${e.message}")
        }
    }

    private fun streamAudioLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) {
            Log.w(tag, "AudioTrack returned invalid minBufferSize ($minBuf)")
            return
        }

        val trackBufferSize = maxOf(minBuf * 2, bufferFrames * 4)
        val track = createAudioTrack(trackBufferSize) ?: return
        activeAudioTrack = track

        try {
            track.play()
        } catch (e: Throwable) {
            Log.w(tag, "Failed to start AudioTrack: ${e.message}")
            return
        }

        val shortBuffer = ShortArray(bufferFrames)
        var phase = 0.0
        var lfoPhase = 0.0
        val twoPi = 2.0 * PI

        var smoothGain = 0.05f

        while (isPlaying && coroutineScope.coroutineContext.isActive) {
            val mult = octaveMultiplier
            val offset = hardwareCalibrationOffsetHz
            val freqs = activeFrequencies
            val currentTimbre = timbre
            val isLfo = isTidalLfoEnabled
            val targetGain = if (isMuted) 0.0f else volume

            // Target master frequency: if multiple, sum or synthesize the primary resonance
            val baseFreq = if (freqs.isNotEmpty()) freqs[0] else 42.6
            // Effective audible frequency applying octave multiplier and calibration offset
            var effectiveFreq = (baseFreq + offset) * mult
            // Guarantee human-audible spectrum between 20 Hz and 15,000 Hz for device speakers
            if (effectiveFreq < 20.0) {
                effectiveFreq = (effectiveFreq * 8.0).coerceAtLeast(30.0)
            }
            effectiveFreq = effectiveFreq.coerceIn(20.0, 16000.0)

            val radPerSample = twoPi * effectiveFreq / sampleRate
            val lfoRadPerSample = twoPi * 0.085 / sampleRate // 11.76s slow tidal cycle

            for (i in 0 until bufferFrames) {
                // Smooth volume interpolation to prevent stepping clicks
                smoothGain += (targetGain - smoothGain) * 0.005f

                // Synthesize selected timbre
                val rawWave = when (currentTimbre) {
                    WaveformTimbre.PURE_SINE -> {
                        sin(phase)
                    }
                    WaveformTimbre.BEDROCK_HARMONIC -> {
                        0.60 * sin(phase) + 0.28 * sin(2.0 * phase) + 0.12 * sin(3.0 * phase)
                    }
                    WaveformTimbre.TECTONIC_PULSE -> {
                        0.70 * sin(phase) + 0.20 * sin(3.0 * phase) + 0.10 * sin(5.0 * phase)
                    }
                }

                // Tidal LFO amplitude tremolo
                val lfoMultiplier = if (isLfo) {
                    0.75 + 0.25 * sin(lfoPhase)
                } else {
                    1.0
                }

                val finalSample = rawWave * lfoMultiplier * smoothGain * 32767.0
                shortBuffer[i] = finalSample.toInt().coerceIn(-32768, 32767).toShort()

                phase = (phase + radPerSample) % twoPi
                lfoPhase = (lfoPhase + lfoRadPerSample) % twoPi
            }

            try {
                val written = track.write(shortBuffer, 0, bufferFrames)
                if (written < 0) {
                    Log.w(tag, "AudioTrack write error code: $written")
                    break
                }
                WavAudioExporter.feedSamples(shortBuffer, bufferFrames)
            } catch (e: Throwable) {
                Log.w(tag, "AudioTrack write exception: ${e.message}")
                break
            }
        }

        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.release()
        } catch (e: Throwable) {
            Log.d(tag, "Track stop/release error: ${e.message}")
        } finally {
            if (activeAudioTrack === track) {
                activeAudioTrack = null
            }
        }
    }

    private fun createAudioTrack(bufferSizeBytes: Int): AudioTrack? {
        return try {
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
                .setBufferSizeInBytes(bufferSizeBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Throwable) {
            Log.w(tag, "AudioTrack creation exception: ${e.message}")
            null
        }
    }

    companion object {
        val SHARED = HarmonicToneGenerator()
    }
}
