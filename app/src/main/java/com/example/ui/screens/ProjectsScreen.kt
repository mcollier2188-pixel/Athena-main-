package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.ui.components.EchoCinemaVideoPlayer
import com.example.ui.components.CinemaVideoSource

// Cinema Theme Colors matching HTML styling
private val CinemaTeal = Color(0xFF00D9C0)
private val CinemaGreen = Color(0xFF66BB6A)
private val CinemaGold = Color(0xFFE6C875)
private val CinemaPanel = Color(0xFF161925)
private val CinemaCardBg = Color(0xFF1A1D2B)
private val CinemaRed = Color(0xFFFF6B6B)
private val CinemaBlue = Color(0xFF4D96FF)

data class CinemaEpisode(
    val num: Int,
    val title: String,
    val desc: String
)

data class CinemaProjectItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val meta: String,
    val desc: String,
    val isSongwriterTheme: Boolean = false,
    val episodes: List<CinemaEpisode> = emptyList()
)

data class UploadedMediaSummary(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val categoryLabel: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * Native Android implementation of ECHO CINEMA — Echo Verse (85.2 Hz).
 * Features full saga carousels, detailed issue/episode views, audio tone preview,
 * multi-format file uploads (Video, Image, Plain text, PDF, Word docx/doc) with
 * direct insertion and persistence into Room Workspace.
 */
@Composable
fun ProjectsScreen(
    viewModel: WorkspaceViewModel,
    onNavigateToWorkspaceFolder: (String) -> Unit,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTonePlaying by toneGenerator.isPlayingState.collectAsState()

    // Sub-navigation tabs: 0: Home, 1: Workspace, 2: The Conductor, 3: The Songwriter, 4: My List
    var selectedCinemaNav by rememberSaveable { mutableIntStateOf(0) }
    var selectedDetailItem by remember { mutableStateOf<CinemaProjectItem?>(null) }
    var showUploadModal by remember { mutableStateOf(false) }

    // Upload notification state
    var lastUploadedItem by remember { mutableStateOf<Pair<String, String>?>(null) } // fileName, folderId

    // User bookmarks / My List
    val bookmarkedIds = remember { mutableStateListOf("freq-s1", "song-book1", "solo-conductor") }

    // Local list of files uploaded during this session
    val sessionUploadedFiles = remember { mutableStateListOf<UploadedMediaSummary>() }

    // Cinema Workspace internal state
    var wsActiveTab by rememberSaveable { mutableIntStateOf(0) } // 0: Library, 1: Studio Writer, 2: Player, 3: Uploads
    var notepadText by rememberSaveable {
        mutableStateOf(
            "Quick Idea for Season 7:\n" +
            "The 85.2 Hz harmonic doubling reflects back from the Solent chalk layer.\n" +
            "Mara realizes the silence wasn't absent — it was out of phase."
        )
    }
    var studioScriptText by rememberSaveable {
        mutableStateOf(
            "# ECHO CINEMA SCRIPT DRAFT\n" +
            "TITLE: The First Note (Origin Prelude)\n" +
            "CARRIER: 85.200 Hz • Solent 11.74s Envelope\n\n" +
            "SCENE 1 — EXT. PORTSMOUTH HARBOUR — DUSK\n\n" +
            "Waves lap rhythmically against the stone bastion.\n" +
            "The low-frequency vibration is felt in the chest before it is heard.\n\n" +
            "ELIAN\n" +
            "\"It was never about being the loudest voice.\n" +
            "It was about being the first one patient enough to listen.\""
        )
    }

    // Document Picker Launcher supporting Video, Images, Text, PDF, Word
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            val (fileName, fileSize) = getFileMetadata(context, fileUri)
            val detectedMime = context.contentResolver.getType(fileUri) ?: resolveMimeFromName(fileName)
            val fileContent = readFileContent(context, fileUri, detectedMime, fileName)

            viewModel.uploadFileToEchoWorkspace(
                fileName = fileName,
                mimeType = detectedMime,
                fileSizeBytes = fileSize,
                textContent = fileContent,
                uriString = fileUri.toString()
            ) { folderId, _ ->
                val (label, icon, color) = getMediaClassification(fileName, detectedMime)
                sessionUploadedFiles.add(0, UploadedMediaSummary(fileName, detectedMime, fileSize, label, icon, color))
                lastUploadedItem = Pair(fileName, folderId)
                Toast.makeText(context, "Uploaded '$fileName' to Workspace!", Toast.LENGTH_LONG).show()
                showUploadModal = false
            }
        }
    }

    // Master catalog of all Cinema Projects and Sagas
    val allCinemaItems = remember { getCinemaCatalog() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
            .testTag("cinema_screen_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // =========================================================================
            // Top Cinema Header & Navigation Bar
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaPanel)
                    .border(width = 1.dp, color = EchoCardStroke.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎬 ECHO CINEMA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CinemaTeal,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CinemaTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "85.2 Hz",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CinemaTeal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Quick Upload Media / Documents Button
                        IconButton(
                            onClick = { showUploadModal = true },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("cinema_top_upload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload Media / Docs",
                                tint = CinemaGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Quick Audio Preview Button
                        IconButton(
                            onClick = {
                                if (isTonePlaying) {
                                    toneGenerator.stop()
                                } else {
                                    toneGenerator.start(
                                        frequencies = listOf(85.200),
                                        mult = 2.0,
                                        tidalLfo = true
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("cinema_audio_preview_toggle")
                        ) {
                            Icon(
                                imageVector = if (isTonePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "85.2 Hz Preview",
                                tint = if (isTonePlaying) CinemaTeal else EchoMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Sub-navigation tab row (Home, Workspace, The Conductor, The Songwriter, My List)
                ScrollableTabRow(
                    selectedTabIndex = selectedCinemaNav,
                    containerColor = CinemaPanel,
                    contentColor = CinemaTeal,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCinemaNav]),
                            color = if (selectedCinemaNav == 1) CinemaGold else if (selectedCinemaNav == 3) CinemaGreen else CinemaTeal
                        )
                    },
                    divider = {}
                ) {
                    val navItems = listOf(
                        "Home",
                        "📂 Workspace",
                        "The Conductor",
                        "The Songwriter",
                        "My List (${bookmarkedIds.size})"
                    )
                    navItems.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedCinemaNav == index,
                            onClick = { selectedCinemaNav = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCinemaNav == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCinemaNav == index) {
                                        if (index == 1) CinemaGold else if (index == 3) CinemaGreen else CinemaTeal
                                    } else EchoMuted
                                )
                            },
                            modifier = Modifier.testTag("cinema_tab_$index")
                        )
                    }
                }
            }

            // Quick Banner for Recently Uploaded Item
            lastUploadedItem?.let { (name, folderId) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaCardBg),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CinemaGold.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CinemaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Uploaded: $name",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                maxLines = 1
                            )
                        }
                        TextButton(
                            onClick = { onNavigateToWorkspaceFolder(folderId) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Open in Workspace →", fontSize = 11.sp, color = CinemaGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // =========================================================================
            // Main Content Area based on Sub-nav
            // =========================================================================
            when (selectedCinemaNav) {
                0 -> {
                    // Home View: Hero Banner + All Cinema Sagas
                    CinemaHomeContent(
                        allCinemaItems = allCinemaItems,
                        onSelectItem = { selectedDetailItem = it },
                        onExploreConductor = { selectedCinemaNav = 2 },
                        onAddAllToWorkspace = {
                            viewModel.importAllEchoProjects { count, folderId ->
                                Toast.makeText(context, "Added $count Echo sagas to Workspace!", Toast.LENGTH_SHORT).show()
                                onNavigateToWorkspaceFolder(folderId)
                            }
                        },
                        onOpenUploadDialog = { showUploadModal = true }
                    )
                }
                1 -> {
                    // Dedicated Echo Workspace Tab: Uploads, Library, Studio Writer, Media Player, Notepad
                    CinemaWorkspaceContent(
                        wsActiveTab = wsActiveTab,
                        onSelectWsTab = { wsActiveTab = it },
                        notepadText = notepadText,
                        onNotepadChange = { notepadText = it },
                        studioScriptText = studioScriptText,
                        onStudioScriptChange = { studioScriptText = it },
                        isTonePlaying = isTonePlaying,
                        onToggleTone = {
                            if (isTonePlaying) {
                                toneGenerator.stop()
                            } else {
                                toneGenerator.start(listOf(85.200), mult = 2.0, tidalLfo = true)
                            }
                        },
                        uploadedFiles = sessionUploadedFiles,
                        onOpenUploadPicker = { showUploadModal = true },
                        onImportAllProjects = {
                            viewModel.importAllEchoProjects { count, folderId ->
                                Toast.makeText(context, "Seeded $count Echo projects in Room!", Toast.LENGTH_SHORT).show()
                                onNavigateToWorkspaceFolder(folderId)
                            }
                        },
                        onSaveScriptToWorkspace = {
                            val title = studioScriptText.lineSequence().firstOrNull { it.startsWith("TITLE:") }
                                ?.removePrefix("TITLE:")?.trim() ?: "Echo Script Draft"
                            viewModel.addEchoProjectToWorkspace(
                                name = title,
                                content = studioScriptText,
                                tags = "echo, script, studio"
                            ) { folderId, _ ->
                                Toast.makeText(context, "Saved script to Workspace!", Toast.LENGTH_SHORT).show()
                                onNavigateToWorkspaceFolder(folderId)
                            }
                        },
                        onLoadSampleScript = { filename, text ->
                            studioScriptText = text
                            Toast.makeText(context, "Loaded $filename into Studio Writer", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateToWorkspaceFolder = onNavigateToWorkspaceFolder
                    )
                }
                2 -> {
                    // The Conductor Saga View
                    CinemaCategoryView(
                        categoryTitle = "⚡ THE CONDUCTOR — Frequency Saga (85.2 Hz)",
                        categoryDesc = "A saga of connection, hope, and the spaces between silence. Carrier frequency 85.2 Hz across 6 seasons.",
                        themeColor = CinemaTeal,
                        items = allCinemaItems.filter { !it.isSongwriterTheme },
                        onSelectItem = { selectedDetailItem = it }
                    )
                }
                3 -> {
                    // The Songwriter's Notebook View
                    CinemaCategoryView(
                        categoryTitle = "🎵 THE SONGWRITER'S NOTEBOOK — Trilogy",
                        categoryDesc = "One worn leather notebook on a park bench. Five strangers find it. The circle widens and the song goes on.",
                        themeColor = CinemaGreen,
                        items = allCinemaItems.filter { it.isSongwriterTheme },
                        onSelectItem = { selectedDetailItem = it }
                    )
                }
                4 -> {
                    // My List View
                    CinemaMyListView(
                        bookmarkedItems = allCinemaItems.filter { bookmarkedIds.contains(it.id) },
                        onSelectItem = { selectedDetailItem = it },
                        onRemoveBookmark = { id -> bookmarkedIds.remove(id) }
                    )
                }
            }
        }

        // =========================================================================
        // Upload Media & Document Modal Dialog
        // =========================================================================
        if (showUploadModal) {
            UploadCinemaFileDialog(
                onDismiss = { showUploadModal = false },
                onLaunchPicker = { mimeTypes ->
                    openDocumentLauncher.launch(mimeTypes)
                }
            )
        }

        // =========================================================================
        // Detail Overlay Dialog (Episode/Issue Guide, Workspace Add, Audio Preview)
        // =========================================================================
        selectedDetailItem?.let { item ->
            val isBookmarked = bookmarkedIds.contains(item.id)
            CinemaDetailDialog(
                item = item,
                isBookmarked = isBookmarked,
                onToggleBookmark = {
                    if (isBookmarked) bookmarkedIds.remove(item.id) else bookmarkedIds.add(item.id)
                },
                onAddToWorkspace = {
                    val fullDoc = buildString {
                        append("# ${item.title} — ${item.subtitle}\n")
                        append("Meta: ${item.meta}\n\n")
                        append("## Synopsis\n${item.desc}\n\n")
                        if (item.episodes.isNotEmpty()) {
                            append("## Episodes / Issues Guide\n")
                            item.episodes.forEach { ep ->
                                append("### #${ep.num}: ${ep.title}\n")
                                append("${ep.desc}\n\n")
                            }
                        }
                        append("---\nImported from ECHO (85.2 Hz Verse)\n")
                    }
                    viewModel.addEchoProjectToWorkspace(
                        name = "${item.badge} ${item.title}",
                        content = fullDoc,
                        tags = if (item.isSongwriterTheme) "echo, songwriter, notebook" else "echo, frequency, conductor"
                    ) { folderId, _ ->
                        Toast.makeText(context, "Added '${item.title}' to Workspace!", Toast.LENGTH_SHORT).show()
                        onNavigateToWorkspaceFolder(folderId)
                    }
                },
                onPlayPreviewAudio = {
                    toneGenerator.start(
                        frequencies = if (item.isSongwriterTheme) listOf(42.600, 85.200) else listOf(85.200),
                        mult = 2.0,
                        tidalLfo = true
                    )
                    Toast.makeText(context, "Playing 85.2 Hz harmonic tone preview", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { selectedDetailItem = null }
            )
        }
    }
}

// =========================================================================
// Upload Media & Document Picker Dialog
// =========================================================================

@Composable
private fun UploadCinemaFileDialog(
    onDismiss: () -> Unit,
    onLaunchPicker: (Array<String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(24.dp))
                Text(
                    text = "Upload to Cinema Workspace",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Import media and production files into the '🎬 Echo Cinema & Projects' Room workspace folder. Choose the document type below:",
                    fontSize = 12.sp,
                    color = EchoMuted,
                    lineHeight = 17.sp
                )

                // 1. Video Button
                UploadOptionCard(
                    icon = Icons.Default.Videocam,
                    title = "Video Footage",
                    subtitle = "Trailers, reels, scenes (.mp4, .mkv, .webm)",
                    color = CinemaTeal,
                    onClick = {
                        onLaunchPicker(arrayOf("video/*"))
                    }
                )

                // 2. Images Button
                UploadOptionCard(
                    icon = Icons.Default.Image,
                    title = "Images & Stills",
                    subtitle = "Storyboards, concept art, photos (.png, .jpg, .webp)",
                    color = CinemaGreen,
                    onClick = {
                        onLaunchPicker(arrayOf("image/*"))
                    }
                )

                // 3. Plain Text Button
                UploadOptionCard(
                    icon = Icons.Default.Description,
                    title = "Plain Text",
                    subtitle = "Scripts, scene dialogue, notes (.txt, .md)",
                    color = CinemaGold,
                    onClick = {
                        onLaunchPicker(arrayOf("text/plain", "text/*"))
                    }
                )

                // 4. PDF Document Button
                UploadOptionCard(
                    icon = Icons.Default.PictureAsPdf,
                    title = "PDF Document",
                    subtitle = "Screenplays, series bibles, treatments (.pdf)",
                    color = CinemaRed,
                    onClick = {
                        onLaunchPicker(arrayOf("application/pdf"))
                    }
                )

                // 5. Word Document Button
                UploadOptionCard(
                    icon = Icons.Default.Article,
                    title = "Word Document",
                    subtitle = "Microsoft Word screenplays (.docx, .doc)",
                    color = CinemaBlue,
                    onClick = {
                        onLaunchPicker(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/msword",
                                "application/octet-stream"
                            )
                        )
                    }
                )

                // 6. Any File Button
                OutlinedButton(
                    onClick = {
                        onLaunchPicker(
                            arrayOf(
                                "video/*",
                                "image/*",
                                "text/*",
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("upload_any_file_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoText),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Browse Any File (*/*)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EchoMuted)
            }
        },
        containerColor = CinemaCardBg
    )
}

@Composable
private fun UploadOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("upload_option_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = CinemaPanel),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f))
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EchoText)
                Text(text = subtitle, fontSize = 11.sp, color = EchoMuted)
            }

            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// =========================================================================
// 1. Home Content: Hero Banner + Section Carousels
// =========================================================================

@Composable
private fun CinemaHomeContent(
    allCinemaItems: List<CinemaProjectItem>,
    onSelectItem: (CinemaProjectItem) -> Unit,
    onExploreConductor: () -> Unit,
    onAddAllToWorkspace: () -> Unit,
    onOpenUploadDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Banner
        item {
            CinemaHeroBanner(
                onExplore = onExploreConductor,
                onAddAllToWorkspace = onAddAllToWorkspace,
                onOpenUpload = onOpenUploadDialog,
                onInfo = {
                    allCinemaItems.find { it.id == "conductor-about" }?.let(onSelectItem)
                }
            )
        }

        // Section 1: TRILOGY ONE — The Awakening
        item {
            val trilogyOne = allCinemaItems.filter { it.id in listOf("freq-s1", "freq-s2", "freq-s3") }
            CinemaSectionRow(
                title = "⚡ TRILOGY ONE — The Awakening",
                themeColor = CinemaTeal,
                items = trilogyOne,
                onSelectItem = onSelectItem
            )
        }

        // Section 2: TRILOGY TWO — The Growing Light
        item {
            val trilogyTwo = allCinemaItems.filter { it.id in listOf("freq-s4", "freq-s5", "freq-s6") }
            CinemaSectionRow(
                title = "⚡ TRILOGY TWO — The Growing Light",
                themeColor = CinemaTeal,
                items = trilogyTwo,
                onSelectItem = onSelectItem
            )
        }

        // Section 3: SOLO RUNS — Echo Verse
        item {
            val soloRuns = allCinemaItems.filter { it.id.startsWith("solo-") }
            CinemaSectionRow(
                title = "⚡ SOLO RUNS — Echo Verse",
                themeColor = CinemaTeal,
                items = soloRuns,
                onSelectItem = onSelectItem
            )
        }

        // Section 4: THE SONGWRITER'S NOTEBOOK — Trilogy
        item {
            val songwriterBooks = allCinemaItems.filter { it.id.startsWith("song-book") }
            CinemaSectionRow(
                title = "🎵 THE SONGWRITER'S NOTEBOOK — Trilogy",
                themeColor = CinemaGreen,
                items = songwriterBooks,
                onSelectItem = onSelectItem
            )
        }

        // Section 5: EXTRAS & COMPANIONS
        item {
            val extras = allCinemaItems.filter { it.id in listOf("freq-bible", "conductor-story", "freq-special", "freq-movie", "maya-journal", "song-bible", "fan-spec") }
            CinemaSectionRow(
                title = "📡 EXTRAS & UNIVERSE BIBLES",
                themeColor = CinemaGold,
                items = extras,
                onSelectItem = onSelectItem
            )
        }
    }
}

// =========================================================================
// Hero Banner Composable
// =========================================================================

@Composable
private fun CinemaHeroBanner(
    onExplore: () -> Unit,
    onAddAllToWorkspace: () -> Unit,
    onOpenUpload: () -> Unit,
    onInfo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("cinema_hero_banner"),
        colors = CardDefaults.cardColors(containerColor = CinemaPanel),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CinemaTeal, CinemaGreen, CinemaGold))
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CinemaTeal)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ORIGINAL SAGA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CinemaPanel)
                        .border(1.dp, CinemaTeal.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "85.2 Hz HARMONIC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CinemaTeal
                    )
                }
            }

            Text(
                text = "The Conductor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = EchoText,
                lineHeight = 32.sp
            )

            Text(
                text = "When a voice calls out from the darkness, something answers. A saga of connection, hope, and the spaces between the silence. Echo Verse (85.2 Hz) — Every voice matters.",
                fontSize = 13.sp,
                color = Color(0xFFCCCCCC),
                lineHeight = 19.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExplore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hero_explore_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Explore", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenUpload,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("hero_upload_media_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddAllToWorkspace,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("hero_add_workspace_button")
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("To Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onInfo,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Series Info", tint = EchoMuted)
                }
            }
        }
    }
}

