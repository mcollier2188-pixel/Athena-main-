package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.DolaVoiceManager
import com.example.ai.VoiceCalibration
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Supported real-time visualization modes.
 */
enum class WaveformVisualizationMode(val label: String, val subtitle: String) {
    OSCILLOGRAM("Oscillogram", "Continuous Time-Domain Compound Wave"),
    SPECTRUM_BARS("Spectrum Bars", "18-Band Frequency Resonance Analysis"),
    LISSAJOUS_ORBIT("Lissajous Orbit", "42.6 Hz : 85.2 Hz Phase Harmonic Portrait")
}

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGreen = Color(0xFF00E676)
private val HarmonicGold = Color(0xFFFFD700)
private val OrbitViolet = Color(0xFFB388FF)
private val DeepSpaceNavy = Color(0xFF090D14)

/**
 * Dedicated Header Widget for the ATHENA Workspace displaying real-time audio waveform telemetry.
 */
@Composable
fun AthenaWaveformHeaderWidget(
    modifier: Modifier = Modifier,
    onOpenVoiceCalibration: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(WaveformVisualizationMode.OSCILLOGRAM) }
    var isFullScreenOpen by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    val toneGen = HarmonicToneGenerator.SHARED
    val isTonePlaying by toneGen.isPlayingState.collectAsState()
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val micRms by voiceManager.rmsState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "athena_waveform_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("athena_waveform_header_widget")
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = DeepSpaceNavy,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.45f),
                    NeonGreen.copy(alpha = 0.35f),
                    HarmonicGold.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Top Bar: Status badges, mode pills, and action controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTonePlaying || isSpeaking || isListening) NeonGreen else NeonCyan
                            )
                    )
                    Text(
                        text = "42.6 Hz TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF131B28)
                    ) {
                        Text(
                            text = when {
                                isSpeaking -> "VOICE ACTIVE"
                                isListening -> "MIC RMS: ${micRms.toInt()} dB"
                                isTonePlaying -> "AUDIO STREAM"
                                else -> "CARRIER LOCK"
                            },
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSpeaking || isTonePlaying) NeonGreen else HarmonicGold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            if (isMuted) {
                                toneGen.stop()
                            } else {
                                toneGen.playChime(SoundEffectType.TAP)
                            }
                        },
                        modifier = Modifier.size(28.dp).testTag("waveform_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = "Mute audio",
                            tint = if (isMuted) EchoMuted else NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onOpenVoiceCalibration != null) {
                        IconButton(
                            onClick = onOpenVoiceCalibration,
                            modifier = Modifier.size(28.dp).testTag("waveform_voice_calib_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice Calibration",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isFullScreenOpen = true },
                        modifier = Modifier.size(28.dp).testTag("waveform_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand Full Screen",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Mode Selector Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WaveformVisualizationMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color(0xFF101622),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) NeonCyan else Color(0xFF222E42)
                        ),
                        modifier = Modifier
                            .testTag("waveform_mode_${mode.name.lowercase()}")
                            .clickable { currentMode = mode }
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NeonCyan else EchoMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Real-Time Canvas Render
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF04060A))
                    .border(1.dp, Color(0xFF151D2A), RoundedCornerShape(8.dp))
            ) {
                AthenaWaveformCanvas(
                    mode = currentMode,
                    phase = phase,
                    amplitude = if (isSpeaking) 1.2f else if (isTonePlaying) 1.0f else 0.75f,
                    micRms = micRms,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (isFullScreenOpen) {
        AthenaWaveformFullScreenDialog(
            initialMode = currentMode,
            onDismiss = { isFullScreenOpen = false }
        )
    }
}

/**
 * Core Canvas rendering logic for all 3 visualization modes.
 */
@Composable
fun AthenaWaveformCanvas(
    mode: WaveformVisualizationMode,
    phase: Float,
    amplitude: Float,
    micRms: Float = 0f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (mode) {
            WaveformVisualizationMode.OSCILLOGRAM -> {
                drawOscillogram(phase, amplitude)
            }
            WaveformVisualizationMode.SPECTRUM_BARS -> {
                drawSpectrumBars(phase, amplitude, micRms)
            }
            WaveformVisualizationMode.LISSAJOUS_ORBIT -> {
                drawLissajousOrbit(phase, amplitude)
            }
        }
    }
}

/**
 * 1. OSCILLOGRAM: Smooth multi-harmonic continuous time-domain curve with glow.
 */
