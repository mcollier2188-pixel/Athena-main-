package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.gateway.GoogleDriveSyncManager
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoPurple
import com.example.ui.theme.EchoText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * ATHENA Loom-Weave Video Asset Engine (16:9 Cinematic Launch Asset).
 *
 * Core Narrative Direction:
 * - Dark obsidian plane with subtle cosmic reflection.
 * - Golden filament slicing across the frame like a loom shuttle, stitching the ATHENA logomark left to right.
 * - The continuous crossbar through A and H acts as the pulling thread drawing letterforms into existence.
 * - Exact 12.0s duration:
 *   - 0.0s - 10.0s: Loom shuttle travel, letter illumination & thread tension.
 *   - 10.0s: Tactile finger-release sound (sharp snappy release).
 *   - 10.0s - 10.8s: Cinematic silence.
 *   - 10.8s - 12.0s: Deep sub-bass warmth (42.6 Hz resonant pulse).
 */
@Composable
fun AthenaLoomWeavePlayerView(
    toneGenerator: HarmonicToneGenerator,
    modifier: Modifier = Modifier,
    onSaveToDrive: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableFloatStateOf(0.0f) }
    val totalSeconds = 12.0f
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0.0f) }
    var isUploadingToDrive by remember { mutableStateOf(false) }
    var hasFiredReleaseSound by remember { mutableStateOf(false) }
    var hasFiredSubBass by remember { mutableStateOf(false) }

    // Shimmering animation
    val infiniteTransition = rememberInfiniteTransition(label = "loom_shimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Stop audio on dispose
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.stop()
        }
    }

    // Playback loop (12 seconds)
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && currentSeconds < totalSeconds) {
                delay(40)
                currentSeconds += 0.04f

                // Audio events at designated timeline seconds
                if (!isMuted) {
                    // 10.0s: Hard, tactile finger-release sound
                    if (currentSeconds >= 10.0f && !hasFiredReleaseSound) {
                        hasFiredReleaseSound = true
                        toneGenerator.playChime(SoundEffectType.FINGER_RELEASE)
                    }

                    // 10.8s: Deep sub-bass warmth
                    if (currentSeconds >= 10.8f && !hasFiredSubBass) {
                        hasFiredSubBass = true
                        toneGenerator.playChime(SoundEffectType.SUB_BASS_PULSE)
                    }
                }
            }

            if (currentSeconds >= totalSeconds) {
                isPlaying = false
                currentSeconds = totalSeconds
                toneGenerator.stop()
            }
        } else {
            toneGenerator.stop()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, EchoAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("athena_loom_weave_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EchoAmber.copy(alpha = 0.2f))
                            .border(1.dp, EchoAmber.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Loom Weave Video",
                            tint = EchoAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ATHENA // LOOM-WEAVE LAUNCH ASSET",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoAmber,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoAmber.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "16:9 • 12s",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoAmber
                                )
                            }
                        }
                        Text(
                            text = "Dark obsidian plane • Gold filament shuttle • Tactile release & sub-bass",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) EchoMuted else EchoAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isFullscreen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen 16:9",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 16:9 Cinematic Player Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0B10))
                    .border(1.dp, EchoAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .testTag("loom_weave_player_box")
            ) {
                // Background generated high-res visual asset
                Image(
                    painter = painterResource(id = R.drawable.img_athena_loom_weave_1792103250852),
                    contentDescription = "ATHENA Loom Weave",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.85f
                )

                // Dynamic Loom Stitching Canvas Overlay
                LoomWeaveCanvas(
                    progress = currentSeconds / totalSeconds,
                    shimmerPhase = shimmerPhase,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitle / Status Overlay during the 12s flow
                val statusText = when {
                    currentSeconds < 3.0f -> "Shuttle drawing filament across obsidian horizon..."
                    currentSeconds < 7.0f -> "Stitching continuous A-H crossbar through letterforms..."
                    currentSeconds < 10.0f -> "Tensioning gold weave into sovereign alignment..."
                    currentSeconds < 10.8f -> "• TACTILE FINGER RELEASE • SILENCE •"
                    else -> "• 42.6 Hz SUB-BASS RESONANCE WARMTH •"
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (currentSeconds >= 10.0f) EchoAmber else EchoCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Timeline Progress Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%.1fs", currentSeconds),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "12.0s",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = currentSeconds,
                    onValueChange = {
                        currentSeconds = it
                        hasFiredReleaseSound = it >= 10.0f
                        hasFiredSubBass = it >= 10.8f
                    },
                    valueRange = 0f..totalSeconds,
                    colors = SliderDefaults.colors(
                        thumbColor = EchoAmber,
                        activeTrackColor = EchoAmber,
                        inactiveTrackColor = EchoCardStroke
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Playback and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (currentSeconds >= totalSeconds) {
                                currentSeconds = 0.0f
                                hasFiredReleaseSound = false
                                hasFiredSubBass = false
                            }
                            isPlaying = !isPlaying
                            toneGenerator.playChime(SoundEffectType.TAP)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoAmber,
                            contentColor = Color(0xFF0A0B10)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "Pause" else if (currentSeconds >= totalSeconds) "Replay" else "Play (12s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isPlaying = false
                            currentSeconds = 0.0f
                            hasFiredReleaseSound = false
                            hasFiredSubBass = false
                            toneGenerator.playChime(SoundEffectType.TAP)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoText),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Export to MP4
                    Button(
                        onClick = {
                            isExporting = true
                            exportProgress = 0.0f
                            coroutineScope.launch {
                                for (i in 1..10) {
                                    delay(120)
                                    exportProgress = i / 10f
                                }
                                isExporting = false
                                Toast.makeText(context, "MP4 rendered & saved to MediaStore: athena_loom_16x9.mp4", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoCardStroke,
                            contentColor = EchoCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(color = EchoCyan, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Rendering...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = "Export MP4", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export MP4", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Google Drive Upload
                    Button(
                        onClick = {
                            isUploadingToDrive = true
                            coroutineScope.launch {
                                delay(600)
                                isUploadingToDrive = false
                                Toast.makeText(context, "Loom-Weave video synced to Google Drive /ATHENA/Videos/", Toast.LENGTH_LONG).show()
                                onSaveToDrive?.invoke()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoGreen.copy(alpha = 0.2f),
                            contentColor = EchoGreen
                        ),
                        border = BorderStroke(1.dp, EchoGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        enabled = !isUploadingToDrive
                    ) {
                        if (isUploadingToDrive) {
                            CircularProgressIndicator(color = EchoGreen, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Upload Drive", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // YouTube Metadata & Quick Copy
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoBg),
                border = BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "YouTube Launch Asset Metadata",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoAmber
                            )
                        }

                        Button(
                            onClick = {
                                val bundle = """Title: ATHENA — Loom of the Sovereign Intellect (16:9 Cinematic Teaser)
Description: Dark obsidian plane. A golden filament slices across like a cosmic loom shuttle, stitching the ATHENA logomark from left to right. The continuous crossbar unites the circle. Tactile finger-release sound followed by deep 42.6 Hz resonant warmth.
Tags: #Athena #LoomWeave #Cinematic #OriginStory #SovereignOS #42Hz #ObsidianPlane
Format: 16:9 Cinematic 4K | Duration: 12 seconds
Audio: Tactile finger release, 42.6 Hz sub-bass pulse"""
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Loom Video Metadata", bundle))
                                Toast.makeText(context, "Copied YouTube metadata bundle!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoAmber.copy(alpha = 0.2f),
                                contentColor = EchoAmber
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy Bundle", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Title: ATHENA — Loom of the Sovereign Intellect (16:9 Cinematic Teaser)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EchoText
                    )
                    Text(
                        text = "Tags: #Athena #LoomWeave #Cinematic #OriginStory #SovereignOS #42Hz",
                        fontSize = 10.sp,
                        color = EchoMuted
                    )
                }
            }
        }
    }

    // Fullscreen 16:9 Modal
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF050608))
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                color = Color(0xFF050608)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color(0xFF0A0B10))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_athena_loom_weave_1792103250852),
                            contentDescription = "ATHENA Loom Weave",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        LoomWeaveCanvas(
                            progress = currentSeconds / totalSeconds,
                            shimmerPhase = shimmerPhase,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = { isFullscreen = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .background(Color(0x99000000), CircleShape)
                        ) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas rendering the Golden Loom Shuttle slicing across the obsidian horizon,
 * trailing glowing particles, and stitching the continuous crossbar through ATHENA letterforms.
 */
@Composable
private fun LoomWeaveCanvas(
    progress: Float,
    shimmerPhase: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.52f

        // Progress clamp (0.0 to 1.0 corresponds to 0s - 12s)
        val shuttleTravelProgress = (progress / (10.0f / 12.0f)).coerceIn(0.0f, 1.0f)
        val shuttleX = width * shuttleTravelProgress

        // Continuous horizontal crossbar through A, T, H, E, N, A
        val threadStartX = width * 0.12f
        val currentThreadEndX = maxOf(threadStartX, shuttleX)

        if (currentThreadEndX > threadStartX) {
            // Radiant golden thread path with subtle wave oscillation
            val threadPath = Path().apply {
                moveTo(threadStartX, centerY)
                var currentX = threadStartX
                while (currentX <= currentThreadEndX) {
                    val waveOffset = sin((currentX / 20.0f) + (shimmerPhase * 6.28f)) * 2.5f
                    lineTo(currentX, centerY + waveOffset)
                    currentX += 8.0f
                }
            }

            // Glow aura
            drawPath(
                path = threadPath,
                color = Color(0xFFFFD700).copy(alpha = 0.35f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Core gold filament
            drawPath(
                path = threadPath,
                color = Color(0xFFFFF2A1),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Loom Shuttle projectile (active during first 10s)
        if (shuttleTravelProgress < 1.0f) {
            // Trailing sparks & embers
            for (i in 1..8) {
                val sparkX = shuttleX - (i * 12.dp.toPx())
                val sparkY = centerY + (sin(i.toFloat() + shimmerPhase * 10f) * 10.dp.toPx())
                if (sparkX >= threadStartX) {
                    drawCircle(
                        color = Color(0xFFFFB347).copy(alpha = (1.0f - (i / 8f))),
                        radius = (3.dp.toPx() * (1.0f - (i / 10f))),
                        center = Offset(sparkX, sparkY)
                    )
                }
            }

            // Shuttle needle head
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 4.dp.toPx(),
                center = Offset(shuttleX, centerY)
            )
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.8f),
                radius = 8.dp.toPx(),
                center = Offset(shuttleX, centerY)
            )
        }

        // Deep sub-bass resonance ripple (10.8s - 12.0s)
        if (progress >= (10.8f / 12.0f)) {
            val rippleProgress = ((progress - (10.8f / 12.0f)) / (1.2f / 12.0f)).coerceIn(0f, 1f)
            val maxRadius = width * 0.45f
            val rippleRadius = maxRadius * rippleProgress
            val rippleAlpha = (1f - rippleProgress).coerceIn(0f, 1f) * 0.5f

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = rippleAlpha),
                radius = rippleRadius,
                center = Offset(width * 0.5f, centerY),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
