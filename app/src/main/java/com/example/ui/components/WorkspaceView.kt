package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.MultimediaProjectEntity
import com.example.data.ProjectEntity
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
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WorkspaceView Composable:
 * Queries the offline Room SQLite database via [WorkspaceViewModel] to display:
 * 1. Multimedia Projects (titles, creation timestamps, local asset file paths, file sizes, formats).
 * 2. General Research Workspaces (titles, descriptions, color tags, timestamps).
 */
@Composable
fun WorkspaceView(
    viewModel: WorkspaceViewModel,
    onSelectProject: ((ProjectEntity) -> Unit)? = null,
    toneGenerator: HarmonicToneGenerator? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    val multimediaProjects by viewModel.multimediaProjects.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Multimedia Projects, 1 = Research Workspaces
    var selectedMediaFilter by remember { mutableStateOf("ALL") } // ALL, VIDEO, AUDIO, IMAGE, FAVORITES

    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }
    var showCreateMediaDialog by remember { mutableStateOf(false) }

    var projectToEdit by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    var mediaToDelete by remember { mutableStateOf<MultimediaProjectEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("workspace_view_container")
    ) {
        // Section Header with Tab Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 0) EchoAmber.copy(alpha = 0.15f) else EchoCyan.copy(alpha = 0.15f))
                        .border(1.dp, if (activeTab == 0) EchoAmber.copy(alpha = 0.4f) else EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activeTab == 0) Icons.Default.Movie else Icons.Default.Workspaces,
                        contentDescription = null,
                        tint = if (activeTab == 0) EchoAmber else EchoCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ROOM SQLITE DATA STORE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${multimediaProjects.size} media assets • ${projects.size} workspaces saved locally",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )
                }
            }

            Button(
                onClick = {
                    toneGenerator?.playChime(SoundEffectType.TAP)
                    if (activeTab == 0) {
                        showCreateMediaDialog = true
                    } else {
                        showCreateWorkspaceDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) EchoAmber else EchoCyan,
                    contentColor = if (activeTab == 0) Color(0xFF0A0B10) else EchoOnCyan
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("create_new_item_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (activeTab == 0) "+ Media Asset" else "+ Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    activeTab = 0
                    toneGenerator?.playChime(SoundEffectType.TAP)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) EchoAmber else EchoCard,
                    contentColor = if (activeTab == 0) Color(0xFF0A0B10) else EchoText
                ),
                border = BorderStroke(1.dp, if (activeTab == 0) EchoAmber else EchoCardStroke),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Multimedia Projects (${multimediaProjects.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    activeTab = 1
                    toneGenerator?.playChime(SoundEffectType.TAP)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) EchoCyan else EchoCard,
                    contentColor = if (activeTab == 1) EchoOnCyan else EchoText
                ),
                border = BorderStroke(1.dp, if (activeTab == 1) EchoCyan else EchoCardStroke),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Workspaces, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Workspaces (${projects.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Content Area based on Active Tab
        if (activeTab == 0) {
            // Multimedia Projects Tab
            MultimediaProjectsSection(
                multimediaProjects = multimediaProjects,
                selectedFilter = selectedMediaFilter,
                onFilterSelected = { selectedMediaFilter = it },
                onToggleFavorite = { viewModel.toggleMultimediaProjectFavorite(it) },
                onDelete = { mediaToDelete = it },
                dateFormat = dateFormat,
                toneGenerator = toneGenerator
            )
        } else {
            // General Research Workspaces Tab
            WorkspacesSection(
                projects = projects,
                onSelectProject = onSelectProject,
                onEditProject = { projectToEdit = it },
                onDeleteProject = { projectToDelete = it },
                onCreateProject = { showCreateWorkspaceDialog = true },
                dateFormat = dateFormat,
                toneGenerator = toneGenerator
            )
        }
    }

    // Dialogs
    if (showCreateMediaDialog) {
        CreateMultimediaProjectDialog(
            onDismiss = { showCreateMediaDialog = false },
            onConfirm = { title, desc, type, path, size, duration, mime, res, aspect, tags ->
                viewModel.createMultimediaProject(
                    title = title,
                    description = desc,
                    mediaType = type,
                    filePath = path,
                    fileSizeBytes = size,
                    durationSeconds = duration,
                    mimeType = mime,
                    resolution = res,
                    aspectRatio = aspect,
                    tags = tags
                )
                showCreateMediaDialog = false
                Toast.makeText(context, "Saved multimedia asset to Room SQLite!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showCreateWorkspaceDialog) {
        ProjectUpsertDialog(
            initialProject = null,
            onDismiss = { showCreateWorkspaceDialog = false },
            onConfirm = { title, desc, colorHex ->
                viewModel.createProject(title = title, description = desc, colorTagHex = colorHex)
                showCreateWorkspaceDialog = false
                Toast.makeText(context, "Created project in Room database!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    projectToEdit?.let { project ->
        ProjectUpsertDialog(
            initialProject = project,
            onDismiss = { projectToEdit = null },
            onConfirm = { title, desc, colorHex ->
                viewModel.updateProject(project.copy(title = title, description = desc, colorTagHex = colorHex, modifiedAt = System.currentTimeMillis()))
                projectToEdit = null
                Toast.makeText(context, "Project updated in Room!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Workspace?", color = EchoText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${project.title}\"? This operation removes it from the local SQLite database.", color = EchoMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(project)
                        projectToDelete = null
                        Toast.makeText(context, "Project deleted.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = EchoText)
                }
            },
            containerColor = EchoCard,
            shape = RoundedCornerShape(14.dp)
        )
    }

    mediaToDelete?.let { media ->
        AlertDialog(
            onDismissRequest = { mediaToDelete = null },
            title = { Text("Delete Media Asset Record?", color = EchoText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${media.title}\" metadata from Room database?", color = EchoMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMultimediaProject(media)
                        mediaToDelete = null
                        Toast.makeText(context, "Media asset record removed.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mediaToDelete = null }) {
                    Text("Cancel", color = EchoText)
                }
            },
            containerColor = EchoCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

/**
 * Displays the list of user-created multimedia projects with local file paths and timestamps.
 */
@Composable
private fun MultimediaProjectsSection(
    multimediaProjects: List<MultimediaProjectEntity>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onToggleFavorite: (MultimediaProjectEntity) -> Unit,
    onDelete: (MultimediaProjectEntity) -> Unit,
    dateFormat: SimpleDateFormat,
    toneGenerator: HarmonicToneGenerator?
) {
    val context = LocalContext.current

    val filteredList = remember(multimediaProjects, selectedFilter) {
        when (selectedFilter) {
            "VIDEO" -> multimediaProjects.filter { it.mediaType == "VIDEO" }
            "AUDIO" -> multimediaProjects.filter { it.mediaType == "AUDIO" }
            "IMAGE" -> multimediaProjects.filter { it.mediaType == "IMAGE" }
            "FAVORITES" -> multimediaProjects.filter { it.isFavorite }
            else -> multimediaProjects
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "VIDEO", "AUDIO", "IMAGE", "FAVORITES").forEach { category ->
                val isSelected = selectedFilter == category
                val count = when (category) {
                    "VIDEO" -> multimediaProjects.count { it.mediaType == "VIDEO" }
                    "AUDIO" -> multimediaProjects.count { it.mediaType == "AUDIO" }
                    "IMAGE" -> multimediaProjects.count { it.mediaType == "IMAGE" }
                    "FAVORITES" -> multimediaProjects.count { it.isFavorite }
                    else -> multimediaProjects.size
                }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onFilterSelected(category)
                        toneGenerator?.playChime(SoundEffectType.TAP)
                    },
                    label = { Text("$category ($count)", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EchoAmber,
                        selectedLabelColor = Color(0xFF0A0B10),
                        containerColor = EchoCard,
                        labelColor = EchoText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = EchoCardStroke,
                        selectedBorderColor = EchoAmber
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        if (filteredList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = EchoCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No multimedia assets match filter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                    Text("Saved videos, audio recordings, and images will appear here.", fontSize = 11.sp, color = EchoMuted)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredList.forEach { item ->
                    MultimediaProjectCard(
                        item = item,
                        dateFormat = dateFormat,
                        onCopyPath = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Local File Path", item.filePath))
                            Toast.makeText(context, "Copied file path to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onToggleFavorite = { onToggleFavorite(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Multimedia Project Metadata Card.
 */
@Composable
private fun MultimediaProjectCard(
    item: MultimediaProjectEntity,
    dateFormat: SimpleDateFormat,
    onCopyPath: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = when (item.mediaType) {
        "VIDEO" -> EchoAmber
        "AUDIO" -> EchoCyan
        "IMAGE" -> EchoPurple
        else -> EchoGreen
    }

    val typeIcon: ImageVector = when (item.mediaType) {
        "VIDEO" -> Icons.Default.Movie
        "AUDIO" -> Icons.Default.Audiotrack
        "IMAGE" -> Icons.Default.Image
        else -> Icons.Default.InsertDriveFile
    }

    val dateFormatted = remember(item.createdAt) { dateFormat.format(Date(item.createdAt)) }
    val sizeMb = remember(item.fileSizeBytes) { String.format(Locale.getDefault(), "%.1f MB", item.fileSizeBytes / (1024.0 * 1024.0)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .testTag("media_project_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = EchoCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Type Badge, Title, Favorite & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.18f))
                            .border(1.dp, typeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(typeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = item.aspectRatio,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) EchoAmber else EchoMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = EchoMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = EchoMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // File Path Block with One-Tap Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(EchoBg)
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(6.dp))
                    .clickable { onCopyPath() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = "Local Path",
                        tint = typeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.filePath,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EchoText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Path",
                    tint = EchoMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Metadata Badges: Timestamp, File Size, Duration, Resolution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = dateFormatted, fontSize = 10.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.durationSeconds > 0) {
                        Text(
                            text = "${item.durationSeconds}s",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (item.fileSizeBytes > 0) {
                        Text(
                            text = sizeMb,
                            fontSize = 10.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (!item.resolution.isNullOrBlank()) {
                        Text(
                            text = item.resolution,
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

/**
 * Renders the General Workspaces list.
 */
@Composable
private fun WorkspacesSection(
    projects: List<ProjectEntity>,
    onSelectProject: ((ProjectEntity) -> Unit)?,
    onEditProject: (ProjectEntity) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onCreateProject: () -> Unit,
    dateFormat: SimpleDateFormat,
    toneGenerator: HarmonicToneGenerator?
) {
    if (projects.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = EchoCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No offline projects found", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = EchoText)
                Text(
                    text = "Create a workspace to group research notes, audio logs, and Chladni experiments.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCreateProject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                    border = BorderStroke(1.dp, EchoCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create First Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            projects.forEach { project ->
                val colorTag = parseColorHex(project.colorTagHex)
                val dateStr = dateFormat.format(Date(project.createdAt))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(14.dp))
                        .clickable {
                            toneGenerator?.playChime(SoundEffectType.TAP)
                            onSelectProject?.invoke(project)
                        }
                        .testTag("project_card_${project.id}"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Color Tag Pill
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colorTag)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Project Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = project.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colorTag.copy(alpha = 0.15f))
                                        .border(1.dp, colorTag.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = project.colorTagHex.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorTag,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (project.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = project.description,
                                    fontSize = 11.sp,
                                    color = EchoMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = "Created", tint = EchoMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = dateStr, fontSize = 10.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Edit & Delete actions
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onEditProject(project) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EchoCyan, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onDeleteProject(project) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EchoRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for creating a new user multimedia project record.
 */
@Composable
private fun CreateMultimediaProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        mediaType: String,
        filePath: String,
        fileSizeBytes: Long,
        durationSeconds: Float,
        mimeType: String,
        resolution: String?,
        aspectRatio: String,
        tags: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("VIDEO") }
    var filePath by remember { mutableStateOf("/storage/emulated/0/Movies/ATHENA/") }
    var durationText by remember { mutableStateOf("12.0") }
    var sizeMbText by remember { mutableStateOf("45.0") }
    var aspectRatio by remember { mutableStateOf("16:9") }
    var tags by remember { mutableStateOf("launch, sovereign, 42hz") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Multimedia Project Asset", color = EchoText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Asset Title") },
                    placeholder = { Text("e.g. ATHENA Loom-Weave 16:9") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Media Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("VIDEO", "AUDIO", "IMAGE").forEach { type ->
                        val isSelected = mediaType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                mediaType = type
                                if (type == "VIDEO") {
                                    filePath = "/storage/emulated/0/Movies/ATHENA/"
                                    aspectRatio = "16:9"
                                } else if (type == "AUDIO") {
                                    filePath = "/storage/emulated/0/Music/ATHENA/"
                                    aspectRatio = "1:1"
                                } else {
                                    filePath = "/storage/emulated/0/Pictures/ATHENA/"
                                    aspectRatio = "1:1"
                                }
                            },
                            label = { Text(type, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoAmber,
                                selectedLabelColor = Color(0xFF0A0B10),
                                containerColor = EchoBg,
                                labelColor = EchoText
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = filePath,
                    onValueChange = { filePath = it },
                    label = { Text("Local Asset File Path") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duration (s)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoAmber,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )

                    OutlinedTextField(
                        value = sizeMbText,
                        onValueChange = { sizeMbText = it },
                        label = { Text("Size (MB)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoAmber,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && filePath.isNotBlank()) {
                        val duration = durationText.toFloatOrNull() ?: 0.0f
                        val sizeMb = sizeMbText.toDoubleOrNull() ?: 10.0
                        val sizeBytes = (sizeMb * 1024 * 1024).toLong()
                        val mime = when (mediaType) {
                            "VIDEO" -> "video/mp4"
                            "AUDIO" -> "audio/wav"
                            "IMAGE" -> "image/png"
                            else -> "application/octet-stream"
                        }
                        val res = if (mediaType == "VIDEO") "1920x1080" else if (mediaType == "IMAGE") "2048x2048" else "44.1kHz"
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            mediaType,
                            filePath.trim(),
                            sizeBytes,
                            duration,
                            mime,
                            res,
                            aspectRatio,
                            tags.trim()
                        )
                    }
                },
                enabled = title.isNotBlank() && filePath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EchoAmber, contentColor = Color(0xFF0A0B10))
            ) {
                Text("Save to SQLite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EchoText)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(14.dp)
    )
}

/**
 * Dialog for creating or editing general project entities.
 */
@Composable
private fun ProjectUpsertDialog(
    initialProject: ProjectEntity?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf(initialProject?.title ?: "") }
    var description by remember { mutableStateOf(initialProject?.description ?: "") }
    var selectedColorHex by remember { mutableStateOf(initialProject?.colorTagHex ?: "#00F0FF") }

    val presetColors = listOf(
        "#00F0FF", // Cyan
        "#00FF66", // Green
        "#FFB347", // Amber
        "#9370DB", // Purple
        "#FF5555"  // Red
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProject == null) "Create Offline Workspace" else "Edit Workspace",
                color = EchoText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workspace Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Tag Picker
                Text(
                    text = "Color Tag",
                    fontSize = 12.sp,
                    color = EchoMuted,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    presetColors.forEach { hex ->
                        val color = parseColorHex(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) EchoText else EchoCardStroke,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EchoBg)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim(), selectedColorHex)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
            ) {
                Text(if (initialProject == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EchoText)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(14.dp)
    )
}

private fun parseColorHex(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorInt or 0x00000000FF000000)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        EchoCyan
    }
}
