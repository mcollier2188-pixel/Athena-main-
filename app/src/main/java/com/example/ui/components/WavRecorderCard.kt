package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.audio.WavAudioExporter
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import java.io.File

/**
 * WAV Audio Recorder & Exporter Card Component:
 * Real-time capture of streaming PCM synthesis with timer, stop, and export to phone file.
 */
@Composable
fun WavRecorderCard(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    currentFrequency: Double = 42.6,
    isPlaying: Boolean = false,
    onStartAudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRecording by WavAudioExporter.isRecording.collectAsState()
    val elapsedSeconds by WavAudioExporter.elapsedSeconds.collectAsState()

    val infinitePulse = rememberInfiniteTransition(label = "rec_pulse")
    val recPulseScale by infinitePulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wav_recorder_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isRecording) EchoRed else EchoCardStroke)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(recPulseScale)
                                .clip(CircleShape)
                                .background(EchoRed)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EchoCyan)
                        )
                    }
                    Text(
                        text = if (isRecording) "RECORDING WAV AUDIO" else "Acoustic Audio Recorder",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) EchoRed else EchoText
                    )
                }

                if (isRecording) {
                    val minutes = elapsedSeconds / 60
                    val seconds = elapsedSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EchoRed
                    )
                } else {
                    Text(
                        text = "16-Bit PCM • 44.1 kHz",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EchoCyan
                    )
                }
            }

            Text(
                text = if (isRecording)
                    "Capturing live synthesized frequency frames into uncompressed audio buffer. Tap Stop to export and save to phone."
                else
                    "Record raw frequency harmonics, Solfeggio bell sweeps, and binaural beats directly to a standard .wav audio file.",
                fontSize = 11.sp,
                color = EchoMuted,
                lineHeight = 16.sp
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRecording) {
                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            val exportedFile = WavAudioExporter.stopAndExport(
                                context = context,
                                frequencyLabel = String.format("%.2fHz", currentFrequency)
                            )
                            if (exportedFile != null) {
                                Toast.makeText(context, "Exported: ${exportedFile.name}", Toast.LENGTH_SHORT).show()
                                WavAudioExporter.shareWavFile(context, exportedFile, "ATHENA ${currentFrequency} Hz Tone")
                            } else {
                                Toast.makeText(context, "Recording was empty.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_stop_export_wav"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Stop & Save WAV to Phone", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            if (!isPlaying && onStartAudio != null) {
                                onStartAudio()
                            }
                            WavAudioExporter.startRecording()
                            Toast.makeText(context, "Recording started (PCM WAV)...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_start_record_wav"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoDarkSurface,
                            contentColor = EchoCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = EchoRed, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start WAV Audio Recording", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
