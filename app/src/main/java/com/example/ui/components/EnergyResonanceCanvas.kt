package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.service.CouncilAvatarProfile
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Theoretical debate topic node for Energy Resonance modeling.
 */
data class ResonanceTopicNode(
    val id: String,
    val title: String,
    val field: String,
    val speaker: CouncilAvatarProfile,
    var frequencyHz: Float,
    val targetFrequencyHz: Float = 42.600f,
    var relX: Float, // Normalized 0.0f..1.0f
    var relY: Float, // Normalized 0.0f..1.0f
    val hypothesisSnippet: String
) {
    /**
     * Calculates Q-Factor resonance coupling efficiency to the 42.600 Hz standing wave.
     * Scale 0.0f (complete phase mismatch) to 1.0f (100% harmonic lock).
     */
    val couplingCoherence: Float
        get() {
            val delta = abs(frequencyHz - targetFrequencyHz)
            val q = 150f
            val ratio = if (targetFrequencyHz > 0f) frequencyHz / targetFrequencyHz else 1f
            val denom = sqrt(1f + (q * (ratio - 1f / ratio)).let { it * it })
            return (1f / denom).coerceIn(0.05f, 1.0f)
        }
}

/**
 * Hardware-accelerated Jetpack Compose Canvas component running a 60fps harmonic
 * Energy Resonance wave simulation. Maps active council debate topics to 42.600 Hz
 * planetary standing wave nodes with interactive drag manipulation and visual
 * spectral dispersion.
 */
