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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.WorkspaceItem
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SystemUpdate
import com.example.launcher.LauncherAppsManager
import com.example.audio.SoundEffectType
import com.example.ui.components.LivingTapestryTeaserView
import com.example.ui.components.AthenaLoomWeavePlayerView
import com.example.ui.components.WorkspaceView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sovereign Science Cockpit view for ATHENA Workspace.
 * Features real-time telemetry, interactive Circular Chladni resonance plate solver,
 * sovereign Android launcher app dock, project folder quick launcher, and live data ledger feed.
 */
@Composable
fun AthenaScienceCockpitView(
    viewModel: WorkspaceViewModel,
    onNavigateToProjects: () -> Unit,
    onNavigateToDataLedger: () -> Unit,
    onNavigateToEcho: () -> Unit,
    onOpenVoiceCalibration: () -> Unit,
    onOpenExportBundle: () -> Unit,
    onOpenNoteReader: (WorkspaceItem) -> Unit,
    onCreateNewNote: () -> Unit,
    onCreateNewFolder: () -> Unit,
    onNavigateToScienceLab: (() -> Unit)? = null,
    onOpenAppDrawer: (() -> Unit)? = null,
    onOpenSystemUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcherManager = remember { LauncherAppsManager.getInstance(context) }
    val favoriteApps by launcherManager.favoriteApps.collectAsState()
    val isDefaultLauncher by launcherManager.isDefaultLauncher.collectAsState()
    var showInternalAppDrawer by remember { mutableStateOf(false) }

    val allFolders by viewModel.allFolders.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }
    val isTonePlaying by toneGenerator.isPlayingState.collectAsState()

    var showFullScreenChladniDialog by remember { mutableStateOf(false) }
    var activeCinematicTab by remember { mutableStateOf(0) } // 0 = Loom-Weave 16:9 (12s), 1 = Living Tapestry 9:16 (40s)

    val recentNotes = remember(allItems) {
        allItems.filter { it.kind != "folder" && !it.isDeleted }
            .sortedByDescending { it.modifiedTimestamp }
            .take(5)
    }

    val noteCount = remember(allItems) { allItems.count { it.kind == "note" && !it.isDeleted } }
    val folderCount = remember(allFolders) { allFolders.size }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("athena_science_cockpit_view"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cockpit Mission Telemetry HUD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = EchoCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(EchoCyan, EchoGreen))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = EchoOnCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ATHENA SOVEREIGN SCIENCE",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Acoustic Telemetry & Resonance Cockpit",
                                    fontSize = 11.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoGreen.copy(alpha = 0.15f))
                                .border(1.dp, EchoGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "COCKPIT ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoGreen
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Telemetry Metric Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Fundamental Base Frequency
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EchoBg)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "CARRIER", fontSize = 9.sp, color = EchoMuted)
                                Text(
                                    text = "42.60 Hz",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoCyan
                                )
                                Text(text = "Bedrock Harmonic", fontSize = 9.sp, color = EchoCyan.copy(alpha = 0.7f))
                            }
                        }

                        // Metric 2: Sovereign Mode Lock
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EchoBg)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "CHLADNI MODE", fontSize = 9.sp, color = EchoMuted)
                                Text(
                                    text = "(3, 2)",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoGreen
                                )
                                Text(text = "Hexagonal Rosette", fontSize = 9.sp, color = EchoMuted)
                            }
                        }

                        // Metric 3: Ledger Records
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EchoBg)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "LEDGER", fontSize = 9.sp, color = EchoMuted)
                                Text(
                                    text = "$noteCount records",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoAmber
                                )
                                Text(text = "$folderCount Projects", fontSize = 9.sp, color = EchoMuted)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Quick Cockpit Tools Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCreateNewNote,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan.copy(alpha = 0.2f),
                                contentColor = EchoCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New Note", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onOpenVoiceCalibration,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoPurple.copy(alpha = 0.2f),
                                contentColor = EchoPurple
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Calibrate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onOpenExportBundle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoAmber.copy(alpha = 0.2f),
                                contentColor = EchoAmber
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Backup", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 2. ATHENA Sovereign OS Launcher Dock & Quick Applications
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(16.dp))
                    .testTag("cockpit_launcher_dock_card"),
                colors = CardDefaults.cardColors(containerColor = EchoCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoCyan.copy(alpha = 0.15f))
                                    .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = EchoCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "APPLICATION MATRIX",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    if (isDefaultLauncher) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EchoGreen.copy(alpha = 0.2f))
                                                .border(1.dp, EchoGreen, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "DEFAULT HOME",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EchoGreen,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isDefaultLauncher) "ATHENA is active system home shell" else "Tap below to set ATHENA as Default Home",
                                    fontSize = 11.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!isDefaultLauncher) {
                                OutlinedButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                                        launcherManager.openDefaultLauncherPrompt(context)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("cockpit_set_default_home_button")
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Set Home", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    if (onOpenAppDrawer != null) {
                                        onOpenAppDrawer()
                                    } else {
                                        showInternalAppDrawer = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoCyan,
                                    contentColor = EchoOnCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("cockpit_open_app_drawer_button")
                            ) {
                                Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("App Drawer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Pinned Favorites Quick Launch Row
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(favoriteApps, key = { it.packageName }) { app ->
                            Column(
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        launcherManager.launchApp(context, app.packageName)
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EchoBgAlt)
                                        .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (app.iconBitmap != null) {
                                        Image(
                                            bitmap = app.iconBitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier.fillMaxSize().padding(3.dp)
                                        )
                                    } else {
                                        Text(
                                            text = app.label.take(1).uppercase(),
                                            color = EchoCyan,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = app.label,
                                    fontSize = 10.sp,
                                    color = EchoText,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Add / More Apps tile
                        item {
                            Column(
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        if (onOpenAppDrawer != null) {
                                            onOpenAppDrawer()
                                        } else {
                                            showInternalAppDrawer = true
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EchoCyan.copy(alpha = 0.1f))
                                        .border(1.dp, EchoCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "More Apps",
                                        tint = EchoCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "All Apps",
                                    fontSize = 10.sp,
                                    color = EchoCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Sovereign Instruments & Solvers (Iconized Quick-Launcher)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = EchoCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SOVEREIGN INSTRUMENTS & LABS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clickable Chladni Solver Icon
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoCyan.copy(alpha = 0.12f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    showFullScreenChladniDialog = true
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.OpenInFull, contentDescription = "Chladni Solver", tint = EchoCyan, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Chladni 42.6", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoCyan, maxLines = 1)
                        }

                        // Clickable Science Lab Icon
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoGreen.copy(alpha = 0.12f))
                                .border(1.dp, EchoGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onNavigateToScienceLab?.invoke()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Science, contentDescription = "Science Lab", tint = EchoGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Science Lab", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoGreen, maxLines = 1)
                        }

                        // Clickable Voice Calibration Icon
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoPurple.copy(alpha = 0.12f))
                                .border(1.dp, EchoPurple.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onOpenVoiceCalibration()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Calibrate Voice", tint = EchoPurple, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Voice Tone", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoPurple, maxLines = 1)
                        }

                        // Clickable Cloud Drive Backup Icon
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoAmber.copy(alpha = 0.12f))
                                .border(1.dp, EchoAmber.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onOpenExportBundle()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = "Drive Backup", tint = EchoAmber, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Drive Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoAmber, maxLines = 1)
                        }

                        // Clickable OTA System Update Icon
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoAmber.copy(alpha = 0.18f))
                                .border(1.dp, EchoAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onOpenSystemUpdate?.invoke()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = "OTA Update & Reboot", tint = EchoAmber, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("OTA Update", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoAmber, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 4. Centerpiece Showcase: ATHENA Cinematic Assets
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cinematic Studio Assets",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Text(
                            text = "Official release teasers & audio synthesis engines",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                activeCinematicTab = 0
                                toneGenerator.playChime(SoundEffectType.TAP)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeCinematicTab == 0) EchoAmber else EchoCard,
                                contentColor = if (activeCinematicTab == 0) Color(0xFF0A0B10) else EchoText
                            ),
                            border = BorderStroke(1.dp, if (activeCinematicTab == 0) EchoAmber else EchoCardStroke),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("16:9 Loom (12s)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                activeCinematicTab = 1
                                toneGenerator.playChime(SoundEffectType.TAP)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeCinematicTab == 1) EchoPurple else EchoCard,
                                contentColor = if (activeCinematicTab == 1) Color.White else EchoText
                            ),
                            border = BorderStroke(1.dp, if (activeCinematicTab == 1) EchoPurple else EchoCardStroke),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("9:16 Tapestry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (activeCinematicTab == 0) {
                    AthenaLoomWeavePlayerView(
                        toneGenerator = toneGenerator,
                        onSaveToDrive = onOpenExportBundle
                    )
                } else {
                    LivingTapestryTeaserView(
                        toneGenerator = toneGenerator,
                        onSaveToDrive = onOpenExportBundle
                    )
                }
            }
        }

        // 5. Offline Room SQLite Database: User Projects & Workspaces
        item {
            WorkspaceView(
                viewModel = viewModel,
                toneGenerator = toneGenerator,
                onSelectProject = { project ->
                    onNavigateToProjects()
                }
            )
        }

        // 3. Projects Cockpit Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Research Projects",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Text(
                            text = "Active experiment folders & repositories",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onCreateNewFolder,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = "New Project",
                                tint = EchoCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Button(
                            onClick = onNavigateToProjects,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = EchoCyan
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (allFolders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No project folders yet", fontSize = 13.sp, color = EchoMuted)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onCreateNewFolder,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Create First Project", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allFolders) { folder ->
                            val isEchoFolder = folder.name.contains("Echo", ignoreCase = true)
                            Card(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable {
                                        viewModel.navigateToFolder(folder.id)
                                        onNavigateToProjects()
                                    },
                                colors = CardDefaults.cardColors(containerColor = EchoCard),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isEchoFolder) EchoCyan.copy(alpha = 0.5f) else EchoCardStroke
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isEchoFolder) EchoCyan else EchoAmber,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        if (isEchoFolder) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(EchoCyan.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("ECHO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = folder.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Project Folder",
                                        fontSize = 10.sp,
                                        color = EchoMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Data Ledger Live Feed
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Data Ledger",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Text(
                            text = "Recent notes, voice journals & field recordings",
                            fontSize = 11.sp,
                            color = EchoMuted
                        )
                    }

                    Button(
                        onClick = onNavigateToDataLedger,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = EchoCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Open Ledger", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (recentNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NoteAlt, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Data Ledger is empty", fontSize = 13.sp, color = EchoMuted)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onCreateNewNote,
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Create First Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentNotes.forEach { item ->
                            val isVoice = item.tags.contains("Voice", ignoreCase = true) || item.tags.contains("Audio", ignoreCase = true)
                            val isChladni = item.tags.contains("Chladni", ignoreCase = true)
                            val iconVector = when {
                                isVoice -> Icons.Default.Mic
                                isChladni -> Icons.Default.GraphicEq
                                else -> Icons.Default.HistoryEdu
                            }
                            val iconColor = when {
                                isVoice -> EchoPurple
                                isChladni -> EchoGreen
                                else -> EchoCyan
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenNoteReader(item) },
                                colors = CardDefaults.cardColors(containerColor = EchoCard),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(iconColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EchoText,
                                            maxLines = 1
                                        )
                                        if (item.text.isNotBlank()) {
                                            Text(
                                                text = item.text.replace("\n", " "),
                                                fontSize = 11.sp,
                                                color = EchoMuted,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(item.modifiedTimestamp)),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-Screen Chladni Plate Dialog
    if (showFullScreenChladniDialog) {
        Dialog(
            onDismissRequest = { showFullScreenChladniDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = EchoBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CHLADNI RESONANCE STUDIO",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = "Full Mathematical Circular Eigenmode Explorer",
                                fontSize = 11.sp,
                                color = EchoMuted
                            )
                        }

                        Button(
                            onClick = { showFullScreenChladniDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCardStroke,
                                contentColor = EchoText
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    CircularChladniCanvas(
                        modifier = Modifier.weight(1f),
                        onSaveSnapshot = { presetName, freq, m, n, notes ->
                            val noteTitle = "Full-Screen Chladni: $presetName ($m, $n)"
                            val noteBody = "Acoustic Frequency: $freq Hz, Modes m=$m, n=$n. $notes"
                            viewModel.createNote(name = noteTitle, initialText = noteBody, tags = "Chladni Studio")
                            Toast.makeText(context, "Saved snapshot to Data Ledger!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (showInternalAppDrawer) {
            AppDrawerSheet(
                onDismiss = { showInternalAppDrawer = false },
                toneGenerator = toneGenerator
            )
        }
    }
}
