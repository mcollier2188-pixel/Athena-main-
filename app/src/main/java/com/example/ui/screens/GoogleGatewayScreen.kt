package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Surface
import com.example.gateway.GoogleDriveVideoSyncManager
import com.example.gateway.DriveVideoItem
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ai.AthenaChatModel
import com.example.ai.AthenaService
import com.example.ai.ChatMessage
import com.example.ai.DolaVoiceManager
import com.example.ai.ImageGenerationItem
import com.example.ai.MusicGenerationItem
import com.example.ai.VideoGenerationItem
import com.example.gateway.GatewayRequester
import com.example.gateway.GatewayResponse
import com.example.ui.components.GoogleGateway
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GoogleSubTab(val title: String, val icon: ImageVector) {
    GATEWAY("Air-Gap Sandbox", Icons.Default.Public),
    GEMINI_SCIENCE("Gemini STEM Chat", Icons.Default.Science),
    LYRIA_MUSIC("Lyria Music", Icons.Default.MusicNote),
    MEDIA_GENERATION("Nano & Veo 3", Icons.Default.Movie),
    LIVE_VOICE("3.8 Live Voice", Icons.Default.Mic),
    CLOUD_AUTH("Firebase & Auth", Icons.Default.Cloud)
}

/**
 * Google Tab Screen:
 * Houses the zero-trust Google Gateway Sandbox alongside all authorized Google AI capabilities:
 * - Gemini STEM & Science Chat (Flash, Pro, Lite with Search & Maps Grounding)
 * - Google Lyria Music generation (30s clips & full tracks)
 * - Gemini Nano image creation & editing
 * - Veo 3 Video generation (16:9 & 9:16) & image animation
 * - Gemini 3.8 Live conversational audio
 * - Firebase Google Sign-In & Firestore status
 */
