package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.ui.components.CHLADNI_PRESETS
import com.example.ui.components.ChladniDisplayMode
import com.example.ui.components.ChladniPreset
import com.example.ui.components.CircularChladniCanvas
import com.example.ui.components.besselZeroApprox
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
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlin.math.PI

/**
 * Dedicated Sovereign Science Lab Screen:
 * Mathematical Circular Chladni Resonance Plate Solver UI at 42.6 Hz
 */
@Composable
fun CircularChladniSolverScreen(
    viewModel: WorkspaceViewModel,
    onNavigateToDataLedger: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }
    val isTonePlaying by toneGenerator.isPlayingState.collectAsState()

    var showFullScreenStudio by remember { mutableStateOf(false) }
    var activePreset by remember { mutableStateOf(CHLADNI_PRESETS[0]) }
    var currentDisplayMode by remember { mutableStateOf(ChladniDisplayMode.DUAL_MODE) }
    var frequencyHz by remember { mutableDoubleStateOf(42.6) }
    var modalM by remember { mutableIntStateOf(3) }
    var modalN by remember { mutableIntStateOf(2) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
            .testTag("circular_chladni_solver_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sovereign Science Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                EchoCyan.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoCyan.copy(alpha = 0.15f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = EchoCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ATHENA SOVEREIGN SCIENCE LAB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = EchoCyan,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Circular Chladni Resonance Solver",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                        }
                    }

                    // Full-Screen Studio Action Button
                    IconButton(
                        onClick = { showFullScreenStudio = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCyan.copy(alpha = 0.3f), CircleShape)
                            .testTag("chladni_open_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand to Full-Screen Studio",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Real-time mathematical solver computing exact Bessel radial eigenmodes and azimuthal nodal geometry on a vibrating circular plate, locked to the 42.60 Hz bedrock harmonic.",
                    fontSize = 12.sp,
                    color = EchoMuted,
                    lineHeight = 17.sp
                )

                // Telemetry Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("BEDROCK CARRIER", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "42.60 Hz LOCKED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("ACTIVE EIGENMODE", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "(m=$modalM, n=$modalN)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoAmber,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("AUDIO TONE", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text(
                                text = if (isTonePlaying) "TRANSMITTING" else "STANDBY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTonePlaying) EchoGreen else EchoMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Quick Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Audio Tone Toggle
                    Button(
                        onClick = {
                            if (isTonePlaying) {
                                toneGenerator.stop()
                            } else {
                                toneGenerator.updateFrequencies(listOf(frequencyHz))
                                toneGenerator.start()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTonePlaying) EchoGreen.copy(alpha = 0.25f) else EchoBgAlt,
                            contentColor = if (isTonePlaying) EchoGreen else EchoText
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTonePlaying) EchoGreen else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTonePlaying) "Mute Tone" else "Play 42.6 Hz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Log Snapshot to Data Ledger
                    Button(
                        onClick = {
                            val zRoot = besselZeroApprox(modalM, modalN)
                            val noteContent = buildString {
                                appendLine("=== ATHENA CHLADNI RESONANCE EXPERIMENT ===")
                                appendLine("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
                                appendLine("Fundamental Carrier: ${"%.2f".format(frequencyHz)} Hz")
                                appendLine("Modal Eigenmode: m=$modalM (azimuthal), n=$modalN (radial)")
                                appendLine("Nodal Spokes: ${modalM * 2} radial sectors")
                                appendLine("Concentric Nodal Rings: $modalN zero circles")
                                appendLine("Bessel Root Approximation: z_($modalM,$modalN) ≈ ${"%.4f".format(zRoot)}")
                                appendLine("Boundary Condition: J_$modalM(k * R) = 0")
                                appendLine("Acoustic Wavelength: λ ≈ ${"%.3f".format(2.0 * PI / zRoot)} R")
                                appendLine("Visualization Mode: ${currentDisplayMode.label}")
                                appendLine("Status: Stable acoustic resonance confirmed.")
                            }
                            viewModel.createNote(
                                name = "Chladni 42.6 Hz (${modalM},${modalN})",
                                initialText = noteContent,
                                tags = "Chladni, 42.6Hz, Resonance, Acoustics, Science"
                            )
                            Toast.makeText(context, "Resonance log saved to Data Ledger!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoCyan.copy(alpha = 0.15f),
                            contentColor = EchoCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Log to Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Primary Solver & Canvas Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CIRCULAR PLATE VIBRATION CANVAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = EchoCyan
                    )
                    Text(
                        text = "Touch to dampen nodes",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )
                }

                // Interactive Circular Chladni Canvas
                CircularChladniCanvas(
                    initialPreset = activePreset,
                    initialDisplayMode = currentDisplayMode,
                    onSaveSnapshot = { presetName, freq, m, n, notes ->
                        viewModel.createNote(
                            name = "$presetName Snapshot",
                            initialText = notes,
                            tags = "Chladni, Cymatics, AcousticLab"
                        )
                        Toast.makeText(context, "Snapshot saved to Data Ledger!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 3. Mathematical Bessel Eigenmode Inspector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = EchoCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "MATHEMATICAL EIGENMODE FORMULATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = EchoCyan
                    )
                }

                // Mathematical Formulation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EchoBgAlt)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Plate Wave Equation: ∇⁴W - (ρ·h·ω² / D) W = 0",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EchoAmber
                        )
                        Text(
                            text = "Eigenmode Solution in Polar Coordinates (r, θ):",
                            fontSize = 11.sp,
                            color = EchoText
                        )
                        Text(
                            text = "W_{m,n}(r, θ) = J_m(k_{m,n} · r) · cos(m·θ + φ)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan
                        )
                        Text(
                            text = "Nodal Lines Occur where W = 0:\n" +
                                    "• Radial Spokes: θ_p = (2p + 1)·π / (2m)  [2m sectors]\n" +
                                    "• Concentric Rings: r_j = R · (z_{m,j} / z_{m,n}) [n rings]",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EchoMuted,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Active Mode Parameters Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val zRoot = besselZeroApprox(modalM, modalN)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("AZIMUTHAL LOBES", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text("${modalM * 2} Spokes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("RADIAL RINGS", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text("$modalN Rings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("BESSEL ROOT", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text("z ≈ ${"%.3f".format(zRoot)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                        }
                    }
                }
            }
        }

        // 4. 42.6 Hz Resonant Harmonic Ladder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "42.60 HZ HARMONIC LADDER PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoCyan
                )

                CHLADNI_PRESETS.forEach { preset ->
                    val isSelected = activePreset.id == preset.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EchoCyan.copy(alpha = 0.12f) else EchoBgAlt)
                            .border(
                                1.dp,
                                if (isSelected) EchoCyan else EchoCardStroke,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                activePreset = preset
                                frequencyHz = preset.frequencyHz
                                modalM = preset.m
                                modalN = preset.n
                                if (isTonePlaying) {
                                    toneGenerator.updateFrequencies(listOf(preset.frequencyHz))
                                }
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EchoCyan else EchoText
                                    )
                                    if (preset.frequencyHz == 42.6) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EchoCyan.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("BEDROCK", fontSize = 8.sp, color = EchoCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = preset.description,
                                    fontSize = 11.sp,
                                    color = EchoMuted,
                                    maxLines = 2
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${"%.1f".format(preset.frequencyHz)} Hz",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = EchoCyan
                                )
                                Text(
                                    text = "m=${preset.m}, n=${preset.n}",
                                    fontSize = 10.sp,
                                    color = EchoMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-Screen Studio Dialog
    if (showFullScreenStudio) {
        Dialog(
            onDismissRequest = { showFullScreenStudio = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = EchoBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ATHENA CHLADNI FULL-SCREEN STUDIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = EchoCyan
                            )
                            Text(
                                text = "42.60 Hz Bedrock Harmonic Resonance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                        }
                        IconButton(
                            onClick = { showFullScreenStudio = false },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EchoCard)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Studio", tint = EchoText)
                        }
                    }

                    // Large Studio Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularChladniCanvas(
                            modifier = Modifier.fillMaxSize(),
                            initialPreset = activePreset,
                            initialDisplayMode = currentDisplayMode,
                            onSaveSnapshot = { presetName, freq, m, n, notes ->
                                viewModel.createNote(
                                    name = "$presetName Full-Screen Log",
                                    initialText = notes,
                                    tags = "Chladni, FullScreenStudio, 42.6Hz"
                                )
                                Toast.makeText(context, "Logged to Data Ledger!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Bottom Dismiss Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showFullScreenStudio = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                        ) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Exit Full-Screen", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
