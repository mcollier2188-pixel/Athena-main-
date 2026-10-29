package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraRoll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.GeminiMediaGenerationService
import com.example.ai.GeneratedImageResult
import com.example.ai.StoryboardShot
import com.example.ai.VeoVideoResult
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.MediaVaultRepository
import com.example.data.VaultMediaItem
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.launch

enum class MediaVaultSubTab {
    LIBRARY,
    HARMONIC_MUSIC_PLAYER,
    AI_IMAGE_STUDIO,
    AI_VIDEO_STUDIO
}

/**
 * Dedicated Media Vault & AI Media Generation Studio.
 * Integrates Google's gemini-2.5-flash-image and Veo video generation alongside
 * zero-permission Android Photo/Video picker, Harmonic Frequency Music Player, and in-app player.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaVaultTab(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onOpenMedia: (Uri, MediaType, String) -> Unit,
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize default media in repository
    LaunchedEffect(Unit) {
        MediaVaultRepository.initializeWithDefaults(context)
    }

    val vaultItems by MediaVaultRepository.items.collectAsState()
    var selectedSubTab by remember { mutableStateOf(MediaVaultSubTab.LIBRARY) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Sub-Tab Navigation Bar
        TabRow(
            selectedTabIndex = selectedSubTab.ordinal,
            containerColor = EchoBgAlt,
            contentColor = EchoCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab.ordinal]),
                    color = EchoCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("media_vault_tabs")
        ) {
            Tab(
                selected = selectedSubTab == MediaVaultSubTab.LIBRARY,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = MediaVaultSubTab.LIBRARY
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                        Text("📁 Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = EchoCyan,
                unselectedContentColor = EchoMuted
            )
            Tab(
                selected = selectedSubTab == MediaVaultSubTab.HARMONIC_MUSIC_PLAYER,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = MediaVaultSubTab.HARMONIC_MUSIC_PLAYER
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(15.dp))
                        Text("🎵 Music Player", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = EchoCyan,
                unselectedContentColor = EchoMuted
            )
            Tab(
                selected = selectedSubTab == MediaVaultSubTab.AI_IMAGE_STUDIO,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = MediaVaultSubTab.AI_IMAGE_STUDIO
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(15.dp))
                        Text("✨ Images", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = EchoCyan,
                unselectedContentColor = EchoMuted
            )
            Tab(
                selected = selectedSubTab == MediaVaultSubTab.AI_VIDEO_STUDIO,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = MediaVaultSubTab.AI_VIDEO_STUDIO
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(15.dp))
                        Text("🎬 Videos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = EchoCyan,
                unselectedContentColor = EchoMuted
            )
        }

        when (selectedSubTab) {
            MediaVaultSubTab.LIBRARY -> {
                MediaLibraryContent(
                    vaultItems = vaultItems,
                    toneGenerator = toneGenerator,
                    onOpenMedia = onOpenMedia,
                    onAttachToWorkspaceNote = onAttachToWorkspaceNote
                )
            }
            MediaVaultSubTab.HARMONIC_MUSIC_PLAYER -> {
                MediaVaultMusicPlayer(
                    toneGenerator = toneGenerator
                )
            }
            MediaVaultSubTab.AI_IMAGE_STUDIO -> {
                AiImageStudioContent(
                    toneGenerator = toneGenerator,
                    onOpenMedia = onOpenMedia,
                    onAttachToWorkspaceNote = onAttachToWorkspaceNote
                )
            }
            MediaVaultSubTab.AI_VIDEO_STUDIO -> {
                AiVideoStudioContent(
                    toneGenerator = toneGenerator,
                    onOpenMedia = onOpenMedia,
                    onAttachToWorkspaceNote = onAttachToWorkspaceNote
                )
            }
        }
    }
}

/**
 * Sub-Screen 1: Photo & Video Library with zero-permission picker and saved items.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MediaLibraryContent(
    vaultItems: List<VaultMediaItem>,
    toneGenerator: HarmonicToneGenerator,
    onOpenMedia: (Uri, MediaType, String) -> Unit,
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)?
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    var activeLongPressedMedia by remember { mutableStateOf<VaultMediaItem?>(null) }

    // Zero-permission modern Android Photo & Video Picker
    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            toneGenerator.playChime(SoundEffectType.SUCCESS)
            val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true ||
                    uri.toString().contains("video", ignoreCase = true)
            val type = if (isVideo) MediaType.VIDEO else MediaType.PHOTO
            val itemTitle = "Device Import ${System.currentTimeMillis() % 10000}"

            MediaVaultRepository.addItem(
                VaultMediaItem(
                    title = itemTitle,
                    subtitle = if (isVideo) "Imported Video" else "Imported Photo",
                    uri = uri,
                    type = type,
                    isAiGenerated = false
                )
            )

            onOpenMedia(uri, type, itemTitle)
        }
    }

    val filteredItems = remember(vaultItems, selectedFilter) {
        when (selectedFilter) {
            "Images" -> vaultItems.filter { it.type == MediaType.PHOTO }
            "Videos" -> vaultItems.filter { it.type == MediaType.VIDEO }
            "AI Generated" -> vaultItems.filter { it.isAiGenerated }
            else -> vaultItems
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Photos & Videos",
                            tint = EchoCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Dedicated In-App Media Player",
                                color = EchoText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Google Photos • 90° Video Rotation • Fullscreen Toggle • AI Vault",
                                color = EchoMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Open photos and videos from Google Photos, device storage, or created via ATHENA AI Studios. Features 90° video rotation (0°, 90°, 180°, 270°) and full-screen immersion without tilting your phone.",
                        color = EchoText.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            singlePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoCyan,
                            contentColor = EchoOnCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_pick_photo_video")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Pick Media")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Device / Google Photos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CropRotate, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                            Text("90° Rotation Toggle", color = EchoGreen, fontSize = 10.sp)
                        }
                        Text("•", color = EchoMuted, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                            Text("Immersive Fullscreen", color = EchoCyan, fontSize = 10.sp)
                        }
                        Text("•", color = EchoMuted, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                            Text("Workspace Sync", color = EchoCyan, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(listOf("All", "Images", "Videos", "AI Generated")) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedFilter = filter
                        },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoCyan,
                            selectedLabelColor = EchoOnCyan,
                            containerColor = EchoBgAlt,
                            labelColor = EchoText
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "VAULT ITEMS (${filteredItems.size})",
                color = EchoMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(filteredItems, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onOpenMedia(item.uri, item.type, item.title)
                        },
                        onLongClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            activeLongPressedMedia = item
                        }
                    )
                    .testTag("media_card_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (item.isAiGenerated) EchoCyan.copy(alpha = 0.6f) else EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.type == MediaType.PHOTO) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Movie,
                                    contentDescription = item.title,
                                    tint = if (item.isVeoVideo) EchoAmber else EchoCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = item.title,
                                    color = EchoText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (item.isAiGenerated) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.isVeoVideo) EchoAmber.copy(alpha = 0.2f) else EchoCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (item.isVeoVideo) "VEO" else "GEMINI",
                                            color = if (item.isVeoVideo) EchoAmber else EchoCyan,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(text = item.subtitle, color = EchoMuted, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onAttachToWorkspaceNote != null) {
                            OutlinedButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                    onAttachToWorkspaceNote(item.title, item.uri)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = "Attach", tint = EchoCyan, modifier = Modifier.size(14.dp))
                            }
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onOpenMedia(item.uri, item.type, item.title)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan.copy(alpha = 0.2f),
                                contentColor = EchoCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet on Long-Pressed Media Item
    activeLongPressedMedia?.let { item ->
        ItemActionBottomSheet(
            item = LongPressItemPayload(
                id = item.id,
                title = item.title,
                textContent = "Media Title: ${item.title}\nSubtitle: ${item.subtitle}\nURI: ${item.uri}\nAI Created: ${item.isAiGenerated}",
                subtitle = item.subtitle,
                category = if (item.type == MediaType.VIDEO) "Video" else "Photo",
                isMedia = true,
                mediaUriString = item.uri.toString()
            ),
            toneGenerator = toneGenerator,
            onDismiss = { activeLongPressedMedia = null },
            onOpenInViewer = {
                onOpenMedia(item.uri, item.type, item.title)
            },
            onSaveToNotes = { title, _ ->
                onAttachToWorkspaceNote?.invoke(title, item.uri)
            }
        )
    }
}

/**
 * Sub-Screen 2: AI Image Studio powered by gemini-2.5-flash-image
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AiImageStudioContent(
    toneGenerator: HarmonicToneGenerator,
    onOpenMedia: (Uri, MediaType, String) -> Unit,
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf(GeminiMediaGenerationService.IMAGE_STYLES.first()) }
    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancingPrompt by remember { mutableStateOf(false) }
    var latestResult by remember { mutableStateOf<GeneratedImageResult?>(null) }
    var isSavedToVault by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(26.dp))
                        Column {
                            Text("Gemini 2.5 Flash Image Studio", color = EchoText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Multimodal image generation • Customizable aspect ratio & style", color = EchoMuted, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Craft high-definition visual concept art, 35mm cinematic stills, and futuristic character designs using Google's direct Gemini image generation model.",
                        color = EchoText.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Prompt Input Field with Magic Wand
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PROMPT DESCRIPTION", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ai_image_prompt"),
                    placeholder = {
                        Text(
                            "e.g. A neon-lit cybernetic observatory overlooking planetary nebulae...",
                            color = EchoMuted,
                            fontSize = 12.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Enhance prompt with Gemini magic wand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (prompt.isNotBlank() && !isEnhancingPrompt) {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                isEnhancingPrompt = true
                                coroutineScope.launch {
                                    val enhanced = GeminiMediaGenerationService.enhancePrompt(context, prompt, "image")
                                    prompt = enhanced
                                    isEnhancingPrompt = false
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                }
                            }
                        },
                        enabled = prompt.isNotBlank() && !isEnhancingPrompt,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_enhance_prompt")
                    ) {
                        if (isEnhancingPrompt) {
                            CircularProgressIndicator(color = EchoCyan, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Expanding...", fontSize = 11.sp, color = EchoCyan)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✨ Enhance with Gemini", fontSize = 11.sp, color = EchoCyan, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (prompt.isNotBlank()) {
                        TextButton(
                            onClick = { prompt = "" },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Clear", color = EchoRed, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Aspect Ratio Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ASPECT RATIO", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GeminiMediaGenerationService.ASPECT_RATIOS) { (ratioKey, ratioLabel) ->
                        val isSelected = selectedAspectRatio == ratioKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedAspectRatio = ratioKey
                            },
                            label = { Text(ratioLabel, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Style Preset Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AESTHETIC STYLE", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GeminiMediaGenerationService.IMAGE_STYLES) { styleName ->
                        val isSelected = selectedStyle == styleName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedStyle = styleName
                            },
                            label = { Text(styleName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Generate Action Button
        item {
            Button(
                onClick = {
                    val promptToUse = prompt.trim().ifEmpty { "Cosmic stargate over a bioluminescent crystal forest" }
                    if (!isGenerating) {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        isGenerating = true
                        isSavedToVault = false
                        coroutineScope.launch {
                            val result = GeminiMediaGenerationService.generateImage(
                                context = context,
                                prompt = promptToUse,
                                aspectRatio = selectedAspectRatio,
                                style = selectedStyle
                            )
                            isGenerating = false
                            result.onSuccess {
                                latestResult = it
                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                            }.onFailure {
                                Toast.makeText(context, "Generation error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_generate_image"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EchoCyan,
                    contentColor = EchoOnCyan
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = EchoOnCyan, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Synthesizing Art with Gemini 2.5 Flash...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎨 Generate Art with Gemini", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Generated Art Result Card
        latestResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("generated_image_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                Text("Generated Art Preview", color = EchoText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoDarkSurface)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (res.isOnline) "⚡ Gemini 2.5 Flash" else "🎨 Fallback Canvas",
                                    color = EchoCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Coil Image Rendering
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoDarkSurface)
                                .clickable {
                                    onOpenMedia(res.localUri, MediaType.PHOTO, res.prompt)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = res.localUri,
                                contentDescription = res.prompt,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        when (res.aspectRatio) {
                                            "16:9" -> 16f / 9f
                                            "9:16" -> 9f / 16f
                                            "4:3" -> 4f / 3f
                                            "3:4" -> 3f / 4f
                                            else -> 1f
                                        }
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = res.prompt, color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoBgAlt)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Style: ${res.style}", color = EchoCyan, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoBgAlt)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Ratio: ${res.aspectRatio}", color = EchoMuted, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Result Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                    MediaVaultRepository.addItem(
                                        VaultMediaItem(
                                            title = res.prompt.take(30),
                                            subtitle = "Gemini Art • ${res.style} • ${res.aspectRatio}",
                                            uri = res.localUri,
                                            type = MediaType.PHOTO,
                                            prompt = res.prompt,
                                            style = res.style,
                                            isAiGenerated = true
                                        )
                                    )
                                    isSavedToVault = true
                                    Toast.makeText(context, "Saved to Media Vault Library!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isSavedToVault,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSavedToVault) EchoGreen.copy(alpha = 0.3f) else EchoCyan,
                                    contentColor = if (isSavedToVault) EchoGreen else EchoOnCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_save_image_vault")
                            ) {
                                Icon(if (isSavedToVault) Icons.Default.Check else Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSavedToVault) "Saved to Vault" else "Save to Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (onAttachToWorkspaceNote != null) {
                                OutlinedButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                                        val noteContent = """
                                            # Gemini AI Art Creation
                                            Prompt: ${res.prompt}
                                            Style: ${res.style}
                                            Aspect Ratio: ${res.aspectRatio}
                                            Media URI: ${res.localUri}
                                            Timestamp: ${System.currentTimeMillis()}
                                        """.trimIndent()
                                        onAttachToWorkspaceNote(res.prompt.take(28), res.localUri)
                                        Toast.makeText(context, "Attached to Workspace Note!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("btn_attach_image_note")
                                ) {
                                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to Note", fontSize = 11.sp, color = EchoCyan, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(
                                onClick = {
                                    onOpenMedia(res.localUri, MediaType.PHOTO, res.prompt)
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = EchoCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sub-Screen 3: AI Video Studio powered by Google Veo & Cinematic Storyboard Director
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AiVideoStudioContent(
    toneGenerator: HarmonicToneGenerator,
    onOpenMedia: (Uri, MediaType, String) -> Unit,
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(GeminiMediaGenerationService.VIDEO_GENRES.first()) }
    var selectedCameraMove by remember { mutableStateOf(GeminiMediaGenerationService.CAMERA_MOVEMENTS.first()) }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancingPrompt by remember { mutableStateOf(false) }
    var latestVideoResult by remember { mutableStateOf<VeoVideoResult?>(null) }
    var isSavedToVault by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                border = BorderStroke(1.dp, EchoAmber.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(26.dp))
                        Column {
                            Text("Google Veo Video Studio", color = EchoText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Cinematic generation • Shot-by-shot storyboarding & motion cues", color = EchoMuted, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Generate high-fidelity videos and complete multi-shot director packages powered by Google Veo (`veo-3.1-fast-generate-preview`) and Gemini cinematography directing.",
                        color = EchoText.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Prompt Input Field
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("VIDEO CONCEPT & SCENARIO", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_veo_prompt"),
                    placeholder = {
                        Text(
                            "e.g. A solitary starship navigator entering hyperspace past Saturn's icy rings...",
                            color = EchoMuted,
                            fontSize = 12.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (prompt.isNotBlank() && !isEnhancingPrompt) {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                isEnhancingPrompt = true
                                coroutineScope.launch {
                                    val enhanced = GeminiMediaGenerationService.enhancePrompt(context, prompt, "video")
                                    prompt = enhanced
                                    isEnhancingPrompt = false
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                }
                            }
                        },
                        enabled = prompt.isNotBlank() && !isEnhancingPrompt,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_enhance_video_prompt")
                    ) {
                        if (isEnhancingPrompt) {
                            CircularProgressIndicator(color = EchoAmber, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Expanding...", fontSize = 11.sp, color = EchoAmber)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✨ Enhance Scene Prompt", fontSize = 11.sp, color = EchoAmber, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (prompt.isNotBlank()) {
                        TextButton(
                            onClick = { prompt = "" },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Clear", color = EchoRed, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Genre Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CINEMATIC GENRE", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GeminiMediaGenerationService.VIDEO_GENRES) { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedGenre = genre
                            },
                            label = { Text(genre, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Camera Movement Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CAMERA MOVEMENT & LENS DYNAMICS", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GeminiMediaGenerationService.CAMERA_MOVEMENTS) { move ->
                        val isSelected = selectedCameraMove == move
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCameraMove = move
                            },
                            label = { Text(move, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Aspect Ratio Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("VIDEO ASPECT RATIO", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("16:9" to "16:9 Cinema", "9:16" to "9:16 Shorts/Reels", "1:1" to "1:1 Square").forEach { (ratio, label) ->
                        val isSelected = selectedAspectRatio == ratio
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedAspectRatio = ratio
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Generate Video Action Button
        item {
            Button(
                onClick = {
                    val promptToUse = prompt.trim().ifEmpty { "Interstellar gateway opening in deep cosmos" }
                    if (!isGenerating) {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        isGenerating = true
                        isSavedToVault = false
                        coroutineScope.launch {
                            val res = GeminiMediaGenerationService.generateVeoVideo(
                                context = context,
                                prompt = promptToUse,
                                aspectRatio = selectedAspectRatio,
                                resolution = "1080p",
                                genre = selectedGenre,
                                cameraMovement = selectedCameraMove
                            )
                            isGenerating = false
                            res.onSuccess {
                                latestVideoResult = it
                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                            }.onFailure {
                                Toast.makeText(context, "Veo dispatch error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_generate_video"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EchoAmber,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Directing Veo Video & Generating Storyboard...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎬 Generate Video with Veo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Veo Generated Package Card
        latestVideoResult?.let { videoRes ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("veo_result_package_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoAmber.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(18.dp))
                                Text(videoRes.title, color = EchoText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(videoRes.status, color = EchoAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video Player Preview Trigger Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, EchoAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable {
                                    if (videoRes.videoUri != null) {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        onOpenMedia(videoRes.videoUri, MediaType.VIDEO, videoRes.title)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(EchoAmber),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.Black, modifier = Modifier.size(28.dp))
                                }
                                Text("Preview Video in In-App Player", color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("90° Rotation • Fullscreen • Chapter Markers", color = EchoMuted, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                    if (videoRes.videoUri != null) {
                                        MediaVaultRepository.addItem(
                                            VaultMediaItem(
                                                title = videoRes.title,
                                                subtitle = "Veo Video • ${videoRes.genre} • ${videoRes.aspectRatio}",
                                                uri = videoRes.videoUri,
                                                type = MediaType.VIDEO,
                                                prompt = videoRes.prompt,
                                                style = videoRes.genre,
                                                isAiGenerated = true,
                                                isVeoVideo = true
                                            )
                                        )
                                    }
                                    isSavedToVault = true
                                    Toast.makeText(context, "Saved Veo Video to Vault!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isSavedToVault,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSavedToVault) EchoGreen.copy(alpha = 0.3f) else EchoAmber,
                                    contentColor = if (isSavedToVault) EchoGreen else Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_save_video_vault")
                            ) {
                                Icon(if (isSavedToVault) Icons.Default.Check else Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSavedToVault) "Saved to Vault" else "Save to Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (onAttachToWorkspaceNote != null) {
                                OutlinedButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                                        val sbText = StringBuilder()
                                        sbText.append("# Veo Video Director Package: ${videoRes.title}\n")
                                        sbText.append("Genre: ${videoRes.genre} | Aspect Ratio: ${videoRes.aspectRatio}\n\n")
                                        sbText.append("## Production Storyboard Sequence:\n")
                                        videoRes.shots.forEach { s ->
                                            sbText.append("### Shot ${s.shotNumber}: ${s.title} (${s.durationSec}s)\n")
                                            sbText.append("- Camera Move: ${s.cameraMovement}\n")
                                            sbText.append("- Lighting: ${s.lighting}\n")
                                            sbText.append("- Visual: ${s.visualDescription}\n")
                                            sbText.append("- Veo Prompt: ${s.veoPrompt}\n\n")
                                        }
                                        onAttachToWorkspaceNote(videoRes.title, videoRes.videoUri ?: Uri.EMPTY)
                                        Toast.makeText(context, "Storyboard saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("btn_export_storyboard_notes")
                                ) {
                                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export Storyboard", fontSize = 11.sp, color = EchoAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Shot Breakdown Header
                        Text(
                            text = "CINEMATIC DIRECTOR STORYBOARD (${videoRes.shots.size} SHOTS)",
                            color = EchoAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Individual Storyboard Cards
                        videoRes.shots.forEach { shot ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                                border = BorderStroke(0.5.dp, EchoCardStroke),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(EchoAmber),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${shot.shotNumber}", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(shot.title, color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text("${shot.durationSec}s", color = EchoMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(shot.visualDescription, color = EchoText.copy(alpha = 0.9f), fontSize = 11.sp, lineHeight = 15.sp)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EchoDarkSurface)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("📹 ${shot.cameraMovement}", color = EchoCyan, fontSize = 9.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EchoDarkSurface)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("💡 ${shot.lighting}", color = EchoAmber, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
