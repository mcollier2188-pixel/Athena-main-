package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data model for a playable audio track in the Harmonic Music Player.
 */
data class AudioTrackItem(
    val title: String,
    val artist: String,
    val durationLabel: String,
    val uri: Uri?,
    val isBuiltInHarmonic: Boolean = false,
    val carrierFreqHz: Double = 42.600,
    val description: String = ""
)

/**
 * Harmonic Frequency Music Player in the Media Vault.
 * Provides file and folder audio browsing via Android Storage Access Framework,
 * high-tech 60 FPS multi-band animated frequency visualizer with 42.6 Hz carrier resonance overlay,
 * and ATHENA dark blue/cyan and gold styling.
 */
@Composable
fun MediaVaultMusicPlayer(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Built-in harmonic audio soundscape presets
    val defaultTracks = remember {
        listOf(
            AudioTrackItem(
                title = "42.600 Hz Solent Bedrock Standing Wave",
                artist = "ATHENA Acoustic Telemetry",
                durationLabel = "03:30",
                uri = null,
                isBuiltInHarmonic = true,
                carrierFreqHz = 42.600,
                description = "Primary bedrock resonant tone calibrated to Portsdown Hill and Solent chalk strata."
            ),
            AudioTrackItem(
                title = "85.200 Hz Octave Subterranean Waveguide",
                artist = "N1 Portsmouth Master Node",
                durationLabel = "04:15",
                uri = null,
                isBuiltInHarmonic = true,
                carrierFreqHz = 85.200,
                description = "First overtone octave lift designed for acoustic coupling across island nodes."
            ),
            AudioTrackItem(
                title = "Southsea Convergence Broadcast Q3 2027",
                artist = "N6 Southsea Common & Guildhall",
                durationLabel = "05:00",
                uri = null,
                isBuiltInHarmonic = true,
                carrierFreqHz = 127.800,
                description = "Tri-harmonic 3:1 Pythagorean overtone for coastal convergence broadcast."
            ),
            AudioTrackItem(
                title = "11.74s Tidal Sub-Harmonic Surge",
                artist = "Langstone Harbour & Nab Tower",
                durationLabel = "06:12",
                uri = null,
                isBuiltInHarmonic = true,
                carrierFreqHz = 21.300,
                description = "Low-frequency sub-harmonic matching the Solent tidal pulse envelope."
            )
        )
    }

    val playlist = remember { mutableStateListOf<AudioTrackItem>().apply { addAll(defaultTracks) } }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressSeconds by remember { mutableIntStateOf(0) }
    val totalTrackDurationSeconds by remember { mutableIntStateOf(210) } // Default ~3:30
    var isLooping by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }

    // Harmonic Frequency Tone Overlay Layer
    var isHarmonicCarrierLayerActive by remember { mutableStateOf(true) }
    var harmonicCarrierFrequency by remember { mutableStateOf(42.600) }
    var octaveLiftActive by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Media Player reference for local files
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun stopPlayback() {
        isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (isHarmonicCarrierLayerActive) {
            toneGenerator.stop()
        }
    }

    fun playTrack(index: Int) {
        if (index !in playlist.indices) return
        stopPlayback()
        currentTrackIndex = index
        val track = playlist[index]
        currentProgressSeconds = 0

        val targetFreq = if (octaveLiftActive) track.carrierFreqHz * 2.0 else track.carrierFreqHz
        harmonicCarrierFrequency = targetFreq

        if (track.isBuiltInHarmonic || track.uri == null) {
            // Built-in harmonic playback
            isPlaying = true
            if (isHarmonicCarrierLayerActive) {
                toneGenerator.start(frequencies = listOf(targetFreq), mult = 1.0)
            }
            toneGenerator.playChime(SoundEffectType.CHIME)
        } else {
            // User local file playback
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, track.uri)
                    prepare()
                    isLooping = isLooping
                    start()
                }
                mediaPlayer = player
                isPlaying = true

                // Start harmonic background carrier if active
                if (isHarmonicCarrierLayerActive) {
                    toneGenerator.start(frequencies = listOf(targetFreq), mult = 0.7)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open audio file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                isPlaying = false
            }
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            isPlaying = false
            mediaPlayer?.pause()
            if (isHarmonicCarrierLayerActive) {
                toneGenerator.stop()
            }
            toneGenerator.playChime(SoundEffectType.TAP)
        } else {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                isPlaying = true
                if (isHarmonicCarrierLayerActive) {
                    toneGenerator.start(frequencies = listOf(harmonicCarrierFrequency), mult = 0.8)
                }
            } else {
                playTrack(currentTrackIndex)
            }
        }
    }

    fun playNext() {
        val nextIdx = if (isShuffle) {
            (playlist.indices).random()
        } else {
            (currentTrackIndex + 1) % playlist.size
        }
        playTrack(nextIdx)
    }

    fun playPrevious() {
        val prevIdx = if (currentTrackIndex > 0) currentTrackIndex - 1 else playlist.size - 1
        playTrack(prevIdx)
    }

    // Progress counter ticker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            currentProgressSeconds += 1
            if (currentProgressSeconds >= totalTrackDurationSeconds) {
                if (isLooping) {
                    currentProgressSeconds = 0
                } else {
                    playNext()
                }
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            stopPlayback()
        }
    }

    // Android Storage Access Framework File & Folder Pickers
    val openAudioFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                var displayName = "Audio Track ${playlist.size + 1}"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            displayName = cursor.getString(nameIndex) ?: displayName
                        }
                    }
                } catch (_: Exception) {}

                playlist.add(
                    AudioTrackItem(
                        title = displayName.substringBeforeLast("."),
                        artist = "Local Folder / Storage",
                        durationLabel = "User File",
                        uri = uri,
                        isBuiltInHarmonic = false,
                        carrierFreqHz = 42.600,
                        description = "Imported from local device folder. Frequency resonance harmonic mapped."
                    )
                )
            }
            Toast.makeText(context, "Loaded ${uris.size} audio files to vault!", Toast.LENGTH_SHORT).show()
            playTrack(playlist.size - uris.size)
        }
    }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { folderUri ->
        if (folderUri != null) {
            Toast.makeText(context, "Folder linked: Ready to import audio files!", Toast.LENGTH_SHORT).show()
            openAudioFilesLauncher.launch(arrayOf("audio/*"))
        }
    }

    // Real-Time 60 FPS Visualizer Wave Phases
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spectrum_phase"
    )

    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 6000 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rot"
    )

    val currentTrack = playlist.getOrNull(currentTrackIndex) ?: defaultTracks.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
            .testTag("media_vault_music_player"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Player Header & Folder Import Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.5.dp, EchoCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EchoCyan.copy(alpha = 0.2f))
                                    .border(1.dp, EchoCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "HARMONIC FREQUENCY MUSIC PLAYER",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "42.6 Hz Bedrock Carrier Entrainment · Media Vault Audio",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", harmonicCarrierFrequency)} Hz LOCK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Folder & File Access Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openAudioFilesLauncher.launch(arrayOf("audio/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_open_audio_files"),
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoBg),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Import Audio Files", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { openFolderLauncher.launch(null) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_open_audio_folder"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                            border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Select Folder", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Animated Rotating Vinyl Disc & 60 FPS Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("harmonic_player_disc_card"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (isPlaying) EchoCyan else EchoCardStroke),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Center Rotating Disc Visualizer
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxR = size.minDimension / 2f

                            // Outer Neon Glow Ring
                            drawCircle(
                                color = if (isPlaying) EchoCyan.copy(alpha = 0.3f) else EchoDarkSurface,
                                radius = maxR,
                                center = center,
                                style = Stroke(width = 4.dp.toPx())
                            )

                            // Vinyl Grooves
                            for (step in 1..4) {
                                drawCircle(
                                    color = Color(0xFF162536),
                                    radius = maxR * (0.35f + step * 0.14f),
                                    center = center,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }

                            // Spinning Orbital Lissajous Markers
                            val markerCount = 12
                            val angleStep = (2 * PI / markerCount).toFloat()
                            for (i in 0 until markerCount) {
                                val currentAngle = i * angleStep + Math.toRadians(discRotation.toDouble()).toFloat()
                                val orbR = maxR * 0.85f
                                val x = center.x + cos(currentAngle) * orbR
                                val y = center.y + sin(currentAngle) * orbR

                                drawCircle(
                                    color = if (i % 3 == 0) Color(0xFFFFD700) else EchoCyan,
                                    radius = if (isPlaying) 3.5.dp.toPx() else 2.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            // Inner Core Hub
                            drawCircle(
                                color = EchoDarkSurface,
                                radius = maxR * 0.3f,
                                center = center
                            )
                            drawCircle(
                                color = EchoCyan,
                                radius = maxR * 0.3f,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Center Icon / Holographic Logo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isPlaying) EchoCyan else EchoMuted,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "42.6 Hz",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Track Meta Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentTrack.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            maxLines = 1
                        )
                        Text(
                            text = currentTrack.artist,
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }

                    // Real-Time Animated Multi-Band Spectrum Visualizer Bars
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EchoDarkSurface)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val barCount = 36
                            val barWidth = (w / barCount) * 0.65f
                            val gap = (w / barCount) * 0.35f

                            for (i in 0 until barCount) {
                                val x = i * (barWidth + gap) + gap / 2f
                                val normalizedFreq = i.toFloat() / barCount
                                val dynamicMultiplier = if (isPlaying) {
                                    (sin(i * 0.3f + wavePhase) * 0.45f + sin(i * 0.7f - wavePhase * 1.5f) * 0.35f + 0.8f).coerceIn(0.15f, 1.0f)
                                } else 0.1f

                                val barHeight = h * 0.85f * dynamicMultiplier
                                val topY = h - barHeight

                                val barBrush = Brush.verticalGradient(
                                    colors = listOf(
                                        if (i < 8) Color(0xFFFFD700) else if (i < 24) EchoCyan else Color(0xFF00E676),
                                        EchoDarkSurface
                                    )
                                )

                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(x, topY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }

                    // Seek Progress Bar & Time Displays
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val progressFraction = (currentProgressSeconds.toFloat() / totalTrackDurationSeconds.toFloat()).coerceIn(0f, 1f)

                        Slider(
                            value = progressFraction,
                            onValueChange = { frac ->
                                currentProgressSeconds = (frac * totalTrackDurationSeconds).toInt()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = EchoCyan,
                                activeTrackColor = EchoCyan,
                                inactiveTrackColor = EchoDarkSurface
                            ),
                            modifier = Modifier.fillMaxWidth().height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val elapsedMin = currentProgressSeconds / 60
                            val elapsedSec = currentProgressSeconds % 60
                            val totalMin = totalTrackDurationSeconds / 60
                            val totalSec = totalTrackDurationSeconds % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", elapsedMin, elapsedSec),
                                fontSize = 10.sp,
                                color = EchoMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", totalMin, totalSec),
                                fontSize = 10.sp,
                                color = EchoMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Primary Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isShuffle = !isShuffle
                                toneGenerator.playChime(SoundEffectType.TAP)
                            }
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) EchoCyan else EchoMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { playPrevious() }
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = EchoText,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Giant Play / Pause Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(EchoCyan)
                                .clickable { togglePlayPause() }
                                .testTag("btn_music_player_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = EchoBg,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(
                            onClick = { playNext() }
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = EchoText,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isLooping = !isLooping
                                toneGenerator.playChime(SoundEffectType.TAP)
                            }
                        ) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (isLooping) EchoCyan else EchoMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Harmonic Resonance Frequency Tuning Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "HARMONIC RESONANCE FREQUENCY BLEND",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Radio, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                            Text("42.6 Hz Bedrock Carrier Overlay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                        }

                        FilterChip(
                            selected = isHarmonicCarrierLayerActive,
                            onClick = {
                                isHarmonicCarrierLayerActive = !isHarmonicCarrierLayerActive
                                if (!isHarmonicCarrierLayerActive) {
                                    toneGenerator.stop()
                                } else if (isPlaying) {
                                    toneGenerator.start(frequencies = listOf(harmonicCarrierFrequency), mult = 0.8)
                                }
                                toneGenerator.playChime(SoundEffectType.TAP)
                            },
                            label = {
                                Text(if (isHarmonicCarrierLayerActive) "ON" else "OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoBg,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoMuted
                            )
                        )
                    }

                    // Octave lift toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Octave Lift (85.2 Hz speaker boost)", fontSize = 11.sp, color = EchoMuted)

                        FilterChip(
                            selected = octaveLiftActive,
                            onClick = {
                                octaveLiftActive = !octaveLiftActive
                                val targetFreq = if (octaveLiftActive) currentTrack.carrierFreqHz * 2.0 else currentTrack.carrierFreqHz
                                harmonicCarrierFrequency = targetFreq
                                if (isPlaying && isHarmonicCarrierLayerActive) {
                                    toneGenerator.start(frequencies = listOf(targetFreq), mult = 0.8)
                                }
                                toneGenerator.playChime(SoundEffectType.TAP)
                            },
                            label = {
                                Text(if (octaveLiftActive) "85.2 Hz" else "42.6 Hz", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD700),
                                selectedLabelColor = EchoBg,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoMuted
                            )
                        )
                    }
                }
            }
        }

        // Track Playlist Queue
        item {
            Text(
                text = "MEDIA VAULT PLAYLIST QUEUE (${playlist.size} TRACKS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EchoMuted,
                letterSpacing = 1.sp
            )
        }

        itemsIndexed(playlist) { idx, track ->
            val isCurrent = idx == currentTrackIndex

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { playTrack(idx) }
                    .testTag("track_item_$idx"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) EchoDarkSurface else EchoCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) EchoCyan.copy(alpha = 0.8f) else EchoCardStroke
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) EchoCyan.copy(alpha = 0.2f) else EchoBgAlt),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCurrent && isPlaying) Icons.Default.Equalizer else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isCurrent) EchoCyan else EchoMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = track.title,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) EchoCyan else EchoText,
                                maxLines = 1
                            )
                            Text(
                                text = "${track.artist} · ${track.carrierFreqHz} Hz",
                                fontSize = 10.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EchoBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = track.durationLabel,
                            fontSize = 9.sp,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