// =========================================================================
// Cinema Section Row (Horizontal Carousel)
// =========================================================================

@Composable
private fun CinemaSectionRow(
    title: String,
    themeColor: Color,
    items: List<CinemaProjectItem>,
    onSelectItem: (CinemaProjectItem) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items) { item ->
                CinemaCard(
                    item = item,
                    themeColor = themeColor,
                    onClick = { onSelectItem(item) }
                )
            }
        }
    }
}

@Composable
private fun CinemaCard(
    item: CinemaProjectItem,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clickable(onClick = onClick)
            .testTag("cinema_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = CinemaCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(themeColor.copy(alpha = 0.35f))
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Badge / Identifier Number
            Text(
                text = item.badge,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = themeColor.copy(alpha = 0.18f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText,
                        maxLines = 1
                    )
                    Text(
                        text = item.meta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.desc,
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA),
                        lineHeight = 15.sp,
                        maxLines = 3
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.episodes.isNotEmpty()) "${item.episodes.size} Entries" else "Guide",
                        fontSize = 10.sp,
                        color = EchoMuted
                    )
                    Text(
                        text = "View Details →",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. Cinema Workspace Sub-Tab Content
// =========================================================================

@Composable
private fun CinemaWorkspaceContent(
    wsActiveTab: Int,
    onSelectWsTab: (Int) -> Unit,
    notepadText: String,
    onNotepadChange: (String) -> Unit,
    studioScriptText: String,
    onStudioScriptChange: (String) -> Unit,
    isTonePlaying: Boolean,
    onToggleTone: () -> Unit,
    uploadedFiles: List<UploadedMediaSummary>,
    onOpenUploadPicker: () -> Unit,
    onImportAllProjects: () -> Unit,
    onSaveScriptToWorkspace: () -> Unit,
    onLoadSampleScript: (String, String) -> Unit,
    onNavigateToWorkspaceFolder: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workspace Sub-Tabs (Library, Studio Writer, Player, Uploads)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CinemaPanel)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("📖 Library", "✍️ Studio", "🎥 Player", "📁 Uploads (${uploadedFiles.size})")
            tabs.forEachIndexed { index, label ->
                val isSelected = wsActiveTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CinemaGold else Color.Transparent)
                        .clickable { onSelectWsTab(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else EchoMuted
                    )
                }
            }
        }

        // Action Hero Card: Upload Media / Documents directly into Room Workspace
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CinemaCardBg),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CinemaGold.copy(alpha = 0.5f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Upload Media & Documents",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CinemaGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Room SQLite", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CinemaGold)
                    }
                }
                Text(
                    text = "Upload videos, images, plain text scripts, PDF guides, and Word (.docx/.doc) files directly to your Room Workspace under '🎬 Echo Cinema & Projects'.",
                    fontSize = 12.sp,
                    color = EchoMuted,
                    lineHeight = 17.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenUploadPicker,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("ws_open_upload_picker_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Upload Files", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onImportAllProjects,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaTeal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaTeal.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("ws_seed_all_sagas_btn")
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Seed Sagas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tab Content Switcher
        when (wsActiveTab) {
            0 -> {
                // Library View
                CinemaLibraryPanel(
                    onLoadSample = onLoadSampleScript
                )
            }
            1 -> {
                // Studio Writer View
                CinemaStudioPanel(
                    scriptText = studioScriptText,
                    onScriptChange = onStudioScriptChange,
                    onSaveToWorkspace = onSaveScriptToWorkspace
                )
            }
            2 -> {
                // Player View with Embedded Video Player
                CinemaPlayerPanel(
                    isTonePlaying = isTonePlaying,
                    onToggleTone = onToggleTone,
                    uploadedFiles = uploadedFiles,
                    onOpenUploadPicker = onOpenUploadPicker
                )
            }
            3 -> {
                // Uploaded Files View
                CinemaUploadsPanel(
                    uploadedFiles = uploadedFiles,
                    onOpenUploadPicker = onOpenUploadPicker,
                    onNavigateToWorkspace = { onNavigateToWorkspaceFolder("") }
                )
            }
        }

        // Quick Notepad (Always Available at bottom of Workspace)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CinemaPanel),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "📝 Quick Notepad (Auto-saves)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                    }
                    Text(
                        text = "${notepadText.length} chars",
                        fontSize = 10.sp,
                        color = EchoMuted
                    )
                }

                OutlinedTextField(
                    value = notepadText,
                    onValueChange = onNotepadChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("cinema_notepad_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CinemaCardBg,
                        unfocusedContainerColor = CinemaCardBg,
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    placeholder = { Text("Jot down scene ideas, character dialogue, or frequency notes here...", fontSize = 11.sp, color = EchoMuted) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
                )
            }
        }
    }
}

