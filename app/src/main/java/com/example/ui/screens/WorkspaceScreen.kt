package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.HelpOutline
import com.example.ui.components.ItemActionBottomSheet
import com.example.ui.components.LongPressItemPayload
import com.example.ui.components.PositiveNewsTab
import com.example.ui.components.WorkspaceFaqTab
import com.example.ui.components.DolaVoiceJournalTab
import com.example.ui.components.WorkspaceExportBundleDialog
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.RecordVoiceOver
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import com.example.ai.DolaAiMode
import com.example.ui.components.DolaAiDialog
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import android.net.Uri
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.components.MediaType
import com.example.ui.components.MediaVaultTab
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.NotepadExportManager
import com.example.ui.components.ProNotepadTab
import com.example.ui.components.AthenaScienceCockpitView
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.WorkspaceItem
import com.example.ui.components.ReaderDialog
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import androidx.compose.material.icons.filled.Apps
import com.example.ui.components.DailyAppsHubTab
import com.example.model.DailyAppCategory
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.components.WorkspaceQuickActionsMenu
import com.example.ui.components.QuickAudioRecordingDialog
import com.example.ui.components.QuickSnapshotReviewDialog
import com.example.gateway.GoogleDriveSyncManager
import com.example.ui.components.CloudSyncSettingsDialog
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudOff
import com.example.ui.components.EchoInterfaceImageShareDialog
import com.example.ui.components.EchoHilseaGreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.ui.text.font.FontFamily

enum class WorkspaceTab {
    COCKPIT,
    SCIENCE_LAB,
    NOTES,
    PRO_NOTEPAD,
    VOICE_JOURNAL,
    FREQUENCY_LAB,
    PHOTOS_VIDEOS,
    FAQ_CAPABILITIES,
    POSITIVE_NEWS,
    APPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel,
    modifier: Modifier = Modifier,
    onNavigateToEcho: () -> Unit = {},
    onOpenAppDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentFolderId by viewModel.currentFolderId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isTrashView by viewModel.isTrashView.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    val items by viewModel.currentItems.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()

    var selectedWorkspaceTab by remember { mutableStateOf(WorkspaceTab.COCKPIT) }
    var activeViewerUri by remember { mutableStateOf<Uri?>(null) }
    var activeViewerType by remember { mutableStateOf(MediaType.VIDEO) }
    var activeViewerTitle by remember { mutableStateOf("") }

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var activeEditingNote by remember { mutableStateOf<WorkspaceItem?>(null) }
    var activeReaderItem by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var showDolaGeneralDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showEchoImageShareDialog by remember { mutableStateOf(false) }
    val isCloudAutoSyncActive by GoogleDriveSyncManager.isAutoSyncEnabled.collectAsState()

    // Quick Actions Floating Menu Dialogs & Camera Launchers
    var showQuickAudioRecordingDialog by remember { mutableStateOf(false) }
    var showQuickSnapshotReviewDialog by remember { mutableStateOf(false) }
    var quickSnapshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var quickSnapshotUri by remember { mutableStateOf<Uri?>(null) }

