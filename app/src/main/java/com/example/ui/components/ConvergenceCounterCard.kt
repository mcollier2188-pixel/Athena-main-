package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val Gold85Hz = Color(0xFFFFD700)

data class ConvergenceMilestone(
    val title: String,
    val period: String,
    val isComplete: Boolean,
    val details: String
)

/**
 * Feature 8: Convergence Counter — Q3 2027 Tracker Composable
 *
 * Live countdown to the Southsea Common Convergence Broadcast (Target: Q3 2027 / August 15, 2027).
 * Displays days, hours, minutes, seconds remaining.
 * Live signal telemetry feed from N6 Southsea node.
 * Milestone checkmarks: Oct 2026 ✅, Q1 2027 Sync, Q2 2027 Bible, Q3 2027 Broadcast.
 * Updates automatically every second with a clean, large futuristic display.
 */
@Composable
fun ConvergenceCounterCard(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Target: August 15, 2027 12:00:00 UTC (Q3 2027 Southsea Common Convergence Broadcast)
    val targetTimestampMs = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2027, Calendar.AUGUST, 15, 12, 0, 0)
        }.timeInMillis
    }

    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live 1-second ticker
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingMs = (targetTimestampMs - currentTimeMs).coerceAtLeast(0L)
    val secondsTotal = remainingMs / 1000
    val days = secondsTotal / (24 * 3600)
    val hours = (secondsTotal % (24 * 3600)) / 3600
    val minutes = (secondsTotal % 3600) / 60
    val seconds = secondsTotal % 60

    // Milestones
    val milestones = remember {
        listOf(
            ConvergenceMilestone(
                title = "Laboratory Calibration & Council Assembly",
                period = "Oct 2026",
                isComplete = true,
                details = "Completed. 42 Geniuses catalogued and bedrock frequencies mapped."
            ),
            ConvergenceMilestone(
                title = "11-Node Waveguide Array Synchronous Lock",
                period = "Q1 2027",
                isComplete = false,
                details = "Pending. Synchronization of all 11 UK monitoring stations to 85.200 Hz master."
            ),
            ConvergenceMilestone(
                title = "Master Codex & Verification Manual Release",
                period = "Q2 2027",
                isComplete = false,
                details = "Genius specifications, G41/G42 audit manuals, and complete schematics publication."
            ),
            ConvergenceMilestone(
                title = "Southsea Common Convergence Broadcast",
                period = "Q3 2027",
                isComplete = false,
                details = "Coherent harmonic bedrock transmission live from Portsmouth N1 & N6 Southsea."
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("convergence_counter_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = BorderStroke(1.5.dp, Gold85Hz.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Gold85Hz.copy(alpha = 0.2f))
                            .border(1.dp, Gold85Hz, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BroadcastOnPersonal, contentDescription = null, tint = Gold85Hz, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "CONVERGENCE COUNTER",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Southsea Common Convergence Broadcast · Q3 2027 Tracker",
                            fontSize = 10.sp,
                            color = EchoMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Gold85Hz.copy(alpha = 0.15f))
                        .border(1.dp, Gold85Hz.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE CLOCK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold85Hz,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Big Futuristic Countdown Grid: Days, Hours, Mins, Secs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountdownTimeBlock(value = String.format(Locale.US, "%03d", days), label = "DAYS", modifier = Modifier.weight(1f))
                CountdownTimeBlock(value = String.format(Locale.US, "%02d", hours), label = "HOURS", modifier = Modifier.weight(1f))
                CountdownTimeBlock(value = String.format(Locale.US, "%02d", minutes), label = "MINS", modifier = Modifier.weight(1f))
                CountdownTimeBlock(value = String.format(Locale.US, "%02d", seconds), label = "SECS", isHighlight = true, modifier = Modifier.weight(1f))
            }

            // N6 Southsea Node Real-Time Telemetry Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                            Text(
                                text = "N6 SOUTHSEA NODE COUPLING FEED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "97.4% SIGNAL LOCK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    LinearProgressIndicator(
                        progress = { 0.974f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EchoGreen,
                        trackColor = EchoBg
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Carrier: 42.600 Hz / 85.200 Hz", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                        Text(text = "RSSI: -68 dBm (Optimal)", fontSize = 9.sp, color = EchoCyan, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Milestone Checkmarks List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ROADMAP MILESTONES TO CONVERGENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )

                milestones.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (m.isComplete) EchoGreen.copy(alpha = 0.08f) else EchoDarkSurface)
                            .border(
                                1.dp,
                                if (m.isComplete) EchoGreen.copy(alpha = 0.3f) else EchoCardStroke,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (m.isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (m.isComplete) EchoGreen else EchoMuted,
                            modifier = Modifier.size(16.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = m.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.isComplete) EchoText else EchoText.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (m.isComplete) "${m.period} ✅" else m.period,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.isComplete) EchoGreen else Gold85Hz,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = m.details,
                                fontSize = 9.sp,
                                color = EchoMuted,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            // Broadcast Chime Preview Button
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.CHIME)
                    Toast.makeText(context, "Convergence broadcast harmonic preview chime initiated (85.2 Hz)", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_broadcast_preview_chime"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold85Hz,
                    contentColor = EchoBg
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("TEST CONVERGENCE BROADCAST HARMONIC CHIME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CountdownTimeBlock(
    value: String,
    label: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
        border = BorderStroke(1.dp, if (isHighlight) Gold85Hz.copy(alpha = 0.7f) else EchoCardStroke),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Gold85Hz else EchoText,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = EchoMuted,
                letterSpacing = 1.sp
            )
        }
    }
}
