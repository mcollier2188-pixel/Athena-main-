package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

// ECHO-INTERFACE V4.2 Palette Constants (From Visual Style Guide)
val EchoSubDepthBlue = Color(0xFF0A192F)
val EchoHilseaGreen = Color(0xFF00FF66)
val EchoAbyssTeal = Color(0xFF00C2A8)
val EchoNodeAmber = Color(0xFFFF8000)
val EchoAlertRed = Color(0xFFFF383B)
val EchoGlassWhite = Color(0xFFE8EDF2)
val EchoFieldGray = Color(0xFF8899A6)
val EchoVoidBlack = Color(0xFF0E1116)

/**
 * ECHO-INTERFACE V4.2 Image Share & Telemetry Card Dialog.
 * Directly renders the Cold-War bio-feedback planetary acoustic telemetry design
 * with rotary encoder status, oscilloscope vector-scan CRT glow, and 1-tap share.
 */
@Composable
fun EchoInterfaceImageShareDialog(
    frequencyHz: Float = 42.598f,
    onSaveToWorkspaceNote: (title: String, content: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentFreq by remember { mutableFloatStateOf(frequencyHz) }
    var selectedNode by remember { mutableStateOf("N1 HILSEA FUNDAMENTAL") }
    var nodeLocked by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EchoAbyssTeal, RoundedCornerShape(16.dp))
                .testTag("echo_v4_image_share_dialog"),
            colors = CardDefaults.cardColors(containerColor = EchoVoidBlack)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ECHO-INTERFACE V4.2",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoHilseaGreen,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PLANETARY RESONANCE • IMAGE SHARE CARD",
                            fontSize = 10.sp,
                            color = EchoAbyssTeal,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoFieldGray)
                    }
                }

                // =============================================================
                // THE STYLIZED SHAREABLE TELEMETRY CARD (ECHO V4.2 SPEC)
                // =============================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, EchoHilseaGreen.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .testTag("echo_telemetry_share_card_preview"),
                    colors = CardDefaults.cardColors(containerColor = EchoSubDepthBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card Top Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SUB-AUDIBLE SIGNALS // GLOBAL NODES",
                                fontSize = 9.sp,
                                color = EchoFieldGray,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EchoHilseaGreen)
                                )
                                Text(
                                    text = "ONLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoHilseaGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Central Frequency Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoVoidBlack)
                                .border(1.dp, EchoAbyssTeal.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = selectedNode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoAbyssTeal,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.3f Hz", currentFreq),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EchoHilseaGreen,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "RAW TELEMETRY • NO DRIFT",
                                    fontSize = 9.sp,
                                    color = EchoFieldGray,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Oscilloscope Display (CRT Green Vector Scan)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoVoidBlack)
                                .border(1.dp, EchoHilseaGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(78.dp)) {
                                val width = size.width
                                val height = size.height
                                val midY = height / 2

                                // CRT Grid Lines
                                for (i in 1..4) {
                                    val y = height * (i / 5f)
                                    drawLine(
                                        color = EchoHilseaGreen.copy(alpha = 0.15f),
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1f
                                    )
                                }
                                for (i in 1..8) {
                                    val x = width * (i / 9f)
                                    drawLine(
                                        color = EchoHilseaGreen.copy(alpha = 0.15f),
                                        start = Offset(x, 0f),
                                        end = Offset(x, height),
                                        strokeWidth = 1f
                                    )
                                }

                                // Dual-Channel Waveform (CH1: 42.6 Hz, CH2: 85.2 Hz)
                                val path1 = Path()
                                val path2 = Path()
                                val points = 100
                                for (i in 0..points) {
                                    val x = (i / points.toFloat()) * width
                                    val rad1 = (i / points.toFloat()) * (Math.PI * 4).toFloat()
                                    val y1 = midY + sin(rad1) * (height * 0.32f)

                                    val rad2 = (i / points.toFloat()) * (Math.PI * 8).toFloat()
                                    val y2 = midY + sin(rad2) * (height * 0.18f)

                                    if (i == 0) {
                                        path1.moveTo(x, y1)
                                        path2.moveTo(x, y2)
                                    } else {
                                        path1.lineTo(x, y1)
                                        path2.lineTo(x, y2)
                                    }
                                }

                                drawPath(path1, color = EchoHilseaGreen, style = Stroke(width = 2.5f))
                                drawPath(path2, color = EchoAbyssTeal.copy(alpha = 0.7f), style = Stroke(width = 1.5f))
                            }

                            // Overlay Labels
                            Row(
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CH1: 42.6 Hz", fontSize = 8.sp, color = EchoHilseaGreen, fontFamily = FontFamily.Monospace)
                                Text("CH2: 85.2 Hz", fontSize = 8.sp, color = EchoAbyssTeal, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TIME: 10.0 s/div", fontSize = 8.sp, color = EchoFieldGray, fontFamily = FontFamily.Monospace)
                                Text("P31 PHOSPHOR", fontSize = 8.sp, color = EchoHilseaGreen, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Data Panel: Coordinates & Depth
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoDarkSurface)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("COORDINATES:", fontSize = 9.sp, color = EchoFieldGray, fontFamily = FontFamily.Monospace)
                                Text("LAT: 50.8500° N • LON: 1.0874° W", fontSize = 9.sp, color = EchoGlassWhite, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SENSOR DEPTH:", fontSize = 9.sp, color = EchoFieldGray, fontFamily = FontFamily.Monospace)
                                Text("11.2 km // OCEAN TRENCH", fontSize = 9.sp, color = EchoAbyssTeal, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("LOCK STATUS:", fontSize = 9.sp, color = EchoFieldGray, fontFamily = FontFamily.Monospace)
                                Text("LOCKED (11/11 NODES STABLE)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EchoHilseaGreen, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Footer Quote from Style Guide
                        Text(
                            text = "“SOME SIGNALS ARE OLDER THAN WORDS.”\nA QUIETER PLANET IS A MORE CONNECTED ONE.",
                            fontSize = 9.sp,
                            color = EchoFieldGray,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 12.sp
                        )
                    }
                }

                // =============================================================
                // ACTION BUTTONS: SHARE TO IMAGE / SAVE TO WORKSPACE
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val shareText = buildString {
                                appendLine("ECHO-INTERFACE V4.2 TELEMETRY REPORT")
                                appendLine("=====================================")
                                appendLine("Frequency: ${String.format("%.3f Hz", currentFreq)}")
                                appendLine("Node: $selectedNode (LOCKED)")
                                appendLine("Location: LAT 50.8500° N, LON 1.0874° W")
                                appendLine("Depth: 11.2 km | Stability: 99.8%")
                                appendLine("Oscilloscope: CH1 42.6 Hz, CH2 85.2 Hz (P31 Phosphor)")
                                appendLine()
                                appendLine("“SOME SIGNALS ARE OLDER THAN WORDS.”")
                                appendLine("Generated via ATHENA Personal Workspace.")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "ECHO-INTERFACE V4.2 Telemetry Card")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share ECHO V4.2 Telemetry Card"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_share_echo_card"),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoHilseaGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share Card", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val noteContent = buildString {
                                appendLine("# ECHO-INTERFACE V4.2 Telemetry Report")
                                appendLine("**Frequency:** ${String.format("%.3f Hz", currentFreq)}")
                                appendLine("**Node:** $selectedNode (Status: LOCKED)")
                                appendLine("**Coordinates:** LAT 50.8500° N, LON 1.0874° W")
                                appendLine("**Sensor Depth:** 11.2 km | Stability: 99.8%")
                                appendLine()
                                appendLine("## Oscilloscope Waveform Analysis")
                                appendLine("- Dual-Channel P31 Green Phosphor Vector Scan")
                                appendLine("- Primary Fundamental: 42.600 Hz")
                                appendLine("- Secondary Harmonic: 85.200 Hz")
                                appendLine()
                                appendLine("> “SOME SIGNALS ARE OLDER THAN WORDS.”")
                                appendLine()
                                appendLine("[Saved from Workspace Frequency Telemetry Lab]")
                            }

                            onSaveToWorkspaceNote("ECHO V4.2: ${String.format("%.3f Hz", currentFreq)}", noteContent)
                            Toast.makeText(context, "Saved Telemetry Card to Workspace!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_save_echo_card_to_workspace"),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoAbyssTeal, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save to Note", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
