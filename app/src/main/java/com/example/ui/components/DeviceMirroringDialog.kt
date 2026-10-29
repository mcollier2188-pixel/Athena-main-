package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mirror.DeviceMirroringManager
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import com.example.util.AppOrientation
import com.example.util.OrientationManager

/**
 * DeviceMirroringDialog
 * Native Device Mirroring & Cast Testing Hub for testing across receiver devices.
 */
@Composable
fun DeviceMirroringDialog(
    onDismiss: () -> Unit,
    orientationManager: OrientationManager = OrientationManager.getInstance(LocalContext.current)
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val mirroringManager = remember { DeviceMirroringManager.getInstance(context) }
    val status by mirroringManager.status.collectAsState()
    val orientation by orientationManager.currentOrientation.collectAsState()

    var isCopiedUrl by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mirroringManager.refreshDisplays()
        if (!status.isServerRunning) {
            mirroringManager.startLocalMirrorServer()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("device_mirroring_dialog"),
        containerColor = EchoBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EchoCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Device Mirroring",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Native Device Mirroring",
                            color = EchoText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cast to Any Receiver • Application Testing",
                            color = EchoCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = EchoMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Orientation Status & Quick 90° Toggle Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SCREEN ORIENTATION",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = EchoMuted
                            )
                            Text(
                                text = orientation.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (orientation == AppOrientation.LANDSCAPE) EchoCyan else EchoGreen
                            )
                            Text(
                                text = if (orientation == AppOrientation.LANDSCAPE) "Native Landscape (Default)" else "90° Rotated to Portrait",
                                fontSize = 11.sp,
                                color = EchoMuted
                            )
                        }

                        Button(
                            onClick = {
                                orientationManager.toggle90Degrees(activity)
                                Toast.makeText(
                                    context,
                                    "Rotated 90° to ${if (orientation == AppOrientation.LANDSCAPE) "Portrait" else "Landscape"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan,
                                contentColor = EchoBg
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotate 90 degrees",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Rotate 90°", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1. Android Native Cast & Wireless Display Shortcut
                Card(
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCyan.copy(alpha = 0.3f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CastConnected,
                                contentDescription = "Android Cast",
                                tint = EchoCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "1. Android System Cast & Wireless Display",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                        }
                        Text(
                            text = "Opens the Android OS Cast / Wireless Display / Miracast drawer to pair directly with TVs, Google Cast receivers, and wireless monitors.",
                            fontSize = 12.sp,
                            color = EchoMuted,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val success = mirroringManager.launchNativeCastSettings(context)
                                    if (!success) {
                                        Toast.makeText(context, "Could not open Cast settings directly on this device", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoCyan,
                                    contentColor = EchoBg
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Cast, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open Cast", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    mirroringManager.launchWifiDirectSettings(context)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp), tint = EchoCyan)
                                Spacer(Modifier.width(6.dp))
                                Text("Wi-Fi Direct", fontSize = 12.sp, color = EchoCyan)
                            }
                        }
                    }
                }

                // 2. Local Web Receiver Mirror (Any Device / Browser)
                Card(
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "Web Mirror",
                                    tint = EchoGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "2. Local Web Cast Receiver",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                            }

                            Switch(
                                checked = status.isServerRunning,
                                onCheckedChange = { start ->
                                    if (start) mirroringManager.startLocalMirrorServer()
                                    else mirroringManager.stopLocalMirrorServer()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EchoGreen,
                                    checkedTrackColor = EchoGreen.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Text(
                            text = "Any receiver on your Wi-Fi (PC browser, smart TV, tablet, phone) can open this URL to mirror the app state and live 85.2 Hz telemetry:",
                            fontSize = 12.sp,
                            color = EchoMuted,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        if (status.isServerRunning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoBg)
                                    .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = status.serverUrl,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = EchoCyan
                                        )
                                        Text(
                                            text = "Port 8520 • ${status.connectedClientsCount} Connected Client(s)",
                                            fontSize = 10.sp,
                                            color = EchoGreen
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Mirror URL", status.serverUrl)
                                            clipboard.setPrimaryClip(clip)
                                            isCopiedUrl = true
                                            Toast.makeText(context, "Mirror URL copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isCopiedUrl) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy URL",
                                            tint = if (isCopiedUrl) EchoGreen else EchoCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(status.serverUrl))
                                            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(browserIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = EchoCyan)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Test in Browser", fontSize = 11.sp, color = EchoCyan)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "ATHENA Dola Display Mirror URL: ${status.serverUrl}")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Mirror Link"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp), tint = EchoText)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share Link", fontSize = 11.sp, color = EchoText)
                                }
                            }
                        }
                    }
                }

                // 3. Physical External Displays (HDMI / Virtual Displays)
                Card(
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = "Displays",
                                    tint = EchoCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "3. Connected Hardware Displays (${status.externalDisplays.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                            }

                            IconButton(
                                onClick = { mirroringManager.refreshDisplays() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EchoMuted, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (status.externalDisplays.isEmpty()) {
                            Text(
                                text = "No secondary monitors or HDMI cables detected. Connect an HDMI adapter or start Cast above.",
                                fontSize = 11.sp,
                                color = EchoMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                status.externalDisplays.forEach { display ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(EchoBgAlt, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = display.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = EchoText
                                            )
                                            Text(
                                                text = "${display.width}x${display.height} @ ${display.refreshRate.toInt()}Hz",
                                                fontSize = 10.sp,
                                                color = EchoMuted,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Text(
                                            text = if (display.id == 0) "Primary" else "Secondary",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (display.id == 0) EchoCyan else EchoGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EchoCyan,
                    contentColor = EchoBg
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
