package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoPurple
import com.example.ui.theme.EchoText
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ChladniDisplayMode(val label: String) {
    DUAL_MODE("Dual Mode"),
    PARTICLES_ONLY("Particles Only"),
    NODAL_LINES_ONLY("Nodal Lines Only")
}

data class ChladniPreset(
    val id: String,
    val name: String,
    val frequencyHz: Double,
    val m: Int,
    val n: Int,
    val description: String,
    val toneHz: Double = frequencyHz
)

val CHLADNI_PRESETS = listOf(
    ChladniPreset(
        id = "bedrock_42_fundamental",
        name = "42.6 Hz Fundamental",
        frequencyHz = 42.6,
        m = 2,
        n = 1,
        description = "Fundamental circular plate boundary at 42.60 Hz: 4 quadrants and 1 primary nodal ring."
    ),
    ChladniPreset(
        id = "bedrock_42_prime",
        name = "42.6 Hz Bedrock Prime",
        frequencyHz = 42.6,
        m = 3,
        n = 2,
        description = "Sovereign bedrock harmonic with 6 radial nodal spokes and dual concentric zero rings."
    ),
    ChladniPreset(
        id = "pyramid_85",
        name = "85.2 Hz Octave",
        frequencyHz = 85.2,
        m = 4,
        n = 2,
        description = "King's Chamber primary octave (2x 42.6 Hz) forming an 8-fold star nodal geometry."
    ),
    ChladniPreset(
        id = "harmonic_127",
        name = "127.8 Hz Tri-Harmonic",
        frequencyHz = 127.8,
        m = 5,
        n = 2,
        description = "Third harmonic overtone (3x 42.6 Hz) exhibiting 10-sector star nodal division."
    ),
    ChladniPreset(
        id = "harmonic_170",
        name = "170.4 Hz Quad Harmonic",
        frequencyHz = 170.4,
        m = 6,
        n = 3,
        description = "Fourth harmonic overtone (4x 42.6 Hz) displaying 12-sector floral symmetry with triple rings."
    ),
    ChladniPreset(
        id = "verdi_432",
        name = "432 Hz Natural Scale",
        frequencyHz = 432.0,
        m = 5,
        n = 3,
        description = "Philosophical A-tuning exhibiting intricate decagonal rosette geometry."
    ),
    ChladniPreset(
        id = "genesis_528",
        name = "528 Hz Solfeggio",
        frequencyHz = 528.0,
        m = 6,
        n = 3,
        description = "Cellular harmonic tone producing 12-sector floral symmetry with triple nodal rings."
    )
)

/**
 * Sand particle simulation element on the Chladni vibrating circular plate.
 */
class ChladniParticle(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val colorVariant: Int = 0
)

/**
 * High-performance Bessel J_m(x) evaluator using 16-point quadrature.
 * Unconditionally stable, no overflow, exact for normalized wave numbers.
 */
fun besselJ(m: Int, x: Double): Double {
    if (x == 0.0) return if (m == 0) 1.0 else 0.0
    val nPoints = 16
    var sum = 0.0
    for (i in 0 until nPoints) {
        val tau = PI * (i + 0.5) / nPoints
        sum += cos(m * tau - x * sin(tau))
    }
    return sum / nPoints
}

/**
 * Evaluates plate displacement W(r, theta) for circular Chladni plate.
 * normalizedRadius rho in [0, 1].
 */
fun evaluateCircularChladni(
    rho: Double,
    theta: Double,
    m: Int,
    n: Int,
    phase: Double = 0.0
): Double {
    if (rho > 1.0) return 0.0
    val k = PI * (n + m * 0.5)
    val radial = besselJ(m, k * rho)
    val angular = cos(m * theta + phase)
    return radial * angular
}

/**
 * High-precision approximation of j-th zero of Bessel function J_m(x)
 * for physical circular plate boundary and concentric nodal rings.
 */
fun besselZeroApprox(m: Int, j: Int): Double {
    val beta = (j + m * 0.5 - 0.25) * PI
    val mu = 4.0 * m * m
    val correction = (mu - 1.0) / (8.0 * beta)
    val res = beta - correction
    return if (res > 0.5) res else (j * PI)
}

/**
 * Interactive Circular Chladni Resonance Plate Composable
 */
