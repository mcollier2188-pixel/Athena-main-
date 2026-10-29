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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ai.DolaPersonalityProvider
import kotlin.math.sin

/**
 * MasterReferenceWaveformBanner
 *
 * A compact, real-time animated frequency visualizer at the top of the screen
 * representing the 85.2 Hz 'Master Reference' frequency, animating gently
 * to reflect the system's active state.
 *
 * Designed as an informational status indicator only with zero intrusive controls.
 */
@Composable
fun MasterReferenceWaveformBanner(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_anim"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("master_reference_waveform_banner")
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF090D14),
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.35f),
                    Color(0xFF00E676).copy(alpha = 0.25f),
                    Color(0xFFFFD54F).copy(alpha = 0.35f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Header Row: Informational badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing beacon dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "85.2 Hz",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MASTER REFERENCE",
                        color = Color(0xFF80DEEA),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.1.sp
                    )
                }

                // Sub-status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF131D2E))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚓ Portsmouth Harbour",
                            color = Color(0xFFB0BEC5),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F2624))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⏱️ 30s Protocol",
                            color = Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Animated Real-Time Oscilloscope Waveform Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF04060A))
                    .border(0.5.dp, Color(0xFF1A2638), RoundedCornerShape(6.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val midY = height / 2f

                    // Subtle background center baseline
                    drawLine(
                        color = Color(0xFF101B2B),
                        start = Offset(0f, midY),
                        end = Offset(width, midY),
                        strokeWidth = 1f
                    )

                    // Draw 85.2 Hz primary harmonic wave
                    val primaryPath = Path()
                    val secondaryPath = Path()
                    val cycles = 4.2f // Visually appealing frequency wave cycles

                    val step = 3f
                    var x = 0f
                    var isFirst = true

                    val primaryAmp = if (isActive) height * 0.36f else height * 0.12f
                    val secondaryAmp = if (isActive) height * 0.18f else height * 0.06f

                    while (x <= width) {
                        val progress = x / width
                        // 85.2 Hz fundamental
                        val wave1 = sin((progress * cycles * 2 * Math.PI + phase).toDouble()).toFloat()
                        // 2nd harmonic (octave overtone)
                        val wave2 = sin((progress * cycles * 4 * Math.PI - phase * 0.7f).toDouble()).toFloat()

                        val y1 = midY + wave1 * primaryAmp
                        val y2 = midY + wave2 * secondaryAmp

                        if (isFirst) {
                            primaryPath.moveTo(x, y1)
                            secondaryPath.moveTo(x, y2)
                            isFirst = false
                        } else {
                            primaryPath.lineTo(x, y1)
                            secondaryPath.lineTo(x, y2)
                        }
                        x += step
                    }

                    // Secondary harmonic wave (emerald / gold aura)
                    drawPath(
                        path = secondaryPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00E676).copy(alpha = 0.45f),
                                Color(0xFFFFD54F).copy(alpha = 0.6f),
                                Color(0xFF00E5FF).copy(alpha = 0.45f)
                            )
                        ),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Primary 85.2 Hz Master Waveform (cyan glowing stroke)
                    drawPath(
                        path = primaryPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF80DEEA),
                                Color(0xFF00E676),
                                Color(0xFF00E5FF)
                            )
                        ),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
