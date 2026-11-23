package com.example.ai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Native Android voice module for Dola (Athena AI Suite).
 * Implements Android native TextToSpeech and SpeechRecognizer matching the
 * voice and speech recognition specifications.
 */
class DolaVoiceManager private constructor(private val appContext: Context) {

    companion object {
        @Volatile
        private var instance: DolaVoiceManager? = null

        fun getInstance(context: Context): DolaVoiceManager {
            return instance ?: synchronized(this) {
                instance ?: DolaVoiceManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val utteranceCounter = AtomicLong(0L)
    private val utteranceCallbacks = ConcurrentHashMap<String, () -> Unit>()

    @Volatile
    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady = false
    @Volatile
    private var isTtsInitializing = false
    private var pendingSpeech: Pair<String, (() -> Unit)?>? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _rmsState = MutableStateFlow(0f)
    val rmsState: StateFlow<Float> = _rmsState.asStateFlow()

    private val voiceCalibration by lazy { VoiceCalibration.getInstance(appContext) }

    val speechRate: Float
        get() = voiceCalibration.rate
    val speechPitch: Float
        get() = voiceCalibration.pitch
    val speechVolume: Float
        get() = voiceCalibration.volume

    init {
        initTts()
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    fun setVoicePersona(rate: Float, pitch: Float, volume: Float = speechVolume) {
        voiceCalibration.calibrate(pitch = pitch, rate = rate, volume = volume)
        tts?.setSpeechRate(voiceCalibration.rate)
        tts?.setPitch(voiceCalibration.pitch)
    }

    /**
     * Saves the calibrated speech rate, pitch, and volume globally to VoiceCalibration
     * so that all speech modules (Ask Dola, Voice Journal, Perspective Weaver,
     * Council narrations) automatically adopt these values.
     */
    fun saveGlobalCalibration(rate: Float, pitch: Float, volume: Float = 1.0f) {
        voiceCalibration.calibrate(pitch = pitch, rate = rate, volume = volume)
        tts?.setSpeechRate(voiceCalibration.rate)
        tts?.setPitch(voiceCalibration.pitch)
    }

    @Synchronized
    private fun initTts() {
        if (isTtsReady && tts != null) return
        if (isTtsInitializing) return
        isTtsInitializing = true

        tts = TextToSpeech(appContext) { status ->
            isTtsInitializing = false
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    // Set British English (en-GB) voice preference matching Dola specifications
                    val locale = Locale.UK
                    val result = engine.setLanguage(locale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.language = Locale.US
                    }

                    // Warm, clear voice: rate = 0.95, pitch = 1.05
                    engine.setSpeechRate(speechRate)
                    engine.setPitch(speechPitch)

                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            utteranceId?.let { id ->
                                utteranceCallbacks.remove(id)?.let { cb -> runOnMain(cb) }
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            utteranceId?.let { id ->
                                utteranceCallbacks.remove(id)?.let { cb -> runOnMain(cb) }
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                            utteranceId?.let { id ->
                                utteranceCallbacks.remove(id)?.let { cb -> runOnMain(cb) }
                            }
                        }
                    })

                    isTtsReady = true
                    val queued = synchronized(this@DolaVoiceManager) {
                        pendingSpeech.also { pendingSpeech = null }
                    }
                    if (queued != null) {
                        dispatchSpeak(engine, queued.first, queued.second)
                    }
                }
            } else {
                isTtsReady = false
                _isSpeaking.value = false
                val queued = synchronized(this@DolaVoiceManager) {
                    pendingSpeech.also { pendingSpeech = null }
                }
                queued?.second?.let { cb -> runOnMain(cb) }
            }
        }
    }

    private fun dispatchSpeak(engine: TextToSpeech, text: String, onFinished: (() -> Unit)?) {
        val safeText = text.trim().take(TextToSpeech.getMaxSpeechInputLength().coerceAtMost(3900))
        if (safeText.isBlank()) {
            _isSpeaking.value = false
            onFinished?.let { runOnMain(it) }
            return
        }
        engine.setSpeechRate(speechRate)
        engine.setPitch(speechPitch)
        val utteranceId = "dola_${System.currentTimeMillis()}_${utteranceCounter.incrementAndGet()}"
        if (onFinished != null) {
            utteranceCallbacks[utteranceId] = onFinished
        }
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, speechVolume)
        }
        _isSpeaking.value = true
        val res = engine.speak(safeText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (res == TextToSpeech.ERROR) {
            _isSpeaking.value = false
            utteranceCallbacks.remove(utteranceId)?.let { runOnMain(it) }
        }
    }

    /**
     * Speaks the given text with Dola's tuned voice profile.
     */
    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        val engine = tts
        if (!isTtsReady || engine == null) {
            synchronized(this) {
                pendingSpeech = text to onFinished
            }
            initTts()
            return
        }
        dispatchSpeak(engine, text, onFinished)
    }

    /**
     * Stops any ongoing speech.
     */
    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        val callbacks = utteranceCallbacks.values.toList()
        utteranceCallbacks.clear()
        callbacks.forEach { cb -> runOnMain(cb) }
    }

    /**
     * Starts listening for voice speech from the user.
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onError("Microphone permission required")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onError("Speech recognition not available on this device")
            return
        }

        stopSpeaking()

        runOnMain {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {
                            _rmsState.value = rmsdB
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                                SpeechRecognizer.ERROR_NETWORK -> "Network connection required"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timeout"
                                else -> "Speech recognition error ($error)"
                            }
                            onError(errorMsg)
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spoken = matches?.firstOrNull() ?: ""
                            if (spoken.isNotBlank()) {
                                _lastRecognizedText.value = spoken
                                onResult(spoken)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partial = partialMatches?.firstOrNull() ?: ""
                            if (partial.isNotBlank()) {
                                _lastRecognizedText.value = partial
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
                onError("Unable to start speech recognizer: ${e.localizedMessage ?: "unknown error"}")
            }
        }
    }

    /**
     * Stops speech recognition.
     */
    fun stopListening() {
        runOnMain {
            runCatching { speechRecognizer?.stopListening() }
            _isListening.value = false
        }
    }

    @Synchronized
    fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        isTtsReady = false
        isTtsInitializing = false
        pendingSpeech = null
        utteranceCallbacks.clear()
        _isSpeaking.value = false

        runOnMain {
            runCatching { speechRecognizer?.destroy() }
            speechRecognizer = null
            _isListening.value = false
        }
    }
}
