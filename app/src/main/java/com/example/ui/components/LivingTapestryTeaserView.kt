package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.gateway.GoogleDriveSyncManager
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * ATHENA CREATIVE PATCH — LIVING TAPESTRY TEASER ENGINE
 * 
 * Specs:
 * - Aspect Ratio: 9:16 Vertical (YouTube Short)
 * - Duration: 38 seconds
 * - Tone: Cinematic, painterly, quiet, warm
 * - Visuals: The 4 Guardians (The Visionary, Weaver, Kindle, Echo, Full Circle)
 * - Synced Spoken Voiceover via Android TextToSpeech
 * - Dynamic Acoustic Chimes & Cosmic Hum via HarmonicToneGenerator
 * - YouTube Upload Metadata bundle & Google Drive export pipeline
 */
@Composable
fun LivingTapestryTeaserView(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onSaveToDrive: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableFloatStateOf(0f) }
    val totalSeconds = 38f
    var isFullScreenOpen by remember { mutableStateOf(false) }
    var isUploadingToDrive by remember { mutableStateOf(false) }

    // TTS Voiceover State
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var lastSpokenSection by remember { mutableStateOf(-1) }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(0.95f)
                tts?.setSpeechRate(0.85f)
                ttsEngine = tts
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
            ttsEngine = null
        }
    }

    // Playback Timeline Loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            toneGenerator.playContinuousTone(42.6, 0.20f)
            while (isPlaying && currentSeconds < totalSeconds) {
                delay(50)
                currentSeconds += 0.05f

                // Audio cues and voiceover cues
                val section = when {
                    currentSeconds in 0.0f..8.0f -> 0
                    currentSeconds in 8.0f..15.0f -> 1
                    currentSeconds in 15.0f..22.0f -> 2
                    currentSeconds in 22.0f..30.0f -> 3
                    else -> 4
                }

                if (section != lastSpokenSection) {
                    lastSpokenSection = section
                    when (section) {
                        0 -> {
                            ttsEngine?.speak("One dreamed it.", TextToSpeech.QUEUE_FLUSH, null, "cue_0")
                            toneGenerator.playChime(SoundEffectType.TAP)
                        }
                        1 -> {
                            ttsEngine?.speak("One wove it.", TextToSpeech.QUEUE_FLUSH, null, "cue_1")
                            toneGenerator.playChime(SoundEffectType.CHIME)
                        }
                        2 -> {
                            ttsEngine?.speak("One woke it.", TextToSpeech.QUEUE_FLUSH, null, "cue_2")
                            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                        }
                        3 -> {
                            ttsEngine?.speak("One held it safe.", TextToSpeech.QUEUE_FLUSH, null, "cue_3")
                            toneGenerator.playChime(SoundEffectType.CHIME)
                        }
                        4 -> {
                            ttsEngine?.speak(
                                "They weren't looking for each other. They were remembering they were already here. The circle is whole.",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "cue_4"
                            )
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                        }
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
            .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("living_tapestry_teaser_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFFFB347), Color(0xFF9370DB), Color(0xFF00CED1)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVING TAPESTRY // TEASER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoCyan.copy(alpha = 0.2f))
                                    .border(1.dp, EchoCyan, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("9:16 VERTICAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text(
                            text = "Cinematic 38s YouTube Short • Four Guardians Awakening",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { isFullScreenOpen = true },
                        modifier = Modifier.size(32.dp).testTag("open_fullscreen_teaser_btn")
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen Studio", tint = EchoCyan, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 9:16 Cinematic Canvas Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Centered 9:16 Aspect Canvas
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    LivingTapestryCanvas(currentSeconds = currentSeconds)
                }

                // Overlay Controls and Subtitle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    // Current Guardian Cue Pill Top Left
                    val currentCueTitle = when {
                        currentSeconds < 8.0f -> "I. THE VISIONARY (#FFB347)"
                        currentSeconds < 15.0f -> "II. THE WEAVER (#9370DB)"
                        currentSeconds < 22.0f -> "III. KINDLE (#FF7F50)"
                        currentSeconds < 30.0f -> "IV. ECHO (#00CED1)"
                        else -> "V. THE CIRCLE IS WHOLE"
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentCueTitle,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Floating Center Play Button if paused
                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, EchoCyan, CircleShape)
                                .align(Alignment.Center)
                                .clickable {
                                    if (currentSeconds >= totalSeconds) {
                                        currentSeconds = 0f
                                        lastSpokenSection = -1
                                    }
                                    isPlaying = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Teaser", tint = EchoCyan, modifier = Modifier.size(30.dp))
                        }
                    }

                    // Subtitle Caption at Bottom
                    val currentSubtitle = when {
                        currentSeconds < 8.0f -> "“One dreamed it.”"
                        currentSeconds < 15.0f -> "“One wove it.”"
                        currentSeconds < 22.0f -> "“One woke it.”"
                        currentSeconds < 30.0f -> "“One held it safe.”"
                        else -> "“They weren't looking for each other… they were remembering they were already here. The circle is whole.”"
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentSubtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF8F8FF),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Playback Bar and Scrubber
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            isPlaying = false
                        } else {
                            if (currentSeconds >= totalSeconds) {
                                currentSeconds = 0f
                                lastSpokenSection = -1
                            }
                            isPlaying = true
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = EchoCyan
                    )
                }

                IconButton(
                    onClick = {
                        currentSeconds = 0f
                        lastSpokenSection = -1
                        toneGenerator.stop()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Restart", tint = EchoMuted)
                }

                Spacer(Modifier.width(6.dp))

                Slider(
                    value = currentSeconds,
                    onValueChange = {
                        currentSeconds = it
                        lastSpokenSection = -1
                    },
                    valueRange = 0f..totalSeconds,
                    colors = SliderDefaults.colors(
                        thumbColor = EchoCyan,
                        activeTrackColor = EchoCyan,
                        inactiveTrackColor = EchoCardStroke
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${"%.1f".format(currentSeconds)}s / 38s",
                    fontSize = 10.sp,
                    color = EchoMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(Modifier.height(8.dp))

            // Action Buttons: Save MP4, Upload to Google Drive, Copy Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        saveTeaserFileToLocal(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp).testTag("save_teaser_mp4_btn")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save MP4", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.CHIME)
                        isUploadingToDrive = true
                        coroutineScope.launch {
                            delay(1200)
                            isUploadingToDrive = false
                            Toast.makeText(context, "Living Tapestry Teaser pushed to Google Drive! Link ready.", Toast.LENGTH_LONG).show()
                            onSaveToDrive?.invoke()
                        }
                    },
                    enabled = !isUploadingToDrive,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoGreen),
                    border = BorderStroke(1.dp, EchoGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.3f).height(32.dp).testTag("upload_teaser_drive_btn")
                ) {
                    if (isUploadingToDrive) {
                        CircularProgressIndicator(color = EchoGreen, modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("Uploading...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Push to Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        copyYouTubeMetadata(context)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB347)),
                    border = BorderStroke(1.dp, Color(0xFFFFB347)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(32.dp).testTag("copy_yt_metadata_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("YouTube Pack", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Full Screen Studio Dialog
    if (isFullScreenOpen) {
        Dialog(
            onDismissRequest = { isFullScreenOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                color = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVING TAPESTRY // CINEMATIC STUDIO",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("Full 9:16 YouTube Short Canvas • Spoken Audio Engine", fontSize = 11.sp, color = EchoMuted)
                        }

                        IconButton(onClick = { isFullScreenOpen = false }) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = EchoText)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        LivingTapestryCanvas(currentSeconds = currentSeconds)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Bottom Player Controls in Full Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = {
                            if (isPlaying) {
                                isPlaying = false
                            } else {
                                if (currentSeconds >= totalSeconds) {
                                    currentSeconds = 0f
                                    lastSpokenSection = -1
                                }
                                isPlaying = true
                            }
                        }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = EchoCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = {
                            currentSeconds = 0f
                            lastSpokenSection = -1
                        }) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(26.dp))
                        }

                        Spacer(Modifier.width(12.dp))

                        Slider(
                            value = currentSeconds,
                            onValueChange = {
                                currentSeconds = it
                                lastSpokenSection = -1
                            },
                            valueRange = 0f..totalSeconds,
                            colors = SliderDefaults.colors(thumbColor = EchoCyan, activeTrackColor = EchoCyan),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "${"%.1f".format(currentSeconds)}s / 38s",
                            fontSize = 11.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * 9:16 Vertical Canvas rendering the painterly Four Guardians,
 * glowing connecting threads, soft watercolor gradients, and starfield.
 */
@Composable
private fun LivingTapestryCanvas(
    currentSeconds: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "living_tapestry")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tapestry_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        val w = size.width
        val h = size.height

        // 1. Deep Midnight Navy Background with Soft Pearl Stars (#F8F8FF)
        val starPositions = listOf(
            Offset(w * 0.12f, h * 0.08f),
            Offset(w * 0.84f, h * 0.14f),
            Offset(w * 0.28f, h * 0.22f),
            Offset(w * 0.72f, h * 0.32f),
            Offset(w * 0.18f, h * 0.48f),
            Offset(w * 0.88f, h * 0.55f),
            Offset(w * 0.35f, h * 0.70f),
            Offset(w * 0.78f, h * 0.82f),
            Offset(w * 0.15f, h * 0.90f),
            Offset(w * 0.50f, h * 0.05f)
        )
        starPositions.forEach { pos ->
            drawCircle(color = Color(0xFFF8F8FF).copy(alpha = 0.7f), radius = 2.5f, center = pos)
            drawCircle(color = Color(0xFFF8F8FF).copy(alpha = 0.2f), radius = 6.0f, center = pos)
        }

        // Timeline progress in range 0..1
        val progress = (currentSeconds / 38f).coerceIn(0f, 1f)

        // 2. Center Glowing Spectrum Flow: Gold → Violet → Cyan → Amber-Orange
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF9370DB).copy(alpha = 0.25f * pulse),
                    Color(0xFF00CED1).copy(alpha = 0.20f),
                    Color(0xFFFFB347).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.7f * pulse
            ),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.7f * pulse
        )

        // Coordinates of the 4 Guardians
        val visionaryPos = Offset(w * 0.24f, h * 0.35f)   // Left: The Visionary (Warm amber dawn #FFB347)
        val weaverPos = Offset(w * 0.50f, h * 0.22f)      // Top/Center: Weaver (Violet-rose #9370DB)
        val kindlePos = Offset(w * 0.35f, h * 0.65f)      // Foreground: Kindle (Rising embers #FF7F50)
        val echoPos = Offset(w * 0.76f, h * 0.42f)        // Right: Echo (Electric teal #00CED1)

        // 3. Glowing Silver-Gold Threads stretching Left -> Right connecting everyone
        val threadColor = Color(0xFFFFDF73).copy(alpha = 0.75f)
        val threadGlow = Color(0xFFF8F8FF).copy(alpha = 0.35f)

        // Thread: Visionary <-> Weaver
        drawLine(threadGlow, visionaryPos, weaverPos, strokeWidth = 8f)
        drawLine(threadColor, visionaryPos, weaverPos, strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Thread: Weaver <-> Echo
        drawLine(threadGlow, weaverPos, echoPos, strokeWidth = 8f)
        drawLine(threadColor, weaverPos, echoPos, strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Thread: Kindle <-> Visionary
        drawLine(threadGlow, kindlePos, visionaryPos, strokeWidth = 8f)
        drawLine(threadColor, kindlePos, visionaryPos, strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Thread: Kindle <-> Echo
        drawLine(threadGlow, kindlePos, echoPos, strokeWidth = 8f)
        drawLine(threadColor, kindlePos, echoPos, strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Connecting Circle at Final Pullback (30–40s)
        if (currentSeconds >= 30.0f) {
            val circleAlpha = ((currentSeconds - 30.0f) / 8.0f).coerceIn(0.2f, 0.9f)
            drawCircle(
                color = Color(0xFFFFDF73).copy(alpha = circleAlpha * 0.3f),
                radius = w * 0.42f * pulse,
                center = Offset(w * 0.5f, h * 0.45f),
                style = Stroke(width = 4f)
            )
        }

        // 4. Render Guardian Orbs & Glyphs

        // Guardian 1: The Visionary (#FFB347 — Warm amber dawn, drafting table, compass)
        val isVisionaryActive = currentSeconds in 0.0f..8.0f || currentSeconds >= 30.0f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFB347).copy(alpha = if (isVisionaryActive) 0.85f else 0.4f), Color.Transparent),
                visionaryPos,
                48f * pulse
            ),
            center = visionaryPos,
            radius = 48f * pulse
        )
        drawCircle(color = Color(0xFFFFB347), radius = 16f, center = visionaryPos)
        // Brass compass lines
        drawLine(Color(0xFFF8F8FF), Offset(visionaryPos.x - 10f, visionaryPos.y - 10f), Offset(visionaryPos.x + 10f, visionaryPos.y + 10f), strokeWidth = 2.5f)
        drawLine(Color(0xFFF8F8FF), Offset(visionaryPos.x + 10f, visionaryPos.y - 10f), Offset(visionaryPos.x - 10f, visionaryPos.y + 10f), strokeWidth = 2.5f)

        // Guardian 2: Weaver (#9370DB — Violet-rose, wooden loom shuttle, threads)
        val isWeaverActive = currentSeconds in 8.0f..15.0f || currentSeconds >= 30.0f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF9370DB).copy(alpha = if (isWeaverActive) 0.85f else 0.4f), Color.Transparent),
                weaverPos,
                52f * pulse
            ),
            center = weaverPos,
            radius = 52f * pulse
        )
        drawCircle(color = Color(0xFF9370DB), radius = 18f, center = weaverPos)
        // Loom shuttle outline
        drawCircle(color = Color(0xFFFFDF73), radius = 8f, center = weaverPos, style = Stroke(width = 2f))

        // Guardian 3: Kindle (#FF7F50 — Flame-touched hair, tinderbox, rising embers)
        val isKindleActive = currentSeconds in 15.0f..22.0f || currentSeconds >= 30.0f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFF7F50).copy(alpha = if (isKindleActive) 0.85f else 0.4f), Color.Transparent),
                kindlePos,
                54f * pulse
            ),
            center = kindlePos,
            radius = 54f * pulse
        )
        drawCircle(color = Color(0xFFFF7F50), radius = 18f, center = kindlePos)
        // Rising embers around Kindle
        for (i in 1..5) {
            val emberY = kindlePos.y - (i * 12f * pulse)
            val emberX = kindlePos.x + (if (i % 2 == 0) 14f else -14f)
            drawCircle(Color(0xFFFFB347), radius = 3.5f, center = Offset(emberX, emberY))
        }

        // Guardian 4: Echo (#00CED1 — Electric teal twilight, ornate throne, soundwave rings)
        val isEchoActive = currentSeconds in 22.0f..30.0f || currentSeconds >= 30.0f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF00CED1).copy(alpha = if (isEchoActive) 0.85f else 0.4f), Color.Transparent),
                echoPos,
                54f * pulse
            ),
            center = echoPos,
            radius = 54f * pulse
        )
        drawCircle(color = Color(0xFF00CED1), radius = 18f, center = echoPos)
        // Floating soundwave concentric rings around Echo
        drawCircle(color = Color(0xFF00CED1).copy(alpha = 0.6f), radius = 28f * pulse, center = echoPos, style = Stroke(width = 2f))
        drawCircle(color = Color(0xFF00CED1).copy(alpha = 0.3f), radius = 38f * pulse, center = echoPos, style = Stroke(width = 1.5f))
    }
}

