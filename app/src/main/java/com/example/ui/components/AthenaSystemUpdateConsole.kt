package com.example.ui.components

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.system.update.AthenaUpdateManager
import com.example.system.update.UpdatePackageInfo
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ATHENA System Update & OTA Recovery Console.
 * Solves USB installation restrictions by providing:
 * 1. Direct one-tap permission authorization for in-app APK installation.
 * 2. Shortcut to Android Developer Options to toggle "Install via USB" or "Verify apps over USB".
 * 3. Discovery and one-tap installation of local APK update packages.
 * 4. Clean auto-relaunch with state recovery preservation.
 */
@Composable
fun AthenaSystemUpdateConsole(
    onDismiss: () -> Unit,
    toneGenerator: HarmonicToneGenerator? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAuthorized by remember { mutableStateOf(AthenaUpdateManager.canRequestPackageInstalls(context)) }
    var availablePackages by remember { mutableStateOf<List<UpdatePackageInfo>>(emptyList()) }
    var showRestartConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    fun refreshPackages() {
        isAuthorized = AthenaUpdateManager.canRequestPackageInstalls(context)
        availablePackages = AthenaUpdateManager.detectAvailableUpdatePackages(context)
    }

    LaunchedEffect(Unit) {
        refreshPackages()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("athena_system_update_console"),
            color = EchoBg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoCyan.copy(alpha = 0.15f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "System Update",
                                tint = EchoCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ATHENA OS // UPDATE & RECOVERY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Installed: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • Direct OTA Engine",
                                fontSize = 11.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                toneGenerator?.playChime(SoundEffectType.TAP)
                                refreshPackages()
                                Toast.makeText(context, "Scanning for update packages...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EchoCyan)
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 1. Direct In-App OTA Installation Authorization Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, if (isAuthorized) EchoGreen.copy(alpha = 0.5f) else EchoAmber.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAuthorized) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isAuthorized) EchoGreen else EchoAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isAuthorized) "DIRECT OTA INSTALL: AUTHORIZED" else "DIRECT OTA INSTALL: RESTRICTED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAuthorized) EchoGreen else EchoAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (!isAuthorized) {
                                Button(
                                    onClick = {
                                        toneGenerator?.playChime(SoundEffectType.TAP)
                                        AthenaUpdateManager.openUnknownAppSourcesSettings(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EchoAmber,
                                        contentColor = Color(0xFF0A0B10)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Authorize", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isAuthorized)
                                "ATHENA has permission to directly accept update packages and trigger the system installer without USB cables or a PC."
                            else
                                "Tap 'Authorize' to grant permission to install updates in Android Settings. This bypasses USB debugging and install blocks.",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 2. USB Debugging & Developer Options Bypass
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EchoCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Usb, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "USB Hardware Settings",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                                Text(
                                    text = "Toggle 'Install via USB' or 'Verify apps over USB' in Device Developer Settings",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                toneGenerator?.playChime(SoundEffectType.TAP)
                                AthenaUpdateManager.openDeveloperOptions(context)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                            border = BorderStroke(1.dp, EchoCyan),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Dev Options", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 3. Available Update Packages
                Text(
                    text = "AVAILABLE UPDATE PACKAGES (${availablePackages.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoText,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))

                if (availablePackages.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("No update APKs found in storage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                            Text("Place your APK in Downloads or build via Athena APK Studio.", fontSize = 10.sp, color = EchoMuted)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availablePackages.forEach { pkg ->
                            val sizeMb = remember(pkg.fileSizeBytes) {
                                String.format(Locale.getDefault(), "%.1f MB", pkg.fileSizeBytes / (1024.0 * 1024.0))
                            }
                            val dateStr = remember(pkg.modifiedAt) { dateFormat.format(Date(pkg.modifiedAt)) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EchoCard),
                                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = pkg.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EchoText
                                                )
                                                Text(
                                                    text = "${pkg.file.name} • $sizeMb • ${pkg.versionString}",
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = EchoMuted
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                toneGenerator?.playChime(SoundEffectType.SUCCESS)
                                                if (!isAuthorized) {
                                                    Toast.makeText(context, "Authorizing in-app installation first...", Toast.LENGTH_SHORT).show()
                                                    AthenaUpdateManager.openUnknownAppSourcesSettings(context)
                                                } else {
                                                    val launched = AthenaUpdateManager.triggerPackageInstall(context, pkg.file, "SystemUpdate")
                                                    if (launched) {
                                                        Toast.makeText(context, "Launching package installer for update...", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Failed to launch package installer", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = EchoGreen,
                                                contentColor = Color(0xFF0A0B10)
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Accept & Install", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(11.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(text = "Package built: $dateStr", fontSize = 9.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 4. Auto-Relaunch & Clean System Restart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoBg),
                    border = BorderStroke(1.dp, EchoAmber.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Relaunch with State Recovery",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoAmber
                                )
                                Text(
                                    text = "Cleanly restarts ATHENA and immediately restores your active cockpit state",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }

                            Button(
                                onClick = { showRestartConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoAmber.copy(alpha = 0.25f),
                                    contentColor = EchoAmber
                                ),
                                border = BorderStroke(1.dp, EchoAmber),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reboot ATHENA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCardStroke, contentColor = EchoText),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }

    if (showRestartConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestartConfirmDialog = false },
            title = { Text("Restart ATHENA Now?", color = EchoText, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "ATHENA will save your workspace checkpoint, terminate the process, and automatically relaunch within 1 second.",
                    color = EchoMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        toneGenerator?.playChime(SoundEffectType.TAP)
                        showRestartConfirmDialog = false
                        Toast.makeText(context, "Rebooting ATHENA with state recovery...", Toast.LENGTH_SHORT).show()
                        AthenaUpdateManager.triggerCleanAppRestart(context, "Cockpit")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoAmber, contentColor = Color(0xFF0A0B10))
                ) {
                    Text("Restart Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirmDialog = false }) {
                    Text("Cancel", color = EchoText)
                }
            },
            containerColor = EchoCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