private fun DrawScope.drawOscillogram(phase: Float, amplitudeScale: Float) {
    val width = size.width
    val height = size.height
    val midY = height / 2f

    if (width <= 0 || height <= 0) return

    // Draw background subtle grid lines
    val gridColor = Color(0xFF0E1624)
    drawLine(gridColor, Offset(0f, midY), Offset(width, midY), strokeWidth = 1f)
    drawLine(gridColor, Offset(0f, midY - height * 0.3f), Offset(width, midY - height * 0.3f), strokeWidth = 1f)
    drawLine(gridColor, Offset(0f, midY + height * 0.3f), Offset(width, midY + height * 0.3f), strokeWidth = 1f)

    val step = 4f
    val path = Path()
    val fillPath = Path()
    val secondaryPath = Path()

    var firstPoint = true
    var x = 0f

    fillPath.moveTo(0f, midY)

    while (x <= width) {
        val normX = x / width
        // 42.6 Hz carrier wave + 85.2 Hz 2nd harmonic + ripple
        val rad1 = (normX * 4f * PI + phase).toFloat()
        val rad2 = (normX * 8f * PI + phase * 1.5f).toFloat()
        val rad3 = (normX * 16f * PI - phase * 2f).toFloat()

        val yVal = sin(rad1) * 0.65f + sin(rad2) * 0.25f + sin(rad3) * 0.10f
        val y = midY + (yVal * (height * 0.36f) * amplitudeScale)

        val secYVal = cos(rad1) * 0.5f + cos(rad2) * 0.3f
        val secY = midY + (secYVal * (height * 0.28f) * amplitudeScale)

        if (firstPoint) {
            path.moveTo(x, y)
            secondaryPath.moveTo(x, secY)
            fillPath.lineTo(x, y)
            firstPoint = false
        } else {
            path.lineTo(x, y)
            secondaryPath.lineTo(x, secY)
            fillPath.lineTo(x, y)
        }
        x += step
    }

    fillPath.lineTo(width, midY)
    fillPath.close()

    // Fill under main wave
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                NeonCyan.copy(alpha = 0.22f),
                NeonCyan.copy(alpha = 0.0f)
            ),
            startY = midY - height * 0.4f,
            endY = height
        )
    )

    // Secondary harmonic trail
    drawPath(
        path = secondaryPath,
        color = NeonGreen.copy(alpha = 0.35f),
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Primary glowing carrier curve
    drawPath(
        path = path,
        color = NeonCyan,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * 2. SPECTRUM BARS: Multi-band frequency analyzer with peak hold dots.
 */
private fun DrawScope.drawSpectrumBars(phase: Float, amplitudeScale: Float, micRms: Float) {
    val width = size.width
    val height = size.height

    if (width <= 0 || height <= 0) return

    val barCount = 18
    val spacing = 4.dp.toPx()
    val totalSpacing = spacing * (barCount + 1)
    val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(3f)

    // Center resonant frequencies centered around 42.6 Hz (bar index 8) and 85.2 Hz (bar index 13)
    for (i in 0 until barCount) {
        val barX = spacing + i * (barWidth + spacing)

        // Resonant curve peaking at indices 8 (42.6Hz) and 13 (85.2Hz)
        val distTo42 = abs(i - 8f)
        val distTo85 = abs(i - 13f)
        val baseResonance = (1.0f / (1.0f + distTo42 * 0.45f)) * 0.9f +
                (1.0f / (1.0f + distTo85 * 0.55f)) * 0.75f

        val waveMod = (sin(phase * 2.2f + i * 0.65f) * 0.25f + 0.75f)
        val rmsBoost = (micRms.coerceIn(0f, 15f) / 15f) * 0.4f

        val normalizedHeight = ((baseResonance * waveMod * amplitudeScale + rmsBoost).coerceIn(0.08f, 0.95f))
        val barHeight = height * normalizedHeight
        val barY = height - barHeight

        // Vertical gradient for each bar
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    HarmonicGold,
                    NeonCyan,
                    Color(0xFF0D47A1)
                ),
                startY = barY,
                endY = height
            ),
            topLeft = Offset(barX, barY),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Peak Hold Dot
        val peakY = (barY - 4.dp.toPx()).coerceAtLeast(2.dp.toPx())
        drawCircle(
            color = if (i == 8 || i == 13) HarmonicGold else NeonGreen,
            radius = (barWidth * 0.45f).coerceIn(1.5f, 3.dp.toPx()),
            center = Offset(barX + barWidth / 2f, peakY)
        )
    }
}

/**
 * 3. LISSAJOUS ORBIT: 42.6 Hz : 85.2 Hz (1:2 ratio) harmonic phase trajectory with orbiting particle.
 */