@Composable
fun EnergyResonanceCanvas(
    nodes: List<ResonanceTopicNode>,
    selectedNodeId: String?,
    onSelectNode: (ResonanceTopicNode) -> Unit,
    onNodeFrequencyChanged: (nodeId: String, newFrequency: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // 60fps continuous sinusoidal wave phase transition
    val infiniteTransition = rememberInfiniteTransition(label = "resonance_wave_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_sweep"
    )

    // Pulse radius animation for 42.6 Hz resonance lock
    val pulseExpand by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_expand"
    )

    var draggedNodeId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(14.dp))
            .background(EchoCard)
            .testTag("energy_resonance_canvas")
            .pointerInput(nodes) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val threshold = 36.dp.toPx()

                    var closestNode: ResonanceTopicNode? = null
                    var closestDist = Float.MAX_VALUE

                    nodes.forEach { node ->
                        val nx = node.relX * w
                        val ny = node.relY * h
                        val dist = hypot(tapOffset.x - nx, tapOffset.y - ny)
                        if (dist < closestDist && dist <= threshold) {
                            closestDist = dist
                            closestNode = node
                        }
                    }

                    if (closestNode != null) {
                        onSelectNode(closestNode!!)
                    }
                }
            }
            .pointerInput(nodes) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val threshold = 40.dp.toPx()

                        nodes.forEach { node ->
                            val nx = node.relX * w
                            val ny = node.relY * h
                            if (hypot(startOffset.x - nx, startOffset.y - ny) <= threshold) {
                                draggedNodeId = node.id
                                onSelectNode(node)
                                return@detectDragGestures
                            }
                        }
                    },
                    onDragEnd = {
                        draggedNodeId = null
                    },
                    onDragCancel = {
                        draggedNodeId = null
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val targetNode = nodes.find { it.id == draggedNodeId } ?: return@detectDragGestures
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Update relative position clamped within bounds
                        val newX = (targetNode.relX + dragAmount.x / w).coerceIn(0.12f, 0.88f)
                        val newY = (targetNode.relY + dragAmount.y / h).coerceIn(0.15f, 0.85f)
                        targetNode.relX = newX
                        targetNode.relY = newY

                        // Proximity to center adjusts frequency towards the 42.600 Hz harmonic singularity
                        val centerX = 0.5f
                        val centerY = 0.5f
                        val distToCenter = hypot(newX - centerX, newY - centerY)
                        // Near center -> locks exactly onto 42.600 Hz. Dragging towards perimeter shifts frequency (30.0 - 85.2 Hz)
                        val dynamicFreq = (42.600f + (distToCenter * 65f * (if (newX > centerX) 1f else -1f))).coerceIn(28.0f, 92.0f)
                        targetNode.frequencyHz = dynamicFreq
                        onNodeFrequencyChanged(targetNode.id, dynamicFreq)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w * 0.5f
            val centerY = h * 0.5f

            // 1. Subtle Harmonic Coordinate Grid
            val gridSpacing = 40f
            for (gx in 0..(w / gridSpacing).toInt()) {
                val x = gx * gridSpacing
                drawLine(
                    color = EchoCardStroke.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 0.5f
                )
            }
            for (gy in 0..(h / gridSpacing).toInt()) {
                val y = gy * gridSpacing
                drawLine(
                    color = EchoCardStroke.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 0.5f
                )
            }

            // 2. Central 42.600 Hz Planetary Standing Wave Resonance Core
            val coreRadius = 48f
            // Outer radiant spectral glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EchoCyan.copy(alpha = 0.35f),
                        EchoCyan.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = coreRadius * 2.5f * pulseExpand
                ),
                radius = coreRadius * 2.5f * pulseExpand,
                center = Offset(centerX, centerY)
            )

            // Outer resonance boundary ring
            drawCircle(
                color = EchoCyan.copy(alpha = 0.55f),
                radius = coreRadius * 1.6f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), phase * 10f))
            )

            // Core solid emblem
            drawCircle(
                color = EchoCyan.copy(alpha = 0.2f),
                radius = coreRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = EchoCyan,
                radius = coreRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.5f)
            )

            // Center Label (42.600 Hz Master Node)
            val centerText = "42.600 Hz"
            val centerSub = "MASTER RES."
            val centerMeasured = textMeasurer.measure(
                text = centerText,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
            )
            val subMeasured = textMeasurer.measure(
                text = centerSub,
                style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium, color = EchoGreen, fontFamily = FontFamily.Monospace)
            )
            drawText(
                textLayoutResult = centerMeasured,
                topLeft = Offset(centerX - centerMeasured.size.width / 2f, centerY - 14f)
            )
            drawText(
                textLayoutResult = subMeasured,
                topLeft = Offset(centerX - subMeasured.size.width / 2f, centerY + 4f)
            )

            // 3. Harmonic Coupling Wave Lines connecting each node to the center and neighboring nodes
            nodes.forEach { node ->
                val nx = node.relX * w
                val ny = node.relY * h
                val coherence = node.couplingCoherence
                val isLocked = abs(node.frequencyHz - 42.600f) <= 0.25f

                // Draw sinusoidal wave path from Center to Node
                val wavePath = Path()
                wavePath.moveTo(centerX, centerY)

                val steps = 30
                val dx = nx - centerX
                val dy = ny - centerY
                val dist = hypot(dx, dy)
                val perpX = -dy / dist
                val perpY = dx / dist

                val waveAmplitude = (1f - coherence) * 18f + 4f // Higher lock = less turbulent amplitude
                val waveFreq = 3f + (node.frequencyHz / 15f)

                for (s in 1..steps) {
                    val progress = s.toFloat() / steps.toFloat()
                    val px = centerX + dx * progress
                    val py = centerY + dy * progress
                    val waveOffset = sin(progress * Math.PI.toFloat() * waveFreq + phase) * waveAmplitude * sin(progress * Math.PI.toFloat())
                    val wx = px + perpX * waveOffset
                    val wy = py + perpY * waveOffset
                    wavePath.lineTo(wx, wy)
                }

                // Line stroke color based on coherence
                val lineColor = when {
                    isLocked -> EchoAmber.copy(alpha = 0.85f)
                    coherence > 0.6f -> EchoGreen.copy(alpha = 0.70f)
                    else -> EchoCyan.copy(alpha = 0.40f)
                }

                drawPath(
                    path = wavePath,
                    color = lineColor,
                    style = Stroke(width = if (isLocked) 2.5f else 1.5f)
                )

                // 4. Render Topic Node
                val nodeRadius = 26f
                val isSelected = node.id == selectedNodeId

                // Radial Spectral Glow around node
                if (isLocked || isSelected) {
                    val glowColor = if (isLocked) EchoAmber else node.speaker.primaryColor
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(nx, ny),
                            radius = nodeRadius * 2.2f
                        ),
                        radius = nodeRadius * 2.2f,
                        center = Offset(nx, ny)
                    )
                }

                // Node Base Circle
                drawCircle(
                    color = node.speaker.primaryColor.copy(alpha = if (isSelected) 0.35f else 0.18f),
                    radius = nodeRadius,
                    center = Offset(nx, ny)
                )
                drawCircle(
                    color = if (isSelected) Color.White else node.speaker.primaryColor,
                    radius = nodeRadius,
                    center = Offset(nx, ny),
                    style = Stroke(width = if (isSelected) 2.8f else 1.8f)
                )

                // Frequency readout inside node
                val freqLabel = String.format("%.1f", node.frequencyHz) + " Hz"
                val freqMeasured = textMeasurer.measure(
                    text = freqLabel,
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) EchoAmber else EchoText,
                        fontFamily = FontFamily.Monospace
                    )
                )
                drawText(
                    textLayoutResult = freqMeasured,
                    topLeft = Offset(nx - freqMeasured.size.width / 2f, ny - 6f)
                )

                // Speaker name above node
                val speakerLabel = node.speaker.displayName.take(8)
                val speakerMeasured = textMeasurer.measure(
                    text = speakerLabel,
                    style = TextStyle(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = node.speaker.primaryColor
                    )
                )
                drawText(
                    textLayoutResult = speakerMeasured,
                    topLeft = Offset(nx - speakerMeasured.size.width / 2f, ny - nodeRadius - 13f)
                )

                // Topic title below node
                val titleTruncated = node.title.take(16) + (if (node.title.length > 16) ".." else "")
                val titleMeasured = textMeasurer.measure(
                    text = titleTruncated,
                    style = TextStyle(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                        color = EchoMuted
                    )
                )
                drawText(
                    textLayoutResult = titleMeasured,
                    topLeft = Offset(nx - titleMeasured.size.width / 2f, ny + nodeRadius + 3f)
                )
            }

            // Telemetry overlay in bottom left corner
            val telemetryText = "HARMONIC CARRIER: 42.600 Hz • Q-FACTOR: 150.0 • NODES: ${nodes.size}"
            val telemetryMeasured = textMeasurer.measure(
                text = telemetryText,
                style = TextStyle(
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = EchoCyan.copy(alpha = 0.8f)
                )
            )
            drawText(
                textLayoutResult = telemetryMeasured,
                topLeft = Offset(12f, h - 18f)
            )
        }
    }
}
