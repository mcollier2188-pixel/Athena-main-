package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

private val Gold85Hz = Color(0xFFFFD700)

/**
 * Feature 5: The 30-Second Timer — Protocol Keeper
 *
 * Dedicated 30-Second Protocol screen.
 * Clean large countdown display.
 * Runs full 30s with gentle pulsing 85.2Hz visual wave animation.
 * At the end, prompts: "Pattern clear? Save observation to Archive?"
 * Quick-save button drops timestamp + note directly into Workspace notes and Archive.
 *
 * Designed with [isScrollable] parameter so that when embedded inside a parent
 * LazyColumn (like BrainstormingLabScreen), it renders as a smooth Column without
 * illegal nested vertical scrolling crashes.
 */
@Composable
fun ProtocolTimer30sView(
    workspaceViewModel: WorkspaceViewModel? = null,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    isScrollable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val totalDurationSeconds = 30
    var remainingTenths by remember { mutableIntStateOf(totalDurationSeconds * 10) }
    var isRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var observationNote by remember { mutableStateOf("") }
    var observationSaved by remember { mutableStateOf(false) }

    // Pulsing 85.2 Hz wave animation transition
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 852, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Countdown timer tick loop
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning && remainingTenths > 0) {
                delay(100)
                remainingTenths -= 1
            }
            if (remainingTenths <= 0) {
                isRunning = false
                isCompleted = true
                toneGenerator.playChime(SoundEffectType.SUCCESS)
            }
        }
    }

    val rootModifier = if (isScrollable) {
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
    } else {
        modifier.fillMaxWidth()
    }

    Column(
        modifier = rootModifier.testTag("protocol_timer_30s_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Calm Minimalist Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Gold85Hz.copy(alpha = 0.2f))
                                .border(1.dp, Gold85Hz, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = Gold85Hz, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "THE 30-SECOND PROTOCOL",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Protocol Keeper · Bedrock Pattern Recognition Calibration",
                                fontSize = 10.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Gold85Hz.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "85.200 Hz CARRIER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold85Hz,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Observe signal stability and harmonic coherence over an uninterrupted 30-second window. Quiet room, focused attention.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // Circular 30s Countdown Display with Pulsing 85.2 Hz Visual Wave
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("protocol_timer_card"),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, if (isRunning) Gold85Hz.copy(alpha = pulseGlow) else EchoCardStroke),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val progressFraction = remainingTenths.toFloat() / (totalDurationSeconds * 10f)
                val secondsDisplay = String.format(Locale.US, "%02d.%d", remainingTenths / 10, remainingTenths % 10)

                // Big Circular Dial with Ring and Center Text
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Background Track
                        drawCircle(
                            color = EchoDarkSurface,
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        // Active Progress Arc
                        val sweepAngle = progressFraction * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(EchoCyan, Gold85Hz, EchoCyan)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Center Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = secondsDisplay,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) EchoGreen else if (isRunning) Gold85Hz else EchoText,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (isCompleted) "PROTOCOL COMPLETE" else if (isRunning) "CALIBRATING..." else "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) EchoGreen else EchoMuted,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Gentle Pulsing 85.2 Hz Visual Waveform Animation Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EchoDarkSurface)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val centerY = height / 2f

                        val baseAmp = if (isRunning) (height * 0.35f * pulseGlow) else (height * 0.15f)
                        val points = 80
                        val dx = width / points

                        val wavePath = Path()
                        for (i in 0..points) {
                            val x = i * dx
                            val y = centerY + sin(i * 0.2f + wavePhase) * baseAmp
                            if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                        }

                        drawPath(
                            path = wavePath,
                            color = if (isRunning) Gold85Hz else EchoCyan.copy(alpha = 0.5f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Primary Timer Controls
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isRunning && !isCompleted) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.CHIME)
                                isRunning = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_timer_start"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold85Hz,
                                contentColor = EchoBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("START 30s PROTOCOL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (isRunning) {
                        Button(
                            onClick = {
                                isRunning = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_timer_pause"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan,
                                contentColor = EchoOnCyan
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("PAUSE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Reset Button
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            remainingTenths = totalDurationSeconds * 10
                            isCompleted = false
                            observationSaved = false
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_timer_reset"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoMuted),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Completion & Pattern Clear Archive Prompt Card
        AnimatedVisibility(visible = isCompleted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pattern_clear_archive_prompt"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoGreen.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Pattern clear? Save observation to Archive?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                    }

                    Text(
                        text = "30-second observation window completed at 85.200 Hz reference. Record any noticed anomalies, harmonic resonance stability, or pattern locks below.",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = observationNote,
                        onValueChange = { observationNote = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_timer_observation"),
                        placeholder = { Text("e.g. Clean 85.2 Hz lock observed, phase coherent throughout 30s window...", color = EchoMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoGreen,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText,
                            focusedContainerColor = EchoDarkSurface,
                            unfocusedContainerColor = EchoDarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                            val noteContent = if (observationNote.isNotBlank()) observationNote.trim() else "30-Second Protocol completed. Clean 85.2 Hz pattern lock verified."

                            workspaceViewModel?.createNote(
                                name = "Protocol Observation — $timestampStr",
                                initialText = "$noteContent\n\n---\nDuration: 30.0s | Carrier: 85.200 Hz | Protocol: ATHENA Pattern Clear Log"
                            )

                            observationSaved = true
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            Toast.makeText(context, "Observation saved to Workspace and Archive log", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !observationSaved,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_save_protocol_observation"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (observationSaved) EchoDarkSurface else EchoGreen,
                            contentColor = if (observationSaved) EchoMuted else EchoBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(if (observationSaved) Icons.Default.BookmarkAdded else Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (observationSaved) "SAVED TO ARCHIVE & WORKSPACE ✓" else "SAVE OBSERVATION TO ARCHIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
