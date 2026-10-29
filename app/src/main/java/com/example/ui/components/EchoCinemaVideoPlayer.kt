package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.VideoPlayerPreferencesManager
import kotlinx.coroutines.delay

open class CinemaVideoSource(
    val uriString: String,
    val title: String,
    val subtitle: String,
    val isUploadedFile: Boolean = false
) {
    class PresetAsset(
        filename: String,
        displayTitle: String,
        displaySubtitle: String
    ) : CinemaVideoSource(
        uriString = "android.resource://com.example/raw/$filename",
        title = displayTitle,
        subtitle = displaySubtitle,
        isUploadedFile = false
    )

    data class UserUpload(
        val contentUri: Uri,
        val filename: String
    ) : CinemaVideoSource(
        uriString = contentUri.toString(),
        title = filename,
        subtitle = "User Uploaded Media",
        isUploadedFile = true
    )
}

/**
 * EchoCinema In-App Hardware-Accelerated Video Player.
 *
 * Enhanced with:
 * 1. 90° Video Rotation Transform (0°, 90°, 180°, 270°) with aspect-ratio compensation.
 * 2. Double-tap detection to smoothly toggle between portrait (0°) and landscape (90°).
 * 3. Settings Preferences integration: respects user toggle for double-tap rotation, default orientation,
 *    and gesture hint badges.
 * 4. Explicit input labels and interactive control pills.
 */
@Composable
fun EchoCinemaVideoPlayer(
    videoSource: CinemaVideoSource,
    modifier: Modifier = Modifier,
    onVideoCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    remember(context) { VideoPlayerPreferencesManager.initialize(context) }

    val doubleTapRotateEnabled by VideoPlayerPreferencesManager.doubleTapRotateEnabled.collectAsState()
    val defaultOrientationDegrees by VideoPlayerPreferencesManager.defaultOrientationDegrees.collectAsState()
    val gestureHintBadgesVisible by VideoPlayerPreferencesManager.gestureHintBadgesVisible.collectAsState()

    var videoRotationDegrees by remember(defaultOrientationDegrees) { mutableFloatStateOf(defaultOrientationDegrees) }
    var isPlaying by remember(videoSource) { mutableStateOf(false) }
    var currentPositionMs by remember(videoSource) { mutableLongStateOf(0L) }
    var durationMs by remember(videoSource) { mutableLongStateOf(1L) }
    var isMuted by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember(videoSource) { mutableStateOf(true) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    fun toggleRotation() {
        videoRotationDegrees = if (videoRotationDegrees == 0f) 90f else (videoRotationDegrees + 90f) % 360f
    }

    // Auto-hide controls after 3 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    // Polling player position when playing and syncing to global manager
    LaunchedEffect(isPlaying, videoViewRef) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                try {
                    currentPositionMs = vv.currentPosition.toLong()
                    if (vv.duration > 0) {
                        durationMs = vv.duration.toLong()
                    }
                    GlobalVideoPlaybackManager.updateProgress(currentPositionMs, durationMs)
                    GlobalVideoPlaybackManager.setPlaying(true)
                } catch (_: Exception) {}
            }
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, Color(0xFF00D9C0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .pointerInput(doubleTapRotateEnabled) {
                detectTapGestures(
                    onDoubleTap = {
                        if (doubleTapRotateEnabled) {
                            toggleRotation()
                            Toast.makeText(context, "Landscape Toggle: ${videoRotationDegrees.toInt()}°", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
            .testTag("echo_cinema_video_player")
    ) {
        // Rotatable Native Android VideoView
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = videoRotationDegrees
                    if (videoRotationDegrees == 90f || videoRotationDegrees == 270f) {
                        val containerAspect = size.width / size.height.coerceAtLeast(1f)
                        scaleX = 1f / containerAspect.coerceAtLeast(0.7f)
                        scaleY = 1f / containerAspect.coerceAtLeast(0.7f)
                    } else {
                        scaleX = 1.0f
                        scaleY = 1.0f
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setVideoURI(Uri.parse(videoSource.uriString))
                        setOnPreparedListener { mp ->
                            mediaPlayerRef = mp
                            durationMs = mp.duration.toLong()
                            isLoading = false
                            mp.isLooping = true
                            if (isMuted) {
                                mp.setVolume(0f, 0f)
                            } else {
                                mp.setVolume(1f, 1f)
                            }
                            start()
                            isPlaying = true
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            onVideoCompleted()
                        }
                        setOnErrorListener { _, _, _ ->
                            isLoading = false
                            true
                        }
                    }
                },
                update = { vv ->
                    if (vv.tag != videoSource.uriString) {
                        vv.tag = videoSource.uriString
                        isLoading = true
                        isPlaying = false
                        currentPositionMs = 0L
                        try {
                            vv.stopPlayback()
                            vv.setVideoURI(Uri.parse(videoSource.uriString))
                            vv.start()
                            isPlaying = true
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00D9C0),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Loading Footage...",
                        fontSize = 11.sp,
                        color = Color(0xFF00D9C0),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // On-Screen Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar: Title, Badge & Rotate Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = videoSource.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = videoSource.subtitle,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00D9C0),
                                    maxLines = 1
                                )
                                if (gestureHintBadgesVisible) {
                                    Text(
                                        text = "• 2x Tap / Press 🔄 to rotate (${videoRotationDegrees.toInt()}°)",
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // YouTube-style Minimize to Docked Miniplayer
                            IconButton(
                                onClick = {
                                    GlobalVideoPlaybackManager.playVideo(videoSource, startMinimized = true)
                                    Toast.makeText(context, "Minimized to docked player", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFF00D9C0).copy(alpha = 0.25f), CircleShape)
                                    .testTag("cinema_minimize_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimize to Miniplayer",
                                    tint = Color(0xFF00D9C0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 90° Rotate Toggle Button
                            IconButton(
                                onClick = { toggleRotation() },
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFF00D9C0).copy(alpha = 0.25f), CircleShape)
                                    .testTag("cinema_rotate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CropRotate,
                                    contentDescription = "Rotate 90°",
                                    tint = Color(0xFF00D9C0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF00D9C0).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (videoSource.isUploadedFile) "USER VIDEO" else "85.2 Hz TRAILER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00D9C0),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Center Play/Pause button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    if (isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF00D9C0), CircleShape)
                                .testTag("video_player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Bottom Bar: Seekbar & Time & Mute
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPositionMs),
                                fontSize = 10.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(
                                    onClick = {
                                        isMuted = !isMuted
                                        mediaPlayerRef?.let { mp ->
                                            if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                        contentDescription = "Mute",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = formatTime(durationMs),
                                fontSize = 10.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f,
                            onValueChange = { fraction ->
                                val targetMs = (fraction * durationMs).toLong()
                                currentPositionMs = targetMs
                                videoViewRef?.seekTo(targetMs.toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00D9C0),
                                activeTrackColor = Color(0xFF00D9C0),
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(20.dp).testTag("video_progress_slider")
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
