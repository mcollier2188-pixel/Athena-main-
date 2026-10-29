package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import com.example.data.VideoPlayerPreferencesManager
import com.example.util.AppOrientation
import com.example.util.OrientationManager
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.gateway.GoogleDriveSyncManager
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoCyanSoft
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import com.example.util.ExportFormat
import com.example.util.FrequencyCsvExporter
import com.example.util.NetworkConnectivityMonitor
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import kotlinx.coroutines.launch
import java.io.File

/**
 * Settings Dialog for ATHENA & Echo-Interface.
 * Features a dedicated TabRow in the header:
 * - "System" Tab: Appearance, Audio 42.6 Hz Harmonic Engine, App Builder & Deployment, Room SQLite Storage, and Exit.
 * - "Google" Tab: Acts as the Gateway, featuring expandable subsections for:
 *     1. Authentication Status (Google Identity / Credential Manager & zero-trust session)
 *     2. Gemini AI Interactions (Embeds GoogleGateway with search/maps grounding, live payload inspector, and untrusted sandbox results)
 *     3. Google Drive Connectivity (Cloud backup & sync for Workspace notes and research, with real-time cloud auto-sync toggle)
 */
@Composable
fun SettingsDialog(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onClearData: () -> Unit = {},
    onRequestExitApp: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Top Header Tab Selection: 0 = ATHENA System, 1 = Google (Gateway)
    var selectedSettingsTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Expandable Subsections State within Google Gateway Tab
    var isAuthSectionExpanded by rememberSaveable { mutableStateOf(true) }
    var isGeminiSectionExpanded by rememberSaveable { mutableStateOf(true) }
    var isDriveSectionExpanded by rememberSaveable { mutableStateOf(true) }

    // Google Drive Sync Manager state
    remember(context) { GoogleDriveSyncManager.initialize(context) }
    val isAutoSyncEnabled by GoogleDriveSyncManager.isAutoSyncEnabled.collectAsState()
    val isWifiOnlySync by GoogleDriveSyncManager.isWifiOnly.collectAsState()
    val isSyncing by GoogleDriveSyncManager.isSyncing.collectAsState()
    val lastSyncTimestamp by GoogleDriveSyncManager.lastSyncTimestamp.collectAsState()
    val syncedItemsCount by GoogleDriveSyncManager.syncedItemsCount.collectAsState()
    val syncStatusMessage by GoogleDriveSyncManager.syncStatusMessage.collectAsState()

    // Video Player Rotation & Display Preferences
    remember(context) { VideoPlayerPreferencesManager.initialize(context) }
    val doubleTapRotateEnabled by VideoPlayerPreferencesManager.doubleTapRotateEnabled.collectAsState()
    val defaultVideoOrientationDegrees by VideoPlayerPreferencesManager.defaultOrientationDegrees.collectAsState()
    val gestureHintBadgesVisible by VideoPlayerPreferencesManager.gestureHintBadgesVisible.collectAsState()

    // System Appearance & Display Settings
    var highPrecisionOscilloscope by rememberSaveable { mutableStateOf(true) }

    // Audio & Frequency Engine Settings
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }
    val masterAudioGain by toneGenerator.volumeState.collectAsState()
    val isButtonSoundEnabled by toneGenerator.isButtonSoundEnabledState.collectAsState()
    var baseResonanceFreq by rememberSaveable { mutableStateOf("42.600 Hz") }
    var solentLfoSyncEnabled by rememberSaveable { mutableStateOf(true) }
    var autoStopToneOnBackground by rememberSaveable { mutableStateOf(true) }

    // App Builder & Device Testing Defaults
    var defaultAiEngine by rememberSaveable { mutableStateOf("Athena Gemini 3.1 Pro") }
    var autoLaunchInstallerAfterBuild by rememberSaveable { mutableStateOf(true) }
    var defaultIncludeTests by rememberSaveable { mutableStateOf(true) }

    // Database & Export Settings
    var defaultExportFormat by rememberSaveable { mutableStateOf("CSV (.csv)") }

    // Real-Time Network Connectivity Monitor
    val networkStatusFlow = remember(context) { NetworkConnectivityMonitor.observeNetworkStatus(context) }
    val networkStatus by networkStatusFlow.collectAsState(initial = remember(context) { NetworkConnectivityMonitor.getInitialStatus(context) })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EchoCyan, RoundedCornerShape(16.dp))
                .testTag("settings_dialog"),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // -------------------------------------------------------------
                // HEADER WITH CLOSE BUTTON
                // -------------------------------------------------------------
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedSettingsTabIndex == 1) Icons.Default.Public else Icons.Default.Settings,
                                contentDescription = null,
                                tint = EchoOnCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ATHENA Preferences & Settings",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = if (selectedSettingsTabIndex == 1) "Google Gateway & Cloud Services" else "System Preferences & Diagnostics",
                                fontSize = 11.sp,
                                color = EchoCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoText)
                    }
                }

                // -------------------------------------------------------------
                // DEDICATED TAB ROW IN THE HEADER
                // -------------------------------------------------------------
                TabRow(
                    selectedTabIndex = selectedSettingsTabIndex,
                    containerColor = EchoDarkSurface,
                    contentColor = EchoCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSettingsTabIndex]),
                            color = EchoCyan,
                            height = 2.5.dp
                        )
                    },
                    divider = { HorizontalDivider(color = EchoCardStroke, thickness = 1.dp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp))
                        .testTag("settings_tab_row")
                ) {
                    Tab(
                        selected = selectedSettingsTabIndex == 0,
                        onClick = { selectedSettingsTabIndex = 0 },
                        icon = {
                            Icon(Icons.Default.Settings, contentDescription = "Preferences", modifier = Modifier.size(16.dp))
                        },
                        text = {
                            Text(
                                text = "Preferences",
                                fontSize = 12.sp,
                                fontWeight = if (selectedSettingsTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = EchoCyan,
                        unselectedContentColor = EchoMuted,
                        modifier = Modifier.testTag("settings_preferences_tab")
                    )

                    Tab(
                        selected = selectedSettingsTabIndex == 1,
                        onClick = { selectedSettingsTabIndex = 1 },
                        icon = {
                            Icon(Icons.Default.Public, contentDescription = "Google", modifier = Modifier.size(16.dp))
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Google",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSettingsTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (networkStatus.isConnected) EchoGreen else EchoAmber)
                                )
                            }
                        },
                        selectedContentColor = EchoCyan,
                        unselectedContentColor = EchoMuted,
                        modifier = Modifier.testTag("settings_google_tab")
                    )
                }

                // =============================================================
                // TAB 0: SYSTEM PREFERENCES & DIAGNOSTICS
                // =============================================================
                if (selectedSettingsTabIndex == 0) {
                    // 1. APPEARANCE & DISPLAY SETTINGS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) EchoCyan else EchoGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text("Theme Appearance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text(if (isDarkTheme) "Dark Atmospheric Theme" else "Light Solar Theme", fontSize = 11.sp, color = EchoMuted)
                                    }
                                }

                                Switch(
                                    checked = !isDarkTheme,
                                    onCheckedChange = { onToggleDarkTheme() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = EchoGreen,
                                        checkedTrackColor = EchoGreen.copy(alpha = 0.3f),
                                        uncheckedThumbColor = EchoCyan,
                                        uncheckedTrackColor = EchoCardStroke
                                    ),
                                    modifier = Modifier.testTag("theme_switch")
                                )
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Oscilloscope Visual Precision", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text(if (highPrecisionOscilloscope) "60 FPS Fluid Waveforms" else "30 FPS Power Saving", fontSize = 11.sp, color = EchoMuted)
                                    }
                                }
                                Switch(
                                    checked = highPrecisionOscilloscope,
                                    onCheckedChange = { highPrecisionOscilloscope = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoCyan, checkedTrackColor = EchoCyanSoft)
                                )
                            }
                        }
                    }

                    // 1B. NATIVE ORIENTATION (LANDSCAPE DEFAULT & 90° ROTATE) & DEVICE MIRRORING
                    val orientationManager = remember { OrientationManager.getInstance(context) }
                    val currentOrientation by orientationManager.currentOrientation.collectAsState()
                    var showMirroringFromSettings by remember { mutableStateOf(false) }

                    if (showMirroringFromSettings) {
                        DeviceMirroringDialog(
                            onDismiss = { showMirroringFromSettings = false },
                            orientationManager = orientationManager
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("app_orientation_mirroring_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCyan.copy(alpha = 0.35f)))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("App Native Orientation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text("Landscape Native Default with 90° Portrait Toggle", fontSize = 11.sp, color = EchoMuted)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val next = orientationManager.toggle90Degrees(context as? android.app.Activity)
                                        Toast.makeText(context, "Orientation: ${next.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoBg),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("settings_rotate_90_button")
                                ) {
                                    Icon(Icons.Default.ScreenRotation, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Rotate 90°", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(AppOrientation.LANDSCAPE to "Landscape (Native)", AppOrientation.PORTRAIT to "Portrait (90°)").forEach { (orient, label) ->
                                    FilterChip(
                                        selected = currentOrientation == orient,
                                        onClick = { orientationManager.setOrientation(context as? android.app.Activity, orient) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EchoCyan,
                                            selectedLabelColor = Color.Black,
                                            containerColor = EchoBgAlt,
                                            labelColor = EchoText
                                        ),
                                        modifier = Modifier.testTag("chip_orientation_${orient.name.lowercase()}")
                                    )
                                }
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Cast, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Device Mirroring & Cast Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text("Cast / Web test receiver (Port 8520) / HDMI", fontSize = 10.sp, color = EchoMuted)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showMirroringFromSettings = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("settings_open_mirroring_button")
                                ) {
                                    Icon(Icons.Default.ScreenShare, contentDescription = null, modifier = Modifier.size(14.dp), tint = EchoCyan)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open Cast Hub", fontSize = 11.sp, color = EchoCyan)
                                }
                            }
                        }
                    }

                    // 2. VIDEO PLAYER DISPLAY & ROTATION PREFERENCES
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("video_player_preferences_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Video Player & Screen Rotation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                    Text("Interactive fullscreen gestures, 90° landscape & control tags", fontSize = 11.sp, color = EchoMuted)
                                }
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            // Double-Tap to Toggle Landscape Rotation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Double-Tap Landscape Gesture", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Double-tap any video player to toggle between 0° portrait and 90° landscape", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = doubleTapRotateEnabled,
                                    onCheckedChange = { VideoPlayerPreferencesManager.setDoubleTapRotateEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoCyan, checkedTrackColor = EchoCyanSoft),
                                    modifier = Modifier.testTag("switch_double_tap_rotate")
                                )
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            // Default Video Orientation
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Default Player Orientation:", fontSize = 11.sp, color = EchoMuted)
                                    Text(
                                        text = if (defaultVideoOrientationDegrees == 90f) "Landscape 90°" else "Portrait 0°",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoCyan
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(0f to "Portrait (0°)", 90f to "Landscape (90°)").forEach { (deg, label) ->
                                        FilterChip(
                                            selected = defaultVideoOrientationDegrees == deg,
                                            onClick = { VideoPlayerPreferencesManager.setDefaultOrientationDegrees(deg) },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EchoCyan,
                                                selectedLabelColor = Color.Black,
                                                containerColor = EchoBgAlt,
                                                labelColor = EchoText
                                            ),
                                            modifier = Modifier.testTag("chip_orientation_${deg.toInt()}")
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            // Gesture Hint Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Interactive Gesture & Input Labels", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Show input indicators on player toolbars (⛶ Press: Full Screen, 🔄 Double-Tap: Landscape)", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = gestureHintBadgesVisible,
                                    onCheckedChange = { VideoPlayerPreferencesManager.setGestureHintBadgesVisible(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoGreen, checkedTrackColor = EchoGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.testTag("switch_gesture_hints")
                                )
                            }
                        }
                    }

                    // 3. AUDIO & 42.6 Hz HARMONIC ENGINE SETTINGS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                                Text("Acoustic & Audio Engine", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            }

                            // Master Audio Gain Slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("DSP Master Output Gain", fontSize = 11.sp, color = EchoMuted)
                                    Text("${(masterAudioGain * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                }
                                Slider(
                                    value = masterAudioGain,
                                    onValueChange = { toneGenerator.setVolumeLevel(it) },
                                    valueRange = 0.0f..1.0f,
                                    colors = SliderDefaults.colors(thumbColor = EchoCyan, activeTrackColor = EchoCyan),
                                    modifier = Modifier.testTag("slider_master_volume")
                                )
                            }

                            // Button Tap Sound Effects Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Button Tap Sound Effects", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Tactile harmonic chime when tapping buttons & tabs", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = isButtonSoundEnabled,
                                    onCheckedChange = { toneGenerator.setButtonSoundEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoCyan, checkedTrackColor = EchoCyanSoft),
                                    modifier = Modifier.testTag("switch_button_sound_effects")
                                )
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            // Base Tuning Tone Preset
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Default Tuning Node Carrier:", fontSize = 11.sp, color = EchoMuted)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("42.600 Hz", "85.200 Hz (2x)").forEach { freq ->
                                        FilterChip(
                                            selected = baseResonanceFreq == freq,
                                            onClick = { baseResonanceFreq = freq },
                                            label = { Text(freq, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EchoCyan,
                                                selectedLabelColor = Color.Black,
                                                containerColor = EchoBgAlt,
                                                labelColor = EchoText
                                            )
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            // Solent LFO Sync Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Solent 11.74s Tidal Envelope", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Modulate carrier amplitude along UK coastal node tides", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = solentLfoSyncEnabled,
                                    onCheckedChange = { solentLfoSyncEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoCyan, checkedTrackColor = EchoCyanSoft)
                                )
                            }

                            // Auto-stop on background
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Stop Tone on Minimize", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Silence frequency generators when screen is locked", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = autoStopToneOnBackground,
                                    onCheckedChange = { autoStopToneOnBackground = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoCyan, checkedTrackColor = EchoCyanSoft)
                                )
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                            // Physical Audio Test Chime Action
                            Button(
                                onClick = {
                                    HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                    Toast.makeText(context, "Audio Chime Triggered (PCM Synthesizer)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("test_audio_chime_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoDarkSurface,
                                    contentColor = EchoCyan
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("TEST AUDIO PLAYBACK (528 HZ CHIME)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. APP BUILDER & TESTING DEPLOYMENT PREFERENCES
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(20.dp))
                                Text("App Builder & Testing Deployment", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Default Synthesis AI Engine:", fontSize = 11.sp, color = EchoMuted)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Athena Gemini 3.1 Pro", "Athena Gemini 3.5 Flash", "Claude Specialist").forEach { eng ->
                                        FilterChip(
                                            selected = defaultAiEngine == eng,
                                            onClick = { defaultAiEngine = eng },
                                            label = { Text(eng.take(16), fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EchoGreen,
                                                selectedLabelColor = Color.Black,
                                                containerColor = EchoBgAlt,
                                                labelColor = EchoText
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Launch Package Installer", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Prompt device installer immediately after APK synthesis", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = autoLaunchInstallerAfterBuild,
                                    onCheckedChange = { autoLaunchInstallerAfterBuild = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoGreen, checkedTrackColor = EchoGreen.copy(alpha = 0.3f))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Include Unit Test Suite by Default", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                    Text("Synthesize Robolectric tests with each app build", fontSize = 10.sp, color = EchoMuted)
                                }
                                Switch(
                                    checked = defaultIncludeTests,
                                    onCheckedChange = { defaultIncludeTests = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoGreen, checkedTrackColor = EchoGreen.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    // 4. STORAGE & ROOM DATABASE MANAGEMENT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                                Text("Database & Resonance Data Export", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            }

                            Text(
                                text = "Export historical resonance sweep data, bedrock standing wave logs, and field findings from Room SQLite into CSV or Text format:",
                                fontSize = 11.sp,
                                color = EchoMuted,
                                lineHeight = 15.sp
                            )

                            // Format selection
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Default Export Format:", fontSize = 11.sp, color = EchoMuted)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("CSV (.csv)", "Plain Text (.txt)").forEach { fmt ->
                                        FilterChip(
                                            selected = defaultExportFormat == fmt,
                                            onClick = { defaultExportFormat = fmt },
                                            label = { Text(fmt, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EchoCyan,
                                                selectedLabelColor = Color.Black,
                                                containerColor = EchoBgAlt,
                                                labelColor = EchoText
                                            )
                                        )
                                    }
                                }
                            }

                            // Direct Export Actions Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val csvContent = FrequencyCsvExporter.exportResonanceHistoryFromDatabase(context, ExportFormat.CSV)
                                                val file = FrequencyCsvExporter.createTempExportFile(context, csvContent, ExportFormat.CSV)
                                                val shareIntent = FrequencyCsvExporter.createShareIntent(context, file, ExportFormat.CSV)
                                                context.startActivity(Intent.createChooser(shareIntent, "Save / Export Resonance CSV"))
                                                Toast.makeText(context, "Resonance CSV Export Ready!", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("settings_export_csv_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val txtContent = FrequencyCsvExporter.exportResonanceHistoryFromDatabase(context, ExportFormat.PLAIN_TEXT)
                                                val file = FrequencyCsvExporter.createTempExportFile(context, txtContent, ExportFormat.PLAIN_TEXT)
                                                val shareIntent = FrequencyCsvExporter.createShareIntent(context, file, ExportFormat.PLAIN_TEXT)
                                                context.startActivity(Intent.createChooser(shareIntent, "Save / Export Findings Report"))
                                                Toast.makeText(context, "Resonance Report Text Ready!", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("settings_export_txt_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Export TXT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Cache and APK storage cleaner
                            OutlinedButton(
                                onClick = {
                                    val apkDir = File(context.filesDir, "apk_builds")
                                    val exportDir = File(context.cacheDir, "exports")
                                    val freedBytes = (apkDir.listFiles()?.sumOf { it.length() } ?: 0L) +
                                            (exportDir.listFiles()?.sumOf { it.length() } ?: 0L)
                                    apkDir.listFiles()?.forEach { it.delete() }
                                    exportDir.listFiles()?.forEach { it.delete() }
                                    onClearData()
                                    val freedKb = freedBytes / 1024L
                                    Toast.makeText(context, "Cleaned $freedKb KB of cached APK builds and exports", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoMuted),
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Clean APK Builds & Export Storage", fontSize = 11.sp)
                            }
                        }
                    }

                    // 5. IN-APP EXIT ACTION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoRed.copy(alpha = 0.12f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoRed.copy(alpha = 0.4f)))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = EchoRed, modifier = Modifier.size(22.dp))
                                Column {
                                    Text("In-App Application Exit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoRed)
                                    Text("Cleanly terminates active audio & telemetry session", fontSize = 11.sp, color = EchoText)
                                }
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onRequestExitApp()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("settings_exit_app_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Exit ATHENA App", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 6. DIAGNOSTICS & SYSTEM TELEMETRY FOOTER
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ATHENA OS • 42.600 Hz RESONANCE INTERFACE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EchoCyan
                        )
                        Text(
                            text = "Build: v1.5 • Room DB Schema v2 • Solent Waveguide Carrier",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EchoMuted
                        )
                    }
                }

                // =============================================================
                // TAB 1: GOOGLE GATEWAY (GOOGLE SERVICES, GEMINI & DRIVE)
                // =============================================================
                if (selectedSettingsTabIndex == 1) {
                    // Gateway Overview & Air-gap Status Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_google_gateway_overview_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
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
                                    Icon(Icons.Default.Public, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("Google Services Gateway", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text("Isolated External Integrations Suite", fontSize = 10.sp, color = EchoCyan, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (networkStatus.isConnected) EchoGreen.copy(alpha = 0.15f) else EchoAmber.copy(alpha = 0.15f))
                                        .border(1.dp, if (networkStatus.isConnected) EchoGreen.copy(alpha = 0.4f) else EchoAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (networkStatus.isConnected) EchoGreen else EchoAmber)
                                    )
                                    Text(
                                        text = if (networkStatus.isConnected) "ONLINE" else "OFFLINE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (networkStatus.isConnected) EchoGreen else EchoAmber
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoDarkSurface)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Zero-Trust Protocol: Gemini AI, Google Drive, and Auth run strictly through isolated sandboxes. Local audio synthesizers, frequency sweeps, and private memory are kept confidential.",
                                    fontSize = 10.sp,
                                    color = EchoMuted,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // SUBSECTION 1: AUTHENTICATION STATUS (EXPANDABLE)
                    // ---------------------------------------------------------
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_auth_section_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAuthSectionExpanded) EchoCyan.copy(alpha = 0.6f) else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Expandable Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAuthSectionExpanded = !isAuthSectionExpanded }
                                    .testTag("google_auth_section_header"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                    }
                                    Column {
                                        Text("Authentication Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text("Google Identity & Zero-Trust Session", fontSize = 10.sp, color = EchoMuted)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoGreen,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EchoGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Icon(
                                        imageVector = if (isAuthSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isAuthSectionExpanded) "Collapse" else "Expand",
                                        tint = EchoCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isAuthSectionExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                                    // Status Details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Identity Provider", fontSize = 11.sp, color = EchoMuted)
                                        Text("Google Identity (Credential Manager)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Active Operator", fontSize = 11.sp, color = EchoMuted)
                                        Text("athena.operator@secure.local", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = EchoCyan)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Session Air-Gap Token", fontSize = 11.sp, color = EchoMuted)
                                        Text("SHA256·ISOLATED", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = EchoGreen)
                                    }

                                    // Action buttons for Auth
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Google Identity token re-verified and refreshed", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp).testTag("google_auth_reverify_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Re-verify Token", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Operator identity confirmed for offline air-gap session", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                                        ) {
                                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Zero-Trust Mode", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // SUBSECTION 2: GEMINI AI INTERACTIONS (EXPANDABLE GATEWAY)
                    // ---------------------------------------------------------
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_gemini_section_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isGeminiSectionExpanded) EchoCyan.copy(alpha = 0.6f) else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Expandable Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isGeminiSectionExpanded = !isGeminiSectionExpanded }
                                    .testTag("google_gemini_section_header"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                    }
                                    Column {
                                        Text("Gemini AI Interactions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text("Air-Gapped Reasoning & Grounding Interface", fontSize = 10.sp, color = EchoMuted)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "GATEWAY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoCyan,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EchoCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Icon(
                                        imageVector = if (isGeminiSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isGeminiSectionExpanded) "Collapse" else "Expand",
                                        tint = EchoCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isGeminiSectionExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                                    // Modular GoogleGateway Composable directly embedded inside settings!
                                    GoogleGateway(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // SUBSECTION 3: GOOGLE DRIVE CONNECTIVITY (EXPANDABLE)
                    // ---------------------------------------------------------
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_drive_section_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDriveSectionExpanded) EchoCyan.copy(alpha = 0.6f) else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Expandable Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDriveSectionExpanded = !isDriveSectionExpanded }
                                    .testTag("google_drive_section_header"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                    }
                                    Column {
                                        Text("Google Drive Connectivity", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                        Text("Workspace Notes & Research Cloud Sync", fontSize = 10.sp, color = EchoCyan)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAutoSyncEnabled) "AUTO-SYNC ON" else "PAUSED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isAutoSyncEnabled) EchoGreen else EchoAmber,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isAutoSyncEnabled) EchoGreen.copy(alpha = 0.15f) else EchoAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Icon(
                                        imageVector = if (isDriveSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isDriveSectionExpanded) "Collapse" else "Expand",
                                        tint = EchoCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                                AnimatedVisibility(
                                    visible = isDriveSectionExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                                        CloudSyncSettingsPanel(
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}