private fun DrawScope.drawLissajousOrbit(phase: Float, amplitudeScale: Float) {
    val width = size.width
    val height = size.height
    val centerX = width / 2f
    val centerY = height / 2f

    if (width <= 0 || height <= 0) return

    val radiusX = (width * 0.40f) * amplitudeScale.coerceIn(0.6f, 1.2f)
    val radiusY = (height * 0.42f) * amplitudeScale.coerceIn(0.6f, 1.2f)

    // Center crosshairs
    val gridColor = Color(0xFF0F1726)
    drawLine(gridColor, Offset(centerX - radiusX * 1.1f, centerY), Offset(centerX + radiusX * 1.1f, centerY), 1f)
    drawLine(gridColor, Offset(centerX, centerY - radiusY * 1.1f), Offset(centerX, centerY + radiusY * 1.1f), 1f)

    val path = Path()
    val totalSteps = 120
    var firstPoint = true

    // 1:2 frequency ratio: a = 1, b = 2, delta = phase
    for (i in 0..totalSteps) {
        val t = (i.toFloat() / totalSteps) * (2f * PI)
        val x = centerX + radiusX * sin(t).toFloat()
        val y = centerY + radiusY * sin(2f * t + phase).toFloat()

        if (firstPoint) {
            path.moveTo(x, y)
            firstPoint = false
        } else {
            path.lineTo(x, y)
        }
    }

    // Draw Lissajous trajectory with dual glow
    drawPath(
        path = path,
        color = OrbitViolet.copy(alpha = 0.4f),
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(NeonCyan, OrbitViolet, HarmonicGold, NeonCyan)
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Orbiting particle tracking phase
    val particleT = phase
    val partX = centerX + radiusX * sin(particleT).toFloat()
    val partY = centerY + radiusY * sin(2f * particleT + phase).toFloat()

    drawCircle(
        color = HarmonicGold,
        radius = 4.dp.toPx(),
        center = Offset(partX, partY)
    )
    drawCircle(
        color = Color.White,
        radius = 2.dp.toPx(),
        center = Offset(partX, partY)
    )
}

/**
 * Full-screen expandable interactive oscilloscope viewer studio.
 */
@Composable
fun AthenaWaveformFullScreenDialog(
    initialMode: WaveformVisualizationMode = WaveformVisualizationMode.OSCILLOGRAM,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(initialMode) }
    var sensitivity by remember { mutableFloatStateOf(1.0f) }
    var isFrozen by remember { mutableStateOf(false) }
    var selectedCarrierHz by remember { mutableStateOf("42.6 Hz (Bedrock)") }

    val infiniteTransition = rememberInfiniteTransition(label = "fs_waveform_phase")
    val livePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fs_phase"
    )

    val renderedPhase = if (isFrozen) 1.25f else livePhase

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("waveform_fullscreen_dialog"),
            color = EchoBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Text(
                                text = "ATHENA WAVEFORM STUDIO",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = EchoText
                            )
                        }
                        Text(
                            text = selectedMode.subtitle,
                            fontSize = 12.sp,
                            color = EchoMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_waveform_fullscreen")
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Close", tint = EchoCyan)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Visualization Mode Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WaveformVisualizationMode.values().forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.label, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NeonCyan,
                                labelColor = EchoMuted
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Large Main Waveform Canvas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030509)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E2838)))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        AthenaWaveformCanvas(
                            mode = selectedMode,
                            phase = renderedPhase,
                            amplitude = sensitivity,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top-right Telemetry Overlay Badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xCC0A0F1A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B283E))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("CARRIER: $selectedCarrierHz", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = NeonCyan)
                                Text("HARMONIC: 85.20 Hz (2x)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = HarmonicGold)
                                Text("STATE: ${if (isFrozen) "FROZEN" else "STREAMING"}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (isFrozen) EchoAmber else NeonGreen)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Control Strip (Sensitivity Slider, Freeze, Tone Chime, Snapshot)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sensitivity Scale: ${"%.1f".format(sensitivity)}x",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = EchoText
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isFrozen) EchoAmber.copy(alpha = 0.25f) else Color(0xFF162232),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isFrozen) EchoAmber else Color(0xFF283B55)),
                                    modifier = Modifier.clickable { isFrozen = !isFrozen }
                                ) {
                                    Text(
                                        text = if (isFrozen) "RESUME" else "FREEZE",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFrozen) EchoAmber else EchoCyan,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF162232),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283B55)),
                                    modifier = Modifier.clickable {
                                        HarmonicToneGenerator.SHARED.playChime(SoundEffectType.HARMONIC_PING)
                                        Toast.makeText(context, "Resonance ping triggered at 42.6 Hz", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = "PING TONE",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Slider(
                            value = sensitivity,
                            onValueChange = { sensitivity = it },
                            valueRange = 0.4f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan
                            )
                        )

                        // Quick Frequency Node Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val frequencies = listOf("42.6 Hz (Bedrock)", "85.2 Hz (Octave)", "7.83 Hz (Schumann)", "127.8 Hz (Tri-Tone)")
                            frequencies.forEach { freq ->
                                val isSelected = selectedCarrierHz == freq
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) NeonGreen.copy(alpha = 0.2f) else Color(0xFF101724),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonGreen else Color(0xFF223046)),
                                    modifier = Modifier.clickable {
                                        selectedCarrierHz = freq
                                        Toast.makeText(context, "Tuned to $freq", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = freq,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) NeonGreen else EchoMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