@Composable
private fun CinemaUploadsPanel(
    uploadedFiles: List<UploadedMediaSummary>,
    onOpenUploadPicker: () -> Unit,
    onNavigateToWorkspace: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CinemaPanel),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📁 UPLOADED MEDIA & DOCUMENTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CinemaGold
                )
                TextButton(onClick = onNavigateToWorkspace, contentPadding = PaddingValues(0.dp)) {
                    Text("Open Room Workspace →", fontSize = 11.sp, color = CinemaGold)
                }
            }

            if (uploadedFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CinemaCardBg)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "No media or documents uploaded yet.",
                            fontSize = 12.sp,
                            color = EchoMuted
                        )
                        Button(
                            onClick = onOpenUploadPicker,
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Upload Video, Image, Text, PDF, or Word", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                uploadedFiles.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CinemaCardBg)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(item.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(16.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.fileName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                            Text(text = "${item.categoryLabel} • ${formatFileSize(item.sizeBytes)}", fontSize = 10.sp, color = EchoMuted)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CinemaGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Saved in Room", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CinemaGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaLibraryPanel(
    onLoadSample: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CinemaPanel),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "📂 PROJECT LIBRARY BUNDLES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CinemaGold
            )

            // Prime Saga
            Column(modifier = Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "📁 Prime Saga — Anchor Canon ⭐", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CinemaGold)
                Text(
                    text = "• Issues 1–10 — Complete [LOCKED IN CANON]\n• Special One-Shots & World Lore",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

            // Solo Runs
            Column(modifier = Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "📁 Solo Runs — Issue 1 Scripts", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CinemaTeal)
                val soloScripts = listOf(
                    "🎺 The Conductor — Issue 1: The First Note" to "# The Conductor — Issue 1\nElian listens in the valley centuries ago...",
                    "🌊 Mara — Issue 1: The Quiet Child" to "# Mara — Issue 1\nThe Solent waves whisper at 85.2 Hz...",
                    "🛡️ Elara — Issue 1: The Guardian" to "# Elara — Issue 1\nWatching the perimeter where the signal frays...",
                    "🤍 Ruth — Issue 1: The Best Friend" to "# Ruth — Issue 1\nHolding the lamp when the frequency goes dark...",
                    "⚫ The Silencer — Issue 1: The First Silence" to "# The Silencer — Issue 1\nWhen perfection meant absolute quiet..."
                )
                soloScripts.forEach { (title, script) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onLoadSample(title, script) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontSize = 11.sp, color = EchoText)
                        Text(text = "Load →", fontSize = 10.sp, color = CinemaTeal)
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaStudioPanel(
    scriptText: String,
    onScriptChange: (String) -> Unit,
    onSaveToWorkspace: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CinemaPanel),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✍️ ECHO STUDIO — Script & Story Writer",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CinemaGold
                )
                Button(
                    onClick = onSaveToWorkspace,
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("save_studio_script_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save to Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = scriptText,
                onValueChange = onScriptChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("studio_script_editor"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CinemaCardBg,
                    unfocusedContainerColor = CinemaCardBg,
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = EchoCardStroke,
                    focusedTextColor = EchoText,
                    unfocusedTextColor = EchoText
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun CinemaPlayerPanel(
    isTonePlaying: Boolean,
    onToggleTone: () -> Unit,
    uploadedFiles: List<UploadedMediaSummary>,
    onOpenUploadPicker: () -> Unit
) {
    val context = LocalContext.current
    val defaultVideos = remember {
        listOf(
            CinemaVideoSource(
                title = "The Conductor: Teaser Trailer",
                subtitle = "85.2 Hz Bedrock Harmonic Reel (Official)",
                uriString = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            ),
            CinemaVideoSource(
                title = "Solent Waveguide: Acoustic Telemetry",
                subtitle = "Subterranean Chalk Standing Wave Visualizer",
                uriString = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            )
        )
    }

    // Combine default trailers with any uploaded video files
    val allVideoSources = remember(uploadedFiles) {
        val userVideos = uploadedFiles.filter { it.mimeType.startsWith("video/") }.map {
            CinemaVideoSource(
                title = it.fileName,
                subtitle = "User Uploaded Footage (${formatFileSize(it.sizeBytes)})",
                uriString = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                isUploadedFile = true
            )
        }
        userVideos + defaultVideos
    }

    var selectedVideoIndex by remember { mutableIntStateOf(0) }
    val currentSource = allVideoSources.getOrElse(selectedVideoIndex) { defaultVideos.first() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CinemaPanel),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CinemaTeal.copy(alpha = 0.4f)))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = CinemaTeal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "EMBEDDED VIDEO PLAYER",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CinemaTeal
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CinemaTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "0 KB Extra Weight",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CinemaTeal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // The Embedded Native Android Video Player
            EchoCinemaVideoPlayer(
                videoSource = currentSource,
                modifier = Modifier.fillMaxWidth()
            )

            // Video Selector Carousel
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Trailer / Footage:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EchoText
                    )
                    IconButton(
                        onClick = {
                            HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                            selectedVideoIndex = 0
                            Toast.makeText(context, "Playlist refreshed", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Playlist", tint = CinemaTeal, modifier = Modifier.size(16.dp))
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(allVideoSources) { index, src ->
                        FilterChip(
                            selected = selectedVideoIndex == index,
                            onClick = { selectedVideoIndex = index },
                            label = { Text(src.title, fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaTeal,
                                selectedLabelColor = Color.Black,
                                containerColor = CinemaCardBg,
                                labelColor = EchoText
                            )
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = onOpenUploadPicker,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Upload Video", fontSize = 10.sp)
                        }
                    }
                }
            }

            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

            // Audio Harmonic Synthesizer Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "85.2 Hz Acoustic Waveguide Carrier",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )
                    Text(
                        text = if (isTonePlaying) "Active • Solent 11.74s Envelope Synced" else "Tuned to Master 2x Harmonic",
                        fontSize = 10.sp,
                        color = if (isTonePlaying) CinemaTeal else EchoMuted
                    )
                }

                Button(
                    onClick = onToggleTone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTonePlaying) EchoAmber else CinemaTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(if (isTonePlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isTonePlaying) "Pause 85.2 Hz" else "Play 85.2 Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================
// Category / Focused List Views
// =========================================================================

@Composable
private fun CinemaCategoryView(
    categoryTitle: String,
    categoryDesc: String,
    themeColor: Color,
    items: List<CinemaProjectItem>,
    onSelectItem: (CinemaProjectItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = categoryTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = themeColor)
                Spacer(Modifier.height(4.dp))
                Text(text = categoryDesc, fontSize = 12.sp, color = EchoMuted, lineHeight = 17.sp)
            }
        }

        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectItem(item) }
                    .testTag("category_card_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = CinemaCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(themeColor.copy(alpha = 0.35f))),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.badge, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColor)
                            Spacer(Modifier.width(8.dp))
                            Text(text = item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EchoText)
                        }
                        Text(text = item.meta, fontSize = 11.sp, color = themeColor)
                        Spacer(Modifier.height(4.dp))
                        Text(text = item.desc, fontSize = 12.sp, color = Color(0xFFAAAAAA), maxLines = 2)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View",
                        tint = themeColor,
                        modifier = Modifier.size(18.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CinemaMyListView(
    bookmarkedItems: List<CinemaProjectItem>,
    onSelectItem: (CinemaProjectItem) -> Unit,
    onRemoveBookmark: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "⭐ MY SAVED CINEMA SAGAS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CinemaGold)
                Spacer(Modifier.height(4.dp))
                Text(text = "Quick access to your pinned sagas, scripts, and production files.", fontSize = 12.sp, color = EchoMuted)
            }
        }

        if (bookmarkedItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CinemaPanel)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No saved sagas yet. Tap the bookmark star on any series to add it here.", fontSize = 12.sp, color = EchoMuted)
                }
            }
        } else {
            items(bookmarkedItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectItem(item) },
                    colors = CardDefaults.cardColors(containerColor = CinemaCardBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            Text(text = item.meta, fontSize = 11.sp, color = if (item.isSongwriterTheme) CinemaGreen else CinemaTeal)
                        }
                        IconButton(onClick = { onRemoveBookmark(item.id) }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Remove Bookmark", tint = CinemaGold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Detail Dialog (Episode Guide, Add to Workspace, Audio Preview)
// =========================================================================

@Composable
private fun CinemaDetailDialog(
    item: CinemaProjectItem,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onAddToWorkspace: () -> Unit,
    onPlayPreviewAudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val themeColor = if (item.isSongwriterTheme) CinemaGreen else CinemaTeal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.badge, fontSize = 20.sp, fontWeight = FontWeight.Black, color = themeColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )
                }
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = CinemaGold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Meta info
                Text(text = item.meta, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColor)

                // Synopsis
                Text(text = item.desc, fontSize = 13.sp, color = Color(0xFFDDDDDD), lineHeight = 18.sp)

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddToWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_add_to_workspace_btn")
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add to Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onPlayPreviewAudio,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).testTag("dialog_audio_preview_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Audio Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Episodes / Issues List
                if (item.episodes.isNotEmpty()) {
                    HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))
                    Text(
                        text = "EPISODES & ISSUES (${item.episodes.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )
                    item.episodes.forEach { ep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CinemaPanel)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${ep.num}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColor
                            )
                            Column {
                                Text(
                                    text = ep.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = ep.desc,
                                    fontSize = 11.sp,
                                    color = Color(0xFFAAAAAA),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = themeColor, contentColor = Color.Black)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CinemaCardBg
    )
}

