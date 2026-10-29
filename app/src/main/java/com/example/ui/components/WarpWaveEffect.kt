package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RocketLaunch
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Color Palette for Warp Wave & Echo Verse
private val Gold85Hz = Color(0xFFFFD700)
private val AthenaCyan = Color(0xFF00E5FF)
private val EngineerEmerald = Color(0xFF00E676)
private val DeepSpaceVignette = Color(0xCC050A14)

/**
 * Data model for hyperspace warp streak particle.
 */
private data class WarpStreak(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val length: Float,
    val angleRad: Float,
    val color: Color
)

/**
 * WarpWaveEffect Composable
 *
 * Renders a pulsing, multi-harmonic sinusoidal waveform animation overlaid across the screen
 * using Jetpack Compose Canvas to represent Echo Verse travel during the Departure sequence.
 *
 * Features:
 * - Primary 85.2 Hz sinusoidal master wave with dynamic amplitude pulsing
 * - Secondary counter-phase 42.6 Hz fundamental harmonic wave
 * - High-frequency 170.4 Hz quantum ripple wave
 * - Hyperspace warp-speed radial streaks streaming from the central nodal horizon
 * - Atmospheric deep-space translucent vignette with real-time HUD telemetry badge
 */
@Composable
fun WarpWaveEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    frequencyLabel: String = "85.200 Hz",
    statusMessage: String = "WARP TRANSIT ACTIVE · DESTINATION: ECHO VERSE",
    onDismiss: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(600))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "warp_wave_transition")

        // 1. Master Phase Animation (Continuous Wave Travel)
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "master_wave_phase"
        )

        // 2. Harmonic Pulse Scale (Pulsing Amplitude)
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "harmonic_pulse_scale"
        )

        // 3. Hyperspace Particle Stream Progress (0.0 to 1.0)
        val particleProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_stream_progress"
        )

        // Precompute fixed pseudorandom warp streaks
        val warpStreaks = remember {
            val random = Random(42)
            List(48) {
                val angle = random.nextFloat() * (2 * PI).toFloat()
                val speed = 0.5f + random.nextFloat() * 1.5f
                val length = 30f + random.nextFloat() * 80f
                val color = when (random.nextInt(3)) {
                    0 -> Gold85Hz
                    1 -> AthenaCyan
                    else -> EngineerEmerald
                }
                WarpStreak(
                    initialX = 0f,
                    initialY = 0f,
                    speed = speed,
                    length = length,
                    angleRad = angle,
                    color = color
                )
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DeepSpaceVignette)
                .testTag("warp_wave_effect_overlay")
        ) {
            // Main Canvas Rendering Sinusoidal Waves & Hyperspace Streaks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val midX = width / 2f
                val midY = height / 2f

                // -------------------------------------------------------------
                // A. RADIAL WARP VIGNETTE GLOW
                // -------------------------------------------------------------
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Gold85Hz.copy(alpha = 0.18f * pulseScale),
                            AthenaCyan.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(midX, midY),
                        radius = width * 0.75f
                    ),
                    radius = width * 0.75f,
                    center = Offset(midX, midY)
                )

                // -------------------------------------------------------------
                // B. HYPERSPACE STREAK PARTICLES RADIATING OUTWARDS
                // -------------------------------------------------------------
                val maxRadius = width.coerceAtLeast(height) * 0.7f
                for (streak in warpStreaks) {
                    val currentDist = ((particleProgress * streak.speed) % 1f) * maxRadius
                    val startX = midX + cos(streak.angleRad) * currentDist
                    val startY = midY + sin(streak.angleRad) * currentDist
                    val endX = midX + cos(streak.angleRad) * (currentDist + streak.length)
                    val endY = midY + sin(streak.angleRad) * (currentDist + streak.length)

                    val alpha = (currentDist / maxRadius).coerceIn(0.1f, 0.85f)
                    drawLine(
                        color = streak.color.copy(alpha = alpha),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = (1.5.dp.toPx() * (currentDist / maxRadius + 0.5f)),
                        cap = StrokeCap.Round
                    )
                }

                // -------------------------------------------------------------
                // C. SECONDARY 42.6 HZ FUNDAMENTAL WAVE (Lower Frequency, Cyan)
                // -------------------------------------------------------------
                val pathFundamental = Path()
                val fundamentalAmp = (height * 0.08f) * pulseScale
                val fundamentalCycles = 2.0

                for (x in 0..width.toInt() step 6) {
                    val progress = x.toFloat() / width
                    val waveY = midY + sin(progress * fundamentalCycles * 2 * PI + (phase * 0.75f)).toFloat() * fundamentalAmp
                    if (x == 0) pathFundamental.moveTo(x.toFloat(), waveY) else pathFundamental.lineTo(x.toFloat(), waveY)
                }

                drawPath(
                    path = pathFundamental,
                    brush = Brush.horizontalGradient(
                        listOf(
                            AthenaCyan.copy(alpha = 0.2f),
                            AthenaCyan.copy(alpha = 0.85f),
                            AthenaCyan.copy(alpha = 0.2f)
                        )
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // -------------------------------------------------------------
                // D. PRIMARY 85.2 HZ MASTER SINUSOIDAL WARP WAVE (Gold 85Hz)
                // -------------------------------------------------------------
                val pathMaster = Path()
                val masterAmp = (height * 0.14f) * pulseScale
                val masterCycles = 4.0 // 4 full sinusoidal crests representing 85.2 Hz 2nd harmonic

                for (x in 0..width.toInt() step 4) {
                    val progress = x.toFloat() / width
                    val waveY = midY + sin(progress * masterCycles * 2 * PI + phase).toFloat() * masterAmp
                    if (x == 0) pathMaster.moveTo(x.toFloat(), waveY) else pathMaster.lineTo(x.toFloat(), waveY)
                }

                // Radiant Glow Underlay for Master Wave
                drawPath(
                    path = pathMaster,
                    brush = Brush.horizontalGradient(
                        listOf(
                            AthenaCyan.copy(alpha = 0.4f),
                            Gold85Hz.copy(alpha = 0.6f * pulseScale),
                            EngineerEmerald.copy(alpha = 0.4f)
                        )
                    ),
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )

                // Crisp Bright Core Line for Master Wave
                drawPath(
                    path = pathMaster,
                    brush = Brush.horizontalGradient(
                        listOf(
                            AthenaCyan,
                            Gold85Hz,
                            EngineerEmerald
                        )
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // -------------------------------------------------------------
                // E. TERTIARY 170.4 HZ QUANTUM RIPPLE WAVE (High-Q Emerald)
                // -------------------------------------------------------------
                val pathRipple = Path()
                val rippleAmp = (height * 0.04f) * (2f - pulseScale)
                val rippleCycles = 8.0 // 8 rapid oscillations

                for (x in 0..width.toInt() step 4) {
                    val progress = x.toFloat() / width
                    val waveY = midY + sin(progress * rippleCycles * 2 * PI - phase * 1.5f).toFloat() * rippleAmp
                    if (x == 0) pathRipple.moveTo(x.toFloat(), waveY) else pathRipple.lineTo(x.toFloat(), waveY)
                }

                drawPath(
                    path = pathRipple,
                    color = EngineerEmerald.copy(alpha = 0.55f),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )

                // -------------------------------------------------------------
                // F. CENTRAL NODAL SINGULARITY CORE
                // -------------------------------------------------------------
                drawCircle(
                    color = Gold85Hz.copy(alpha = 0.9f),
                    radius = 4.dp.toPx() * pulseScale,
                    center = Offset(midX, midY)
                )
                drawCircle(
                    color = AthenaCyan.copy(alpha = 0.5f),
                    radius = 12.dp.toPx() * pulseScale,
                    center = Offset(midX, midY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // -----------------------------------------------------------------
            // TOP HUD TELEMETRY BANNER OVERLAY
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE6080E1C))
                    .border(1.5.dp, Gold85Hz.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("warp_transit_hud_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Gold85Hz.copy(alpha = 0.25f))
                            .border(1.dp, Gold85Hz, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Gold85Hz,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "WARP TRANSIT VELOCITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold85Hz,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EngineerEmerald)
                            )
                        }
                        Text(
                            text = "$statusMessage ($frequencyLabel)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // BOTTOM HUD VELOCITY INDICATOR
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE6080E1C))
                    .border(1.dp, AthenaCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AthenaCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "85.2 HZ QUANTUM HARMONIC LOCK · TRANSIT VECTOR: 100%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AthenaCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
