package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.gateway.DriveSyncCategory
import com.example.gateway.GoogleDriveSyncManager
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoCyanSoft
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText

/**
 * Cloud Sync Settings Dialog modal for direct access from Workspace or system shortcuts.
 */
@Composable
fun CloudSyncSettingsDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EchoCyan, RoundedCornerShape(16.dp))
                .testTag("cloud_sync_dialog_modal"),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt)
        ) {
            CloudSyncSettingsPanel(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                onClose = onDismiss
            )
        }
    }
}

/**
 * Reusable Cloud Sync Settings Panel component featuring:
 * - Master auto-sync & Wi-Fi only connectivity toggles
 * - Individual category toggles: Workspace Notes, Photos/Snapshots, and AI Generated Videos
 * - Space & telemetry footprint telemetry per category
 * - Individual and collective "Sync Now" controls
 * - Google Drive container shortcuts and privacy assurance
 */
@Composable
fun CloudSyncSettingsPanel(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    remember(context) { GoogleDriveSyncManager.initialize(context) }

    val isAutoSyncEnabled by GoogleDriveSyncManager.isAutoSyncEnabled.collectAsState()
    val isWifiOnlySync by GoogleDriveSyncManager.isWifiOnly.collectAsState()
    val isSyncing by GoogleDriveSyncManager.isSyncing.collectAsState()
    val syncStatusMessage by GoogleDriveSyncManager.syncStatusMessage.collectAsState()
    val lastSyncTimestamp by GoogleDriveSyncManager.lastSyncTimestamp.collectAsState()
    val totalSyncedCount by GoogleDriveSyncManager.syncedItemsCount.collectAsState()

    val notesAutoSync by GoogleDriveSyncManager.notesAutoSyncEnabled.collectAsState()
    val photosAutoSync by GoogleDriveSyncManager.photosAutoSyncEnabled.collectAsState()
    val videosAutoSync by GoogleDriveSyncManager.videosAutoSyncEnabled.collectAsState()

    val notesCount by GoogleDriveSyncManager.notesSyncedCount.collectAsState()
    val photosCount by GoogleDriveSyncManager.photosSyncedCount.collectAsState()
    val videosCount by GoogleDriveSyncManager.videosSyncedCount.collectAsState()

    val notesLastSync by GoogleDriveSyncManager.notesLastSyncTime.collectAsState()
    val photosLastSync by GoogleDriveSyncManager.photosLastSyncTime.collectAsState()
    val videosLastSync by GoogleDriveSyncManager.videosLastSyncTime.collectAsState()

    Column(
        modifier = modifier.testTag("cloud_sync_settings_panel"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // HEADER ROW (With optional Close Button)
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EchoCyan.copy(alpha = 0.15f))
                        .border(1.dp, EchoCyan, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Cloud Sync",
                        tint = EchoCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Cloud Sync Settings",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )
                    Text(
                        text = "Google Drive Multi-Category Auto-Sync",
                        fontSize = 11.sp,
                        color = EchoCyan
                    )
                }
            }

            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_close_cloud_sync_panel")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = EchoMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // MASTER AUTO-SYNC CARD
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isAutoSyncEnabled) EchoCyan else EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isAutoSyncEnabled) EchoGreen.copy(alpha = 0.2f) else EchoAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAutoSyncEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isAutoSyncEnabled) EchoGreen else EchoAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Real-Time Google Drive Sync",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = if (isAutoSyncEnabled)
                                    "Continuous background sync for active categories"
                                else
                                    "Master sync disabled (offline local-only mode)",
                                fontSize = 11.sp,
                                color = if (isAutoSyncEnabled) EchoCyan else EchoMuted
                            )
                        }
                    }

                    Switch(
                        checked = isAutoSyncEnabled,
                        onCheckedChange = { newState ->
                            GoogleDriveSyncManager.setAutoSyncEnabled(context, newState)
                            val msg = if (newState) "Google Drive real-time auto-sync activated" else "Auto-sync paused"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EchoCyan,
                            checkedTrackColor = EchoCyanSoft,
                            uncheckedThumbColor = EchoAmber,
                            uncheckedTrackColor = EchoCardStroke
                        ),
                        modifier = Modifier.testTag("master_cloud_autosync_switch")
                    )
                }

                HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                // Global Telemetry & Status Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoDarkSurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sync Engine Status:", fontSize = 11.sp, color = EchoMuted)
                        Text(
                            text = syncStatusMessage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAutoSyncEnabled) EchoGreen else EchoAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Synced Items:", fontSize = 11.sp, color = EchoMuted)
                        Text(
                            text = "$totalSyncedCount items across all categories",
                            fontSize = 11.sp,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Cloud Storage:", fontSize = 11.sp, color = EchoMuted)
                        Text(
                            text = GoogleDriveSyncManager.getTotalStorageUsed(context),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Global Sync:", fontSize = 11.sp, color = EchoMuted)
                        Text(
                            text = GoogleDriveSyncManager.formatTimestamp(lastSyncTimestamp),
                            fontSize = 11.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Global Connectivity Controls: Wi-Fi Only Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Sync on Wi-Fi Only", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                            Text("Conserves mobile data & bandwidth", fontSize = 10.sp, color = EchoMuted)
                        }
                    }
                    Switch(
                        checked = isWifiOnlySync,
                        onCheckedChange = { GoogleDriveSyncManager.setWifiOnly(context, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EchoCyan, checkedTrackColor = EchoCyanSoft),
                        modifier = Modifier.testTag("cloud_sync_wifi_only_switch")
                    )
                }

                // Master "Sync All" & "Open Google Drive" Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            GoogleDriveSyncManager.performSyncAll(context, coroutineScope)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_sync_all_categories"),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = EchoOnCyan, strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("SYNCING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sync All Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            GoogleDriveSyncManager.openGoogleDrive(context)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_open_google_drive_app"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // CATEGORY SECTION HEADER
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INDIVIDUAL CATEGORY SYNC CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EchoCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "3 Categories",
                fontSize = 11.sp,
                color = EchoMuted
            )
        }

        // -------------------------------------------------------------
        // CATEGORY 1: WORKSPACE NOTES & RESEARCH
        // -------------------------------------------------------------
        CategorySyncCard(
            category = DriveSyncCategory.NOTES,
            icon = Icons.Default.Description,
            isEnabled = notesAutoSync,
            onToggle = { enabled ->
                GoogleDriveSyncManager.setCategoryAutoSyncEnabled(context, DriveSyncCategory.NOTES, enabled)
                val msg = if (enabled) "Notes auto-sync enabled" else "Notes auto-sync paused"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            syncedCount = notesCount,
            unitLabel = "notes & documents",
            storageUsed = GoogleDriveSyncManager.getCategoryStorageFootprint(context, DriveSyncCategory.NOTES),
            lastSyncTimeFormatted = GoogleDriveSyncManager.formatTimestamp(notesLastSync),
            isSyncing = isSyncing,
            onSyncNow = {
                GoogleDriveSyncManager.performCategorySync(context, DriveSyncCategory.NOTES, coroutineScope)
            },
            onShareBackup = {
                val intent = GoogleDriveSyncManager.createCategoryShareIntent(context, DriveSyncCategory.NOTES)
                context.startActivity(Intent.createChooser(intent, "Share Notes Backup to Google Drive"))
            },
            onRestore = {
                GoogleDriveSyncManager.performRestore(context, coroutineScope)
            },
            testTagPrefix = "category_notes"
        )

        // -------------------------------------------------------------
        // CATEGORY 2: PHOTOS & CAMERA SNAPSHOTS
        // -------------------------------------------------------------
        CategorySyncCard(
            category = DriveSyncCategory.PHOTOS,
            icon = Icons.Default.PhotoCamera,
            isEnabled = photosAutoSync,
            onToggle = { enabled ->
                GoogleDriveSyncManager.setCategoryAutoSyncEnabled(context, DriveSyncCategory.PHOTOS, enabled)
                val msg = if (enabled) "Photos & Snapshots auto-sync enabled" else "Photos auto-sync paused"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            syncedCount = photosCount,
            unitLabel = "photos & camera snapshots",
            storageUsed = GoogleDriveSyncManager.getCategoryStorageFootprint(context, DriveSyncCategory.PHOTOS),
            lastSyncTimeFormatted = GoogleDriveSyncManager.formatTimestamp(photosLastSync),
            isSyncing = isSyncing,
            onSyncNow = {
                GoogleDriveSyncManager.performCategorySync(context, DriveSyncCategory.PHOTOS, coroutineScope)
            },
            onShareBackup = {
                val intent = GoogleDriveSyncManager.createCategoryShareIntent(context, DriveSyncCategory.PHOTOS)
                context.startActivity(Intent.createChooser(intent, "Share Photos Backup to Google Drive"))
            },
            testTagPrefix = "category_photos"
        )

        // -------------------------------------------------------------
        // CATEGORY 3: AI GENERATED VIDEOS
        // -------------------------------------------------------------
        CategorySyncCard(
            category = DriveSyncCategory.VIDEOS,
            icon = Icons.Default.Videocam,
            isEnabled = videosAutoSync,
            onToggle = { enabled ->
                GoogleDriveSyncManager.setCategoryAutoSyncEnabled(context, DriveSyncCategory.VIDEOS, enabled)
                val msg = if (enabled) "AI Videos auto-sync enabled" else "AI Videos auto-sync paused"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            syncedCount = videosCount,
            unitLabel = "rendered AI videos",
            storageUsed = GoogleDriveSyncManager.getCategoryStorageFootprint(context, DriveSyncCategory.VIDEOS),
            lastSyncTimeFormatted = GoogleDriveSyncManager.formatTimestamp(videosLastSync),
            isSyncing = isSyncing,
            onSyncNow = {
                GoogleDriveSyncManager.performCategorySync(context, DriveSyncCategory.VIDEOS, coroutineScope)
            },
            onShareBackup = {
                val intent = GoogleDriveSyncManager.createCategoryShareIntent(context, DriveSyncCategory.VIDEOS)
                context.startActivity(Intent.createChooser(intent, "Share AI Videos to Google Drive"))
            },
            testTagPrefix = "category_videos"
        )

        // -------------------------------------------------------------
        // ZERO-LEAK PRIVACY & SECURITY BANNER
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(EchoDarkSurface)
                .border(1.dp, EchoCardStroke.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = EchoCyan,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Encrypted Google Drive Quarantine",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )
                    Text(
                        text = "Individual category files are sandboxed into their respective Google Drive folders. Internal synthesizer audio streams, FFT spectrogram telemetry, and sensitive system memories are never transmitted.",
                        fontSize = 10.sp,
                        color = EchoMuted,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Individual Category Card with switch toggle, telemetry stats, and dedicated sync actions.
 */
@Composable
private fun CategorySyncCard(
    category: DriveSyncCategory,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    syncedCount: Int,
    unitLabel: String,
    storageUsed: String,
    lastSyncTimeFormatted: String,
    isSyncing: Boolean,
    onSyncNow: () -> Unit,
    onShareBackup: () -> Unit,
    onRestore: (() -> Unit)? = null,
    testTagPrefix: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isEnabled) EchoCyan.copy(alpha = 0.7f) else EchoCardStroke
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Category Icon, Title, and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isEnabled) EchoCyan.copy(alpha = 0.15f) else EchoDarkSurface)
                            .border(
                                1.dp,
                                if (isEnabled) EchoCyan else EchoCardStroke,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isEnabled) EchoCyan else EchoMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = category.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(12.dp))
                            Text(
                                text = category.driveFolder,
                                fontSize = 10.sp,
                                color = EchoMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EchoCyan,
                        checkedTrackColor = EchoCyanSoft,
                        uncheckedThumbColor = EchoAmber,
                        uncheckedTrackColor = EchoCardStroke
                    ),
                    modifier = Modifier.testTag("${testTagPrefix}_switch")
                )
            }

            // Category Telemetry Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(EchoDarkSurface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Synced Content:", fontSize = 10.sp, color = EchoMuted)
                    Text(
                        text = "$syncedCount $unitLabel",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) EchoCyan else EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Space:", fontSize = 10.sp, color = EchoMuted)
                    Text(
                        text = storageUsed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EchoText,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Last Category Sync:", fontSize = 10.sp, color = EchoMuted)
                    Text(
                        text = lastSyncTimeFormatted,
                        fontSize = 10.sp,
                        color = EchoText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Action Buttons Row: Sync Category Now & Share / Restore
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onSyncNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("${testTagPrefix}_sync_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) EchoCyan else EchoCardStroke,
                        contentColor = if (isEnabled) EchoOnCyan else EchoText
                    ),
                    enabled = isEnabled && !isSyncing,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                if (onRestore != null) {
                    Button(
                        onClick = onRestore,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("${testTagPrefix}_restore_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onShareBackup,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("${testTagPrefix}_export_btn"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export", fontSize = 10.sp)
                }
            }
        }
    }
}
