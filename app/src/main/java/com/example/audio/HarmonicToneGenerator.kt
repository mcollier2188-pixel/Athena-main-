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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
 * Streams dynamic 16-bit PCM stereo audio via Android AudioTrack on a dedicated background coroutine.
 * Supports continuous polyphonic frequency synthesis (pure sine, bedrock harmonic, tectonic pulse),
 * true stereo L/R binaural beat separation, octave multiplication, tidal LFO tremolo,
 * and tactile cybernetic UI sound effects.
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
    var isBinauralStereo: Boolean = false
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

    fun updateFrequencies(frequencies: List<Double>, binauralStereo: Boolean = isBinauralStereo) {
        activeFrequencies = if (frequencies.isEmpty()) listOf(42.6) else frequencies
        isBinauralStereo = binauralStereo
    }

    @Synchronized
    fun start(
        frequencies: List<Double> = activeFrequencies,
        mult: Double = octaveMultiplier,
        tidalLfo: Boolean = isTidalLfoEnabled,
        timbreMode: WaveformTimbre = timbre,
        binauralStereo: Boolean = false
    ) {
        activeFrequencies = if (frequencies.isEmpty()) listOf(42.6) else frequencies
        octaveMultiplier = mult.coerceAtLeast(0.1)
        isTidalLfoEnabled = tidalLfo
        timbre = timbreMode
        isBinauralStereo = binauralStereo

        // If already actively streaming, update live @Volatile parameters smoothly without
        // tearing down and reallocating the native AudioTrack (prevents slider drag clicks/thrashing).
        if (isPlaying && playbackJob?.isActive == true) {
            return
        }

        isPlaying = true
        _isPlayingState.value = true

        playbackJob?.cancel()
        playbackJob = coroutineScope.launch {
            streamAudioLoop()
        }
    }

    /**
     * Starts true stereo binaural beat synthesis (Left channel = [leftHz], Right channel = [rightHz]).
     */
    fun startBinaural(
        leftHz: Double,
        rightHz: Double,
        mult: Double = 1.0,
        timbreMode: WaveformTimbre = WaveformTimbre.PURE_SINE
    ) {
        start(
            frequencies = listOf(leftHz, rightHz),
            mult = mult,
            tidalLfo = false,
            timbreMode = timbreMode,
            binauralStereo = true
        )
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        _isPlayingState.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    /**
     * Releases all coroutines and audio resources for non-singleton instances.
     */
    fun close() {
        stop()
        if (this !== SHARED) {
            coroutineScope.cancel()
        }
    }

    /**
     * Plays a brief, crisp tactile UI sound chime (tap, confirmation, Solfeggio bell, alert).
     */
    fun playChime(effect: SoundEffectType = SoundEffectType.CHIME) {
        if (isMuted) return
        if (effect == SoundEffectType.TAP && !isButtonSoundEnabled) return
        coroutineScope.launch(Dispatchers.IO) {
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
        coroutineScope.launch(Dispatchers.IO) {
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

    private suspend fun synthesizeShortTone(frequencyHz: Double, durationMs: Long, peakVolume: Float) = withContext(Dispatchers.IO) {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(128)
        val bufferSizeBytes = totalSamples * 2
        val track = createMonoStaticAudioTrack(bufferSizeBytes) ?: return@withContext

        try {
            val shortBuffer = ShortArray(totalSamples)
            val twoPi = 2.0 * PI
            val effectiveFreq = frequencyHz.coerceIn(20.0, 18000.0)
            val radPerSample = twoPi * effectiveFreq / sampleRate

            val attackSamples = (totalSamples * 0.1).toInt().coerceAtLeast(1)
            val decaySamples = (totalSamples * 0.8).toInt().coerceAtLeast(1)

            for (i in 0 until totalSamples) {
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

            val written = track.write(shortBuffer, 0, totalSamples)
            if (written > 0) {
                track.play()
                delay(durationMs + 25)
            }
        } catch (e: Throwable) {
            Log.d(tag, "Short tone failed: ${e.message}")
        } finally {
            runCatching {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
            }
            runCatching { track.release() }
        }
    }

    private fun evalTimbreWave(phase: Double, mode: WaveformTimbre): Double {
        return when (mode) {
            WaveformTimbre.PURE_SINE -> sin(phase)
            WaveformTimbre.BEDROCK_HARMONIC ->
                0.60 * sin(phase) + 0.28 * sin(2.0 * phase) + 0.12 * sin(3.0 * phase)
            WaveformTimbre.TECTONIC_PULSE ->
                0.70 * sin(phase) + 0.20 * sin(3.0 * phase) + 0.10 * sin(5.0 * phase)
        }
    }

    private suspend fun streamAudioLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) {
            Log.w(tag, "AudioTrack returned invalid minBufferSize ($minBuf)")
            return
        }

        val trackBufferSize = maxOf(minBuf * 2, bufferFrames * 8)
        val track = createStereoStreamAudioTrack(trackBufferSize) ?: return

        try {
            track.play()

            val stereoBuffer = ShortArray(bufferFrames * 2)
            val monoExportBuffer = ShortArray(bufferFrames)
            var phases = DoubleArray(8)
            var lfoPhase = 0.0
            val twoPi = 2.0 * PI
            val lfoRadPerSample = twoPi * 0.085 / sampleRate // 11.76s slow tidal cycle
            var smoothGain = 0.05f

            while (isPlaying && currentCoroutineContext().isActive) {
                val mult = octaveMultiplier
                val offset = hardwareCalibrationOffsetHz
                val freqs = activeFrequencies.ifEmpty { listOf(42.6) }
                val binaural = isBinauralStereo && freqs.size >= 2
                val currentTimbre = timbre
                val isLfo = isTidalLfoEnabled
                val targetGain = if (isMuted) 0.0f else volume

                val partialCount = freqs.size.coerceAtMost(16)
                if (phases.size < partialCount) {
                    phases = phases.copyOf(partialCount)
                }

                val radPerSampleByPartial = DoubleArray(partialCount) { idx ->
                    val rawHz = (freqs[idx] + offset) * mult
                    val clampedHz = if (rawHz <= 0.0) 0.0 else rawHz.coerceIn(1.0, 16000.0)
                    twoPi * clampedHz / sampleRate
                }
                val chordNorm = 1.0 / partialCount.coerceAtLeast(1)

                for (i in 0 until bufferFrames) {
                    smoothGain += (targetGain - smoothGain) * 0.005f

                    val lfoMultiplier = if (isLfo) {
                        0.75 + 0.25 * sin(lfoPhase)
                    } else {
                        1.0
                    }

                    val leftSample: Double
                    val rightSample: Double

                    if (binaural) {
                        val rawL = evalTimbreWave(phases[0], currentTimbre)
                        val rawR = evalTimbreWave(phases[1], currentTimbre)
                        phases[0] = (phases[0] + radPerSampleByPartial[0]) % twoPi
                        phases[1] = (phases[1] + radPerSampleByPartial[1]) % twoPi
                        leftSample = rawL * lfoMultiplier * smoothGain * 32767.0
                        rightSample = rawR * lfoMultiplier * smoothGain * 32767.0
                    } else {
                        var sum = 0.0
                        for (p in 0 until partialCount) {
                            sum += evalTimbreWave(phases[p], currentTimbre)
                            phases[p] = (phases[p] + radPerSampleByPartial[p]) % twoPi
                        }
                        val monoSample = sum * chordNorm * lfoMultiplier * smoothGain * 32767.0
                        leftSample = monoSample
                        rightSample = monoSample
                    }

                    val sL = leftSample.toInt().coerceIn(-32768, 32767).toShort()
                    val sR = rightSample.toInt().coerceIn(-32768, 32767).toShort()
                    stereoBuffer[i * 2] = sL
                    stereoBuffer[i * 2 + 1] = sR
                    monoExportBuffer[i] = ((sL.toInt() + sR.toInt()) / 2).toShort()

                    lfoPhase = (lfoPhase + lfoRadPerSample) % twoPi
                }

                val written = track.write(stereoBuffer, 0, stereoBuffer.size)
                if (written < 0) {
                    Log.w(tag, "AudioTrack write error code: $written")
                    break
                }
                WavAudioExporter.feedSamples(monoExportBuffer, bufferFrames)
            }
        } catch (e: Throwable) {
            Log.w(tag, "AudioTrack stream notice: ${e.message}")
        } finally {
            runCatching {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
            }
            runCatching { track.release() }
        }
    }

    private fun createStereoStreamAudioTrack(bufferSizeBytes: Int): AudioTrack? {
        return try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
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

    private fun createMonoStaticAudioTrack(bufferSizeBytes: Int): AudioTrack? {
        return try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        } catch (e: Throwable) {
            Log.w(tag, "Static AudioTrack creation exception: ${e.message}")
            null
        }
    }

    companion object {
        val SHARED = HarmonicToneGenerator()
    }
}
