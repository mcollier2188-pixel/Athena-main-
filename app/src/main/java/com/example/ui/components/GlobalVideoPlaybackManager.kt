package com.example.ui.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * YouTube-style centralized Video Playback Manager.
 * Orchestrates playback state, background/docked miniplayer mode,
 * progress tracking, and full-screen expansion across all screens.
 */
object GlobalVideoPlaybackManager {

    private val _activeVideoSource = MutableStateFlow<CinemaVideoSource?>(null)
    val activeVideoSource: StateFlow<CinemaVideoSource?> = _activeVideoSource.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isMiniplayerVisible = MutableStateFlow(false)
    val isMiniplayerVisible: StateFlow<Boolean> = _isMiniplayerVisible.asStateFlow()

    private val _isFullPlayerRequested = MutableStateFlow(false)
    val isFullPlayerRequested: StateFlow<Boolean> = _isFullPlayerRequested.asStateFlow()

    fun playVideo(source: CinemaVideoSource, startMinimized: Boolean = false) {
        _activeVideoSource.value = source
        _isPlaying.value = true
        _currentPositionMs.value = 0L
        _isMiniplayerVisible.value = startMinimized
        _isFullPlayerRequested.value = !startMinimized
    }

    fun minimizeToMiniplayer() {
        if (_activeVideoSource.value != null) {
            _isMiniplayerVisible.value = true
            _isFullPlayerRequested.value = false
        }
    }

    fun expandFullPlayer() {
        if (_activeVideoSource.value != null) {
            _isMiniplayerVisible.value = false
            _isFullPlayerRequested.value = true
        }
    }

    fun consumeFullPlayerRequest() {
        _isFullPlayerRequested.value = false
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun updateProgress(currentMs: Long, totalMs: Long) {
        _currentPositionMs.value = currentMs
        if (totalMs > 0L) {
            _durationMs.value = totalMs
        }
    }

    fun dismissMiniplayer() {
        _isPlaying.value = false
        _isMiniplayerVisible.value = false
        _activeVideoSource.value = null
        _isFullPlayerRequested.value = false
    }
}
