package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TouchApp
import com.example.data.VideoPlayerPreferencesManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.delay
import java.util.Locale

enum class MediaType {
    PHOTO,
    VIDEO
}

data class MediaItem(
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val sizeBytes: Long = 0L,
    val durationSeconds: Int = 0
)

/**
 * Dedicated In-App Photo & Video Media Player Dialog.
 *
 * Capabilities:
 * 1. Supports viewing user photos (local gallery & Google Photos via zero-permission Android Photo Picker).
 * 2. Dedicated in-app video player with playback, timeline scrubbing, audio mute/unmute.
 * 3. Video-Only Rotation: Rotates specifically the video canvas (0°, 90°, 180°, 270°) without
 *    rotating the rest of the application or forcing system orientation shifts.
 * 4. Immersive Fullscreen Toggle: Expands playback edge-to-edge with auto-hiding overlays.
 * 5. Direct export/attachment to Workspace notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerDialog(
    mediaUri: Uri?,
    mediaType: MediaType,
    mediaTitle: String = "Media Viewer",
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (mediaUri == null) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    remember(context) { VideoPlayerPreferencesManager.initialize(context) }
    val doubleTapRotateEnabled by VideoPlayerPreferencesManager.doubleTapRotateEnabled.collectAsState()
    val defaultOrientationDegrees by VideoPlayerPreferencesManager.defaultOrientationDegrees.collectAsState()
    val gestureHintBadgesVisible by VideoPlayerPreferencesManager.gestureHintBadgesVisible.collectAsState()

    var videoRotationDegrees by remember(defaultOrientationDegrees) { mutableFloatStateOf(defaultOrientationDegrees) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showOverlayControls by remember { mutableStateOf(true) }

    fun toggleVideoRotation() {
        toneGenerator.playChime(SoundEffectType.TAP)
        videoRotationDegrees = if (videoRotationDegrees == 0f) 90f else (videoRotationDegrees + 90f) % 360f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (isFullscreen) {
            // =========================================================================
            // IMMERSIVE FULLSCREEN MODE (Edge-to-Edge with auto-hiding floating controls)
            // =========================================================================
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .testTag("media_fullscreen_surface"),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showOverlayControls = !showOverlayControls },
                    contentAlignment = Alignment.Center
                ) {
                    // Visual Viewport Container
                    if (mediaType == MediaType.PHOTO) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = mediaTitle,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = videoRotationDegrees
                                }
                                .testTag("photo_viewer_image_fullscreen")
                        )
                    } else {
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
                                }
                                .testTag("video_player_container_fullscreen"),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(mediaUri)
                                        setOnPreparedListener { mp ->
                                            totalDurationMs = mp.duration.toLong()
                                            mp.isLooping = true
                                            if (isMuted) mp.setVolume(0f, 0f)
                                            start()
                                            isPlaying = true
                                        }
                                        setOnCompletionListener { isPlaying = false }
                                        videoViewRef = this
                                    }
                                },
                                update = { view ->
                                    videoViewRef = view
                                    if (isPlaying) {
                                        if (!view.isPlaying) view.start()
                                    } else {
                                        if (view.isPlaying) view.pause()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Periodic duration & progress tracker
                        LaunchedEffect(isPlaying) {
                            while (isPlaying) {
                                delay(300)
                                videoViewRef?.let { vv ->
                                    currentPositionMs = vv.currentPosition.toLong()
                                    if (totalDurationMs <= 0 && vv.duration > 0) {
                                        totalDurationMs = vv.duration.toLong()
                                    }
                                }
                            }
                        }
                    }

                    // Floating Double-Tap Gesture Sensor Pill
                    if (doubleTapRotateEnabled) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(width = 64.dp, height = 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            toggleVideoRotation()
                                            Toast.makeText(context, "Landscape Toggle: ${videoRotationDegrees.toInt()}°", Toast.LENGTH_SHORT).show()
                                        },
                                        onTap = {
                                            showOverlayControls = !showOverlayControls
                                        }
                                    )
                                }
                                .testTag("media_fullscreen_gesture_detector_strip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ScreenRotation,
                                    contentDescription = "Double-Tap to Rotate",
                                    tint = EchoCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "2x TAP\nROTATE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }

                    // Floating Top Bar Overlay
                    AnimatedVisibility(
                        visible = showOverlayControls,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (mediaType == MediaType.VIDEO) Icons.Default.Movie else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = EchoCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = mediaTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Orientation: ${videoRotationDegrees.toInt()}°",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = EchoCyan
                                        )
                                        if (gestureHintBadgesVisible) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(EchoGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(10.dp))
                                                    Text("Interactive Player", color = EchoGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 90° Rotate Button with clear label
                                Button(
                                    onClick = { toggleVideoRotation() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (videoRotationDegrees != 0f) EchoGreen else EchoDarkSurface,
                                        contentColor = if (videoRotationDegrees != 0f) EchoOnCyan else EchoCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("rotate_video_fullscreen_button")
                                ) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${videoRotationDegrees.toInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Exit Fullscreen
                                Button(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        isFullscreen = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EchoDarkSurface,
                                        contentColor = EchoCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("exit_fullscreen_media_button")
                                ) {
                                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Exit", fontSize = 11.sp)
                                }

                                // Close Dialog
                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        onDismiss()
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Floating Bottom Controls Bar (For Video)
                    if (mediaType == MediaType.VIDEO) {
                        AnimatedVisibility(
                            visible = showOverlayControls,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Timeline Scrubber
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val curSec = (currentPositionMs / 1000).toInt()
                                    val totSec = (totalDurationMs / 1000).toInt().coerceAtLeast(1)

                                    Text(
                                        text = formatTimestamp(curSec),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoCyan
                                    )

                                    Slider(
                                        value = if (totSec > 0) curSec.toFloat() / totSec else 0f,
                                        onValueChange = { frac ->
                                            val newPos = (frac * totalDurationMs).toInt()
                                            videoViewRef?.seekTo(newPos)
                                            currentPositionMs = newPos.toLong()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = EchoCyan,
                                            activeTrackColor = EchoCyan,
                                            inactiveTrackColor = EchoDarkSurface
                                        )
                                    )

                                    Text(
                                        text = formatTimestamp(totSec),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoMuted
                                    )
                                }

                                // Playback action row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                isPlaying = !isPlaying
                                                videoViewRef?.let { vv ->
                                                    if (isPlaying) vv.start() else vv.pause()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isPlaying) EchoCyan else EchoDarkSurface,
                                                contentColor = if (isPlaying) EchoOnCyan else EchoText
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isPlaying) "PAUSE" else "PLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                videoViewRef?.seekTo(0)
                                                currentPositionMs = 0L
                                                if (!isPlaying) {
                                                    isPlaying = true
                                                    videoViewRef?.start()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Replay, contentDescription = "Restart", modifier = Modifier.size(14.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                isMuted = !isMuted
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                                tint = if (isMuted) EchoRed else EchoCyan
                                            )
                                        }
                                    }

                                    if (onAttachToWorkspaceNote != null) {
                                        Button(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                onAttachToWorkspaceNote(mediaTitle, mediaUri)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = EchoCyan,
                                                contentColor = EchoOnCyan
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Save to Notes", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // STANDARD MODAL DIALOG MODE (Framed Card with Controls, Rotate & Fullscreen)
            // =========================================================================
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EchoBg),
                color = EchoBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Bar with Title, Video Rotation Button, Fullscreen Button, and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (mediaType == MediaType.VIDEO) Icons.Default.Movie else Icons.Default.Image,
                                contentDescription = null,
                                tint = EchoCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = mediaTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                                Text(
                                    text = if (mediaType == MediaType.VIDEO) "DEDICATED VIDEO PLAYER" else "HIGH-RES PHOTO VIEWER",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = EchoCyan
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // VIDEO-ONLY ROTATION BUTTON (Cycles 0° -> 90° -> 180° -> 270°)
                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    videoRotationDegrees = (videoRotationDegrees + 90f) % 360f
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (videoRotationDegrees != 0f) EchoGreen else EchoDarkSurface,
                                    contentColor = if (videoRotationDegrees != 0f) EchoOnCyan else EchoCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("rotate_video_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate Video Only",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${videoRotationDegrees.toInt()}°",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Fullscreen Toggle Button
                            IconButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    isFullscreen = true
                                },
                                modifier = Modifier.testTag("fullscreen_media_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = EchoCyan
                                )
                            }

                            IconButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onDismiss()
                                },
                                modifier = Modifier.testTag("media_dialog_close")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = EchoMuted
                                )
                            }
                        }
                    }

                    // Interactive Gesture & Input Guide Pill
                    if (gestureHintBadgesVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoBgAlt)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (mediaType == MediaType.VIDEO) "Interactive Video Player Active" else "High-Res Image Viewer",
                                    fontSize = 10.sp,
                                    color = EchoGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(12.dp))
                                Text("Double-tap or Press to Rotate", fontSize = 10.sp, color = EchoMuted)
                            }
                        }
                    }

                    HorizontalDivider(
                        color = EchoCardStroke,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Main Media Viewport Box with Double-Tap Rotation Detector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                            .pointerInput(doubleTapRotateEnabled) {
                                if (doubleTapRotateEnabled) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            toggleVideoRotation()
                                            Toast.makeText(context, "Landscape Toggle: ${videoRotationDegrees.toInt()}°", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (mediaType == MediaType.PHOTO) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(mediaUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = mediaTitle,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationZ = videoRotationDegrees
                                    }
                                    .testTag("photo_viewer_image")
                            )
                        } else {
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
                                    }
                                    .testTag("video_player_container"),
                                contentAlignment = Alignment.Center
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoURI(mediaUri)
                                            setOnPreparedListener { mp ->
                                                totalDurationMs = mp.duration.toLong()
                                                mp.isLooping = true
                                                if (isMuted) {
                                                    mp.setVolume(0f, 0f)
                                                }
                                                start()
                                                isPlaying = true
                                            }
                                            setOnCompletionListener {
                                                isPlaying = false
                                            }
                                            videoViewRef = this
                                        }
                                    },
                                    update = { view ->
                                        videoViewRef = view
                                        if (isPlaying) {
                                            if (!view.isPlaying) view.start()
                                        } else {
                                            if (view.isPlaying) view.pause()
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Periodic duration & progress tracker
                            LaunchedEffect(isPlaying) {
                                while (isPlaying) {
                                    delay(300)
                                    videoViewRef?.let { vv ->
                                        currentPositionMs = vv.currentPosition.toLong()
                                        if (totalDurationMs <= 0 && vv.duration > 0) {
                                            totalDurationMs = vv.duration.toLong()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Video Controls Bar (Only for Video playback)
                    if (mediaType == MediaType.VIDEO) {
                        Spacer(Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EchoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Timeline Scrubber
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val curSec = (currentPositionMs / 1000).toInt()
                                    val totSec = (totalDurationMs / 1000).toInt().coerceAtLeast(1)

                                    Text(
                                        text = formatTimestamp(curSec),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoCyan
                                    )

                                    Slider(
                                        value = if (totSec > 0) curSec.toFloat() / totSec else 0f,
                                        onValueChange = { frac ->
                                            val newPos = (frac * totalDurationMs).toInt()
                                            videoViewRef?.seekTo(newPos)
                                            currentPositionMs = newPos.toLong()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = EchoCyan,
                                            activeTrackColor = EchoCyan,
                                            inactiveTrackColor = EchoDarkSurface
                                        )
                                    )

                                    Text(
                                        text = formatTimestamp(totSec),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoMuted
                                    )
                                }

                                // Playback & Sound Control Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Play / Pause Toggle
                                        Button(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                isPlaying = !isPlaying
                                                videoViewRef?.let { vv ->
                                                    if (isPlaying) vv.start() else vv.pause()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isPlaying) EchoCyan else EchoDarkSurface,
                                                contentColor = if (isPlaying) EchoOnCyan else EchoText
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isPlaying) "PAUSE" else "PLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Replay / Rewind
                                        OutlinedButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                videoViewRef?.seekTo(0)
                                                currentPositionMs = 0L
                                                if (!isPlaying) {
                                                    isPlaying = true
                                                    videoViewRef?.start()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Replay, contentDescription = "Restart", modifier = Modifier.size(14.dp))
                                        }

                                        // Mute / Unmute
                                        IconButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                isMuted = !isMuted
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                                tint = if (isMuted) EchoRed else EchoCyan
                                            )
                                        }
                                    }

                                    // Rotation status badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CropRotate,
                                            contentDescription = null,
                                            tint = EchoGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Video Orientation: ${videoRotationDegrees.toInt()}°",
                                            fontSize = 11.sp,
                                            color = EchoGreen,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Bottom Action: Attach to Workspace Note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onAttachToWorkspaceNote != null) {
                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                    onAttachToWorkspaceNote(mediaTitle, mediaUri)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("attach_media_to_note_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoCyan,
                                    contentColor = EchoOnCyan
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("SAVE TO WORKSPACE NOTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CLOSE", fontSize = 11.sp, color = EchoText)
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
            videoViewRef = null
        }
    }
}

private fun formatTimestamp(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d", m, s)
}
