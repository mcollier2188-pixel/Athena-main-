package com.example.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.DolaVoiceManager
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.model.NetworkNode
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
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
import com.example.ui.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sin

data class VoiceJournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val transcript: String,
    val carrierFrequency: Double = 85.2,
    val activeNodeCode: String = "N1",
    val activeNodeName: String = "Portsmouth Master",
    val isSynced: Boolean = true
) {
    val formattedTime: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Feature 3: Dola Voice Journal — Auto-Log & Sync Composable
 *
 * Hold-to-speak recording using DolaVoiceManager.
 * Automatically timestamps and tags with current carrier frequency and active node.
 * Saves directly into Workspace and supports read-back via Dola voice.
 */
@Composable
fun DolaVoiceJournalTab(
    workspaceViewModel: WorkspaceViewModel? = null,
    activeFrequency: Double = 85.2,
    activeNode: NetworkNode = NetworkNode.ALL_NODES.first(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val liveTranscript by voiceManager.lastRecognizedText.collectAsState()
    val rmsLevel by voiceManager.rmsState.collectAsState()

    val entries = remember {
        mutableStateListOf(
            VoiceJournalEntry(
                timestamp = System.currentTimeMillis() - 1000 * 60 * 42,
                transcript = "Bedrock resonance confirmed at 42.600 Hz. Ground coupling at Hilsea Lines indicates standing wave coherence of ninety-eight percent.",
                carrierFrequency = 42.6,
                activeNodeCode = "N1",
                activeNodeName = "Portsmouth Master",
                isSynced = true
            ),
            VoiceJournalEntry(
                timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                transcript = "Harmonic lock synchronized with Southsea Common node. Phase drift compensation stable within point zero two hertz.",
                carrierFrequency = 85.2,
                activeNodeCode = "N6",
                activeNodeName = "Southsea Common",
                isSynced = true
            )
        )
    }

    var currentlyReadingId by remember { mutableStateOf<String?>(null) }

    // Permission launcher for microphone
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (!granted) {
            Toast.makeText(context, "Microphone permission is required for Dola Voice Journal", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
            .testTag("dola_voice_journal_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    .background(EchoCyan.copy(alpha = 0.2f))
                                    .border(1.dp, EchoCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "DOLA VOICE JOURNAL",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Auto-Log & Synchronous Bedrock Field Diary",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        // Sync indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(12.dp))
                            Text(
                                text = "AUTO-SYNC ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "Hold to speak or tap record. Dola transcribes voice observations, tags them with live carrier frequency and node coordinates, and logs them into your Workspace.",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 16.sp
                    )

                    // Current Telemetry Tags Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CARRIER: ${activeFrequency} Hz",
                                fontSize = 9.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NODE: ${activeNode.id} ${activeNode.name}",
                                fontSize = 9.sp,
                                color = EchoCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Voice Recording Station
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (isListening) EchoCyan else EchoCardStroke),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Audio waveform canvas when listening
                    if (isListening) {
                        DolaAudioRmsWaveform(rmsLevel = rmsLevel)
                        Text(
                            text = if (liveTranscript.isBlank()) "Dola is listening... speak clearly" else "\"$liveTranscript\"",
                            fontSize = 12.sp,
                            color = EchoCyan,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // Large Mic Button
                    Button(
                        onClick = {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                return@Button
                            }

                            if (isListening) {
                                voiceManager.stopListening()
                                val captured = liveTranscript.trim()
                                if (captured.isNotBlank()) {
                                    val newEntry = VoiceJournalEntry(
                                        transcript = captured,
                                        carrierFrequency = activeFrequency,
                                        activeNodeCode = activeNode.id,
                                        activeNodeName = activeNode.name,
                                        isSynced = true
                                    )
                                    entries.add(0, newEntry)

                                    // Save to workspace notes if viewmodel is provided
                                    workspaceViewModel?.createNote(
                                        name = "Voice Log [${newEntry.activeNodeCode}] ${newEntry.formattedTime}",
                                        initialText = "${captured}\n\n---\nCarrier: ${newEntry.carrierFrequency} Hz | Node: ${newEntry.activeNodeName} | Logged by Dola Voice Module."
                                    )

                                    HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                    Toast.makeText(context, "Voice entry logged and saved to Workspace", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                HarmonicToneGenerator.SHARED.playChime(SoundEffectType.HARMONIC_PING)
                                voiceManager.startListening(
                                    onResult = { spoken ->
                                        if (spoken.isNotBlank()) {
                                            val newEntry = VoiceJournalEntry(
                                                transcript = spoken.trim(),
                                                carrierFrequency = activeFrequency,
                                                activeNodeCode = activeNode.id,
                                                activeNodeName = activeNode.name,
                                                isSynced = true
                                            )
                                            entries.add(0, newEntry)
                                            workspaceViewModel?.createNote(
                                                name = "Voice Log [${newEntry.activeNodeCode}] ${newEntry.formattedTime}",
                                                initialText = "${spoken.trim()}\n\n---\nCarrier: ${newEntry.carrierFrequency} Hz | Node: ${newEntry.activeNodeName} | Logged by Dola Voice Module."
                                            )
                                            HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                            Toast.makeText(context, "Voice entry logged to Workspace", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(52.dp)
                            .testTag("dola_voice_record_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) EchoRed else EchoCyan,
                            contentColor = EchoOnCyan
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "STOP & LOG OBSERVATION" else "RECORD OBSERVATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Section Title: Logged Entries
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FIELD JOURNAL ENTRIES (${entries.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )
                if (isSpeaking) {
                    Text(
                        text = "● Dola Speaking...",
                        fontSize = 10.sp,
                        color = EchoCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List of entries
        items(entries, key = { it.id }) { entry ->
            val isCurrentReading = currentlyReadingId == entry.id && isSpeaking

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_entry_${entry.id}"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (isCurrentReading) EchoCyan else EchoCardStroke),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header: Time, Frequency tag, Node tag, Sync status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.formattedTime,
                            fontSize = 10.sp,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${entry.carrierFrequency} Hz",
                                    fontSize = 9.sp,
                                    color = EchoCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = entry.activeNodeCode,
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Transcript Text
                    Text(
                        text = entry.transcript,
                        fontSize = 13.sp,
                        color = EchoText,
                        lineHeight = 18.sp
                    )

                    // Node Location info
                    Text(
                        text = "Recorded at ${entry.activeNodeName}",
                        fontSize = 9.sp,
                        color = EchoMuted,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Read Aloud with Dola
                        OutlinedButton(
                            onClick = {
                                if (isCurrentReading) {
                                    voiceManager.stopSpeaking()
                                    currentlyReadingId = null
                                } else {
                                    currentlyReadingId = entry.id
                                    voiceManager.speak(entry.transcript)
                                }
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_read_dola_${entry.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isCurrentReading) EchoRed else EchoCyan
                            ),
                            border = BorderStroke(1.dp, if (isCurrentReading) EchoRed else EchoCyan.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrentReading) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isCurrentReading) "Stop Reading" else "Dola Read-Back",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Copy to clipboard
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Dola Voice Entry", entry.transcript)
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied transcript to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EchoMuted, modifier = Modifier.size(14.dp))
                        }

                        // Delete
                        IconButton(
                            onClick = {
                                if (currentlyReadingId == entry.id) {
                                    voiceManager.stopSpeaking()
                                }
                                entries.remove(entry)
                                Toast.makeText(context, "Journal entry removed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = EchoRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DolaAudioRmsWaveform(rmsLevel: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "rms_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(EchoDarkSurface)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val amplitude = (rmsLevel.coerceIn(0f, 15f) / 15f * centerY * 0.85f).coerceAtLeast(4f)

        val points = 60
        val dx = width / points

        for (i in 0 until points - 1) {
            val x1 = i * dx
            val y1 = centerY + sin(i * 0.25f + phase) * amplitude
            val x2 = (i + 1) * dx
            val y2 = centerY + sin((i + 1) * 0.25f + phase) * amplitude

            drawLine(
                color = EchoCyan,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