@Composable
fun GoogleGatewayScreen(
    modifier: Modifier = Modifier,
    initialRequester: GatewayRequester = GatewayRequester.ATHENA,
    onResultReceived: ((GatewayResponse) -> Unit)? = null
) {
    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
    ) {
        // Top Horizontal Sub-Navigation Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedSubTabIndex,
            containerColor = EchoBgAlt,
            contentColor = EchoCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTabIndex]),
                    color = EchoCyan,
                    height = 2.5.dp
                )
            },
            divider = { HorizontalDivider(color = EchoCardStroke, thickness = 1.dp) }
        ) {
            GoogleSubTab.values().forEachIndexed { index, tab ->
                val isSelected = selectedSubTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedSubTabIndex = index },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) EchoCyan else EchoMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) EchoCyan else EchoMuted
                        )
                    },
                    modifier = Modifier.testTag("google_subtab_${index}")
                )
            }
        }

        // Sub-Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedSubTabIndex) {
                GoogleSubTab.GATEWAY.ordinal -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        GoogleGateway(
                            modifier = Modifier.fillMaxWidth(),
                            initialRequester = initialRequester,
                            onResultReceived = onResultReceived
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }
                GoogleSubTab.GEMINI_SCIENCE.ordinal -> {
                    GoogleGeminiScienceView()
                }
                GoogleSubTab.LYRIA_MUSIC.ordinal -> {
                    GoogleLyriaMusicView()
                }
                GoogleSubTab.MEDIA_GENERATION.ordinal -> {
                    GoogleMediaGenerationView()
                }
                GoogleSubTab.LIVE_VOICE.ordinal -> {
                    GoogleLiveVoiceView()
                }
                GoogleSubTab.CLOUD_AUTH.ordinal -> {
                    GoogleCloudAuthView()
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 1. GEMINI SCIENCE & STEM CHAT VIEW
// -------------------------------------------------------------------------
@Composable
private fun GoogleGeminiScienceView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedModel by rememberSaveable { mutableStateOf(AthenaChatModel.PRO) }
    var enableSearch by rememberSaveable { mutableStateOf(true) }
    var enableMaps by rememberSaveable { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "model",
                content = "Google Gemini STEM & Science Intelligence ready. Equipped with models gemini-3.1-pro-preview for complex scientific reasoning, gemini-3.5-flash for general STEM, and gemini-3.1-flash-lite for instant responses.",
                modelUsed = "Gemini 3.1 Pro Preview"
            )
        )
    }

    var inputQuery by rememberSaveable { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Model Selection & Grounding Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT GEMINI REASONING MODEL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoCyan
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AthenaChatModel.values()) { model ->
                        val isSelected = selectedModel == model
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedModel = model },
                            label = { Text(model.displayName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = enableSearch,
                        onClick = { enableSearch = !enableSearch },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(13.dp)) },
                        label = { Text("Search Grounding", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoGreen,
                            selectedLabelColor = EchoOnCyan
                        )
                    )

                    FilterChip(
                        selected = enableMaps,
                        onClick = { enableMaps = !enableMaps },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp)) },
                        label = { Text("Maps Grounding", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoGreen,
                            selectedLabelColor = EchoOnCyan
                        )
                    )
                }
            }
        }

        // Messages Thread
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) EchoDarkSurface else EchoCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isUser) EchoCyan.copy(alpha = 0.5f) else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (msg.modelUsed.isNotBlank()) {
                                Text(msg.modelUsed, fontSize = 9.sp, color = EchoCyan, fontFamily = FontFamily.Monospace)
                            }
                            Text(msg.content, fontSize = 12.sp, color = EchoText, lineHeight = 17.sp)

                            if (msg.sources.isNotEmpty()) {
                                HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                                Text("Citations: " + msg.sources.joinToString(", "), fontSize = 9.sp, color = EchoMuted)
                            }
                        }
                    }
                }
            }
        }

        // Input & Send Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("gemini_science_query_input"),
                placeholder = { Text("Ask scientific query (STEM reasoning, formulas, physics)...", fontSize = 12.sp, color = EchoMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = EchoText,
                    unfocusedTextColor = EchoText,
                    focusedBorderColor = EchoCyan,
                    unfocusedBorderColor = EchoCardStroke,
                    focusedContainerColor = EchoBgAlt,
                    unfocusedContainerColor = EchoBgAlt
                ),
                maxLines = 3,
                shape = RoundedCornerShape(8.dp),
                enabled = !isSending
            )

            Button(
                onClick = {
                    if (inputQuery.isBlank()) return@Button
                    val query = inputQuery.trim()
                    inputQuery = ""
                    messages.add(ChatMessage(role = "user", content = query))
                    isSending = true

                    coroutineScope.launch {
                        val result = AthenaService.sendMultiTurnChat(
                            history = messages,
                            newMessage = query,
                            selectedModel = selectedModel,
                            enableSearchGrounding = enableSearch,
                            enableMapsGrounding = enableMaps
                        )
                        result.onSuccess { reply ->
                            messages.add(reply)
                        }
                        isSending = false
                    }
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("gemini_science_send_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                enabled = inputQuery.isNotBlank() && !isSending,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EchoOnCyan, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 2. GOOGLE LYRIA MUSIC STUDIO VIEW
// -------------------------------------------------------------------------
@Composable
private fun GoogleLyriaMusicView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var musicPrompt by rememberSaveable { mutableStateOf("Harmonic 42.600 Hz Bedrock Ambient Soundscape") }
    var selectedModel by rememberSaveable { mutableStateOf("lyria-3-clip-preview") } // or lyria-3-pro-preview
    var selectedGenre by rememberSaveable { mutableStateOf("Harmonic Ambient") }
    var isGenerating by remember { mutableStateOf(false) }

    val generatedTracks = remember {
        mutableStateListOf(
            MusicGenerationItem(
                prompt = "Solent Tidal Bedrock Resonance (Hilsea baseline)",
                model = "lyria-3-pro-preview",
                durationSeconds = 180,
                genre = "Acoustic Seismology",
                title = "Solent Tidal Resonance 42.6 Hz"
            ),
            MusicGenerationItem(
                prompt = "Nikola Tesla Colorado Springs 43 Hz harmonic clip",
                model = "lyria-3-clip-preview",
                durationSeconds = 30,
                genre = "Terrestrial Electronic",
                title = "Tesla 43 Hz Terrestrial Tone"
            )
        )
    }

    var currentlyPlayingId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = EchoCyan)
                    Column {
                        Text("GOOGLE LYRIA MUSIC ENGINE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EchoText, fontFamily = FontFamily.Monospace)
                        Text("lyria-3-clip-preview (up to 30s) · lyria-3-pro-preview (full track)", fontSize = 10.sp, color = EchoCyan)
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                // Model Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedModel == "lyria-3-clip-preview",
                        onClick = { selectedModel = "lyria-3-clip-preview" },
                        label = { Text("lyria-3-clip-preview (30s)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EchoCyan, selectedLabelColor = EchoOnCyan)
                    )
                    FilterChip(
                        selected = selectedModel == "lyria-3-pro-preview",
                        onClick = { selectedModel = "lyria-3-pro-preview" },
                        label = { Text("lyria-3-pro-preview (Full)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EchoCyan, selectedLabelColor = EchoOnCyan)
                    )
                }

                OutlinedTextField(
                    value = musicPrompt,
                    onValueChange = { musicPrompt = it },
                    modifier = Modifier.fillMaxWidth().testTag("lyria_prompt_input"),
                    label = { Text("Music Generation Prompt", color = EchoCyan, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            val isFull = selectedModel == "lyria-3-pro-preview"
                            val result = AthenaService.generateLyriaMusic(
                                prompt = musicPrompt,
                                isFullLengthTrack = isFull,
                                genre = selectedGenre
                            )
                            result.onSuccess { track ->
                                generatedTracks.add(0, track)
                                Toast.makeText(context, "Generated Lyria Track!", Toast.LENGTH_SHORT).show()
                            }
                            isGenerating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("lyria_generate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    enabled = musicPrompt.isNotBlank() && !isGenerating,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EchoOnCyan, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("GENERATING WITH LYRIA...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("GENERATE LYRIA AUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("GENERATED LYRIA TRACKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)

        generatedTracks.forEach { track ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(track.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EchoText)
                        Text("${track.model} · ${track.durationSeconds}s · ${track.genre}", fontSize = 10.sp, color = EchoCyan, fontFamily = FontFamily.Monospace)
                        Text(track.prompt, fontSize = 11.sp, color = EchoMuted, maxLines = 1)
                    }

                    val isPlaying = currentlyPlayingId == track.id
                    IconButton(
                        onClick = {
                            currentlyPlayingId = if (isPlaying) null else track.id
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) EchoGreen else EchoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 3. GOOGLE MEDIA (NANO IMAGE & VEO 3 VIDEO)
// -------------------------------------------------------------------------
@Composable
private fun GoogleMediaGenerationView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mediaTypeTab by rememberSaveable { mutableIntStateOf(0) } // 0: Veo Video, 1: Nano Image
    var mediaPrompt by rememberSaveable { mutableStateOf("Subterranean acoustic standing wave resonance in Portsmouth Solent chalk bedrock") }
    var videoAspectRatio by rememberSaveable { mutableStateOf("16:9") } // 16:9 or 9:16
    var isProcessing by remember { mutableStateOf(false) }

    val videos = remember {
        mutableStateListOf(
            VideoGenerationItem(
                prompt = "Acoustic wavefront propagation through Stonehenge Preseli bluestones",
                model = "veo-3.1-fast-generate-preview",
                aspectRatio = "16:9",
                videoTitle = "Preseli Megalithic Acoustic Wavefront",
                durationSeconds = 6
            )
        )
    }

    val images = remember {
        mutableStateListOf(
            ImageGenerationItem(
                prompt = "Bedrock resonance standing wave interferometry at 42.600 Hz",
                model = "gemini-nano-banana-2.1",
                aspectRatio = "1:1",
                title = "Bedrock Resonant Interferometry"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = EchoCyan)
                    Column {
                        Text("VEO 3 VIDEO & GEMINI NANO IMAGE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EchoText, fontFamily = FontFamily.Monospace)
                        Text("veo-3.1-fast-generate-preview (16:9 / 9:16) · gemini-nano-banana-2.1", fontSize = 10.sp, color = EchoCyan)
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mediaTypeTab == 0,
                        onClick = { mediaTypeTab = 0 },
                        label = { Text("Veo 3 Video", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EchoCyan, selectedLabelColor = EchoOnCyan)
                    )
                    FilterChip(
                        selected = mediaTypeTab == 1,
                        onClick = { mediaTypeTab = 1 },
                        label = { Text("Nano Image", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EchoCyan, selectedLabelColor = EchoOnCyan)
                    )
                }

                if (mediaTypeTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aspect Ratio:", fontSize = 11.sp, color = EchoMuted)
                        FilterChip(
                            selected = videoAspectRatio == "16:9",
                            onClick = { videoAspectRatio = "16:9" },
                            label = { Text("16:9 Landscape", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EchoGreen, selectedLabelColor = EchoOnCyan)
                        )
                        FilterChip(
                            selected = videoAspectRatio == "9:16",
                            onClick = { videoAspectRatio = "9:16" },
                            label = { Text("9:16 Portrait", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EchoGreen, selectedLabelColor = EchoOnCyan)
                        )
                    }
                }

                OutlinedTextField(
                    value = mediaPrompt,
                    onValueChange = { mediaPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (mediaTypeTab == 0) "Veo 3 Video Prompt" else "Gemini Nano Image Prompt", color = EchoCyan, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        isProcessing = true
                        coroutineScope.launch {
                            if (mediaTypeTab == 0) {
                                val res = AthenaService.generateVeoVideo(prompt = mediaPrompt, aspectRatio = videoAspectRatio)
                                res.onSuccess { item ->
                                    videos.add(0, item)
                                    GoogleDriveVideoSyncManager.autoSaveVideoToDrive(
                                        context = context,
                                        title = item.videoTitle,
                                        prompt = item.prompt,
                                        model = item.model,
                                        aspectRatio = item.aspectRatio,
                                        durationSec = item.durationSeconds
                                    )
                                }
                            } else {
                                val res = AthenaService.generateOrEditImage(prompt = mediaPrompt)
                                res.onSuccess { item -> images.add(0, item) }
                            }
                            isProcessing = false
                            Toast.makeText(context, "Generated media item!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    enabled = mediaPrompt.isNotBlank() && !isProcessing,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EchoOnCyan, strokeWidth = 2.dp)
                    } else {
                        Text(if (mediaTypeTab == 0) "GENERATE VEO 3 VIDEO" else "CREATE NANO IMAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(if (mediaTypeTab == 0) "VEO 3 VIDEO GALLERY" else "NANO IMAGE GALLERY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)

        if (mediaTypeTab == 0) {
            videos.forEach { v ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(v.videoTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EchoText)
                            Text(v.aspectRatio, fontSize = 10.sp, color = EchoGreen, fontFamily = FontFamily.Monospace)
                        }
                        Text("Model: ${v.model} · ${v.durationSeconds}s", fontSize = 10.sp, color = EchoCyan, fontFamily = FontFamily.Monospace)
                        Text(v.prompt, fontSize = 11.sp, color = EchoMuted)

                        // Google Drive Sync row & Export button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = EchoGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, EchoGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(12.dp))
                                    Text("Drive: /ATHENA_AI_Videos", color = EchoGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    val driveItem = DriveVideoItem(
                                        id = "drive_vid_${System.currentTimeMillis()}",
                                        title = v.videoTitle,
                                        prompt = v.prompt,
                                        model = v.model,
                                        aspectRatio = v.aspectRatio,
                                        durationSec = v.durationSeconds,
                                        videoUriString = null
                                    )
                                    val shareIntent = GoogleDriveVideoSyncManager.createExportToDriveIntent(context, driveItem)
                                    context.startActivity(shareIntent)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export to Drive", fontSize = 10.sp, color = EchoGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            images.forEach { img ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(img.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EchoText)
                        Text("Model: ${img.model} · ${img.aspectRatio}", fontSize = 10.sp, color = EchoCyan, fontFamily = FontFamily.Monospace)
                        Text(img.prompt, fontSize = 11.sp, color = EchoMuted)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 4. GOOGLE LIVE VOICE (GEMINI 3.8 LIVE)
// -------------------------------------------------------------------------
@Composable
private fun GoogleLiveVoiceView() {
    val context = LocalContext.current
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    var isLiveActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = EchoCyan)
                    Column {
                        Text("GEMINI 3.8 LIVE (VOICE CONVERSATIONS)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EchoText, fontFamily = FontFamily.Monospace)
                        Text("Real-time low-latency bidirectional voice session", fontSize = 10.sp, color = EchoCyan)
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                Text(
                    text = "Live API integrates bidirectional conversational speech with sub-second latency, streaming response audio with Bedrock intelligence.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // Animated Voice Spark Orb
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(if (isLiveActive) EchoCyan.copy(alpha = 0.2f) else EchoDarkSurface)
                .border(2.dp, if (isLiveActive) EchoCyan else EchoCardStroke, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (isLiveActive) EchoCyan else EchoMuted,
                modifier = Modifier.size(54.dp)
            )
        }

        Text(
            text = if (isLiveActive) "GEMINI 3.8 LIVE STREAMING ACTIVE" else "LIVE VOICE SESSION STANDBY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isLiveActive) EchoGreen else EchoMuted
        )

        Button(
            onClick = {
                isLiveActive = !isLiveActive
                if (isLiveActive) {
                    voiceManager.speak("Gemini 3.8 Live session initialized. Bedrock resonance monitoring is active.")
                } else {
                    voiceManager.stopSpeaking()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLiveActive) EchoRed else EchoCyan,
                contentColor = if (isLiveActive) Color.White else EchoOnCyan
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(if (isLiveActive) Icons.Default.Pause else Icons.Default.Mic, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (isLiveActive) "DISCONNECT LIVE VOICE" else "START GEMINI 3.8 LIVE SESSION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// -------------------------------------------------------------------------
// 5. GOOGLE CLOUD DATABASE & AUTH VIEW (FIREBASE)
// -------------------------------------------------------------------------
@Composable
private fun GoogleCloudAuthView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = EchoCyan)
                    Column {
                        Text("GOOGLE CLOUD PERSISTENCE & AUTH", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EchoText, fontFamily = FontFamily.Monospace)
                        Text("Google Sign-In with Firebase Auth & Cloud Firestore", fontSize = 10.sp, color = EchoCyan)
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                Text(
                    text = "Secure Google Sign-In with Firebase Authentication securely identifies operators, while Cloud Firestore synchronizes project notes, resonance telemetry, and canon field documents.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoGreen.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(16.dp))
                    Text("SECURE IDENTITY & STORAGE STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoGreen, fontFamily = FontFamily.Monospace)
                }

                Text("• Authentication Provider: Google Identity (Credential Manager)", fontSize = 11.sp, color = EchoText)
                Text("• Database Provider: Cloud Firestore (Rules verified)", fontSize = 11.sp, color = EchoText)
                Text("• Zero-Trust Isolation: Local app remains 100% functional offline", fontSize = 11.sp, color = EchoCyan)
            }
        }
    }
}