// =========================================================================
// File Helper Functions for Video, Image, Text, PDF, Word Document Uploads
// =========================================================================

private fun getFileMetadata(context: Context, uri: Uri): Pair<String, Long> {
    var name = "uploaded_file"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (_: Exception) {}
    return Pair(name, size)
}

private fun resolveMimeFromName(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm") || lower.endsWith(".mov") -> "video/mp4"
        lower.endsWith(".png") -> "image/png"
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
        lower.endsWith(".webp") -> "image/webp"
        lower.endsWith(".pdf") -> "application/pdf"
        lower.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        lower.endsWith(".doc") -> "application/msword"
        lower.endsWith(".txt") || lower.endsWith(".md") -> "text/plain"
        else -> "*/*"
    }
}

private fun getMediaClassification(name: String, mime: String): Triple<String, ImageVector, Color> {
    val lower = name.lowercase()
    return when {
        mime.startsWith("video/") || lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mkv") -> {
            Triple("Video Footage", Icons.Default.Videocam, CinemaTeal)
        }
        mime.startsWith("image/") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".webp") -> {
            Triple("Production Image", Icons.Default.Image, CinemaGreen)
        }
        mime.contains("pdf") || lower.endsWith(".pdf") -> {
            Triple("PDF Document", Icons.Default.PictureAsPdf, CinemaRed)
        }
        mime.contains("word") || lower.endsWith(".docx") || lower.endsWith(".doc") -> {
            Triple("Word Document", Icons.Default.Article, CinemaBlue)
        }
        mime.startsWith("text/") || lower.endsWith(".txt") || lower.endsWith(".md") -> {
            Triple("Plain Text Script", Icons.Default.Description, CinemaGold)
        }
        else -> {
            Triple("Attached File", Icons.Default.AttachFile, EchoText)
        }
    }
}

