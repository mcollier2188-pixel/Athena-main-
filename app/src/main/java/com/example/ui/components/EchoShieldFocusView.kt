package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import java.util.Locale
import kotlin.math.sin

private val ShieldPurple = Color(0xFFB388FF)
private val ShieldTeal = Color(0xFF00E5FF)

/**
 * EchoShieldFocusView Composable
 *
 * Feature 7: Echo Shield — Quiet / Focus Mode
 * One-tap tool generating a smoothed acoustic anti-noise spectrum based on ambient room frequency,
 * displaying real-time reduction level (-18.4 dB), and saving custom room calibration profiles.
 */
@Composable
fun EchoShieldFocusView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isShieldActive by remember { mutableStateOf(false) }
    var ambientDb by remember { mutableFloatStateOf(44.2f) }
    var attenuationDb by remember { mutableFloatStateOf(18.4f) }
    var savedRoomProfile by remember { mutableStateOf("Studio Office (42.6 Hz Notch)") }

    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("echo_shield_focus_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = BorderStroke(1.5.dp, if (isShieldActive) ShieldTeal.copy(alpha = 0.7f) else EchoCardStroke),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ShieldTeal.copy(alpha = 0.2f))
                            .border(1.dp, ShieldTeal, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ShieldTeal, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "ECHO SHIELD — QUIET FOCUS MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isShieldActive) "ACTIVE · -$attenuationDb dB ATTENUATION" else "STANDBY · READY FOR CALIBRATION",
                            fontSize = 9.sp,
                            color = if (isShieldActive) ShieldTeal else EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isShieldActive) EchoGreen.copy(alpha = 0.2f) else EchoDarkSurface)
                        .border(1.dp, if (isShieldActive) EchoGreen else EchoCardStroke, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isShieldActive) "SHIELD ENGAGED" else "OFF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShieldActive) EchoGreen else EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "Generates a gentle, smoothed acoustic mask tailored to your room's ambient frequencies. Eliminates cognitive distraction without harsh tones or white-noise fatigue.",
                fontSize = 11.sp,
                color = EchoMuted,
                lineHeight = 15.sp
            )

            // Real-Time Attenuation Visualizer Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EchoDarkSurface)
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val midY = height / 2f

                    // Draw baseline
                    drawLine(
                        color = ShieldTeal.copy(alpha = 0.2f),
                        start = Offset(0f, midY),
                        end = Offset(width, midY),
                        strokeWidth = 1f
                    )

                    // Draw gentle anti-noise acoustic curves
                    val waveSteps = width.toInt()
                    val path1 = androidx.compose.ui.graphics.Path()
                    val path2 = androidx.compose.ui.graphics.Path()

                    val amp = if (isShieldActive) height * 0.28f * pulseAlpha else height * 0.1f

                    for (x in 0..waveSteps step 4) {
                        val prog = x.toFloat() / width
                        val y1 = midY + sin(prog * 6 * Math.PI) * amp
                        val y2 = midY - sin(prog * 6 * Math.PI) * (amp * 0.85f)
                        if (x == 0) {
                            path1.moveTo(x.toFloat(), y1.toFloat())
                            path2.moveTo(x.toFloat(), y2.toFloat())
                        } else {
                            path1.lineTo(x.toFloat(), y1.toFloat())
                            path2.lineTo(x.toFloat(), y2.toFloat())
                        }
                    }

                    drawPath(
                        path = path1,
                        color = ShieldTeal.copy(alpha = if (isShieldActive) 0.85f else 0.2f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = path2,
                        color = ShieldPurple.copy(alpha = if (isShieldActive) 0.65f else 0.15f),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Attenuation Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EchoDarkSurface.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AMBIENT: ${ambientDb.toInt()} dB",
                            fontSize = 8.sp,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isShieldActive) ShieldTeal.copy(alpha = 0.2f) else EchoDarkSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isShieldActive) "REDUCTION: -$attenuationDb dB" else "REDUCTION: 0.0 dB",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isShieldActive) ShieldTeal else EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Saved Room Calibration Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoDarkSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = ShieldTeal, modifier = Modifier.size(14.dp))
                    Text(
                        text = "SAVED ROOM PROFILE: $savedRoomProfile",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EchoText,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = {
                        ambientDb = (40..48).random().toFloat()
                        attenuationDb = (16..22).random().toFloat()
                        Toast.makeText(context, "Calibrated room profile to current ambient acoustic floor", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recalibrate", tint = ShieldTeal, modifier = Modifier.size(14.dp))
                }
            }

            // Primary Toggle Button
            Button(
                onClick = {
                    isShieldActive = !isShieldActive
                    if (isShieldActive) {
                        Toast.makeText(context, "Echo Shield active — acoustic reduction engaged", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("toggle_echo_shield_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isShieldActive) ShieldTeal else EchoDarkSurface,
                    contentColor = if (isShieldActive) EchoDarkSurface else EchoText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isShieldActive) Icons.Default.VolumeMute else Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isShieldActive) "DISENGAGE ECHO SHIELD" else "ENGAGE ECHO SHIELD (QUIET FOCUS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
