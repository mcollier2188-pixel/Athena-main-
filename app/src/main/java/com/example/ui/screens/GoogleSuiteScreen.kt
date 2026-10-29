package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.example.ui.components.ItemActionBottomSheet
import com.example.ui.components.LongPressItemPayload
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Language
import com.example.auth.ConnectedAccountsManager
import com.example.auth.ConnectedUser
import com.example.ui.components.ConnectedAccountSheet
import com.example.ui.components.DolaBrowserDialog
import com.example.ui.components.PromptTemplateDialog
import com.example.ui.components.YouTubePlayerDialog
import com.example.ui.components.MediaVaultTab
import com.example.ui.components.GeminiPlaygroundTab
import com.example.ui.components.PlaylistMediaDetailDialog
import com.example.data.model.CreativePlaylistItem
import com.example.data.model.CreativeVideoItem
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.SettingsDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ai.CreativeHubAiService
import com.example.ai.CreativeMessage
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.gateway.GatewayRequester
import com.example.gateway.GatewayResponse
import com.example.ui.components.MediaType
import com.example.ui.components.DailyAppsHubTab
import com.example.model.DailyAppCategory
import com.example.ui.components.MediaViewerDialog
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoBlue
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.launch
import java.util.Locale

enum class CreativeHubTab(val title: String, val icon: ImageVector) {
    GEMINI_PLAYGROUND("Gemini Playground", Icons.Default.AutoAwesome),
    MEDIA_PLAYER("Photos & Videos", Icons.Default.PhotoLibrary),
    YOUTUBE("YouTube & Video", Icons.Default.PlayCircle),
    CREATIVE_STUDIO("Studio & Planner", Icons.Default.CalendarMonth),
    INSPIRATION_LIBRARY("Inspiration Library", Icons.AutoMirrored.Filled.MenuBook),
    LOCATIONS("World & Locations", Icons.Default.Map),
    APPS("Apps", Icons.Default.Apps)
}



data class CreativeTaskItem(
    val id: String,
    val title: String,
    val category: String,
    val dateTime: String,
    val notes: String,
    var isDone: Boolean = false
)

data class CreativeLocationItem(
    val name: String,
    val region: String,
    val coordinates: String,
    val aestheticStyle: String,
    val description: String,
    val creativeIdea: String
)

data class CreativeBookItem(
    val id: String,
    val title: String,
    val author: String,
    val year: String,
    val genre: String,
    val keyQuote: String,
    val reflection: String
)