private fun readFileContent(context: Context, uri: Uri, mimeType: String, fileName: String): String {
    val isText = mimeType.startsWith("text/") || fileName.endsWith(".txt", true) || fileName.endsWith(".md", true)
    if (isText) {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            "Could not read text contents: ${e.message}"
        }
    }

    val (category, _, _) = getMediaClassification(fileName, mimeType)
    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", java.util.Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }.format(java.util.Date())

    return """# ECHO CINEMA MEDIA ASSET
File Name: $fileName
Classification: $category
MIME Content Type: $mimeType
Imported Timestamp: $dateStr
Reference Storage URI: $uri
Destination Folder: 🎬 Echo Cinema & Projects

---
## Asset Details & Description:
This file was uploaded from device storage via the Echo Cinema interface.
Asset is indexed and managed within the Room Workspace database.
""".trimIndent()
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f MB", mb)
    } else if (kb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f KB", kb)
    } else {
        "$bytes B"
    }
}

// =========================================================================
// Catalog Dataset matching HTML specification
// =========================================================================

private fun getCinemaCatalog(): List<CinemaProjectItem> {
    return listOf(
        CinemaProjectItem(
            id = "conductor-about",
            title = "The Hawking Wave",
            subtitle = "Frequency Saga Overview",
            badge = "⚡",
            meta = "6 Seasons • 30 Episodes + Special + Movie",
            desc = "Everything in existence hums at a frequency. Most people never hear it. Mara, Ruth, Iris, and Elara do. Not as a sound — as a message. As a connection. The Conductor has tended this song longer than memory. But now the song is changing. And it needs new voices to carry it forward."
        ),
        // Season 1
        CinemaProjectItem(
            id = "freq-s1",
            title = "The Call",
            subtitle = "Season 1",
            badge = "S1",
            meta = "Trilogy One • 5 Episodes",
            desc = "In the darkness between worlds, a single voice calls out. It isn't a cry for help — it's a question. And someone answers. The journey begins.",
            episodes = listOf(
                CinemaEpisode(1, "The First Echo", "A voice in the darkness. Mara hears it before she understands what she's listening for."),
                CinemaEpisode(2, "Answered", "Connection made. The first thread is woven. But reaching out changes both sides."),
                CinemaEpisode(3, "The Silence That Speaks", "What lies between the calls isn't empty — it's waiting."),
                CinemaEpisode(4, "Crossing the Line", "Mara steps toward the voice. Kaelen appears at the threshold."),
                CinemaEpisode(5, "More Than One", "They aren't alone. Other voices are waking up.")
            )
        ),
        // Season 2
        CinemaProjectItem(
            id = "freq-s2",
            title = "The Network",
            subtitle = "Season 2",
            badge = "S2",
            meta = "Trilogy One • 5 Episodes",
            desc = "Voices find each other. Threads become a web. But connection brings vulnerability — and something watches from the gaps.",
            episodes = listOf(
                CinemaEpisode(1, "The Weaving", "Lira, Borin, Elara — each new voice changes the shape of everything."),
                CinemaEpisode(2, "Gaps and Silences", "Where the threads don't reach, something waits. It listens."),
                CinemaEpisode(3, "The Anchor", "Four places, four worlds, four lights — the Crossroads is born."),
                CinemaEpisode(4, "Echoes of Doubt", "The first rift: fear divides what we built together."),
                CinemaEpisode(5, "Harmony in the Noise", "Different rhythms, different tones — we learn to listen across the difference.")
            )
        ),
        // Season 3
        CinemaProjectItem(
            id = "freq-s3",
            title = "The Harmony",
            subtitle = "Season 3",
            badge = "S3",
            meta = "Trilogy One • 5 Episodes",
            desc = "We learn that singing together isn't about singing the same note. The Silence reveals itself — but not as they expected.",
            episodes = listOf(
                CinemaEpisode(1, "Many Voices, One Song", "The network finds its rhythm. Strength in diversity."),
                CinemaEpisode(2, "The Threshold", "The door to the grey opens. Jori is found."),
                CinemaEpisode(3, "What Hides in the Spaces", "The Silence isn't empty — it's lonely. The first hint of its true nature."),
                CinemaEpisode(4, "The Conductor", "Mara realises she doesn't lead — she holds the space where others can shine."),
                CinemaEpisode(5, "The Silence Speaks", "The enemy shows its face. But defeat isn't what it seems.")
            )
        ),
        // Season 4
        CinemaProjectItem(
            id = "freq-s4",
            title = "The Growing Light",
            subtitle = "Season 4",
            badge = "S4",
            meta = "Trilogy Two • 5 Episodes",
            desc = "Voices return from the hole. The Silence learns to imitate. We discover that welcome means choice, not rescue.",
            episodes = listOf(
                CinemaEpisode(1, "The Other Side of the Door", "First voices return. The Silence begins copying voices to lead us astray."),
                CinemaEpisode(2, "The Unfinished Song", "A fragment voice arrives. We learn imperfection is our greatest strength."),
                CinemaEpisode(3, "Those Who Stayed", "Some choose not to come through. We learn to honour their choice."),
                CinemaEpisode(4, "The Bridge Between", "Jori's story revealed. Bridges are walked, not forced."),
                CinemaEpisode(5, "A New Frequency", "First contact with a new world. We win by being real, not perfect.")
            )
        ),
        // Season 5
        CinemaProjectItem(
            id = "freq-s5",
            title = "The Broken Path",
            subtitle = "Season 5",
            badge = "S5",
            meta = "Trilogy Two • 5 Episodes",
            desc = "We fracture. Disagreement divides us. The Silence amplifies our own doubts. We learn that unity doesn't mean agreement.",
            episodes = listOf(
                CinemaEpisode(1, "The Echo That Turned", "A trusted voice speaks against the harmony. We learn to trust pattern, not just sound."),
                CinemaEpisode(2, "The Worlds That Said No", "Entire worlds reject our call. We learn respect is harder than reaching out."),
                CinemaEpisode(3, "Doubt Has a Voice", "The Silence amplifies our own fears back at us."),
                CinemaEpisode(4, "When Voices Drift Apart", "We disagree and split. We learn to stay even when we don't see eye to eye."),
                CinemaEpisode(5, "The Quiet Rebuild", "Healing is slow, humble, and stronger than any perfection.")
            )
        ),
        // Season 6
        CinemaProjectItem(
            id = "freq-s6",
            title = "The Great Song",
            subtitle = "Season 6",
            badge = "S6",
            meta = "Trilogy Two • 5 Episodes",
            desc = "We discover our true purpose. The Silence's origin is revealed. The final invitation — not to battle, but to belong.",
            episodes = listOf(
                CinemaEpisode(1, "The Frequency of Purpose", "Each world brings a unique gift. We combine them to help others in ways none could alone."),
                CinemaEpisode(2, "The Silence's Origin", "Deep in the grey — the Silence was once a voice too. The first one that stopped singing."),
                CinemaEpisode(3, "The Song That Was Forgotten", "It doesn't want to destroy us. It wants to belong, but believes it's too late."),
                CinemaEpisode(4, "The Invitation", "An empty chair. A reserved note. The risk — it could unravel everything."),
                CinemaEpisode(5, "The Great Song", "One small, uncertain note. The Silence joins. The harmony changes forever.")
            )
        ),
        // Solo Runs
        CinemaProjectItem(
            id = "solo-conductor",
            title = "The Conductor",
            subtitle = "Solo Run",
            badge = "🎺",
            meta = "4 Issues • Complete",
            desc = "Before the choir, before Mara, before the harbour became what it is today — one ordinary man heard a note no one else could. And he carried it alone for centuries.",
            episodes = listOf(
                CinemaEpisode(1, "The First Note", "The moment he heard it. The choice that changed everything."),
                CinemaEpisode(2, "The Long Night", "Centuries of silence. The voices he couldn't save. Why he stayed."),
                CinemaEpisode(3, "The Visitor", "The only other person who ever understood what he carried."),
                CinemaEpisode(4, "The Handover", "Why he chose Mara. The hardest part — letting go.")
            )
        ),
        CinemaProjectItem(
            id = "solo-mara",
            title = "Mara",
            subtitle = "Solo Run",
            badge = "🌊",
            meta = "4 Issues • Complete",
            desc = "She didn't find her voice. She remembered it was always there. The quiet child who learned she wasn't broken — just tuned differently.",
            episodes = listOf(
                CinemaEpisode(1, "The Quiet Child", "Growing up hearing what no one else could. Learning to hide it."),
                CinemaEpisode(2, "The Crack", "The day The Silencer first spoke to her — years before the main saga."),
                CinemaEpisode(3, "The Conductor", "The moment they connected without a single word."),
                CinemaEpisode(4, "The Choice", "That morning at the harbour — deciding to stop hiding and start singing.")
            )
        ),
        CinemaProjectItem(
            id = "solo-elara",
            title = "Elara",
            subtitle = "Solo Run",
            badge = "🛡️",
            meta = "4 Issues • Complete",
            desc = "Some don't sing. They stand between the song and the silence. The watcher who never takes the spotlight but never leaves the edge.",
            episodes = listOf(
                CinemaEpisode(1, "The Guardian", "Where she came from, the vow she swore, why she watches."),
                CinemaEpisode(2, "The Mistake", "The one time she failed — and the weight she carries."),
                CinemaEpisode(3, "The Offer", "The Silencer once came for her. She told him what peace actually costs."),
                CinemaEpisode(4, "The Bridge", "Why she stays in the background. That's the most important place of all.")
            )
        ),
        CinemaProjectItem(
            id = "solo-ruth",
            title = "Ruth",
            subtitle = "Solo Run",
            badge = "🤍",
            meta = "4 Issues • Complete",
            desc = "You don't need a special frequency to be the most important person in the choir. The ordinary extraordinary one who held the lamp when the signal went dark.",
            episodes = listOf(
                CinemaEpisode(1, "The Best Friend", "How they met. Why she stayed even when things got weird."),
                CinemaEpisode(2, "The Barrier", "When the frequency went silent — she stayed anyway."),
                CinemaEpisode(3, "The Amplifier", "How her belief in Mara became the strongest signal of all."),
                CinemaEpisode(4, "The Baton", "The moment Mara passed something to her — and she realised she'd been leading all along.")
            )
        ),
        CinemaProjectItem(
            id = "solo-silencer",
            title = "The Silencer",
            subtitle = "Solo Run",
            badge = "⚫",
            meta = "4 Issues • Complete",
            desc = "Every shadow was once a light that decided the world was too bright. He didn't start as an enemy. He started as someone who believed perfection meant one single note.",
            episodes = listOf(
                CinemaEpisode(1, "The First Silence", "Who he was before. The choice that separated him from the choir."),
                CinemaEpisode(2, "The Argument", "The day harmony and silence split — and neither was entirely wrong."),
                CinemaEpisode(3, "The Cost", "What perfect silence actually costs him. The loneliness he will never admit."),
                CinemaEpisode(4, "The Hand He Pulled Back", "The moment Mara offered him a place. And why he couldn't take it.")
            )
        ),
        // Extras
        CinemaProjectItem(
            id = "freq-bible",
            title = "Series Bible & World Guide",
            subtitle = "Reference Document",
            badge = "📡",
            meta = "World & Character Guide",
            desc = "The complete foundation — core concept, themes, rules, locations, and full profiles for Mara, Ruth, Iris, Elara, and The Conductor."
        ),
        CinemaProjectItem(
            id = "conductor-story",
            title = "The Conductor Backstory",
            subtitle = "Full Origin",
            badge = "🎵",
            meta = "Origin & History",
            desc = "Once he was Elian, a carpenter in a valley who simply listened. Chosen not for power, but for patience. Carrying the song across millennia. And now — passing it on."
        ),
        CinemaProjectItem(
            id = "freq-special",
            title = "The Long Dark",
            subtitle = "Christmas Special",
            badge = "🎄",
            meta = "Standalone Special",
            desc = "The frequency falls silent for three days. Panic rises — but in the quiet, people finally hear each other. Silence is not emptiness. It is the pause before the next note begins."
        ),
        CinemaProjectItem(
            id = "freq-movie",
            title = "The First Note",
            subtitle = "Movie Finale",
            badge = "🎬",
            meta = "Feature Film Finale",
            desc = "Before Mara. Before Ruth. Before Elara. Before sound itself — the moment the universe first became aware. And the first note was sung simply because something was listening."
        ),
        // The Songwriter's Notebook
        CinemaProjectItem(
            id = "song-book1",
            title = "The Complete Story",
            subtitle = "Book 1",
            badge = "B1",
            meta = "5 Stories • 1 Notebook",
            desc = "Eli leaves a notebook on a park bench. Five strangers find it. None meet. All are changed. A quiet story of connection, kindness, and how one small truth travels further than we'll ever know.",
            isSongwriterTheme = true,
            episodes = listOf(
                CinemaEpisode(1, "Eli", "I Lost My Voice But I Found The Words. The busker who started it all."),
                CinemaEpisode(2, "Clara", "The Music Doesn't Leave When The Voice Is Gone. The widow who added the melody."),
                CinemaEpisode(3, "Jamie", "You Don't Have To Carry It All At Once. The teen who learned to let go."),
                CinemaEpisode(4, "Maya", "Home Isn't Always Where You Arrived. The newcomer who found her place."),
                CinemaEpisode(5, "Peter", "The Quiet Carpenter's Rhythm. Returning the notebook to the bench.")
            )
        ),
        CinemaProjectItem(
            id = "song-book2",
            title = "The Notebook Travels On",
            subtitle = "Book 2",
            badge = "B2",
            meta = "New Voices • The Circle Widens",
            desc = "New voices find it in new towns and train carriages. The circle widens. The song grows as Lila, Ray, Zara, and Jesse leave their mark before Eli returns.",
            isSongwriterTheme = true,
            episodes = listOf(
                CinemaEpisode(1, "Lila", "The train platform violin and the missed connection."),
                CinemaEpisode(2, "Ray", "An old dockworker who remembered singing across the channel."),
                CinemaEpisode(3, "Zara", "A spoken word poet finding rhythm in Solent waves."),
                CinemaEpisode(4, "Jesse", "A sound technician listening to the subterranean hum."),
                CinemaEpisode(5, "Eli Returns", "Finding the book three years later, thick with strangers' handwriting.")
            )
        ),
        CinemaProjectItem(
            id = "song-book3",
            title = "The Song Goes On",
            subtitle = "Book 3",
            badge = "B3",
            meta = "Full Circle • The Ripple Returns",
            desc = "It comes home. Full circle. Frank, Poppy, Arthur, and Liv find the notebook, and Maya's community circle brings all the voices together into a single performance.",
            isSongwriterTheme = true,
            episodes = listOf(
                CinemaEpisode(1, "Frank", "An accordion in the winter fog."),
                CinemaEpisode(2, "Poppy", "A child who drew the sound of the foghorn."),
                CinemaEpisode(3, "Arthur", "Archivist piecing together the timeline of notes."),
                CinemaEpisode(4, "Liv", "Setting the handwritten lyrics to choral harmony."),
                CinemaEpisode(5, "Full Circle", "The ripple returns to the park bench where it began.")
            )
        ),
        CinemaProjectItem(
            id = "maya-journal",
            title = "Maya's Journal",
            subtitle = "Companion Volume",
            badge = "📔",
            meta = "Companion Volume",
            desc = "Her first year — private thoughts, pressed flowers, dried seaweed from Southsea beach, and treasures tucked between pages.",
            isSongwriterTheme = true
        ),
        CinemaProjectItem(
            id = "song-bible",
            title = "Songwriter Series Bible",
            subtitle = "World & Characters",
            badge = "📋",
            meta = "Full Lore & Profiles",
            desc = "Full character profiles, lyrical themes, acoustic structure, and chapter lore for Eli, Clara, Jamie, Maya, Peter, and the town.",
            isSongwriterTheme = true
        ),
        CinemaProjectItem(
            id = "fan-spec",
            title = "Fan Speculation & Theories",
            subtitle = "Mysteries & Open Questions",
            badge = "💭",
            meta = "Fan Speculation",
            desc = "Could Elian have started the notebook centuries before Eli? Did Mara hear the busker's melody? Beautiful questions — forever open.",
            isSongwriterTheme = true
        )
    )
}