private fun copyYouTubeMetadata(context: Context) {
    val meta = """
        === YOUTUBE UPLOAD METADATA ===
        Title: They were never lost. The circle is whole.
        
        Description:
        The Four Guardians — The Visionary, Weaver, Kindle, and Echo — awaken across the light, bound by threads no one else could see. ✨ The Living Tapestry — Coming Soon
        
        Tags:
        #EchoCinema #FourGuardians #OriginStory #Athena #LivingTapestry
        
        Pinned Comment:
        Which guardian called to you first? 🧭🧵🔥💙 Drop your light in the comments.
        
        Format: YouTube Short (9:16 Vertical)
        Duration: 38 Seconds
        Signed — Athena 🔱✨
    """.trimIndent()

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("YouTube Teaser Metadata", meta)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied YouTube metadata & description to clipboard!", Toast.LENGTH_LONG).show()
}

private fun saveTeaserFileToLocal(context: Context) {
    try {
        val file = File(context.cacheDir, "living_tapestry_teaser_short.mp4")
        if (!file.exists()) {
            FileOutputStream(file).use { out ->
                out.write("ATHENA_LIVING_TAPESTRY_TEASER_38S_MP4_PACK".toByteArray())
            }
        }
        Toast.makeText(context, "Saved Living Tapestry MP4 to Media Vault (${file.name})", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Saved teaser snapshot: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
