package com.example.audio

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WAV Audio Exporter & Real-Time PCM Recorder for ATHENA Synthesizer.
 * Encodes 16-bit PCM mono acoustic frequency sweeps and resonance carriers
 * into standard uncompressed RIFF/WAVE (.wav) files for local storage & Android sharing.
 */
object WavAudioExporter {
    private const val TAG = "WavAudioExporter"
    private const val SAMPLE_RATE = 44100
    private const val CHANNELS = 1
    private const val BITS_PER_SAMPLE = 16

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val recordedSamples = ArrayList<Short>(SAMPLE_RATE * 60) // Up to ~60s initial capacity
    private val bufferLock = Any()
    private var timerJob: Job? = null

    /**
     * Start recording real-time synthesized PCM samples.
     */
    fun startRecording() {
        synchronized(bufferLock) {
            recordedSamples.clear()
            _isRecording.value = true
            _elapsedSeconds.value = 0
        }

        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _isRecording.value) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
        Log.i(TAG, "Acoustic WAV recording started.")
    }

    /**
     * Feed PCM short samples into the recording buffer while audio is streaming.
     */
    fun feedSamples(samples: ShortArray, count: Int) {
        if (!_isRecording.value) return
        synchronized(bufferLock) {
            // Safety limit: up to 10 minutes (44100 * 60 * 10 samples)
            if (recordedSamples.size < SAMPLE_RATE * 600) {
                for (i in 0 until count) {
                    recordedSamples.add(samples[i])
                }
            }
        }
    }

    /**
     * Stop recording and export the captured buffer into a valid .wav file in the exports directory.
     */
    fun stopAndExport(context: Context, frequencyLabel: String = "42.6Hz"): File? {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false

        val snapshot: ShortArray
        synchronized(bufferLock) {
            if (recordedSamples.isEmpty()) {
                Log.w(TAG, "Recorded buffer was empty; export cancelled.")
                return null
            }
            snapshot = ShortArray(recordedSamples.size) { recordedSamples[it] }
            recordedSamples.clear()
        }

        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val safeLabel = frequencyLabel.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val wavFile = File(exportDir, "Athena_${safeLabel}_$timeStamp.wav")

            writeWavFile(wavFile, snapshot, SAMPLE_RATE)
            Log.i(TAG, "Exported WAV: ${wavFile.absolutePath} (${snapshot.size} samples, ${wavFile.length()} bytes)")
            wavFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write WAV file: ${e.message}", e)
            null
        }
    }

    /**
     * Encodes 16-bit PCM ShortArray to standard RIFF/WAVE file format.
     */
    fun writeWavFile(outputFile: File, samples: ShortArray, sampleRate: Int = SAMPLE_RATE) {
        val totalAudioLen = (samples.size * 2).toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * CHANNELS * BITS_PER_SAMPLE / 8).toLong()

        FileOutputStream(outputFile).use { out ->
            writeWavHeader(out, totalAudioLen, totalDataLen, sampleRate.toLong(), CHANNELS, byteRate)

            val byteBuffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in samples) {
                byteBuffer.putShort(sample)
            }
            out.write(byteBuffer.array())
            out.flush()
        }
    }

    private fun writeWavHeader(
        out: OutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        longSampleRate: Long,
        channels: Int,
        byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte() // block align
        header[33] = 0
        header[34] = BITS_PER_SAMPLE.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }

    /**
     * Shares the exported WAV file using Android standard ACTION_SEND and FileProvider.
     */
    fun shareWavFile(context: Context, file: File, title: String = "ATHENA Acoustic WAV Tone") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Synthesized acoustic tone recorded with ATHENA Echo-Interface.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share WAV Audio"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share WAV file: ${e.message}", e)
        }
    }
}
