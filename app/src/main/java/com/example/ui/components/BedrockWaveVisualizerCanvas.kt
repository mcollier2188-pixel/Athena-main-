package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkNode
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Gold85Hz = Color(0xFFFFD700)

/**
 * BedrockWaveVisualizerCanvas Composable
 *
 * Renders the 42.6 Hz standing wave traveling between all 11 network nodes.
 * Features:
 * - Geometric node layout mapping N1 Portsmouth and N2–N11
 * - Radiating wave lines connecting Portsmouth to all other nodes (thicker/brighter when strong)
 * - Real-time animated 1:2 frequency ratio Lissajous figure (42.6 Hz : 85.2 Hz) at the center
 * - Dark blue/cyan theme adhering to ATHENA design language
 */
@Composable
fun BedrockWaveVisualizerCanvas(
    activeFrequency: Double = 42.6,
    isPlaying: Boolean = true,
    selectedNode: NetworkNode? = null,
    onNodeSelected: ((NetworkNode) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val nodes = NetworkNode.ALL_NODES
    val infiniteTransition = rememberInfiniteTransition(label = "bedrock_wave_anim")

    // Lissajous & Wave phase animation
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bedrock_wave_visualiser_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Telemetry Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(EchoCyan.copy(alpha = 0.2f))
                            .border(1.dp, EchoCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = "BEDROCK WAVE VISUALISER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "42.600 Hz Subterranean Standing Wave · 11-Node Array",
                            fontSize = 9.sp,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPlaying) EchoGreen.copy(alpha = 0.15f) else EchoDarkSurface)
                        .border(1.dp, if (isPlaying) EchoGreen.copy(alpha = 0.5f) else EchoCardStroke, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPlaying) "WAVE ACTIVE" else "STANDBY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) EchoGreen else EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Live Standing Wave Canvas with Lissajous Figure
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(EchoDarkSurface)
                .border(1.5.dp, EchoCardStroke, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val center = Offset(width / 2f, height / 2f)

                // 1. Concentric Standing Wave Interference Rings
                val ringCount = 5
                val maxRadius = width.coerceAtMost(height) * 0.44f
                for (i in 1..ringCount) {
                    val ringRadius = maxRadius * (i.toFloat() / ringCount) * (if (isPlaying) pulseScale else 1f)
                    drawCircle(
                        color = EchoCyan.copy(alpha = (0.06f * (ringCount - i + 1)).coerceIn(0.02f, 0.25f)),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Node Positions around perimeter and center
                // N1 Portsmouth at South-Center (master clock anchor)
                val nodePositions = mutableMapOf<String, Offset>()
                val numPerimeterNodes = nodes.size - 1

                // Portsmouth placed near bottom center (50.8° N)
                val portsmouthPos = Offset(center.x, center.y + maxRadius * 0.72f)
                nodePositions["N1"] = portsmouthPos

                // Position remaining 10 nodes circularly
                val otherNodes = nodes.filter { it.id != "N1" }
                otherNodes.forEachIndexed { index, node ->
                    val angle = (2 * PI * index / numPerimeterNodes) - (PI / 2) // Start from top
                    val r = maxRadius * (0.85f + 0.1f * sin(index.toDouble() + phase)).toFloat()
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()
                    nodePositions[node.id] = Offset(x, y)
                }

                // 3. Radiating Lines Connecting Portsmouth (N1) to Each Node
                otherNodes.forEachIndexed { index, node ->
                    val targetPos = nodePositions[node.id] ?: center
                    val signalStrength = (0.5f + 0.5f * sin((phase + index * 0.6).toDouble())).toFloat()

                    // Thicker and brighter when signal is strong
                    val strokeWidth = (1.5f + signalStrength * 3.5f).dp.toPx()
                    val lineAlpha = (0.25f + signalStrength * 0.65f).coerceIn(0.1f, 0.95f)
                    val lineColor = if (node.baseRatio == 2 || node.baseRatio == 4) Gold85Hz else EchoCyan

                    // Draw direct transmission line
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Gold85Hz.copy(alpha = lineAlpha),
                                lineColor.copy(alpha = lineAlpha * 0.8f)
                            ),
                            start = portsmouthPos,
                            end = targetPos
                        ),
                        start = portsmouthPos,
                        end = targetPos,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 4. Central Lissajous Figure (42.6 Hz vs 85.2 Hz = 1:2 frequency ratio)
                val lissajousPath = Path()
                val lissajousWidth = maxRadius * 0.42f
                val lissajousHeight = maxRadius * 0.42f
                val aRatio = 1.0 // 42.6 Hz
                val bRatio = 2.0 // 85.2 Hz
                val delta = phase.toDouble()

                val steps = 180
                for (step in 0..steps) {
                    val t = (2 * PI * step / steps)
                    val lx = center.x + (lissajousWidth * sin(aRatio * t + delta)).toFloat()
                    val ly = center.y + (lissajousHeight * sin(bRatio * t)).toFloat()
                    if (step == 0) lissajousPath.moveTo(lx, ly) else lissajousPath.lineTo(lx, ly)
                }
                lissajousPath.close()

                // Lissajous Glowing Aura
                drawPath(
                    path = lissajousPath,
                    color = EchoCyan.copy(alpha = 0.35f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Lissajous Crisp Core Line
                drawPath(
                    path = lissajousPath,
                    brush = Brush.sweepGradient(
                        colors = listOf(EchoCyan, Gold85Hz, EchoGreen, EchoCyan),
                        center = center
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // 5. Draw Node Dots & Badges
                nodes.forEach { node ->
                    val pos = nodePositions[node.id] ?: center
                    val isMaster = node.id == "N1"
                    val isSelected = selectedNode?.id == node.id

                    val dotRadius = if (isMaster) 10.dp.toPx() else 6.5.dp.toPx()
                    val dotColor = when {
                        isMaster -> Gold85Hz
                        isSelected -> EchoGreen
                        else -> EchoCyan
                    }

                    // Outer pulse ring for master
                    if (isMaster && isPlaying) {
                        drawCircle(
                            color = Gold85Hz.copy(alpha = 0.3f),
                            radius = dotRadius * pulseScale * 1.5f,
                            center = pos,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Solid node dot
                    drawCircle(color = dotColor, radius = dotRadius, center = pos)
                    drawCircle(color = EchoBg, radius = dotRadius * 0.4f, center = pos)
                }
            }

            // HUD Overlays
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoBg.copy(alpha = 0.85f))
                    .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LISSAJOUS: 42.6 : 85.2 Hz (1:2 LOCK)",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoCyan,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoBg.copy(alpha = 0.85f))
                    .border(1.dp, Gold85Hz.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "N1 PORTSMOUTH MASTER ANCHOR",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold85Hz,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