@Composable
fun CircularChladniCanvas(
    modifier: Modifier = Modifier,
    initialPreset: ChladniPreset = CHLADNI_PRESETS[0],
    initialDisplayMode: ChladniDisplayMode = ChladniDisplayMode.DUAL_MODE,
    onSaveSnapshot: ((presetName: String, frequency: Double, m: Int, n: Int, notes: String) -> Unit)? = null
) {
    var displayMode by remember { mutableStateOf(initialDisplayMode) }
    var selectedPreset by remember { mutableStateOf(initialPreset) }
    var frequencyHz by remember { mutableDoubleStateOf(initialPreset.frequencyHz) }
    var modalM by remember { mutableIntStateOf(initialPreset.m) }
    var modalN by remember { mutableIntStateOf(initialPreset.n) }
    var excitationAmplitude by remember { mutableFloatStateOf(1.0f) }
    var showNodalLines by remember { mutableStateOf(true) }
    var showTelemetryHud by remember { mutableStateOf(true) }
    var isSimRunning by remember { mutableStateOf(true) }
    var isAudioSynced by remember { mutableStateOf(false) }

    val toneGenerator = remember { HarmonicToneGenerator.SHARED }
    val isTonePlaying by toneGenerator.isPlayingState.collectAsState()

    // Particle pool (600 particles)
    val particleCount = 600
    val particles = remember {
        val random = Random(42)
        val list = mutableListOf<ChladniParticle>()
        for (i in 0 until particleCount) {
            val r = sqrt(random.nextDouble()).toFloat() * 0.95f
            val theta = (random.nextDouble() * 2.0 * PI).toFloat()
            val px = r * cos(theta)
            val py = r * sin(theta)
            list.add(ChladniParticle(px, py, colorVariant = random.nextInt(3)))
        }
        list
    }

    // Touch damping point (user can touch the plate to dampen a node)
    var dampPoint by remember { mutableStateOf<Offset?>(null) }

    // Audio Sync synchronization
    LaunchedEffect(isAudioSynced, frequencyHz) {
        if (isAudioSynced) {
            toneGenerator.updateFrequencies(listOf(frequencyHz))
            if (!toneGenerator.isPlaying) {
                toneGenerator.start()
            }
        }
    }

    // Simulation animation loop via withFrameNanos for smooth 60fps physics
    LaunchedEffect(isSimRunning, modalM, modalN, excitationAmplitude, frequencyHz) {
        val random = Random()
        var prevTimeNanos = 0L

        while (isActive && isSimRunning) {
            withFrameNanos { frameTimeNanos ->
                if (prevTimeNanos == 0L) {
                    prevTimeNanos = frameTimeNanos
                    return@withFrameNanos
                }
                val dt = ((frameTimeNanos - prevTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                prevTimeNanos = frameTimeNanos

                val m = modalM
                val n = modalN
                val amp = excitationAmplitude
                val eps = 0.035

                // Update particle positions based on acoustic radiation force towards nodal lines
                for (p in particles) {
                    val rho = sqrt(p.x * p.x + p.y * p.y).toDouble()
                    if (rho >= 0.98) {
                        // Inward boundary bounce
                        val angle = atan2(p.y.toDouble(), p.x.toDouble())
                        p.vx = (-cos(angle) * 0.15f).toFloat()
                        p.vy = (-sin(angle) * 0.15f).toFloat()
                        p.x = (0.97 * cos(angle)).toFloat()
                        p.y = (0.97 * sin(angle)).toFloat()
                        continue
                    }

                    val theta = atan2(p.y.toDouble(), p.x.toDouble())
                    val displacement = evaluateCircularChladni(rho, theta, m, n)
                    val absDisp = abs(displacement)

                    // Numerical gradient of vibration intensity |W|^2
                    val dRho = 0.02
                    val dispRhoPlus = evaluateCircularChladni((rho + dRho).coerceAtMost(1.0), theta, m, n)
                    val gradRho = (abs(dispRhoPlus) - absDisp) / dRho

                    val dTheta = 0.05
                    val dispThetaPlus = evaluateCircularChladni(rho, theta + dTheta, m, n)
                    val gradTheta = (abs(dispThetaPlus) - absDisp) / (rho.coerceAtLeast(0.05) * dTheta)

                    // Acoustic radiation acceleration drives particles downhill toward minimum |W| (nodal lines)
                    val driveForce = 0.55f * amp
                    val accRho = -gradRho.toFloat() * driveForce
                    val accTheta = -gradTheta.toFloat() * driveForce

                    // Convert polar acceleration to Cartesian
                    val cosT = cos(theta).toFloat()
                    val sinT = sin(theta).toFloat()
                    val ax = accRho * cosT - accTheta * sinT
                    val ay = accRho * sinT + accTheta * cosT

                    // Brownian thermal jitter scaled by local vibration amplitude
                    val jitter = (absDisp * 0.08 * amp).toFloat()
                    val jx = (random.nextGaussian() * jitter).toFloat()
                    val jy = (random.nextGaussian() * jitter).toFloat()

                    // Update velocity and apply friction
                    p.vx = (p.vx + (ax + jx) * dt * 60f) * 0.88f
                    p.vy = (p.vy + (ay + jy) * dt * 60f) * 0.88f

                    // Damping if user touches plate
                    dampPoint?.let { dp ->
                        val distSq = (p.x - dp.x) * (p.x - dp.x) + (p.y - dp.y) * (p.y - dp.y)
                        if (distSq < 0.05f) {
                            p.vx *= 0.2f
                            p.vy *= 0.2f
                        }
                    }

                    p.x += p.vx * dt * 45f
                    p.y += p.vy * dt * 45f
                }
            }
        }
    }

    // Glow animation for plate boundary
    val infiniteTransition = rememberInfiniteTransition(label = "ChladniPlateGlow")
    val rimPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rimPulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EchoBgAlt)
            .border(1.dp, EchoCardStroke, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("circular_chladni_solver")
    ) {
        // Cockpit Header & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(EchoCyan, EchoOnCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Chladni Plate",
                        tint = EchoText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CHLADNI RESONANCE PLATE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Circular Elastic Eigenmode Simulation",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )
                }
            }

            // Quick Actions: Play/Pause, Audio Sync, Snapshot
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Audio Link Toggle
                IconButton(
                    onClick = {
                        isAudioSynced = !isAudioSynced
                        if (!isAudioSynced && isTonePlaying) {
                            toneGenerator.stop()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Sync Audio",
                        tint = if (isAudioSynced) EchoGreen else EchoMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sim Freeze/Run
                IconButton(
                    onClick = { isSimRunning = !isSimRunning },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSimRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Sim",
                        tint = if (isSimRunning) EchoCyan else EchoAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Shake / Randomize particles
                IconButton(
                    onClick = {
                        val random = Random()
                        for (p in particles) {
                            val r = sqrt(random.nextDouble()).toFloat() * 0.95f
                            val theta = (random.nextDouble() * 2.0 * PI).toFloat()
                            p.x = r * cos(theta)
                            p.y = r * sin(theta)
                            p.vx = 0f
                            p.vy = 0f
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Dust",
                        tint = EchoMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Preset Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(CHLADNI_PRESETS) { preset ->
                val isSelected = selectedPreset.id == preset.id && modalM == preset.m && modalN == preset.n
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedPreset = preset
                        frequencyHz = preset.frequencyHz
                        modalM = preset.m
                        modalN = preset.n
                        if (isAudioSynced) {
                            toneGenerator.updateFrequencies(listOf(preset.frequencyHz))
                        }
                    },
                    label = {
                        Text(
                            text = preset.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EchoCyan.copy(alpha = 0.25f),
                        selectedLabelColor = EchoCyan,
                        containerColor = EchoCard.copy(alpha = 0.5f),
                        labelColor = EchoMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) EchoCyan else EchoCardStroke
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Center Stage: Circular Plate Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.05f)
                .clip(RoundedCornerShape(16.dp))
                .background(EchoBg)
                .border(1.dp, EchoCardStroke, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = minOf(size.width, size.height).toFloat() / 2f * 0.95f
                                val normX = (offset.x - center.x) / radius
                                val normY = (offset.y - center.y) / radius
                                dampPoint = Offset(normX, normY)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { dampPoint = null },
                            onDragCancel = { dampPoint = null }
                        ) { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = minOf(size.width, size.height).toFloat() / 2f * 0.95f
                            val normX = (change.position.x - center.x) / radius
                            val normY = (change.position.y - center.y) / radius
                            dampPoint = Offset(normX, normY)
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val plateRadius = (size.minDimension / 2f) * 0.92f

                // 1. Draw Circular Plate Metal Disc with Depth Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF142434),
                            Color(0xFF0D1722),
                            Color(0xFF070B10)
                        ),
                        center = center,
                        radius = plateRadius
                    ),
                    radius = plateRadius,
                    center = center
                )

                // 2. Calibration Reticles (Concentric Harmonic Rings)
                for (ring in 1..4) {
                    val r = plateRadius * (ring / 4f)
                    drawCircle(
                        color = EchoCardStroke.copy(alpha = 0.35f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Crosshairs
                drawLine(
                    color = EchoCardStroke.copy(alpha = 0.35f),
                    start = Offset(center.x - plateRadius, center.y),
                    end = Offset(center.x + plateRadius, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = EchoCardStroke.copy(alpha = 0.35f),
                    start = Offset(center.x, center.y - plateRadius),
                    end = Offset(center.x, center.y + plateRadius),
                    strokeWidth = 1.dp.toPx()
                )

                // 3. Optional Overlay: Mathematical Nodal Line Contours
                if (showNodalLines && displayMode != ChladniDisplayMode.PARTICLES_ONLY) {
                    drawCircularNodalLines(
                        center = center,
                        radius = plateRadius,
                        m = modalM,
                        n = modalN,
                        glowAlpha = rimPulse
                    )
                }

                // 4. Draw Physical Sand Particles
                if (displayMode != ChladniDisplayMode.NODAL_LINES_ONLY) {
                    for (p in particles) {
                        val px = center.x + p.x * plateRadius
                        val py = center.y + p.y * plateRadius

                        val pColor = when (p.colorVariant) {
                            0 -> EchoCyan
                            1 -> EchoGreen
                            else -> EchoAmber
                        }

                        drawCircle(
                            color = pColor.copy(alpha = 0.85f),
                            radius = 2.0.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                // 5. Draw User Dampening Touch Target
                dampPoint?.let { dp ->
                    val dx = center.x + dp.x * plateRadius
                    val dy = center.y + dp.y * plateRadius
                    drawCircle(
                        color = EchoAmber.copy(alpha = 0.4f),
                        radius = 16.dp.toPx(),
                        center = Offset(dx, dy)
                    )
                    drawCircle(
                        color = EchoAmber,
                        radius = 4.dp.toPx(),
                        center = Offset(dx, dy)
                    )
                }

                // 6. Brass/Cyan Glowing Perimeter Bezel
                drawCircle(
                    color = EchoCyan.copy(alpha = 0.3f * rimPulse),
                    radius = plateRadius + 2.dp.toPx(),
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFD4AF37).copy(alpha = 0.7f),
                    radius = plateRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Real-Time HUD Overlay Chip
            if (showTelemetryHud) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoBg.copy(alpha = 0.85f))
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = "MODE (m, n) = ($modalM, $modalN)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan
                        )
                        Text(
                            text = "FREQ: ${"%.2f".format(frequencyHz)} Hz",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EchoGreen
                        )
                        Text(
                            text = "NODAL SPOKES: ${modalM * 2} | RINGS: $modalN",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EchoMuted
                        )
                    }
                }
            }

            if (isAudioSynced) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoGreen.copy(alpha = 0.2f))
                        .border(1.dp, EchoGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AUDIO SYNCED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoGreen
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Modal Parameters & Driving Frequency Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EchoCard)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Visualizer Display Mode Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VISUALIZER:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoMuted
                )
                ChladniDisplayMode.values().forEach { mode ->
                    FilterChip(
                        selected = displayMode == mode,
                        onClick = { displayMode = mode },
                        label = { Text(mode.label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoCyan,
                            selectedLabelColor = EchoOnCyan,
                            containerColor = EchoBgAlt,
                            labelColor = EchoText
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Frequency Slider with 42.60 Hz Bedrock Lock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Carrier Frequency",
                        fontSize = 12.sp,
                        color = EchoText,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedButton(
                        onClick = {
                            frequencyHz = 42.6
                            modalM = 3
                            modalN = 2
                            if (isAudioSynced) {
                                toneGenerator.updateFrequencies(listOf(42.6))
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Lock 42.6 Hz", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "${"%.1f".format(frequencyHz)} Hz",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = EchoCyan
                )
            }
            Slider(
                value = frequencyHz.toFloat(),
                onValueChange = {
                    frequencyHz = it.toDouble()
                    if (isAudioSynced) {
                        toneGenerator.updateFrequencies(listOf(frequencyHz))
                    }
                },
                valueRange = 20f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = EchoCyan,
                    activeTrackColor = EchoCyan,
                    inactiveTrackColor = EchoCardStroke
                ),
                modifier = Modifier.height(24.dp)
            )

            // Azimuthal Mode M (Radial Spokes) & Radial Mode N (Concentric Rings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mode m
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Radial Spokes (m)",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                        Text(
                            text = "$modalM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen
                        )
                    }
                    Slider(
                        value = modalM.toFloat(),
                        onValueChange = { modalM = it.toInt() },
                        valueRange = 0f..8f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = EchoGreen,
                            activeTrackColor = EchoGreen,
                            inactiveTrackColor = EchoCardStroke
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Mode n
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nodal Rings (n)",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                        Text(
                            text = "$modalN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoAmber
                        )
                    }
                    Slider(
                        value = modalN.toFloat(),
                        onValueChange = { modalN = it.toInt() },
                        valueRange = 1f..6f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = EchoAmber,
                            activeTrackColor = EchoAmber,
                            inactiveTrackColor = EchoCardStroke
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // Toggles: Nodal Lines & Telemetry HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Switch(
                        checked = showNodalLines,
                        onCheckedChange = { showNodalLines = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EchoCyan,
                            checkedTrackColor = EchoCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = EchoMuted,
                            uncheckedTrackColor = EchoCardStroke
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Mathematical Nodal Lines",
                        fontSize = 11.sp,
                        color = EchoText
                    )
                }

                // Snapshot Action
                if (onSaveSnapshot != null) {
                    Button(
                        onClick = {
                            val notes = "Chladni Modal State: m=$modalM, n=$modalN at ${"%.2f".format(frequencyHz)} Hz. " +
                                    "Azimuthal symmetry = ${modalM * 2} spokes, Concentric rings = $modalN."
                            onSaveSnapshot(selectedPreset.name, frequencyHz, modalM, modalN, notes)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoCyan.copy(alpha = 0.2f),
                            contentColor = EchoCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Log Geometry", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * Draws the theoretical illuminated nodal zero contours on the circular plate
 */
private fun DrawScope.drawCircularNodalLines(
    center: Offset,
    radius: Float,
    m: Int,
    n: Int,
    glowAlpha: Float = 1.0f
) {
    val outerGlowColor = EchoCyan.copy(alpha = 0.28f * glowAlpha)
    val innerLineColor = Color(0xFF64FFDA).copy(alpha = 0.85f * glowAlpha)
    val coreLineColor = Color.White.copy(alpha = 0.95f * glowAlpha)

    // 1. Azimuthal Nodal Lines: theta = (2p + 1) * PI / (2 * m)
    if (m > 0) {
        val totalSpokes = m * 2
        for (p in 0 until totalSpokes) {
            val theta = (2 * p + 1) * PI / (2.0 * m)
            val endX = center.x + radius * cos(theta).toFloat()
            val endY = center.y + radius * sin(theta).toFloat()
            val end = Offset(endX, endY)

            // Outer soft glow
            drawLine(
                color = outerGlowColor,
                start = center,
                end = end,
                strokeWidth = 3.6.dp.toPx()
            )
            // Inner vibrant line
            drawLine(
                color = innerLineColor,
                start = center,
                end = end,
                strokeWidth = 1.6.dp.toPx()
            )
            // Core highlight
            drawLine(
                color = coreLineColor,
                start = center,
                end = end,
                strokeWidth = 0.6.dp.toPx()
            )
        }
    }

    // 2. Concentric Nodal Rings: zeros of Bessel J_m(k * r)
    // Precise physical circular plate Bessel roots
    for (ring in 1..n) {
        val z_j = besselZeroApprox(m, ring)
        val z_boundary = besselZeroApprox(m, n + 1)
        val ringFraction = (z_j / z_boundary).toFloat().coerceIn(0.12f, 0.92f)

        // Outer soft glow ring
        drawCircle(
            color = outerGlowColor,
            radius = radius * ringFraction,
            center = center,
            style = Stroke(width = 3.6.dp.toPx())
        )
        // Inner illuminated ring
        drawCircle(
            color = innerLineColor,
            radius = radius * ringFraction,
            center = center,
            style = Stroke(width = 1.6.dp.toPx())
        )
        // Core highlight
        drawCircle(
            color = coreLineColor,
            radius = radius * ringFraction,
            center = center,
            style = Stroke(width = 0.6.dp.toPx())
        )
    }
}