/**
 * Creative Hub Screen.
 *
 * Designed as the user's primary creative atelier and universal knowledge hub:
 * 1. Creative AI: Responsive, open-ended Online AI Assistant powered by Gemini 3.5 Flash / Pro.
 *    Users can ask or search about anything—stories, art, music, code, science, recipes, philosophy, life!
 * 2. Photos & Videos: Dedicated media player supporting user photos/videos via zero-permission Photo Picker,
 *    complete with video-only rotation (0°, 90°, 180°, 270°) maintaining portrait UI orientation.
 * 3. YouTube & Video Explorer: Broad creative discovery across filmmaking, music synthesis, digital art, cosmos, and tutorials.
 * 4. Studio & Planner: Content calendars, creative tasks, and project milestones.
 * 5. Inspiration Library: Literature, quotes, poetry, philosophy, and citation generator.
 * 6. World & Locations: Inspiring visual backdrops and coordinates for cinematic & creative projects.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSuiteScreen(
    workspaceViewModel: WorkspaceViewModel? = null,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    modifier: Modifier = Modifier,
    initialRequester: GatewayRequester = GatewayRequester.ATHENA,
    onOpenPreferences: (() -> Unit)? = null,
    onResultReceived: ((GatewayResponse) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showInternalPreferencesDialog by remember { mutableStateOf(false) }

    // Media Viewer Dialog State for Photos and Videos
    var activeViewerUri by remember { mutableStateOf<Uri?>(null) }
    var activeViewerType by remember { mutableStateOf(MediaType.VIDEO) }
    var activeViewerTitle by remember { mutableStateOf("Media Viewer") }

    // API Key Dialog State
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Connected Accounts Manager & Dialog State
    val accountsManager = remember { ConnectedAccountsManager.getInstance(context) }
    val currentUser by accountsManager.currentUser.collectAsState()
    var showAccountSheet by remember { mutableStateOf(false) }

    // Lightweight In-App Dola Browser Modal State
    var activeBrowserUrl by remember { mutableStateOf<String?>(null) }
    var activeBrowserTitle by remember { mutableStateOf("Web Source") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
    ) {
        // 85.2 Hz Master Reference Waveform Status Banner (Informational Status Indicator)
        com.example.ui.components.MasterReferenceWaveformBanner()

        // Creative Hub Header Bar with Connected Accounts Indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (currentUser != null) EchoGreen else EchoCyan)
                    )
                    Column {
                        Text(
                            text = "CREATIVE HUB",
                            color = EchoCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        val user = currentUser
                        Text(
                            text = if (user != null) "Signed in: ${user.displayName}" else "Universal AI • Media Studio • Inspiration",
                            color = if (user != null) EchoGreen else EchoMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Google / Connected Account Button
                    OutlinedButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            showAccountSheet = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (currentUser != null) EchoGreen else EchoCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentUser != null) EchoGreen.copy(alpha = 0.5f) else EchoCyan.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_connected_account_hub")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account Hub",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentUser != null) "Google: Linked" else "Sign In",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // API Key Config Button
                    OutlinedButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            showApiKeyDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_api_key_config")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = "API Key", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Key", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // System Preferences Button
                    OutlinedButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            if (onOpenPreferences != null) {
                                onOpenPreferences()
                            } else {
                                showInternalPreferencesDialog = true
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_creative_preferences")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Preferences", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preferences", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Subtabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = EchoDarkSurface,
            contentColor = EchoCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = EchoCyan,
                    height = 2.5.dp
                )
            },
            divider = { HorizontalDivider(color = EchoCardStroke) },
            modifier = Modifier.fillMaxWidth()
        ) {
            CreativeHubTab.values().forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        selectedTabIndex = index
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) EchoCyan else EchoMuted
                            )
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EchoText else EchoMuted
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_creative_${tab.name.lowercase(Locale.ROOT)}")
                )
            }
        }

        // Subtab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (CreativeHubTab.values()[selectedTabIndex]) {
                CreativeHubTab.GEMINI_PLAYGROUND -> {
                    GeminiPlaygroundTab(
                        toneGenerator = toneGenerator,
                        workspaceViewModel = workspaceViewModel,
                        onOpenApiKeyDialog = { showApiKeyDialog = true },
                        onOpenPreferences = {
                            if (onOpenPreferences != null) onOpenPreferences() else showInternalPreferencesDialog = true
                        },
                        onOpenBrowser = { url, title ->
                            activeBrowserUrl = url
                            activeBrowserTitle = title
                        },
                        onOpenMedia = { uri, type, title ->
                            activeViewerUri = uri
                            activeViewerType = type
                            activeViewerTitle = title
                        }
                    )
                }

                CreativeHubTab.MEDIA_PLAYER -> {
                    CreativeMediaPlayerTab(
                        toneGenerator = toneGenerator,
                        onOpenMedia = { uri, type, title ->
                            activeViewerUri = uri
                            activeViewerType = type
                            activeViewerTitle = title
                        }
                    )
                }

                CreativeHubTab.YOUTUBE -> {
                    CreativeYouTubeTab(
                        toneGenerator = toneGenerator,
                        workspaceViewModel = workspaceViewModel,
                        onOpenMedia = { uri, type, title ->
                            activeViewerUri = uri
                            activeViewerType = type
                            activeViewerTitle = title
                        },
                        onOpenBrowser = { url, title ->
                            activeBrowserUrl = url
                            activeBrowserTitle = title
                        }
                    )
                }

                CreativeHubTab.CREATIVE_STUDIO -> {
                    CreativeStudioTab(
                        toneGenerator = toneGenerator,
                        workspaceViewModel = workspaceViewModel
                    )
                }

                CreativeHubTab.INSPIRATION_LIBRARY -> {
                    CreativeInspirationLibraryTab(
                        toneGenerator = toneGenerator,
                        workspaceViewModel = workspaceViewModel
                    )
                }

                CreativeHubTab.LOCATIONS -> {
                    CreativeLocationsTab(
                        toneGenerator = toneGenerator,
                        workspaceViewModel = workspaceViewModel
                    )
                }

                CreativeHubTab.APPS -> {
                    DailyAppsHubTab(
                        initialCategory = DailyAppCategory.CREATIVE,
                        headerTitle = "Creative & Daily Life Apps Hub",
                        headerSubtitle = "20 daily utilities and on-device tools ready to use"
                    )
                }
            }
        }
    }

    // Dedicated In-App Photo & Video Media Player Dialog (with Video-Only Rotation!)
    if (activeViewerUri != null) {
        MediaViewerDialog(
            mediaUri = activeViewerUri,
            mediaType = activeViewerType,
            mediaTitle = activeViewerTitle,
            toneGenerator = toneGenerator,
            onAttachToWorkspaceNote = { noteTitle, uri ->
                workspaceViewModel?.createNote(
                    name = noteTitle,
                    initialText = "Attached Media Item: $noteTitle\nURI: $uri\nDate: ${System.currentTimeMillis()}"
                )
                Toast.makeText(context, "Attached to Workspace Note!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                activeViewerUri = null
            }
        )
    }

    // API Key Management Dialog
    if (showApiKeyDialog) {
        ApiKeySetupDialog(
            toneGenerator = toneGenerator,
            onDismiss = { showApiKeyDialog = false }
        )
    }

    // Google & Connected Accounts Dialog Sheet
    if (showAccountSheet) {
        ConnectedAccountSheet(
            accountsManager = accountsManager,
            toneGenerator = toneGenerator,
            onDismiss = { showAccountSheet = false }
        )
    }

    // Lightweight In-App Dola Browser Modal with Integrated Gemini Copilot
    if (activeBrowserUrl != null) {
        DolaBrowserDialog(
            initialUrl = activeBrowserUrl!!,
            title = activeBrowserTitle,
            toneGenerator = toneGenerator,
            workspaceViewModel = workspaceViewModel,
            onDismiss = { activeBrowserUrl = null }
        )
    }

    // System Preferences & Diagnostics Dialog
    if (showInternalPreferencesDialog) {
        SettingsDialog(
            isDarkTheme = true,
            onToggleDarkTheme = {},
            onRequestExitApp = {},
            onDismiss = { showInternalPreferencesDialog = false }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: CREATIVE AI ASSISTANT ("Ask or search about anything")
// -----------------------------------------------------------------------------------------
@Composable
fun CreativeAiAssistantTab(
    toneGenerator: HarmonicToneGenerator,
    workspaceViewModel: WorkspaceViewModel?,
    onOpenApiKeyDialog: () -> Unit,
    onOpenBrowser: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatMessages = remember {
        mutableStateListOf(
            CreativeMessage(
                role = "model",
                text = "Welcome to your Creative Hub! 🎨✨\n\nI am your online creative assistant. Ask or search about anything you wish—write stories, compose song lyrics, brainstorm video concepts, solve complex code, explore philosophy, design visual aesthetics, or discover new ideas.\n\nType whatever is on your mind below!",
                modelTag = "Gemini 3.5 Flash",
                isOnline = true
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var enableSearchGrounding by remember { mutableStateOf(true) }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var selectedTone by remember { mutableStateOf("Imaginative & Playful") }
    var showPromptStudioDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val promptSuggestions = listOf(
        "✍️ Sci-Fi Short Story",
        "🎵 Acoustic Song Lyrics",
        "🎨 Cinematic Visual Palette",
        "💡 5 Creative App Concepts",
        "🎬 YouTube Video Hook & Script",
        "🌌 Quantum Wonders Explained",
        "🍝 Gourmet Fusion Recipe",
        "⚡ Philosophical Dialogue"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Status & Settings Header Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (CreativeHubAiService.isOnlineConfigured(context)) EchoGreen else EchoAmber)
                    )
                    Text(
                        text = if (CreativeHubAiService.isOnlineConfigured(context)) "Online: Gemini Active" else "AI Ready",
                        color = if (CreativeHubAiService.isOnlineConfigured(context)) EchoGreen else EchoAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Live Web Search", color = EchoMuted, fontSize = 10.sp)
                    Switch(
                        checked = enableSearchGrounding,
                        onCheckedChange = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            enableSearchGrounding = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EchoCyan,
                            checkedTrackColor = EchoCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = EchoMuted,
                            uncheckedTrackColor = EchoBg
                        ),
                        modifier = Modifier.testTag("switch_search_grounding")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Creative Inspiration Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = true,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        showPromptStudioDialog = true
                    },
                    label = { Text("✨ Master Prompt Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EchoCyan,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("btn_open_prompt_studio")
                )
            }
            items(promptSuggestions) { suggestion ->
                FilterChip(
                    selected = false,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        val prompt = suggestion.substringAfter(" ").trim()
                        inputText = when (suggestion) {
                            "✍️ Sci-Fi Short Story" -> "Write a vivid, cinematic sci-fi scene about a nomad who tunes into ancient signals from deep space."
                            "🎵 Acoustic Song Lyrics" -> "Write inspiring acoustic song lyrics about freedom, memory, and open highways."
                            "🎨 Cinematic Visual Palette" -> "Design a creative visual aesthetic and color palette for a futuristic neo-noir film."
                            "💡 5 Creative App Concepts" -> "Brainstorm 5 innovative, delightfully unexpected mobile app concepts."
                            "🎬 YouTube Video Hook & Script" -> "Create an irresistible opening hook and 3-beat outline for a creative video essay."
                            "🌌 Quantum Wonders Explained" -> "Explain quantum superposition in an intuitive, wonderfully poetic way."
                            "🍝 Gourmet Fusion Recipe" -> "Invent a creative gourmet dinner recipe blending Italian and Japanese flavors."
                            else -> "Tell me something thought-provoking about the relationship between art and mathematics."
                        }
                    },
                    label = { Text(suggestion, fontSize = 11.sp, color = EchoText) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = EchoDarkSurface),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = EchoCardStroke,
                        selectedBorderColor = EchoCyan,
                        enabled = true,
                        selected = false
                    ),
                    modifier = Modifier.testTag("chip_prompt_${suggestion.take(4)}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Conversation Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { message ->
                CreativeChatBubble(
                    message = message,
                    toneGenerator = toneGenerator,
                    onSaveNote = { noteContent ->
                        workspaceViewModel?.createNote(
                            name = "Creative Idea Note",
                            initialText = noteContent
                        )
                        Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                    },
                    onOpenBrowser = onOpenBrowser
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = EchoCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Consulting online Gemini assistant...",
                            color = EchoCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field & Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_creative_prompt"),
                placeholder = {
                    Text(
                        "Ask or search about anything...",
                        color = EchoMuted,
                        fontSize = 13.sp
                    )
                },
                maxLines = 3,
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

            Button(
                onClick = {
                    val promptToSend = inputText.trim()
                    if (promptToSend.isNotBlank() && !isLoading) {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        chatMessages.add(
                            CreativeMessage(
                                role = "user",
                                text = promptToSend,
                                isOnline = true
                            )
                        )
                        inputText = ""
                        isLoading = true

                        coroutineScope.launch {
                            try {
                                val reply = CreativeHubAiService.askAnything(
                                    context = context,
                                    prompt = promptToSend,
                                    history = chatMessages.toList(),
                                    model = selectedModel,
                                    enableGoogleSearch = enableSearchGrounding,
                                    creativeTone = selectedTone
                                )
                                chatMessages.add(reply)
                                toneGenerator.playChime(SoundEffectType.TAP)
                            } catch (e: Exception) {
                                chatMessages.add(
                                    CreativeMessage(
                                        role = "model",
                                        text = "Creative response generated: An unexpected error occurred while communicating with the online service (${e.localizedMessage}). Please check your connection or API key.",
                                        isOnline = false
                                    )
                                )
                            } finally {
                                isLoading = false
                                listState.animateScrollToItem(chatMessages.size - 1)
                            }
                        }
                    }
                },
                enabled = inputText.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EchoCyan,
                    contentColor = EchoOnCyan,
                    disabledContainerColor = EchoCardStroke
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("btn_send_creative")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
            }
        }

        // Master Prompt Studio Dialog
        if (showPromptStudioDialog) {
            PromptTemplateDialog(
                toneGenerator = toneGenerator,
                onSelectPrompt = { selectedPrompt ->
                    inputText = selectedPrompt
                },
                onDismiss = { showPromptStudioDialog = false }
            )
        }
    }
}

@Composable
fun CreativeChatBubble(
    message: CreativeMessage,
    toneGenerator: HarmonicToneGenerator,
    onSaveNote: (String) -> Unit,
    onOpenBrowser: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.98f),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) EchoCyan.copy(alpha = 0.15f) else EchoBgAlt
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) EchoCyan.copy(alpha = 0.5f) else EchoCardStroke
            ),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header of message
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else message.modelTag,
                        color = if (isUser) EchoCyan else EchoAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isUser) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy button
                            IconButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Creative Hub", message.text))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = EchoMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Save note button
                            IconButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onSaveNote(message.text)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.NoteAdd,
                                    contentDescription = "Save Note",
                                    tint = EchoCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message Text
                Text(
                    text = message.text,
                    color = EchoText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Search Sources citations if available
                if (message.searchSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Web Grounding",
                            tint = EchoCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Grounded Web Sources & Research:",
                            color = EchoCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    message.searchSources.take(4).forEach { source ->
                        val urlCandidate = if (source.contains("http")) {
                            source.substringAfter("http").substringBefore(")").let { "http$it" }
                        } else null

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    if (urlCandidate != null) {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        if (onOpenBrowser != null) {
                                            onOpenBrowser(urlCandidate, source.substringBefore("(").trim().ifBlank { "Research Source" })
                                        } else {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    Uri.parse(urlCandidate)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                cm.setPrimaryClip(ClipData.newPlainText("Source Link", source))
                                                Toast.makeText(context, "Copied source link!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("Source Link", source))
                                        Toast.makeText(context, "Copied citation!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open Link",
                                tint = EchoMuted,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = source,
                                color = EchoCyan.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: PHOTOS & VIDEOS (DEDICATED MEDIA VAULT WITH ROTATION & FULLSCREEN)
// -----------------------------------------------------------------------------------------
@Composable
fun CreativeMediaPlayerTab(
    toneGenerator: HarmonicToneGenerator,
    onOpenMedia: (Uri, MediaType, String) -> Unit,
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)? = null
) {
    MediaVaultTab(
        toneGenerator = toneGenerator,
        onOpenMedia = onOpenMedia,
        onAttachToWorkspaceNote = onAttachToWorkspaceNote
    )
}

// -----------------------------------------------------------------------------------------
// TAB 3: YOUTUBE & CREATIVE VIDEO EXPLORER
// -----------------------------------------------------------------------------------------
enum class YouTubeSubTab {
    PLAYLISTS,
    TRENDING,
    LIVE_SEARCH
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CreativeYouTubeTab(
    toneGenerator: HarmonicToneGenerator,
    workspaceViewModel: WorkspaceViewModel?,
    onOpenMedia: ((Uri, MediaType, String) -> Unit)? = null,
    onOpenBrowser: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableStateOf(YouTubeSubTab.PLAYLISTS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("🔥 All Trending") }
    var activePlayingVideo by remember { mutableStateOf<CreativeVideoItem?>(null) }
    var activePlayingPlaylist by remember { mutableStateOf<CreativePlaylistItem?>(null) }
    var activeInspectingPlaylist by remember { mutableStateOf<CreativePlaylistItem?>(null) }
    var showAddPlaylistDialog by remember { mutableStateOf(false) }
    var activeLongPressedVideo by remember { mutableStateOf<CreativeVideoItem?>(null) }
    var activeLongPressedPlaylist by remember { mutableStateOf<CreativePlaylistItem?>(null) }

    // Custom and curated playlists with accessible inner media tracks
    val playlists = remember {
        mutableStateListOf(
            CreativePlaylistItem(
                id = "PLzbXoZZQ5k9f-c68mN3mZ-63i6zE9P6pU",
                title = "Cinematic Epic Soundtracks & Film Score Suites",
                author = "Cinema Scores Hub",
                videoCount = "3 Videos",
                description = "Grand orchestral scores, emotive brass swells, and cinematic anthems from landmark world cinema.",
                sampleVideoId = "hFZFjoX2cGg",
                videos = listOf(
                    CreativeVideoItem("af_1", "Oppenheimer - Can You Hear The Music Live", "Ludwig Göransson", "5:20", "24M views", "Soundtrack", "Dynamic tempo acceleration, live string orchestration, and cinematic emotional tension.", "hFZFjoX2cGg"),
                    CreativeVideoItem("af_2", "Dune: Part Two - Worm Ride & Harvester Suite", "Hans Zimmer", "4:15", "16M views", "Soundtrack", "Acoustic percussion, visceral horn calls, and desert sound design masterclass.", "y7e-GC6oGhg"),
                    CreativeVideoItem("af_3", "Blade Runner 2049 - Tears in the Rain Live Suite", "Hans Zimmer & Benjamin Wallfisch", "6:10", "32M views", "Soundtrack", "Synthesizer brass, grand CS-80 motifs, and neo-noir futuristic soundscapes.", "fJ9rUzIMcZQ")
                )
            ),
            CreativePlaylistItem(
                id = "PLw-VjHDlEOgvWPpRBs9FRGgJcKdCguLnv",
                title = "Modular Synthesizer & Sound Design Field Studies",
                author = "Acoustic Archetype",
                videoCount = "3 Videos",
                description = "Analog oscillators, patch cable mechanics, wavetable modulation, and generative soundscapes.",
                sampleVideoId = "hFZFjoX2cGg",
                videos = listOf(
                    CreativeVideoItem("mod_1", "Analog Oscillator Mechanics & Filter Sweeps", "Acoustic Archetype", "14:20", "2.1M views", "Synthesizer", "Hands-on Eurorack patch design and analog subtractive tone shaping.", "hFZFjoX2cGg"),
                    CreativeVideoItem("mod_2", "Generative Ambient Modular Patch Session", "Acoustic Archetype", "28:10", "1.4M views", "Synthesizer", "Self-playing generative acoustic patch utilizing shift registers and quantized CV.", "qJpS3V7q3G0"),
                    CreativeVideoItem("mod_3", "Wavetable Modulation Masterclass", "Sound Lab", "19:45", "980K views", "Synthesizer", "Complex morphing spectral tables and wave-folding techniques.", "y7e-GC6oGhg")
                )
            ),
            CreativePlaylistItem(
                id = "PLOzDu-MXXLliODt5Unh01z43R13456789",
                title = "Cinematic Orchestral Masterworks & Epic Scores",
                author = "Hans Zimmer & Film Scores",
                videoCount = "3 Videos",
                description = "Timeless cinematic symphonies, pipe organs, brass swells, and choral resonance.",
                sampleVideoId = "fJ9rUzIMcZQ",
                videos = listOf(
                    CreativeVideoItem("cin_1", "Interstellar Main Theme (Live in Prague)", "Hans Zimmer", "4:48", "120M views", "Cinema", "Pipe organ and orchestral suite evoking infinite cosmic expanse.", "hFZFjoX2cGg"),
                    CreativeVideoItem("cin_2", "Nuvole Bianche (Live Piano Solo)", "Ludovico Einaudi", "6:01", "95M views", "Cinema", "Neoclassical acoustic piano performance of emotional clarity.", "y7e-GC6oGhg"),
                    CreativeVideoItem("cin_3", "Bohemian Rhapsody Remastered", "Queen", "5:59", "1.7B views", "Cinema", "Operatic progressive rock suite that redefined cinematic music videos.", "fJ9rUzIMcZQ")
                )
            ),
            CreativePlaylistItem(
                id = "PL6NdkxsVQx7TuG3m3_k8xWf2x8g5h8k9l",
                title = "Lofi Beats for Creative Deep Focus & Code",
                author = "Lofi Girl & Chill Beats",
                videoCount = "3 Tracks",
                description = "Nostalgic vinyl warmth, mellow chord progressions, and ambient rhythm for uninterrupted creative flow.",
                sampleVideoId = "1ZYbU82GVz4",
                videos = listOf(
                    CreativeVideoItem("lofi_1", "Lofi Hip Hop Radio - Beats to Relax/Study to", "Lofi Girl", "Live 24/7", "14M subs", "Lofi", "Continuous relaxing beats for sustained intellectual immersion.", "1ZYbU82GVz4"),
                    CreativeVideoItem("lofi_2", "Rainy Night in Tokyo - Instrumental Jazz", "Chillhop", "42:15", "9.2M views", "Lofi", "Mellow Rhodes piano, soft upright bass, and ambient rain field recordings.", "qJpS3V7q3G0"),
                    CreativeVideoItem("lofi_3", "Coffee Shop Ambient Beats", "Lofi Coffee", "35:10", "4.8M views", "Lofi", "Warm acoustic background beats and gentle café ambience.", "hFZFjoX2cGg")
                )
            ),
            CreativePlaylistItem(
                id = "PLrAXtmErZgOdP_8GztsuKi9WAqDbUSn42",
                title = "Art, Architecture & Spatial Imagination",
                author = "Design Archive",
                videoCount = "3 Videos",
                description = "Essays in visual proportion, Bauhaus geometry, algorithmic patterns, and cinema.",
                sampleVideoId = "aircAruvnKk",
                videos = listOf(
                    CreativeVideoItem("art_1", "Apple Vision Pro - Spatial Computing Reveal", "Apple", "9:22", "60M views", "Design", "Spatial interface seamlessly blending digital creativity with physical space.", "aircAruvnKk"),
                    CreativeVideoItem("art_2", "SpaceX Falcon Heavy Synchronized Landing", "SpaceX", "2:29", "42M views", "Design", "Marvelous engineering and booster recovery.", "sBws8MSXN7A"),
                    CreativeVideoItem("art_3", "First Video on YouTube (Me at the zoo)", "jawed", "0:19", "320M views", "History", "The beginning of video sharing on the internet.", "jNQXAC9IVRw")
                )
            )
        )
    }

    val categories = listOf(
        "🔥 All Trending",
        "🎬 Entertainment",
        "🎮 Gaming",
        "📰 News & World",
        "🎵 Music Hits",
        "⚡ Viral Tech"
    )

    val trendingVideos = remember {
        listOf(
            // ENTERTAINMENT & VIRAL
            CreativeVideoItem(
                id = "yt_ent_1",
                title = "$456,000 Squid Game In Real Life!",
                channel = "MrBeast",
                duration = "25:41",
                views = "630M views",
                category = "🎬 Entertainment",
                summary = "Recreating every iconic set and challenge with 456 real participants competing for a grand $456,000 prize.",
                youtubeVideoId = "L_LUpnjgPso"
            ),
            CreativeVideoItem(
                id = "yt_ent_2",
                title = "Never Gonna Give You Up (Official Music Video)",
                channel = "Rick Astley",
                duration = "3:33",
                views = "1.5B views",
                category = "🎬 Entertainment",
                summary = "The timeless 80s British synth-pop anthem, iconic music video, and internet culture phenomenon.",
                youtubeVideoId = "dQw4w9WgXcQ"
            ),
            CreativeVideoItem(
                id = "yt_ent_3",
                title = "Bohemian Rhapsody (Official Video Remastered)",
                channel = "Queen Official",
                duration = "5:59",
                views = "1.7B views",
                category = "🎬 Entertainment",
                summary = "Queen's revolutionary 1975 progressive rock masterpiece and operatic suite that redefined musical cinema.",
                youtubeVideoId = "fJ9rUzIMcZQ"
            ),

            // GAMING
            CreativeVideoItem(
                id = "yt_game_1",
                title = "Grand Theft Auto VI - Trailer 1",
                channel = "Rockstar Games",
                duration = "1:31",
                views = "215M views",
                category = "🎮 Gaming",
                summary = "Official trailer reveal taking players to the neon-soaked streets of Vice City in groundbreaking graphics.",
                youtubeVideoId = "k-XPN5LwYcg"
            ),
            CreativeVideoItem(
                id = "yt_game_2",
                title = "Minecraft Official Trailer & Community Showcase",
                channel = "Minecraft",
                duration = "1:22",
                views = "168M views",
                category = "🎮 Gaming",
                summary = "Discover boundless voxel creativity, deep underground mining expeditions, and marvelous collaborative architecture.",
                youtubeVideoId = "0E44Dd688WU"
            ),
            CreativeVideoItem(
                id = "yt_game_3",
                title = "Elden Ring - Official Gameplay Reveal",
                channel = "BANDAI NAMCO Europe",
                duration = "3:00",
                views = "25M views",
                category = "🎮 Gaming",
                summary = "Trek through the mythical Lands Between, mount spectral steeds, battle towering lords, and mend the Elden Ring.",
                youtubeVideoId = "r7bYw6O6JzM"
            ),

            // NEWS & WORLD
            CreativeVideoItem(
                id = "yt_news_1",
                title = "NASA Artemis I Launch to the Moon",
                channel = "NASA",
                duration = "3:45",
                views = "16M views",
                category = "📰 News & World",
                summary = "Historic deep-space broadcast of the powerful Space Launch System rocket beginning humanity's return to the Moon.",
                youtubeVideoId = "21X5lGlDOfg"
            ),
            CreativeVideoItem(
                id = "yt_news_2",
                title = "James Webb Space Telescope - First Deep Field Reveal",
                channel = "NASA",
                duration = "4:12",
                views = "8.5M views",
                category = "📰 News & World",
                summary = "The sharpest infrared glimpse of the deep cosmos, revealing galaxies shining over 13 billion light years away.",
                youtubeVideoId = "ScMzIvxBSi4"
            ),
            CreativeVideoItem(
                id = "yt_news_3",
                title = "Apple Vision Pro - Official Reveal & Spatial Computing",
                channel = "Apple",
                duration = "9:22",
                views = "60M views",
                category = "📰 News & World",
                summary = "A revolutionary spatial computer seamlessly blending digital entertainment, apps, and photos with physical space.",
                youtubeVideoId = "aircAruvnKk"
            ),

            // MUSIC HITS
            CreativeVideoItem(
                id = "yt_music_1",
                title = "Interstellar Main Theme (Live in Prague)",
                channel = "Hans Zimmer",
                duration = "4:48",
                views = "120M views",
                category = "🎵 Music Hits",
                summary = "Magnificent live symphonic performance featuring pipe organ, choir, and cinematic acoustic resonance.",
                youtubeVideoId = "hFZFjoX2cGg"
            ),
            CreativeVideoItem(
                id = "yt_music_2",
                title = "Lofi Hip Hop Radio - Beats to Relax/Study to",
                channel = "Lofi Girl",
                duration = "Live 24/7",
                views = "14M subscribers",
                category = "🎵 Music Hits",
                summary = "Continuous streaming chill instrumental beats, nostalgic vinyl warmth, and atmospheric creative focus.",
                youtubeVideoId = "1ZYbU82GVz4"
            ),
            CreativeVideoItem(
                id = "yt_music_3",
                title = "Nuvole Bianche (Live Piano Solo)",
                channel = "Ludovico Einaudi",
                duration = "6:01",
                views = "95M views",
                category = "🎵 Music Hits",
                summary = "Deeply moving neoclassical piano performance resonant with emotional clarity and lyrical grace.",
                youtubeVideoId = "y7e-GC6oGhg"
            ),

            // VIRAL TECH
            CreativeVideoItem(
                id = "yt_tech_1",
                title = "Me at the zoo - The Very First Video on YouTube",
                channel = "jawed",
                duration = "0:19",
                views = "320M views",
                category = "⚡ Viral Tech",
                summary = "Recorded at the San Diego Zoo on April 23, 2005. The 19-second video that ignited the global video-sharing revolution.",
                youtubeVideoId = "jNQXAC9IVRw"
            ),
            CreativeVideoItem(
                id = "yt_tech_2",
                title = "SpaceX Falcon Heavy Animation & Dual Booster Recovery",
                channel = "SpaceX",
                duration = "2:29",
                views = "42M views",
                category = "⚡ Viral Tech",
                summary = "Engineering animation and real-world synchronized booster touchdowns that brought science fiction into reality.",
                youtubeVideoId = "sBws8MSXN7A"
            ),
            CreativeVideoItem(
                id = "yt_tech_3",
                title = "528 Hz Ancient Harmonic Solfeggio Resonance",
                channel = "Meditative Resonance",
                duration = "1:00:00",
                views = "18M views",
                category = "⚡ Viral Tech",
                summary = "Subtle acoustic wave tuned to 528 Hz for mind relaxation, cellular rejuvenation, and deep focus.",
                youtubeVideoId = "qJpS3V7q3G0"
            )
        )
    }

    fun launchYouTubeExternal(queryOrId: String, isSearch: Boolean = false, isPlaylist: Boolean = false) {
        toneGenerator.playChime(SoundEffectType.TAP)
        val url = if (isSearch) {
            if (queryOrId.isBlank()) "https://www.youtube.com/feed/trending"
            else "https://www.youtube.com/results?search_query=${Uri.encode(queryOrId)}"
        } else if (isPlaylist) {
            "https://www.youtube.com/playlist?list=$queryOrId"
        } else {
            "https://www.youtube.com/watch?v=$queryOrId"
        }

        try {
            val appUri = if (isSearch) {
                Uri.parse(url)
            } else if (isPlaylist) {
                Uri.parse("vnd.youtube:playlist/$queryOrId")
            } else {
                Uri.parse("vnd.youtube:$queryOrId")
            }
            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    fun extractPlaylistId(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.contains("list=")) {
            trimmed.substringAfter("list=").substringBefore("&").substringBefore("#")
        } else {
            trimmed
        }
    }

    val filteredVideos = trendingVideos.filter { video ->
        (selectedCategory == "🔥 All Trending" || video.category == selectedCategory) &&
                (searchQuery.isBlank() ||
                        video.title.contains(searchQuery, ignoreCase = true) ||
                        video.channel.contains(searchQuery, ignoreCase = true) ||
                        video.summary.contains(searchQuery, ignoreCase = true))
    }

    val filteredPlaylists = playlists.filter { pl ->
        searchQuery.isBlank() ||
                pl.title.contains(searchQuery, ignoreCase = true) ||
                pl.author.contains(searchQuery, ignoreCase = true) ||
                pl.description.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Sub-Navigation Tabs: Playlists vs Trending vs Live Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedSubTab == YouTubeSubTab.PLAYLISTS,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = YouTubeSubTab.PLAYLISTS
                },
                leadingIcon = {
                    Icon(Icons.Default.PlaylistPlay, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = { Text("My Playlists", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EchoCyan,
                    selectedLabelColor = EchoOnCyan,
                    selectedLeadingIconColor = EchoOnCyan,
                    containerColor = EchoBgAlt,
                    labelColor = EchoText
                ),
                modifier = Modifier.weight(1f).testTag("tab_yt_playlists")
            )

            FilterChip(
                selected = selectedSubTab == YouTubeSubTab.TRENDING,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = YouTubeSubTab.TRENDING
                },
                leadingIcon = {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = { Text("Trending Feed", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EchoCyan,
                    selectedLabelColor = EchoOnCyan,
                    selectedLeadingIconColor = EchoOnCyan,
                    containerColor = EchoBgAlt,
                    labelColor = EchoText
                ),
                modifier = Modifier.weight(1f).testTag("tab_yt_trending")
            )

            FilterChip(
                selected = selectedSubTab == YouTubeSubTab.LIVE_SEARCH,
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    selectedSubTab = YouTubeSubTab.LIVE_SEARCH
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = { Text("Search Live", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EchoCyan,
                    selectedLabelColor = EchoOnCyan,
                    selectedLeadingIconColor = EchoOnCyan,
                    containerColor = EchoBgAlt,
                    labelColor = EchoText
                ),
                modifier = Modifier.weight(1f).testTag("tab_yt_search")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedSubTab) {
            // =========================================================================
            // SUB-TAB 1: MY PLAYLISTS
            // =========================================================================
            YouTubeSubTab.PLAYLISTS -> {
                // Playlists Banner with Add Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
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
                                text = "YouTube Playlists Hub",
                                color = EchoText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "In-app player with 90° rotation & fullscreen • Direct YouTube app launch",
                                color = EchoMuted,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                showAddPlaylistDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_add_youtube_playlist")
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().weight(1f).testTag("playlist_list")
                ) {
                    items(filteredPlaylists, key = { it.id }) { pl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        activeInspectingPlaylist = pl
                                    },
                                    onLongClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        activeLongPressedPlaylist = pl
                                    }
                                )
                                .testTag("playlist_card_${pl.id}"),
                            colors = CardDefaults.cardColors(containerColor = EchoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Thumbnail with Playlist Overlay
                                 Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EchoDarkSurface)
                                        .clickable {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            activeInspectingPlaylist = pl
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("https://img.youtube.com/vi/${pl.sampleVideoId}/hqdefault.jpg")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = pl.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )

                                    // Play icon overlay
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .border(1.5.dp, EchoCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = "Play Playlist",
                                            tint = EchoCyan,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    // Video Count Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.85f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${pl.videos.size} Media Tracks",
                                            color = EchoCyan,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pl.title,
                                            color = EchoText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "By ${pl.author}",
                                            color = EchoCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                activeInspectingPlaylist = pl
                                            },
                                            modifier = Modifier.size(28.dp).testTag("btn_inspect_pl_${pl.id}")
                                        ) {
                                            Icon(Icons.Default.FeaturedPlayList, contentDescription = "View Tracks", tint = EchoCyan, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                playlists.removeIf { it.id == pl.id }
                                                Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp).testTag("btn_delete_pl_${pl.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = EchoRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pl.description,
                                    color = EchoText.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. View Tracks / Media Inside Playlist
                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            activeInspectingPlaylist = pl
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EchoCyan,
                                            contentColor = EchoOnCyan
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1.3f).height(36.dp).testTag("btn_browse_tracks_${pl.id}")
                                    ) {
                                        Icon(Icons.Default.FeaturedPlayList, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Media Tracks (${pl.videos.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // 2. In-App Player
                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            activePlayingPlaylist = pl
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EchoBgAlt,
                                            contentColor = EchoCyan
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1.1f).height(36.dp).testTag("btn_play_playlist_${pl.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Play In-App", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // 3. Open in YouTube App
                                    Button(
                                        onClick = {
                                            launchYouTubeExternal(pl.id, isPlaylist = true)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EchoRed,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp).testTag("btn_yt_app_${pl.id}")
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("YouTube", fontSize = 11.sp)
                                    }

                                    // 4. Save Note
                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            workspaceViewModel?.createNote(
                                                name = "Playlist: ${pl.title.take(30)}",
                                                initialText = "YouTube Playlist: ${pl.title}\nCurator: ${pl.author}\nLink: https://www.youtube.com/playlist?list=${pl.id}\nTracks Count: ${pl.videos.size}\n\nDescription:\n${pl.description}"
                                            )
                                            Toast.makeText(context, "Saved Playlist to Workspace Notes!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                                        modifier = Modifier.height(36.dp).testTag("btn_save_note_pl_${pl.id}")
                                    ) {
                                        Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", modifier = Modifier.size(14.dp), tint = EchoCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // SUB-TAB 2: TRENDING & CATEGORIES
            // =========================================================================
            YouTubeSubTab.TRENDING -> {
                // Category Filter Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCategory = cat
                            },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = EchoCardStroke,
                                selectedBorderColor = EchoCyan,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize().weight(1f).testTag("youtube_video_list")
                ) {
                    items(filteredVideos, key = { it.id }) { video ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        activePlayingVideo = video
                                    },
                                    onLongClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        activeLongPressedVideo = video
                                    }
                                )
                                .testTag("video_card_${video.youtubeVideoId}"),
                            colors = CardDefaults.cardColors(containerColor = EchoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EchoDarkSurface)
                                        .clickable {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            activePlayingVideo = video
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("https://img.youtube.com/vi/${video.youtubeVideoId}/hqdefault.jpg")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .border(1.5.dp, EchoCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Video",
                                            tint = EchoCyan,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.8f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = video.duration,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = video.title,
                                        color = EchoText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EchoBgAlt)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = video.category,
                                            color = EchoCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = video.channel, color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "•", color = EchoMuted, fontSize = 11.sp)
                                    Text(text = video.views, color = EchoMuted, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = video.summary,
                                    color = EchoText.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            activePlayingVideo = video
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1.2f).height(36.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Watch In-App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { launchYouTubeExternal(video.youtubeVideoId, isSearch = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1.1f).height(36.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("YouTube App", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            workspaceViewModel?.createNote(
                                                name = "Video: ${video.title.take(30)}",
                                                initialText = "YouTube Video: ${video.title}\nChannel: ${video.channel}\nLink: https://www.youtube.com/watch?v=${video.youtubeVideoId}\n\nSummary:\n${video.summary}"
                                            )
                                            Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", modifier = Modifier.size(14.dp), tint = EchoMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // SUB-TAB 3: LIVE SEARCH
            // =========================================================================
            YouTubeSubTab.LIVE_SEARCH -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search videos, channels, playlists...", color = EchoMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = EchoCyan, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = EchoMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("input_youtube_search"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText,
                            focusedContainerColor = EchoBgAlt,
                            unfocusedContainerColor = EchoBgAlt
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = { launchYouTubeExternal(searchQuery.trim(), isSearch = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp).testTag("btn_search_youtube_live")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (searchQuery.isBlank()) "Browse" else "Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredVideos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(48.dp))
                            Text(text = "No in-app results for \"$searchQuery\"", color = EchoText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Button(
                                onClick = { launchYouTubeExternal(searchQuery, isSearch = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Search \"$searchQuery\" on YouTube App", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        items(filteredVideos, key = { it.id }) { video ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EchoCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 96.dp, height = 54.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoDarkSurface)
                                            .clickable {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                activePlayingVideo = video
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data("https://img.youtube.com/vi/${video.youtubeVideoId}/hqdefault.jpg")
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = video.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(22.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = video.title, color = EchoText, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(text = "${video.channel} • ${video.duration}", color = EchoCyan, fontSize = 11.sp)
                                    }

                                    IconButton(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            activePlayingVideo = video
                                        }
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = EchoCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Playlist Dialog
    if (showAddPlaylistDialog) {
        var inputUrlOrId by remember { mutableStateOf("") }
        var inputTitle by remember { mutableStateOf("") }
        var inputDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPlaylistDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = EchoCyan)
                    Text("Add YouTube Playlist", color = EchoCyan, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste your YouTube Playlist URL (e.g., https://www.youtube.com/playlist?list=PL...) or enter the Playlist ID directly.",
                        color = EchoText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = inputUrlOrId,
                        onValueChange = { inputUrlOrId = it },
                        placeholder = { Text("Playlist URL or ID (PL...)", color = EchoMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        placeholder = { Text("Playlist Title (e.g. My Study Mix)", color = EchoMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputDesc,
                        onValueChange = { inputDesc = it },
                        placeholder = { Text("Description / Notes (Optional)", color = EchoMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedId = extractPlaylistId(inputUrlOrId)
                        if (parsedId.isNotBlank()) {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            val fallbackSample = "dQw4w9WgXcQ"
                            val newPlTitle = inputTitle.ifBlank { "Custom Playlist (${parsedId.take(8)})" }
                            playlists.add(
                                0,
                                CreativePlaylistItem(
                                    id = parsedId,
                                    title = newPlTitle,
                                    author = "My Playlists",
                                    videoCount = "1 Track",
                                    description = inputDesc.ifBlank { "Custom user playlist added via YouTube Link/ID: $parsedId" },
                                    sampleVideoId = fallbackSample,
                                    isCustom = true,
                                    videos = listOf(
                                        CreativeVideoItem(
                                            id = "trk_${System.currentTimeMillis()}",
                                            title = "$newPlTitle — Primary Track",
                                            channel = "YouTube Creator",
                                            duration = "Featured Stream",
                                            views = "Playlist Item",
                                            category = "Playlist Track",
                                            summary = "Stream from playlist $parsedId",
                                            youtubeVideoId = fallbackSample
                                        )
                                    )
                                )
                            )
                            Toast.makeText(context, "Playlist added successfully!", Toast.LENGTH_SHORT).show()
                            showAddPlaylistDialog = false
                        } else {
                            Toast.makeText(context, "Please enter a valid playlist ID or link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                ) {
                    Text("Add to My Playlists")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddPlaylistDialog = false }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Active In-App YouTube Player Dialog (Single Video)
    if (activePlayingVideo != null) {
        YouTubePlayerDialog(
            videoId = activePlayingVideo!!.youtubeVideoId,
            videoTitle = activePlayingVideo!!.title,
            channelName = activePlayingVideo!!.channel,
            toneGenerator = toneGenerator,
            onSaveToWorkspaceNote = { title, url ->
                workspaceViewModel?.createNote(
                    name = "Video: ${title.take(30)}",
                    initialText = "Saved Video: $title\nURL: $url\nChannel: ${activePlayingVideo!!.channel}\n\nSummary:\n${activePlayingVideo!!.summary}"
                )
                Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { activePlayingVideo = null }
        )
    }

    // Active In-App YouTube Player Dialog (Playlist)
    if (activePlayingPlaylist != null) {
        YouTubePlayerDialog(
            videoId = activePlayingPlaylist!!.sampleVideoId,
            playlistId = activePlayingPlaylist!!.id,
            videoTitle = activePlayingPlaylist!!.title,
            channelName = activePlayingPlaylist!!.author,
            toneGenerator = toneGenerator,
            onSaveToWorkspaceNote = { title, url ->
                workspaceViewModel?.createNote(
                    name = "Playlist: ${title.take(30)}",
                    initialText = "Saved YouTube Playlist: $title\nURL: $url\nCurator: ${activePlayingPlaylist!!.author}\n\nDescription:\n${activePlayingPlaylist!!.description}"
                )
                Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { activePlayingPlaylist = null }
        )
    }

    // Active Playlist Inspection & Media Items Management Dialog
    activeInspectingPlaylist?.let { pl ->
        PlaylistMediaDetailDialog(
            playlist = pl,
            toneGenerator = toneGenerator,
            onPlayVideo = { video ->
                activePlayingVideo = video
            },
            onPlayPlaylist = { playlistToPlay ->
                activePlayingPlaylist = playlistToPlay
            },
            onOpenExternalYouTube = { id, isPl ->
                launchYouTubeExternal(id, isPlaylist = isPl)
            },
            onUpdatePlaylist = { updated ->
                val index = playlists.indexOfFirst { it.id == updated.id }
                if (index != -1) {
                    playlists[index] = updated
                }
                activeInspectingPlaylist = updated
            },
            onDeletePlaylist = { toDelete ->
                playlists.removeIf { it.id == toDelete.id }
                activeInspectingPlaylist = null
            },
            onSaveToNote = { noteTitle, noteContent ->
                workspaceViewModel?.createNote(name = noteTitle, initialText = noteContent)
            },
            onDismiss = { activeInspectingPlaylist = null }
        )
    }

    // Modal Bottom Sheet on Long-Pressed Video
    activeLongPressedVideo?.let { targetVideo ->
        ItemActionBottomSheet(
            item = LongPressItemPayload(
                id = targetVideo.id,
                title = targetVideo.title,
                textContent = "YouTube Video: ${targetVideo.title}\nChannel: ${targetVideo.channel}\nDuration: ${targetVideo.duration}\nViews: ${targetVideo.views}\nCategory: ${targetVideo.category}\nLink: https://www.youtube.com/watch?v=${targetVideo.youtubeVideoId}\n\nSummary:\n${targetVideo.summary}",
                subtitle = "${targetVideo.channel} • ${targetVideo.duration}",
                category = "YouTube Video",
                isMedia = true,
                mediaUriString = "https://www.youtube.com/watch?v=${targetVideo.youtubeVideoId}"
            ),
            toneGenerator = toneGenerator,
            onDismiss = { activeLongPressedVideo = null },
            onOpenInViewer = {
                activePlayingVideo = targetVideo
            },
            onSaveToNotes = { title, content ->
                workspaceViewModel?.createNote(name = "Video: ${targetVideo.title.take(30)}", initialText = content)
            }
        )
    }

    // Modal Bottom Sheet on Long-Pressed Playlist
    activeLongPressedPlaylist?.let { targetPl ->
        ItemActionBottomSheet(
            item = LongPressItemPayload(
                id = targetPl.id,
                title = targetPl.title,
                textContent = "YouTube Playlist: ${targetPl.title}\nCurator: ${targetPl.author}\nTracks: ${targetPl.videos.size}\nLink: https://www.youtube.com/playlist?list=${targetPl.id}\n\nDescription:\n${targetPl.description}",
                subtitle = "${targetPl.author} • ${targetPl.videos.size} Tracks",
                category = "Playlist",
                isMedia = true,
                mediaUriString = "https://www.youtube.com/playlist?list=${targetPl.id}"
            ),
            toneGenerator = toneGenerator,
            onDismiss = { activeLongPressedPlaylist = null },
            onOpenInViewer = {
                activePlayingPlaylist = targetPl
            },
            onSaveToNotes = { title, content ->
                workspaceViewModel?.createNote(name = "Playlist: ${targetPl.title.take(30)}", initialText = content)
            },
            onDelete = {
                playlists.removeIf { it.id == targetPl.id }
                Toast.makeText(context, "Deleted playlist", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 4: CREATIVE STUDIO & PLANNER (Tasks & Schedules)
// -----------------------------------------------------------------------------------------
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CreativeStudioTab(
    toneGenerator: HarmonicToneGenerator,
    workspaceViewModel: WorkspaceViewModel?
) {
    val context = LocalContext.current
    val tasks = remember {
        mutableStateListOf(
            CreativeTaskItem("1", "Record Sunset B-Roll & Visual Atmosphere", "Video", "Tomorrow, 18:30", "Capture golden hour slow motion at 60fps"),
            CreativeTaskItem("2", "Synthesize Bassline & Harmonic Arpeggio", "Music", "Wednesday, 14:00", "Analog synth patch in D-minor"),
            CreativeTaskItem("3", "Draft Sci-Fi Opening Chapter Scene", "Writing", "Friday, 10:00", "Introduce the acoustic archive setting"),
            CreativeTaskItem("4", "Export Master Project to Google Drive", "Cloud", "Sunday, 20:00", "Full backup and workspace synchronisation")
        )
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var activeLongPressedTask by remember { mutableStateOf<CreativeTaskItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Creative Content Planner", color = EchoText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Production milestones & creative schedules", color = EchoMuted, fontSize = 11.sp)
            }

            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    showAddTaskDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Google Drive & Cloud Workspace Sync Card
        val accountsManager = remember { ConnectedAccountsManager.getInstance(context) }
        val currentUser by accountsManager.currentUser.collectAsState()
        val isSyncing by accountsManager.isSyncing.collectAsState()
        val lastSyncTime by accountsManager.lastSyncTime.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Cloud Storage",
                            tint = EchoGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Google Drive & Cloud Sync",
                                color = EchoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val userEmail = currentUser?.email ?: "mcollier2188@gmail.com"
                            Text(
                                text = "Connected: $userEmail",
                                color = EchoMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            accountsManager.triggerCloudSync { success, msg ->
                                if (success) {
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_sync_google_drive")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = EchoOnCyan, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Syncing...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync to Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                val idx = tasks.indexOf(task)
                                if (idx != -1) {
                                    tasks[idx] = task.copy(isDone = !task.isDone)
                                }
                            },
                            onLongClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                activeLongPressedTask = task
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    val idx = tasks.indexOf(task)
                                    if (idx != -1) {
                                        tasks[idx] = task.copy(isDone = !task.isDone)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.isDone) Icons.Default.Check else Icons.Default.CalendarMonth,
                                    contentDescription = "Toggle Done",
                                    tint = if (task.isDone) EchoGreen else EchoMuted
                                )
                            }

                            Column {
                                Text(
                                    text = task.title,
                                    color = if (task.isDone) EchoMuted else EchoText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${task.category} • ${task.dateTime}",
                                    color = EchoCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (task.notes.isNotBlank()) {
                                    Text(text = task.notes, color = EchoMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Video") }
        var newNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Creative Task", color = EchoCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("Task Title", color = EchoMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EchoCyan, focusedTextColor = EchoText)
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        placeholder = { Text("Category (e.g. Video, Music, Writing)", color = EchoMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EchoCyan, focusedTextColor = EchoText)
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        placeholder = { Text("Notes / Details", color = EchoMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EchoCyan, focusedTextColor = EchoText)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            tasks.add(
                                CreativeTaskItem(
                                    id = System.currentTimeMillis().toString(),
                                    title = newTitle,
                                    category = newCategory,
                                    dateTime = "Scheduled Today",
                                    notes = newNotes
                                )
                            )
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Modal Bottom Sheet on Long-Pressed Task
    activeLongPressedTask?.let { targetTask ->
        ItemActionBottomSheet(
            item = LongPressItemPayload(
                id = targetTask.id,
                title = targetTask.title,
                textContent = "Production Milestone: ${targetTask.title}\nCategory: ${targetTask.category}\nScheduled: ${targetTask.dateTime}\nNotes: ${targetTask.notes}",
                subtitle = "${targetTask.category} • ${targetTask.dateTime}",
                category = "Creative Task"
            ),
            toneGenerator = toneGenerator,
            onDismiss = { activeLongPressedTask = null },
            onSaveToNotes = { title, content ->
                workspaceViewModel?.createNote(name = "Task: $title", initialText = content)
            },
            onDuplicate = {
                tasks.add(targetTask.copy(id = System.currentTimeMillis().toString(), title = "${targetTask.title} (Copy)"))
                Toast.makeText(context, "Duplicated task!", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                tasks.removeIf { it.id == targetTask.id }
                Toast.makeText(context, "Deleted task", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 5: INSPIRATION & LITERATURE LIBRARY
// -----------------------------------------------------------------------------------------
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CreativeInspirationLibraryTab(
    toneGenerator: HarmonicToneGenerator,
    workspaceViewModel: WorkspaceViewModel?
) {
    val context = LocalContext.current
    var activeLongPressedBook by remember { mutableStateOf<CreativeBookItem?>(null) }
    val books = listOf(
        CreativeBookItem(
            id = "b1",
            title = "The Creative Act: A Way of Being",
            author = "Rick Rubin",
            year = "2023",
            genre = "Philosophy & Art",
            keyQuote = "\"The object isn’t to make art, it’s to be in that wonderful state which makes art inevitable.\"",
            reflection = "Cultivating receptivity, tuning intuition, and distilling raw creative inspiration into timeless artifacts."
        ),
        CreativeBookItem(
            id = "b2",
            title = "Steal Like an Artist",
            author = "Austin Kleon",
            year = "2012",
            genre = "Creative Process",
            keyQuote = "\"Don't wait until you know who you are to get started. Make things, steal ideas, remix everything.\"",
            reflection = "Embracing creative lineage, recombining influences, and finding authentic voice through action."
        ),
        CreativeBookItem(
            id = "b3",
            title = "Invisible Cities",
            author = "Italo Calvino",
            year = "1972",
            genre = "Poetic Fiction",
            keyQuote = "\"Cities, like dreams, are made of desires and fears, even if the thread of their discourse is secret.\"",
            reflection = "Masterpiece of structural imagination, poetic architecture, and kaleidoscopic perspective shifts."
        ),
        CreativeBookItem(
            id = "b4",
            title = "Gödel, Escher, Bach: An Eternal Golden Braid",
            author = "Douglas Hofstadter",
            year = "1979",
            genre = "Cognitive Science & Art",
            keyQuote = "\"Meaning lies as much in the mind of the reader as in the haiku.\"",
            reflection = "Interweaving recursive loops, fugues, visual paradoxes, and the birth of consciousness."
        )
    )

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
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Inspiration Library & Literature", color = EchoText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Deep reading, quote architecture, and creative philosophy", color = EchoMuted, fontSize = 11.sp)
                }
            }
        }

        items(books) { book ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { /* normal view */ },
                        onLongClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            activeLongPressedBook = book
                        }
                    ),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = book.title, color = EchoCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = book.year, color = EchoMuted, fontSize = 11.sp)
                    }

                    Text(text = "${book.author} • ${book.genre}", color = EchoAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = book.keyQuote,
                        color = EchoText,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = book.reflection, color = EchoMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                workspaceViewModel?.createNote(
                                    name = "Quote: ${book.title.take(25)}",
                                    initialText = "${book.keyQuote}\n\n— ${book.author}, ${book.title} (${book.year})\n\nReflection:\n${book.reflection}"
                                )
                                Toast.makeText(context, "Saved Quote to Workspace Notes!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan.copy(alpha = 0.2f), contentColor = EchoCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Quote", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet on Long-Pressed Book Quote
    activeLongPressedBook?.let { targetBook ->
        ItemActionBottomSheet(
            item = LongPressItemPayload(
                id = targetBook.id,
                title = targetBook.title,
                textContent = "${targetBook.keyQuote}\n\n— ${targetBook.author}, ${targetBook.title} (${targetBook.year})\n\nReflection:\n${targetBook.reflection}",
                subtitle = "${targetBook.author} • ${targetBook.genre}",
                category = "Literature & Quote"
            ),
            toneGenerator = toneGenerator,
            onDismiss = { activeLongPressedBook = null },
            onSaveToNotes = { title, content ->
                workspaceViewModel?.createNote(name = "Quote: $title", initialText = content)
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 6: WORLD & LOCATIONS (Creative Settings & Landscapes)
// -----------------------------------------------------------------------------------------
@Composable
fun CreativeLocationsTab(
    toneGenerator: HarmonicToneGenerator,
    workspaceViewModel: WorkspaceViewModel?
) {
    val context = LocalContext.current
    val locations = listOf(
        CreativeLocationItem(
            name = "Icelandic Black Sand & Basalt Columns",
            region = "Reynisfjara, Iceland",
            coordinates = "63.4044° N, 19.0468° W",
            aestheticStyle = "Ethereal Obsidian & Oceanic Mist",
            description = "Monumental hexagonal basalt organs rising out of roaring North Atlantic surf.",
            creativeIdea = "Ideal backdrop for solitary sci-fi exploration, monochromatic cinematography, or ambient drone sequences."
        ),
        CreativeLocationItem(
            name = "Atacama High Desert Stargazing Plateau",
            region = "San Pedro de Atacama, Chile",
            coordinates = "23.8634° S, 69.1328° W",
            aestheticStyle = "Cosmic Silence & Martian Rust",
            description = "The clearest night skies on Earth with glowing Milky Way ribbons arching over silent salt flats.",
            creativeIdea = "Inspiring setting for astronomical storytelling, time-lapse photography, and philosophical introspections."
        ),
        CreativeLocationItem(
            name = "Ancient Megalithic Resonance Chambers",
            region = "Wiltshire & Salisbury, UK",
            coordinates = "51.1789° N, 1.8262° W",
            aestheticStyle = "Ancient Sarsen & Standing Echoes",
            description = "Concentric rings aligned to equinox solar trajectories with cinematic spatial acoustic focus.",
            creativeIdea = "Perfect setting for film sound design, historical narrative arcs, and atmospheric archaeology."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(locations) { loc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = loc.name, color = EchoText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${loc.region} • ${loc.coordinates}", color = EchoCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "Style: ${loc.aestheticStyle}", color = EchoAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = loc.description, color = EchoText.copy(alpha = 0.9f), fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "💡 Creative Pitch: ${loc.creativeIdea}",
                            color = EchoMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                workspaceViewModel?.createNote(
                                    name = "Location: ${loc.name.take(25)}",
                                    initialText = "Location: ${loc.name}\nCoordinates: ${loc.coordinates}\nRegion: ${loc.region}\nAesthetic: ${loc.aestheticStyle}\n\nCreative Idea:\n${loc.creativeIdea}"
                                )
                                Toast.makeText(context, "Saved Location to Workspace Notes!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan.copy(alpha = 0.2f), contentColor = EchoCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// API KEY CONFIGURATION DIALOG
// -----------------------------------------------------------------------------------------
@Composable
fun ApiKeySetupDialog(
    toneGenerator: HarmonicToneGenerator,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputKey by remember { mutableStateOf(CreativeHubAiService.getStoredApiKey(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = "Key", tint = EchoCyan)
                Text("Gemini Online AI Setup", color = EchoCyan, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your Google Gemini API key to enable live, unconstrained online intelligence across the entire Creative Hub. When connected, you can query anything on the web!",
                    color = EchoText,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = { Text("Paste your Gemini API key (AIzaSy...)", color = EchoMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Tip: You can obtain a free Gemini API key from Google AI Studio (aistudio.google.com).",
                    color = EchoMuted,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                    CreativeHubAiService.saveCustomApiKey(context, inputKey)
                    Toast.makeText(context, "API Key saved successfully!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
            ) {
                Text("Save Key")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close", color = EchoMuted)
            }
        },
        containerColor = EchoDarkSurface
    )
}