    val cameraSnapshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            quickSnapshotBitmap = bitmap
            quickSnapshotUri = null
            showQuickSnapshotReviewDialog = true
        } else {
            Toast.makeText(context, "No snapshot captured", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraSnapshotLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission required for snapshot capture", Toast.LENGTH_SHORT).show()
        }
    }

    // Long-Press Contextual Action Target
    var activeLongPressedItem by remember { mutableStateOf<WorkspaceItem?>(null) }
    var itemToRename by remember { mutableStateOf<WorkspaceItem?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var showExportBundleDialog by remember { mutableStateOf(false) }
    var showVoiceCalibrationDialog by remember { mutableStateOf(false) }

    // Breadcrumb title & Echo Projects detection
    val currentFolder = allFolders.find { it.id == currentFolderId }
    val echoFolder = allFolders.find { it.name.contains("Echo Projects", ignoreCase = true) || it.name.contains("Echo Cinema", ignoreCase = true) || it.name.contains("Vinema", ignoreCase = true) }
    val isInsideEchoFolder = currentFolderId != null && currentFolder?.id == echoFolder?.id

    Box(modifier = modifier.fillMaxSize().background(EchoBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Real-Time Multi-Mode Audio Frequency Waveform Visualizer (Oscillogram, Spectrum Bars, Lissajous Orbit)
            com.example.ui.components.AthenaWaveformHeaderWidget(
                onOpenVoiceCalibration = { showVoiceCalibrationDialog = true }
            )

            // Workspace Header Bar with Mode Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isTrashView) "Workspace — Trash" else "Workspace",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Text(
                            text = if (isTrashView) "Deleted items kept for restoration" else "Notes, folders, field logs and media vault",
                            fontSize = 13.sp,
                            color = EchoMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showExportBundleDialog = true },
                            modifier = Modifier.testTag("workspace_export_bundle_button")
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = "Export Backup Bundle", tint = EchoCyan, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = {
                                HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                viewModel.searchQuery.value = ""
                                Toast.makeText(context, "Workspace refreshed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("workspace_refresh_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Notes", tint = EchoCyan, modifier = Modifier.size(20.dp))
                        }

                        if (currentFolderId != null && !isTrashView && selectedWorkspaceTab == WorkspaceTab.NOTES) {
                            IconButton(
                                onClick = {
                                    val parent = allFolders.find { it.id == currentFolderId }?.parentId
                                    viewModel.navigateToFolder(parent)
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go up", tint = EchoCyan)
                            }
                        }
                    }
                }

                if (!isTrashView) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.COCKPIT,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.COCKPIT },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Cockpit", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_cockpit")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.SCIENCE_LAB,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.SCIENCE_LAB },
                            leadingIcon = {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Science Lab", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_science_lab")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.NOTES,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.NOTES },
                            leadingIcon = {
                                Icon(Icons.Default.Note, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Data Ledger", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_notes")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.PRO_NOTEPAD,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.PRO_NOTEPAD },
                            leadingIcon = {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Pro Notepad", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_pro_notepad")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.VOICE_JOURNAL,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.VOICE_JOURNAL },
                            leadingIcon = {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Voice Journal 🎙️", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_voice_journal")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.FREQUENCY_LAB,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.FREQUENCY_LAB },
                            leadingIcon = {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Frequency & Telemetry", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_frequency_lab")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.PHOTOS_VIDEOS,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.PHOTOS_VIDEOS },
                            leadingIcon = {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Media Vault", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_photos_videos")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.FAQ_CAPABILITIES,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.FAQ_CAPABILITIES },
                            leadingIcon = {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Capabilities & FAQ", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_faq_capabilities")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.POSITIVE_NEWS,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.POSITIVE_NEWS },
                            leadingIcon = {
                                Text("☀️", fontSize = 12.sp)
                            },
                            label = { Text("Positive News", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_positive_news")
                        )
                        FilterChip(
                            selected = selectedWorkspaceTab == WorkspaceTab.APPS,
                            onClick = { selectedWorkspaceTab = WorkspaceTab.APPS },
                            leadingIcon = {
                                Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text("Apps", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                selectedLeadingIconColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            modifier = Modifier.testTag("tab_workspace_apps")
                        )
                    }
                }
            }

            if (selectedWorkspaceTab == WorkspaceTab.COCKPIT && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
                    AthenaScienceCockpitView(
                        viewModel = viewModel,
                        onNavigateToProjects = {
                            viewModel.navigateToFolder(null)
                            selectedWorkspaceTab = WorkspaceTab.NOTES
                        },
                        onNavigateToDataLedger = {
                            selectedWorkspaceTab = WorkspaceTab.NOTES
                        },
                        onNavigateToEcho = onNavigateToEcho,
                        onOpenVoiceCalibration = { showVoiceCalibrationDialog = true },
                        onOpenExportBundle = { showExportBundleDialog = true },
                        onOpenNoteReader = { item ->
                            activeReaderItem = Triple(item.name, item.text, item.tags)
                        },
                        onCreateNewNote = {
                            val newId = viewModel.createNote(name = "Untitled note", initialText = "")
                            activeEditingNote = WorkspaceItem(id = newId, name = "Untitled note", text = "", kind = "note")
                        },
                        onCreateNewFolder = {
                            newFolderName = ""
                            showNewFolderDialog = true
                        },
                        onNavigateToScienceLab = {
                            selectedWorkspaceTab = WorkspaceTab.SCIENCE_LAB
                        },
                        onOpenAppDrawer = onOpenAppDrawer
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.SCIENCE_LAB && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
                    CircularChladniSolverScreen(
                        viewModel = viewModel,
                        onNavigateToDataLedger = {
                            selectedWorkspaceTab = WorkspaceTab.NOTES
                        }
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.APPS && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    DailyAppsHubTab(
                        initialCategory = DailyAppCategory.ALL,
                        headerTitle = "Workspace Daily Apps & Tools",
                        headerSubtitle = "20 daily utilities and on-device applications ready to use"
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.POSITIVE_NEWS && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    PositiveNewsTab(
                        toneGenerator = HarmonicToneGenerator.SHARED,
                        onSaveToWorkspaceNote = { noteTitle, content ->
                            viewModel.createNote(
                                name = noteTitle,
                                initialText = content
                            )
                        }
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.FAQ_CAPABILITIES && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    WorkspaceFaqTab(
                        toneGenerator = HarmonicToneGenerator.SHARED,
                        onSaveToWorkspaceNote = { noteTitle, content ->
                            viewModel.createNote(
                                name = noteTitle,
                                initialText = content
                            )
                        }
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.PHOTOS_VIDEOS && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    MediaVaultTab(
                        toneGenerator = HarmonicToneGenerator.SHARED,
                        onOpenMedia = { uri, type, title ->
                            activeViewerUri = uri
                            activeViewerType = type
                            activeViewerTitle = title
                        },
                        onAttachToWorkspaceNote = { noteTitle, uri ->
                            viewModel.createNote(
                                name = noteTitle,
                                initialText = "Attached Media Item: $noteTitle\nURI: $uri\nDate: ${System.currentTimeMillis()}"
                            )
                            Toast.makeText(context, "Attached to Workspace Note!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.PRO_NOTEPAD && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    ProNotepadTab(
                        toneGenerator = HarmonicToneGenerator.SHARED,
                        onSaveToWorkspaceNote = { noteTitle, content ->
                            viewModel.createNote(
                                name = noteTitle,
                                initialText = content
                            )
                        }
                    )
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.VOICE_JOURNAL && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    DolaVoiceJournalTab(workspaceViewModel = viewModel)
                }
            } else if (selectedWorkspaceTab == WorkspaceTab.FREQUENCY_LAB && !isTrashView) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Telemetry Header Bar with ECHO V4.2 Image Share Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EchoBgAlt)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ECHO-INTERFACE V4.2 TELEMETRY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Planetary Acoustics • Workspace Dedicated Lab",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }

                            Button(
                                onClick = { showEchoImageShareDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoHilseaGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_open_echo_image_share")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Image Share Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Embed Full FrequencyScreen inside Workspace
                        FrequencyScreen(toneGenerator = HarmonicToneGenerator.SHARED)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

            // Breadcrumb path trail
            if (!isTrashView) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Home",
                            fontWeight = if (currentFolderId == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentFolderId == null) EchoCyan else EchoMuted,
                            modifier = Modifier.clickable { viewModel.navigateToFolder(null) }
                        )
                        if (currentFolder != null) {
                            Text(" / ", color = EchoMuted)
                            Text(
                                text = currentFolder.name,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan
                            )
                        }
                    }
                }
            }

            // Action Buttons Toolbar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isTrashView) {
                        item {
                            Button(
                                onClick = {
                                    val noteId = viewModel.createNote()
                                    // Open editor immediately
                                    activeEditingNote = WorkspaceItem(
                                        id = noteId,
                                        parentId = currentFolderId,
                                        kind = "note",
                                        name = "Untitled note",
                                        text = "",
                                        modifiedTimestamp = System.currentTimeMillis()
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("new_note_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New note", fontWeight = FontWeight.Bold)
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    val text = clip?.getItemAt(0)?.text?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        val firstLine = text.trim().lines().firstOrNull()?.take(40) ?: "Pasted note"
                                        viewModel.createNote(name = firstLine, initialText = text)
                                        Toast.makeText(context, "Pasted as new note", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Paste as note", color = EchoText)
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { showNewFolderDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("new_folder_button")
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New folder", color = EchoText)
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    if (echoFolder != null) {
                                        viewModel.navigateToFolder(echoFolder.id)
                                    } else {
                                        viewModel.createFolder("🎬 Echo Projects")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9C0).copy(alpha = 0.2f), contentColor = Color(0xFF00D9C0)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D9C0)),
                                modifier = Modifier.testTag("toolbar_echo_bit_button")
                            ) {
                                Text("🎬 Echo Projects", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        item {
                            Button(
                                onClick = { showDolaGeneralDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCard, contentColor = EchoCyan),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan),
                                modifier = Modifier.testTag("workspace_dola_ai_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = EchoCyan)
                                Spacer(Modifier.width(4.dp))
                                Text("Dola AI", fontWeight = FontWeight.Bold)
                            }
                        }
                        item {
                            Button(
                                onClick = { showCloudSyncDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCloudAutoSyncActive) EchoCyan.copy(alpha = 0.18f) else EchoCard,
                                    contentColor = if (isCloudAutoSyncActive) EchoCyan else EchoMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCloudAutoSyncActive) EchoCyan else EchoCardStroke
                                ),
                                modifier = Modifier.testTag("workspace_toolbar_cloud_sync_button")
                            ) {
                                Icon(
                                    imageVector = if (isCloudAutoSyncActive) Icons.Default.CloudSync else Icons.Default.CloudOff,
                                    contentDescription = "Cloud Sync",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isCloudAutoSyncActive) EchoCyan else EchoAmber
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Cloud Sync", fontWeight = FontWeight.Bold)
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { viewModel.toggleSelectionMode() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isSelectionMode) "Done" else "Select", color = EchoText)
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = { viewModel.toggleTrashView() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isTrashView) "Back to Files" else "Trash", color = EchoText)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "ECHO-INTERFACE Workspace Backup")
                                    putExtra(Intent.EXTRA_TEXT, "42 Geniuses Workspace Export\nTotal items: ${items.size}\nCanon v4.0")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Backup"))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Backup", color = EchoText)
                        }
                    }
                }
            }

            // Featured Echo Projects Section
            if (isInsideEchoFolder) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("echo_workspace_vault_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00D9C0)))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎬", fontSize = 22.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ECHO PRODUCTIONS VAULT",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00D9C0)
                                        )
                                        Text(
                                            text = "Media Vault: Videos, Images, Plain Text, PDFs & Word Scripts",
                                            fontSize = 11.sp,
                                            color = EchoMuted
                                        )
                                    }
                                }
                                Button(
                                    onClick = onNavigateToEcho,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9C0), contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Echo Tab ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Supported: .mp4 • .png/.jpg • .txt/.md • .pdf • .docx",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                                Text(
                                    text = "${items.size} media items",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00D9C0),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else if (currentFolderId == null && !isTrashView) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (echoFolder != null) {
                                    viewModel.navigateToFolder(echoFolder.id)
                                } else {
                                    viewModel.createFolder("🎬 Echo Projects")
                                }
                            }
                            .testTag("echo_projects_quick_access_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00D9C0).copy(alpha = 0.5f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF00D9C0).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎬", fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Echo Projects & Media Vault",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoText
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF00D9C0).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ECHO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00D9C0)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Access your imported videos, stills, plain text, PDFs & Word docs",
                                        fontSize = 11.sp,
                                        color = EchoMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    if (echoFolder != null) {
                                        viewModel.navigateToFolder(echoFolder.id)
                                    } else {
                                        viewModel.createFolder("🎬 Echo Projects")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9C0), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Open Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Selection batch toolbar (when items are selected)
            if (isSelectionMode && selectedItemIds.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCyan))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedItemIds.size} selected",
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                fontSize = 14.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.togglePinSelected() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Pin", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        if (isTrashView) viewModel.deleteSelectedPermanently()
                                        else viewModel.moveSelectedToTrash()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isTrashView) "Delete Forever" else "Trash", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text("Search names, tags and text", color = EchoMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EchoMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = EchoMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoCard,
                        unfocusedContainerColor = EchoCard
                    )
                )
            }

            // Items List
            if (items.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTrashView) "Trash is empty." else "Nothing here yet.\nTap 'New note' or 'New folder' to begin.",
                            fontSize = 14.sp,
                            color = EchoMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                items(items, key = { it.id }) { item ->
                    WorkspaceItemRow(
                        item = item,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedItemIds.contains(item.id),
                        isTrashView = isTrashView,
                        onToggleSelect = { viewModel.toggleSelectItem(item.id) },
                        onClick = {
                            if (isSelectionMode) {
                                viewModel.toggleSelectItem(item.id)
                            } else if (item.kind == "folder") {
                                viewModel.navigateToFolder(item.id)
                            } else {
                                activeEditingNote = item
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                HarmonicToneGenerator.SHARED.playChime(com.example.audio.SoundEffectType.TAP)
                                activeLongPressedItem = item
                            }
                        },
                        onRestore = { viewModel.restoreItem(item.id) },
                        onDeleteForever = { viewModel.deletePermanently(item.id) }
                    )
                }
            }
        }
    }
}

        // New Folder Dialog
        if (showNewFolderDialog) {
            AlertDialog(
                onDismissRequest = { showNewFolderDialog = false },
                title = { Text("New Folder", color = EchoText) },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("Folder name", color = EchoMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createFolder(newFolderName)
                            newFolderName = ""
                            showNewFolderDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewFolderDialog = false }) {
                        Text("Cancel", color = EchoMuted)
                    }
                },
                containerColor = EchoBgAlt
            )
        }

        // Note Editor / Viewer Dialog
        activeEditingNote?.let { note ->
            NoteEditorDialog(
                note = note,
                allFolders = allFolders,
                onSave = { updated ->
                    viewModel.updateItem(updated)
                    activeEditingNote = null
                },
                onDelete = {
                    viewModel.moveToTrash(note.id)
                    activeEditingNote = null
                },
                onOpenReader = {
                    activeReaderItem = Triple(note.name, note.tags, note.text)
                },
                onDismiss = {
                    activeEditingNote = null
                }
            )
        }

        // Reader overlay
        activeReaderItem?.let { (title, subtitle, content) ->
            ReaderDialog(
                title = title,
                subtitle = subtitle,
                content = content,
                onDismiss = { activeReaderItem = null }
            )
        }

        // Dola AI Copilot Dialog
        if (showDolaGeneralDialog) {
            DolaAiDialog(
                initialText = "",
                initialMode = DolaAiMode.CHAT,
                onSaveAsNewNote = { newText ->
                    val firstLine = newText.trim().lines().firstOrNull()?.take(40) ?: "Dola AI Note"
                    viewModel.createNote(name = firstLine, initialText = newText)
                },
                onDismiss = { showDolaGeneralDialog = false }
            )
        }

        // Media Viewer Dialog (Dedicated Player with 90° rotation and Fullscreen toggle)
        if (activeViewerUri != null) {
            MediaViewerDialog(
                mediaUri = activeViewerUri,
                mediaType = activeViewerType,
                mediaTitle = activeViewerTitle,
                toneGenerator = HarmonicToneGenerator.SHARED,
                onAttachToWorkspaceNote = { noteTitle, uri ->
                    viewModel.createNote(
                        name = noteTitle,
                        initialText = "Attached Media Item: $noteTitle\nURI: $uri\nDate: ${System.currentTimeMillis()}"
                    )
                    Toast.makeText(context, "Attached to Workspace Note!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { activeViewerUri = null }
            )
        }

        // Voice Calibration Dialog (Persists pitch, rate, volume across all speech modules)
        if (showVoiceCalibrationDialog) {
            com.example.ui.components.VoiceCalibrationDialog(
                onDismiss = { showVoiceCalibrationDialog = false }
            )
        }

        // Long-Press Action Bottom Sheet for Workspace Notes & Folders
        activeLongPressedItem?.let { targetItem ->
            val isFolder = targetItem.kind == "folder"
            ItemActionBottomSheet(
                item = LongPressItemPayload(
                    id = targetItem.id,
                    title = targetItem.name,
                    textContent = targetItem.text,
                    subtitle = if (isFolder) "Workspace Folder" else "${targetItem.text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size} words · Modified recently",
                    category = if (isFolder) "Folder" else "Workspace Note",
                    isPinned = targetItem.isPinned,
                    isFolder = isFolder
                ),
                toneGenerator = HarmonicToneGenerator.SHARED,
                onDismiss = { activeLongPressedItem = null },
                onSaveToNotes = if (!isFolder) {
                    { title, content ->
                        viewModel.createNote(name = "$title (Copy)", initialText = content)
                    }
                } else null,
                onTogglePin = {
                    viewModel.togglePin(targetItem)
                },
                onDuplicate = if (!isFolder) {
                    {
                        viewModel.createNote(
                            name = "${targetItem.name} (Copy)",
                            initialText = targetItem.text,
                            tags = targetItem.tags
                        )
                        Toast.makeText(context, "Duplicated note!", Toast.LENGTH_SHORT).show()
                    }
                } else null,
                onRename = {
                    itemToRename = targetItem
                    renameInputText = targetItem.name
                },
                onDelete = {
                    if (isTrashView) {
                        viewModel.deletePermanently(targetItem.id)
                    } else {
                        viewModel.moveToTrash(targetItem.id)
                        Toast.makeText(context, "Moved to trash", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Rename Dialog
        itemToRename?.let { targetToRename ->
            AlertDialog(
                onDismissRequest = { itemToRename = null },
                title = { Text("Rename ${if (targetToRename.kind == "folder") "Folder" else "Note"}", color = EchoText) },
                text = {
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        placeholder = { Text("Enter name", color = EchoMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            if (renameInputText.isNotBlank()) {
                                viewModel.updateItem(targetToRename.copy(name = renameInputText.trim()))
                                Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show()
                                itemToRename = null
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.OutlinedButton(onClick = { itemToRename = null }) {
                        Text("Cancel", color = EchoMuted)
                    }
                },
                containerColor = EchoBgAlt
            )
        }

        // Floating 'Quick Actions' Menu (Speed Dial)
        WorkspaceQuickActionsMenu(
            onNewNote = {
                val noteId = viewModel.createNote()
                activeEditingNote = WorkspaceItem(
                    id = noteId,
                    parentId = currentFolderId,
                    kind = "note",
                    name = "Untitled note",
                    text = "",
                    modifiedTimestamp = System.currentTimeMillis()
                )
            },
            onStartRecording = {
                showQuickAudioRecordingDialog = true
            },
            onSnapshotCapture = {
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasCameraPermission) {
                    cameraSnapshotLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPasteNote = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                val text = clip?.getItemAt(0)?.text?.toString() ?: ""
                if (text.isNotBlank()) {
                    val firstLine = text.trim().lines().firstOrNull()?.take(40) ?: "Pasted note"
                    viewModel.createNote(name = firstLine, initialText = text)
                    Toast.makeText(context, "Pasted as new note", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                }
            },
            onDolaAi = {
                showDolaGeneralDialog = true
            },
            onNewFolder = {
                showNewFolderDialog = true
            },
            onCloudSync = {
                showCloudSyncDialog = true
            }
        )

        // Cloud Sync Settings Dialog Modal
        if (showCloudSyncDialog) {
            CloudSyncSettingsDialog(
                onDismiss = { showCloudSyncDialog = false }
            )
        }

        // Full Workspace Export Backup Bundle Dialog
        if (showExportBundleDialog) {
            WorkspaceExportBundleDialog(
                workspaceViewModel = viewModel,
                onDismissRequest = { showExportBundleDialog = false }
            )
        }

        // ECHO-INTERFACE V4.2 Image Share Dialog Overlay
        if (showEchoImageShareDialog) {
            EchoInterfaceImageShareDialog(
                frequencyHz = 42.598f,
                onSaveToWorkspaceNote = { title, content ->
                    viewModel.createNote(
                        name = title,
                        initialText = content,
                        tags = "Frequency, Telemetry, EchoV4"
                    )
                },
                onDismiss = { showEchoImageShareDialog = false }
            )
        }

        // Quick Audio Recording Dialog Overlay
        if (showQuickAudioRecordingDialog) {
            QuickAudioRecordingDialog(
                onDismiss = { showQuickAudioRecordingDialog = false },
                onSaveVoiceNote = { title, content, audioUri ->
                    viewModel.createNote(
                        name = title,
                        initialText = content,
                        tags = "audio, voicememo, recording"
                    )
                }
            )
        }

        // Quick Snapshot Capture Review Dialog Overlay
        if (showQuickSnapshotReviewDialog) {
            QuickSnapshotReviewDialog(
                capturedBitmap = quickSnapshotBitmap,
                capturedUri = quickSnapshotUri,
                onDismiss = {
                    showQuickSnapshotReviewDialog = false
                    quickSnapshotBitmap = null
                    quickSnapshotUri = null
                },
                onSaveSnapshotNote = { title, content, imageUri ->
                    viewModel.createNote(
                        name = title,
                        initialText = content,
                        tags = "photo, snapshot, camera"
                    )
                }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WorkspaceItemRow(
    item: WorkspaceItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    isTrashView: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("item_row_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) EchoCyan else EchoCardStroke
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = EchoCyan, checkmarkColor = EchoOnCyan)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Text(
                    text = if (item.kind == "folder") "📁" else "📝",
                    fontSize = 20.sp
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isPinned) {
                        Text("📌 ", fontSize = 12.sp)
                    }
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        color = EchoText,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                val wordCount = if (item.kind == "note") "${item.text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size} words" else "Folder"
                val tagStr = if (item.tags.isNotBlank()) " · ${item.tags}" else ""
                Text(
                    text = "$wordCount$tagStr",
                    fontSize = 12.sp,
                    color = EchoMuted
                )
            }

            if (isTrashView) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = EchoGreen)
                    }
                    IconButton(onClick = onDeleteForever) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorDialog(
    note: WorkspaceItem,
    allFolders: List<WorkspaceItem>,
    onSave: (WorkspaceItem) -> Unit,
    onDelete: () -> Unit,
    onOpenReader: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(note.name) }
    var text by remember { mutableStateOf(note.text) }
    var tags by remember { mutableStateOf(note.tags) }
    var isPinned by remember { mutableStateOf(note.isPinned) }
    var parentId by remember { mutableStateOf(note.parentId) }
    var showFolderMenu by remember { mutableStateOf(false) }
    var showDolaEditorDialog by remember { mutableStateOf(false) }
    var dolaEditorMode by remember { mutableStateOf(DolaAiMode.REFINE) }

    val wordCount = remember(text) {
        if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                // Top control bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onSave(note.copy(name = title, text = text, tags = tags, isPinned = isPinned, parentId = parentId))
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = EchoCyan)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                dolaEditorMode = DolaAiMode.REFINE
                                showDolaEditorDialog = true
                            },
                            modifier = Modifier.testTag("note_dola_ai_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Dola AI Copilot", tint = EchoCyan)
                        }
                        IconButton(onClick = onOpenReader) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Read Mode", tint = EchoText)
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Note text", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EchoText)
                        }
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = if (isPinned) EchoCyan else EchoMuted)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoText)
                        }
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Tags input
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Folder destination selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentFolderTitle = allFolders.find { it.id == parentId }?.name ?: "Home"
                    Text("Folder: $currentFolderTitle", color = EchoMuted, fontSize = 13.sp)

                    Box {
                        TextButton(onClick = { showFolderMenu = true }) {
                            Text("Move to...", color = EchoCyan, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = showFolderMenu,
                            onDismissRequest = { showFolderMenu = false },
                            modifier = Modifier.background(EchoBgAlt)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Home (Root)", color = EchoText) },
                                onClick = {
                                    parentId = null
                                    showFolderMenu = false
                                }
                            )
                            allFolders.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name, color = EchoText) },
                                    onClick = {
                                        parentId = f.id
                                        showFolderMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Word count & Dola Quick Actions banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$wordCount words · ${text.length} chars",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = {
                                dolaEditorMode = DolaAiMode.REFINE
                                showDolaEditorDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ Refine", color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = {
                                dolaEditorMode = DolaAiMode.EXPAND
                                showDolaEditorDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("✨ Expand", color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = {
                                dolaEditorMode = DolaAiMode.SUMMARIZE
                                showDolaEditorDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("📋 Summary", color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Note Body Editor
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Write your field note, logs, or thoughts here...", color = EchoMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    )
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            NotepadExportManager.exportPdf(context, title, text)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("note_btn_export_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            NotepadExportManager.exportPlainText(context, title, text)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("note_btn_export_txt")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "TXT", modifier = Modifier.size(14.dp), tint = EchoCyan)
                        Spacer(Modifier.width(4.dp))
                        Text("TXT", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            NotepadExportManager.exportHtml(context, title, text)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("note_btn_export_html")
                    ) {
                        Icon(Icons.Default.Html, contentDescription = "HTML", modifier = Modifier.size(14.dp), tint = EchoCyan)
                        Spacer(Modifier.width(4.dp))
                        Text("HTML", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val clipboard = NotepadExportManager.getClipboardText(context)
                            if (!clipboard.isNullOrBlank()) {
                                text = if (text.isBlank()) clipboard else "$text\n$clipboard"
                                Toast.makeText(context, "Pasted clipboard into note", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("note_btn_paste")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(14.dp))
                    }
                }

                if (showDolaEditorDialog) {
                    DolaAiDialog(
                        initialText = text,
                        initialMode = dolaEditorMode,
                        onUseReplacement = { text = it },
                        onSaveAsNewNote = { newText ->
                            onSave(note.copy(name = "$title (Revised)", text = newText))
                        },
                        onDismiss = { showDolaEditorDialog = false }
                    )
                }
            }
        }
    }
}
