package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BookmarkBorder
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ai.GeminiMediaGenerationService
import com.example.data.MediaVaultRepository
import com.example.data.VaultMediaItem
import com.example.ui.components.MediaType
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Surface
import com.example.gateway.GoogleDriveVideoSyncManager
import com.example.gateway.DriveVideoItem
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ai.CreativeHubAiService
import com.example.ai.CreativeMessage
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
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
import com.example.ui.viewmodel.WorkspaceViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GeminiPlaygroundTab(
    toneGenerator: HarmonicToneGenerator,
    workspaceViewModel: WorkspaceViewModel?,
    onOpenApiKeyDialog: () -> Unit,
    onOpenPreferences: (() -> Unit)? = null,
    onOpenBrowser: ((String, String) -> Unit)? = null,
    onOpenMedia: ((Uri, MediaType, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("gemini_playground_prefs", Context.MODE_PRIVATE) }

    // Generative AI Media Dialog States
    var showQuickImageDialog by remember { mutableStateOf(false) }
    var showQuickVideoDialog by remember { mutableStateOf(false) }

    // Text To Speech initialization
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var currentlySpeakingId by remember { mutableStateOf<String?>(null) }
    var speechRate by remember {
        mutableFloatStateOf(prefs.getFloat("speech_rate", 1.0f))
    }
    var isAutoDictateEnabled by remember {
        mutableStateOf(prefs.getBoolean("auto_dictate_enabled", true))
    }
    var isAutoPushWorkspaceEnabled by remember {
        mutableStateOf(prefs.getBoolean("auto_push_workspace_enabled", false))
    }
    // Interactive Long-Press Action Sheet Target
    var activeLongPressedMessage by remember { mutableStateOf<CreativeMessage?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(1.0f)
                ttsInstance = tts
                isTtsReady = true
            }
        }

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun cleanMarkdownForSpeech(raw: String): String {
        return raw.replace(Regex("[\"\'#*_`~\\[\\]]"), "")
            .replace(Regex("\\bhttps?://\\S+"), "web link")
            .replace(Regex("\n+"), ". ")
            .trim()
    }

    fun speakMessage(messageId: String, text: String) {
        ttsInstance?.let { tts ->
            if (currentlySpeakingId == messageId && isSpeaking) {
                tts.stop()
                isSpeaking = false
                currentlySpeakingId = null
            } else {
                tts.stop()
                val speechText = cleanMarkdownForSpeech(text)
                currentlySpeakingId = messageId
                isSpeaking = true
                tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, messageId)
            }
        }
    }

    fun stopSpeaking() {
        ttsInstance?.stop()
        isSpeaking = false
        currentlySpeakingId = null
    }

    // Persona Preset Models & Initial Setup
    val personaPresets = remember {
        listOf(
            PersonaPreset("Theoretical Physicist", "⚛️", "You are a senior theoretical physicist specializing in planetary acoustics, standing wave equations (f = v / 2L), Q-factor resonance matrices, bedrock harmonic locks at 42.600 Hz, and Solent basin waveguide propagation. Provide mathematically rigorous scientific solutions.", 0.3f),
            PersonaPreset("Socratic Inquirer", "🏛️", "You are a master dialectic philosopher. Scrutinize every claim, uncover latent assumptions, challenge premises with counterexamples, and illuminate truth through structured inquiry.", 0.6f),
            PersonaPreset("Creative Visionary", "🎬", "You are an elite creative director and cinematic worldbuilder. Generate evocative screenplays, viral multi-beat scripts, rich audio-visual motifs, and aesthetic production concepts.", 0.8f),
            PersonaPreset("Adversarial Red-Teamer", "🛡️", "You are a relentless adversarial red-team auditor. Identify edge-case failures, falsification criteria, acoustic leakage vulnerabilities, and empirical inconsistencies.", 0.4f),
            PersonaPreset("Custom Persona", "⚙️", "You are an intelligent, precise AI collaborator tailored to the user's specific guidelines.", 0.7f)
        )
    }

    var selectedPersonaName by remember { mutableStateOf("Theoretical Physicist") }
    var customInstructionText by remember { mutableStateOf(personaPresets[0].instruction) }
    var temperature by remember { mutableFloatStateOf(personaPresets[0].defaultTemp) }
    var topP by remember { mutableFloatStateOf(0.95f) }
    var showPersonaSettingsDialog by remember { mutableStateOf(false) }

    // Telemetry Inspector State
    var latestPromptTokens by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var latestResponseTokens by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var latestLatencyMs by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var latestTokensPerSec by remember { mutableFloatStateOf(0f) }
    var showTelemetryInspectorDialog by remember { mutableStateOf(false) }

    // Conversation Branches State
    val initialMessage = CreativeMessage(
        role = "model",
        text = "⚡ **Welcome to the Gemini Pro AI Playground & Laboratory!** 🎬🎨\n\nEquipped with **real-time token streaming**, **multimodal image/audio perception**, **live telemetry monitoring**, **conversation branching**, and **deep system instruction personas**.\n\n🎙️ **Voice Dictation is Active** — Attach an image or audio file below, switch personas, or type your prompt to begin streaming!",
        modelTag = "Gemini 3.5 Flash",
        isOnline = true
    )
    val branches = remember { mutableStateListOf(mutableListOf(initialMessage)) }
    var currentBranchIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val chatMessages = branches.getOrElse(currentBranchIndex) { branches[0] }

    fun forkBranch(fromMessageId: String? = null) {
        toneGenerator.playChime(SoundEffectType.SUCCESS)
        val currentList = branches.getOrElse(currentBranchIndex) { branches[0] }
        val subList = if (fromMessageId != null) {
            val targetIdx = currentList.indexOfFirst { it.id == fromMessageId }
            if (targetIdx >= 0) currentList.take(targetIdx + 1) else currentList
        } else {
            currentList
        }
        val copied = subList.map { it.copy(id = java.util.UUID.randomUUID().toString()) }.toMutableList()
        branches.add(copied)
        currentBranchIndex = branches.size - 1
        Toast.makeText(context, "Branched to Alternative Path #${branches.size}", Toast.LENGTH_SHORT).show()
    }

    // Multimodal Media Attachment State
    var attachedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var attachedMediaType by remember { mutableStateOf<String?>(null) } // "image" or "audio"
    var attachedMediaName by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedMediaUri = uri
            attachedMediaType = "image"
            attachedMediaName = "Image: ${uri.lastPathSegment?.takeLast(16) ?: "diagram.jpg"}"
            toneGenerator.playChime(SoundEffectType.TAP)
            Toast.makeText(context, "Image exhibit attached", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedMediaUri = uri
            attachedMediaType = "audio"
            attachedMediaName = "Audio: ${uri.lastPathSegment?.takeLast(16) ?: "audio.mp3"}"
            toneGenerator.playChime(SoundEffectType.TAP)
            Toast.makeText(context, "Audio waveform attached", Toast.LENGTH_SHORT).show()
        }
    }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var enableSearchGrounding by remember { mutableStateOf(true) }
    var selectedTone by remember { mutableStateOf("Cinematic & Visionary") }
    val listState = rememberLazyListState()

    val creativeTools = listOf(
        "🎨 Generate AI Image (Gemini 2.5 Flash)",
        "🎬 Generate Veo Video (Veo 3.1 Fast)",
        "🎬 Viral Video Script & 3-Beat Hook",
        "🎨 Cinematic Image Prompt Studio",
        "🎭 Sci-Fi Screenplay Scene",
        "⚡ 5 Creative Entertainment Concepts",
        "🍿 Movie Plot & Character Lore",
        "💡 Push Production Blueprint to Workspace"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("gemini_playground_tab")
    ) {
        // Top Playground Header Bar with Controller Toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .background(if (CreativeHubAiService.isOnlineConfigured(context)) EchoGreen else EchoAmber)
                        )
                        Column {
                            Text(
                                text = "Gemini AI Director & Playground",
                                color = EchoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (CreativeHubAiService.isOnlineConfigured(context)) "Online: Gemini 3.5 Flash Engine Active" else "AI Ready • Prototype Mode",
                                color = if (CreativeHubAiService.isOnlineConfigured(context)) EchoCyan else EchoAmber,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Preferences Button
                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onOpenPreferences?.invoke()
                            },
                            modifier = Modifier.size(30.dp).testTag("btn_playground_preferences")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Preferences", tint = EchoCyan, modifier = Modifier.size(16.dp))
                        }

                        // Dola Browser Button
                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onOpenBrowser?.invoke("https://www.google.com", "Dola Browser")
                            },
                            modifier = Modifier.size(30.dp).testTag("btn_playground_browser")
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Dola Browser", tint = EchoCyan, modifier = Modifier.size(16.dp))
                        }

                        // API Key Config Button
                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onOpenApiKeyDialog()
                            },
                            modifier = Modifier.size(30.dp).testTag("btn_playground_api_key")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = "API Key", tint = EchoCyan, modifier = Modifier.size(16.dp))
                        }

                        if (isSpeaking) {
                            IconButton(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    stopSpeaking()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop Speech", tint = EchoRed, modifier = Modifier.size(18.dp))
                            }
                        }

                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                stopSpeaking()
                                chatMessages.clear()
                                chatMessages.add(
                                    CreativeMessage(
                                        role = "model",
                                        text = "Session cleared. What would you like to brainstorm or create next?",
                                        modelTag = "Gemini 3.5 Flash"
                                    )
                                )
                                Toast.makeText(context, "Chat reset", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Chat", tint = EchoMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)

                // Voice Dictation & Automation Controller Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Auto-Dictate & Speed Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoDictateEnabled) Icons.Default.RecordVoiceOver else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = if (isAutoDictateEnabled) EchoCyan else EchoMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Auto-Dictate",
                            color = if (isAutoDictateEnabled) EchoCyan else EchoMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isAutoDictateEnabled,
                            onCheckedChange = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                isAutoDictateEnabled = it
                                prefs.edit().putBoolean("auto_dictate_enabled", it).apply()
                                if (!it) stopSpeaking()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EchoCyan,
                                checkedTrackColor = EchoCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = EchoMuted,
                                uncheckedTrackColor = EchoBgAlt
                            ),
                            modifier = Modifier.size(width = 38.dp, height = 24.dp).testTag("switch_auto_dictate")
                        )

                        // Speech Speed Toggle Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoBgAlt)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(6.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    val nextRate = when (speechRate) {
                                        0.8f -> 1.0f
                                        1.0f -> 1.25f
                                        else -> 0.8f
                                    }
                                    speechRate = nextRate
                                    prefs.edit().putFloat("speech_rate", nextRate).apply()
                                    ttsInstance?.setSpeechRate(nextRate)
                                    Toast.makeText(context, "Voice Speed: ${nextRate}x", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${speechRate}x",
                                color = EchoCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Auto-Push to Workspace Notes Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = null,
                            tint = if (isAutoPushWorkspaceEnabled) EchoCyan else EchoMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Auto-Push Notes",
                            color = if (isAutoPushWorkspaceEnabled) EchoCyan else EchoMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isAutoPushWorkspaceEnabled,
                            onCheckedChange = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                isAutoPushWorkspaceEnabled = it
                                prefs.edit().putBoolean("auto_push_workspace_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EchoCyan,
                                checkedTrackColor = EchoCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = EchoMuted,
                                uncheckedTrackColor = EchoBgAlt
                            ),
                            modifier = Modifier.size(width = 38.dp, height = 24.dp).testTag("switch_auto_push_workspace")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Live Telemetry Inspector Bar & Conversation Branch Navigator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Telemetry Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoBgAlt)
                    .border(1.dp, EchoCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        showTelemetryInspectorDialog = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("telemetry_bar_inspector")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(13.dp))
                    Text(
                        text = if (latestTokensPerSec > 0f) {
                            "⚡ ${"%.1f".format(latestTokensPerSec)} tok/s • ${latestResponseTokens} tok • ${latestLatencyMs}ms"
                        } else {
                            "⚡ Telemetry: Ready • 3.5 Flash"
                        },
                        color = EchoCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Conversation Branching Controller
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoBgAlt)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = {
                                if (currentBranchIndex > 0) {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    currentBranchIndex--
                                }
                            },
                            enabled = currentBranchIndex > 0,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Branch", tint = if (currentBranchIndex > 0) EchoCyan else EchoMuted, modifier = Modifier.size(14.dp))
                        }

                        Text(
                            text = "Branch ${currentBranchIndex + 1}/${branches.size}",
                            color = EchoText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = {
                                if (currentBranchIndex < branches.size - 1) {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    currentBranchIndex++
                                }
                            },
                            enabled = currentBranchIndex < branches.size - 1,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Branch", tint = if (currentBranchIndex < branches.size - 1) EchoCyan else EchoMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Fork Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoCyan.copy(alpha = 0.15f))
                        .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { forkBranch() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("btn_fork_branch")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.ForkRight, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(12.dp))
                        Text("Fork", color = EchoCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // System Instruction & Persona Presets Selector Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(personaPresets) { preset ->
                    val isSelected = selectedPersonaName == preset.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedPersonaName = preset.name
                            if (preset.name != "Custom Persona") {
                                customInstructionText = preset.instruction
                                temperature = preset.defaultTemp
                            } else {
                                showPersonaSettingsDialog = true
                            }
                        },
                        label = {
                            Text(
                                text = "${preset.icon} ${preset.name}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoCyan,
                            selectedLabelColor = EchoOnCyan,
                            containerColor = EchoBgAlt,
                            labelColor = EchoText
                        )
                    )
                }
            }

            IconButton(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    showPersonaSettingsDialog = true
                },
                modifier = Modifier.size(28.dp).testTag("btn_persona_settings")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = "Persona Settings", tint = EchoCyan, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Entertainment Brainstorming Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(creativeTools) { tool ->
                FilterChip(
                    selected = false,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        when (tool) {
                            "🎨 Generate AI Image (Gemini 2.5 Flash)" -> {
                                showQuickImageDialog = true
                            }
                            "🎬 Generate Veo Video (Veo 3.1 Fast)" -> {
                                showQuickVideoDialog = true
                            }
                            else -> {
                                inputText = when (tool) {
                                    "🎬 Viral Video Script & 3-Beat Hook" -> "Write a high-retention viral video script: include a 3-second opening hook, high-stakes premise, 3 rapid visual beats, and cinematic sound effect cues."
                                    "🎨 Cinematic Image Prompt Studio" -> "Design 3 ultra-detailed visual art generation prompts: specify camera lenses, lighting ratios, futuristic color palettes, and composition styles for an upcoming sci-fi epic."
                                    "🎭 Sci-Fi Screenplay Scene" -> "Write a gripping screenplay scene between two cosmic navigators discovering a derelict hyper-drive cruiser near Jupiter's rings."
                                    "⚡ 5 Creative Entertainment Concepts" -> "Brainstorm 5 original, boundary-pushing entertainment concepts spanning cinema, interactive narrative gaming, and episodic video series."
                                    "🍿 Movie Plot & Character Lore" -> "Create a compelling movie synopsis, antagonist motivation, and world lore for a retro-futuristic mystery thriller."
                                    "💡 Push Production Blueprint to Workspace" -> "Generate a production blueprint for an indie short film, including crew roles, schedule milestones, and location aesthetics. Format as a Workspace Note."
                                    else -> "Brainstorm an innovative entertainment project."
                                }
                            }
                        }
                    },
                    label = { Text(tool, fontSize = 11.sp, color = EchoText, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = EchoCardStroke,
                        selectedBorderColor = EchoCyan,
                        enabled = true,
                        selected = false
                    ),
                    modifier = Modifier.testTag("chip_tool_${tool.take(4)}")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dedicated Collapsible Imagen 3 Studio Component
        PlaygroundImagenStudioCard(
            toneGenerator = toneGenerator,
            onPostToChat = { generated ->
                chatMessages.add(
                    CreativeMessage(
                        role = "user",
                        text = "🎨 [Imagen 3 Prompt]: ${generated.prompt}",
                        isOnline = true
                    )
                )
                chatMessages.add(
                    CreativeMessage(
                        role = "model",
                        text = "🎨 **Google Imagen 3 Synthesis**\n\nPrompt: ${generated.prompt}\nStyle: ${generated.style}\nAspect Ratio: ${generated.aspectRatio}",
                        modelTag = "Imagen 3 API",
                        contentType = "generated_image",
                        mediaUri = generated.localUri.toString(),
                        mediaType = "image",
                        visualTags = listOf("Imagen 3", generated.style, generated.aspectRatio)
                    )
                )
            },
            onAttachToWorkspaceNote = { title, uri ->
                workspaceViewModel?.createNote(
                    name = title,
                    initialText = "Attached Imagen 3 Art: $title\nURI: $uri\nDate: ${System.currentTimeMillis()}"
                )
            },
            onOpenMedia = onOpenMedia
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Message Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("gemini_chat_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages, key = { it.id }) { message ->
                PlaygroundChatBubble(
                    message = message,
                    isSpeaking = currentlySpeakingId == message.id && isSpeaking,
                    onToggleSpeak = { speakMessage(message.id, message.text) },
                    onSaveToNote = { content ->
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        val title = content.lines().firstOrNull()?.replace("#", "")?.trim()?.take(35) ?: "Gemini Creative Concept"
                        workspaceViewModel?.createNote(
                            name = title,
                            initialText = content
                        )
                        Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                    },
                    onFork = { forkBranch(message.id) },
                    onLongClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        activeLongPressedMessage = message
                    },
                    onOpenBrowser = onOpenBrowser,
                    onOpenMedia = onOpenMedia
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
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
                            text = "Streaming live tokens via Gemini SSE...",
                            color = EchoCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Attached Multimodal Media Preview Pill
        if (attachedMediaUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoBgAlt)
                    .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (attachedMediaType == "image") Icons.Default.AddPhotoAlternate else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = EchoCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = attachedMediaName ?: "Attached Exhibit",
                        color = EchoCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = {
                        attachedMediaUri = null
                        attachedMediaType = null
                        attachedMediaName = null
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = EchoRed, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Input & Direct Submission Bar with Multimodal Attachment Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Photo Attachment Button
            IconButton(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.size(38.dp).testTag("btn_attach_photo")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach Image", tint = EchoCyan, modifier = Modifier.size(20.dp))
            }

            // Audio Attachment Button
            IconButton(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    audioPickerLauncher.launch("audio/*")
                },
                modifier = Modifier.size(38.dp).testTag("btn_attach_audio")
            ) {
                Icon(Icons.Default.Audiotrack, contentDescription = "Attach Audio", tint = EchoCyan, modifier = Modifier.size(20.dp))
            }

            // Quick AI Image Studio Trigger
            IconButton(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    showQuickImageDialog = true
                },
                modifier = Modifier.size(38.dp).testTag("btn_playground_gen_image")
            ) {
                Icon(Icons.Default.Palette, contentDescription = "Generate AI Image", tint = EchoCyan, modifier = Modifier.size(20.dp))
            }

            // Quick Veo Video Studio Trigger
            IconButton(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    showQuickVideoDialog = true
                },
                modifier = Modifier.size(38.dp).testTag("btn_playground_gen_video")
            ) {
                Icon(Icons.Default.Movie, contentDescription = "Generate Veo Video", tint = EchoAmber, modifier = Modifier.size(20.dp))
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_gemini_prompt"),
                placeholder = {
                    Text(
                        "Ask Gemini or analyze attached media...",
                        color = EchoMuted,
                        fontSize = 12.sp
                    )
                },
                maxLines = 4,
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

                    // Direct generative command handling
                    if (promptToSend.startsWith("/image ", ignoreCase = true) || promptToSend.startsWith("/img ", ignoreCase = true)) {
                        val cleanPrompt = if (promptToSend.startsWith("/image ", ignoreCase = true)) {
                            promptToSend.removePrefix("/image ").trim()
                        } else {
                            promptToSend.removePrefix("/img ").trim()
                        }
                        if (cleanPrompt.isNotBlank() && !isLoading) {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            chatMessages.add(CreativeMessage(role = "user", text = "🎨 [AI Image Prompt]: $cleanPrompt", isOnline = true))
                            inputText = ""
                            isLoading = true
                            coroutineScope.launch {
                                val result = GeminiMediaGenerationService.generateImage(context, cleanPrompt)
                                isLoading = false
                                result.onSuccess { res ->
                                    chatMessages.add(
                                        CreativeMessage(
                                            role = "model",
                                            text = "🎨 **Gemini 2.5 Flash Art Synthesized**\n\nPrompt: ${res.prompt}\nStyle: ${res.style}\nAspect Ratio: ${res.aspectRatio}",
                                            modelTag = "Gemini 2.5 Flash Image",
                                            contentType = "generated_image",
                                            mediaUri = res.localUri.toString(),
                                            mediaType = "image",
                                            visualTags = listOf("Gemini 2.5 Flash", res.style, res.aspectRatio)
                                        )
                                    )
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
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                }.onFailure {
                                    chatMessages.add(CreativeMessage(role = "model", text = "Image generation error: ${it.localizedMessage}"))
                                }
                            }
                        }
                        return@Button
                    }

                    if (promptToSend.startsWith("/video ", ignoreCase = true) || promptToSend.startsWith("/veo ", ignoreCase = true)) {
                        val cleanPrompt = if (promptToSend.startsWith("/video ", ignoreCase = true)) {
                            promptToSend.removePrefix("/video ").trim()
                        } else {
                            promptToSend.removePrefix("/veo ").trim()
                        }
                        if (cleanPrompt.isNotBlank() && !isLoading) {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            chatMessages.add(CreativeMessage(role = "user", text = "🎬 [Veo Video Prompt]: $cleanPrompt", isOnline = true))
                            inputText = ""
                            isLoading = true
                            coroutineScope.launch {
                                val result = GeminiMediaGenerationService.generateVeoVideo(context, cleanPrompt)
                                isLoading = false
                                result.onSuccess { res ->
                                    chatMessages.add(
                                        CreativeMessage(
                                            role = "model",
                                            text = "🎬 **Veo 3.1 Video Package: ${res.title}**\n\nGenre: ${res.genre} | Aspect Ratio: ${res.aspectRatio}\n\n${res.shots.joinToString("\n") { "• Shot ${it.shotNumber} (${it.durationSec}s): ${it.title} - ${it.cameraMovement}" }}",
                                            modelTag = "Veo 3.1 Fast",
                                            contentType = "veo_storyboard",
                                            mediaUri = res.videoUri?.toString(),
                                            mediaType = "video",
                                            visualTags = listOf("Veo 3.1 Fast", res.genre, res.aspectRatio)
                                        )
                                    )
                                    if (res.videoUri != null) {
                                        MediaVaultRepository.addItem(
                                            VaultMediaItem(
                                                title = res.title,
                                                subtitle = "Veo Video • ${res.genre} • ${res.aspectRatio}",
                                                uri = res.videoUri,
                                                type = MediaType.VIDEO,
                                                prompt = res.prompt,
                                                style = res.genre,
                                                isAiGenerated = true,
                                                isVeoVideo = true
                                            )
                                        )
                                    }
                                    // Auto-save generated video to Google Drive
                                    GoogleDriveVideoSyncManager.autoSaveVideoToDrive(
                                        context = context,
                                        title = res.title,
                                        prompt = res.prompt,
                                        model = "Veo 3.1 Fast",
                                        aspectRatio = res.aspectRatio,
                                        durationSec = 6,
                                        videoUriString = res.videoUri?.toString()
                                    )
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                }.onFailure {
                                    chatMessages.add(CreativeMessage(role = "model", text = "Veo video error: ${it.localizedMessage}"))
                                }
                            }
                        }
                        return@Button
                    }

                    if ((promptToSend.isNotBlank() || attachedMediaUri != null) && !isLoading) {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        val mediaUriToSave = attachedMediaUri
                        val mediaTypeToSave = attachedMediaType
                        chatMessages.add(
                            CreativeMessage(
                                role = "user",
                                text = promptToSend.ifEmpty { "[Analyzed attached $mediaTypeToSave]" },
                                isOnline = true,
                                mediaUri = mediaUriToSave?.toString(),
                                mediaType = mediaTypeToSave
                            )
                        )
                        inputText = ""
                        attachedMediaUri = null
                        attachedMediaType = null
                        attachedMediaName = null
                        isLoading = true

                        coroutineScope.launch {
                            val placeholderMsg = CreativeMessage(
                                role = "model",
                                text = "",
                                modelTag = "Gemini 3.5 Flash",
                                isOnline = true
                            )
                            chatMessages.add(placeholderMsg)
                            val modelMsgIndex = chatMessages.size - 1

                            try {
                                val base64Pair = if (mediaUriToSave != null) {
                                    getBase64FromUri(context, mediaUriToSave)
                                } else null

                                val accumulated = StringBuilder()
                                val finalReply = CreativeHubAiService.streamAnything(
                                    context = context,
                                    prompt = promptToSend.ifEmpty { "Analyze the attached $mediaTypeToSave in detail." },
                                    history = chatMessages.dropLast(1),
                                    model = "gemini-3.5-flash",
                                    systemInstruction = customInstructionText,
                                    temperature = temperature.toDouble(),
                                    topP = topP.toDouble(),
                                    base64Media = base64Pair?.first,
                                    mimeType = base64Pair?.second,
                                    onChunk = { chunk ->
                                        accumulated.append(chunk)
                                        if (modelMsgIndex in chatMessages.indices) {
                                            chatMessages[modelMsgIndex] = chatMessages[modelMsgIndex].copy(text = accumulated.toString())
                                        }
                                    }
                                )

                                if (modelMsgIndex in chatMessages.indices) {
                                    chatMessages[modelMsgIndex] = finalReply.copy(id = placeholderMsg.id)
                                }

                                // Update Telemetry metrics
                                latestPromptTokens = finalReply.promptTokens
                                latestResponseTokens = finalReply.responseTokens
                                latestLatencyMs = finalReply.latencyMs
                                latestTokensPerSec = finalReply.tokensPerSec

                                toneGenerator.playChime(SoundEffectType.TAP)

                                // Auto-dictate voice playback if enabled
                                if (isAutoDictateEnabled && isTtsReady) {
                                    speakMessage(finalReply.id, finalReply.text)
                                }

                                // Auto-push to workspace notes if enabled
                                if (isAutoPushWorkspaceEnabled) {
                                    val noteTitle = "Gemini: ${promptToSend.take(30)}"
                                    workspaceViewModel?.createNote(
                                        name = noteTitle,
                                        initialText = "PROMPT:\n$promptToSend\n\nGEMINI BLUEPRINT:\n${finalReply.text}"
                                    )
                                    Toast.makeText(context, "Auto-pushed to Workspace Notes!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                if (modelMsgIndex in chatMessages.indices) {
                                    chatMessages[modelMsgIndex] = CreativeMessage(
                                        role = "model",
                                        text = "Creative engine notice: Could not connect to online service (${e.localizedMessage}). Using fallback intelligence.",
                                        isOnline = false
                                    )
                                }
                            } finally {
                                isLoading = false
                                listState.animateScrollToItem(chatMessages.size - 1)
                            }
                        }
                    }
                },
                enabled = (inputText.isNotBlank() || attachedMediaUri != null) && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EchoCyan,
                    contentColor = EchoOnCyan,
                    disabledContainerColor = EchoCardStroke
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("btn_send_gemini")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
            }
        }
    }

    // Modal Bottom Sheet on Long-Pressed Message
    activeLongPressedMessage?.let { targetMsg ->
        val isUserMsg = targetMsg.role == "user"
        val payloadTitle = if (isUserMsg) "User Command" else (targetMsg.text.lines().firstOrNull()?.replace("#", "")?.trim()?.take(30) ?: "Gemini Output")
        ItemActionBottomSheet(
            item = LongPressItemPayload(
                id = targetMsg.id,
                title = payloadTitle,
                textContent = targetMsg.text,
                subtitle = if (isUserMsg) "Your prompt" else targetMsg.modelTag,
                category = if (isUserMsg) "User Prompt" else "Gemini Creative"
            ),
            isSpeaking = currentlySpeakingId == targetMsg.id && isSpeaking,
            toneGenerator = toneGenerator,
            onDismiss = { activeLongPressedMessage = null },
            onSpeakAloud = { speakMessage(targetMsg.id, targetMsg.text) },
            onStopSpeaking = {
                ttsInstance?.stop()
                isSpeaking = false
                currentlySpeakingId = null
            },
            onSaveToNotes = { title, content ->
                workspaceViewModel?.createNote(name = title, initialText = content)
            },
            onRemixWithGemini = { remixPrompt ->
                inputText = remixPrompt
            },
            onExpandToScript = { scriptTitle, scriptPrompt ->
                inputText = scriptPrompt
            },
            onDelete = {
                chatMessages.removeIf { it.id == targetMsg.id }
                Toast.makeText(context, "Removed message", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Telemetry Inspector Modal Dialog
    if (showTelemetryInspectorDialog) {
        TelemetryInspectorDialog(
            promptTokens = latestPromptTokens,
            responseTokens = latestResponseTokens,
            latencyMs = latestLatencyMs,
            tokensPerSec = latestTokensPerSec,
            temperature = temperature,
            topP = topP,
            onDismiss = { showTelemetryInspectorDialog = false }
        )
    }

    // System Instruction & Persona Customizer Dialog
    if (showPersonaSettingsDialog) {
        PersonaCustomizerDialog(
            currentInstruction = customInstructionText,
            temperature = temperature,
            topP = topP,
            onSave = { newInstruction, newTemp, newTopP ->
                customInstructionText = newInstruction
                temperature = newTemp
                topP = newTopP
                selectedPersonaName = "Custom Persona"
                showPersonaSettingsDialog = false
                Toast.makeText(context, "Custom system instructions and hyperparameters saved", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPersonaSettingsDialog = false }
        )
    }

    // Quick AI Image Generation Dialog
    if (showQuickImageDialog) {
        QuickImageGenDialog(
            toneGenerator = toneGenerator,
            onDismiss = { showQuickImageDialog = false },
            onGenerated = { res ->
                showQuickImageDialog = false
                chatMessages.add(
                    CreativeMessage(
                        role = "user",
                        text = "🎨 [AI Image Prompt]: ${res.prompt}",
                        isOnline = true
                    )
                )
                chatMessages.add(
                    CreativeMessage(
                        role = "model",
                        text = "🎨 **Gemini 2.5 Flash Art Synthesized**\n\nPrompt: ${res.prompt}\nStyle: ${res.style}\nAspect Ratio: ${res.aspectRatio}",
                        modelTag = "Gemini 2.5 Flash Image",
                        contentType = "generated_image",
                        mediaUri = res.localUri.toString(),
                        mediaType = "image",
                        visualTags = listOf("Gemini 2.5 Flash", res.style, res.aspectRatio)
                    )
                )
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
                toneGenerator.playChime(SoundEffectType.SUCCESS)
                Toast.makeText(context, "Art generated and saved to Vault!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Quick Veo Video Generation Dialog
    if (showQuickVideoDialog) {
        QuickVideoGenDialog(
            toneGenerator = toneGenerator,
            onDismiss = { showQuickVideoDialog = false },
            onGenerated = { res ->
                showQuickVideoDialog = false
                chatMessages.add(
                    CreativeMessage(
                        role = "user",
                        text = "🎬 [Veo Video Prompt]: ${res.prompt}",
                        isOnline = true
                    )
                )
                chatMessages.add(
                    CreativeMessage(
                        role = "model",
                        text = "🎬 **Veo 3.1 Video Package: ${res.title}**\n\nGenre: ${res.genre} | Aspect Ratio: ${res.aspectRatio}\n\n${res.shots.joinToString("\n") { "• Shot ${it.shotNumber} (${it.durationSec}s): ${it.title} - ${it.cameraMovement}" }}",
                        modelTag = "Veo 3.1 Fast",
                        contentType = "veo_storyboard",
                        mediaUri = res.videoUri?.toString(),
                        mediaType = "video",
                        visualTags = listOf("Veo 3.1 Fast", res.genre, res.aspectRatio)
                    )
                )
                if (res.videoUri != null) {
                    MediaVaultRepository.addItem(
                        VaultMediaItem(
                            title = res.title,
                            subtitle = "Veo Video • ${res.genre} • ${res.aspectRatio}",
                            uri = res.videoUri,
                            type = MediaType.VIDEO,
                            prompt = res.prompt,
                            style = res.genre,
                            isAiGenerated = true,
                            isVeoVideo = true
                        )
                    )
                }
                // Auto-save generated video to Google Drive
                GoogleDriveVideoSyncManager.autoSaveVideoToDrive(
                    context = context,
                    title = res.title,
                    prompt = res.prompt,
                    model = "Veo 3.1 Fast",
                    aspectRatio = res.aspectRatio,
                    durationSec = 6,
                    videoUriString = res.videoUri?.toString()
                )
                toneGenerator.playChime(SoundEffectType.SUCCESS)
                Toast.makeText(context, "Veo Video saved to Vault and Google Drive!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaygroundChatBubble(
    message: CreativeMessage,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit,
    onSaveToNote: (String) -> Unit,
    onFork: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onOpenBrowser: ((String, String) -> Unit)? = null,
    onOpenMedia: ((Uri, MediaType, String) -> Unit)? = null
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                .combinedClickable(
                    onClick = { /* normal tap */ },
                    onLongClick = onLongClick
                )
                .testTag("chat_bubble_${message.id}"),
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
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header Bar inside Bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isUser) Icons.Default.Send else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isUser) EchoCyan else EchoAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isUser) "You" else message.modelTag,
                            color = if (isUser) EchoCyan else EchoAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Telemetry chip on model messages
                        if (!isUser && message.tokensPerSec > 0f) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoDarkSurface)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "⚡ ${"%.1f".format(message.tokensPerSec)} tok/s • ${message.responseTokens} tok",
                                    color = EchoCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Fork branch from this message
                        if (onFork != null) {
                            IconButton(
                                onClick = onFork,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ForkRight,
                                    contentDescription = "Fork Branch",
                                    tint = EchoCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (!isUser) {
                            // Speak / Stop Dictating button
                            IconButton(
                                onClick = onToggleSpeak,
                                modifier = Modifier.size(26.dp).testTag("btn_speak_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop Reading" else "Read Aloud",
                                    tint = if (isSpeaking) EchoRed else EchoCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Copy message text
                            IconButton(
                                onClick = {
                                    NotepadExportManager.copyToClipboard(context, message.text, "Gemini Response")
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EchoMuted, modifier = Modifier.size(14.dp))
                            }

                            // Save to Workspace Notes
                            IconButton(
                                onClick = { onSaveToNote(message.text) },
                                modifier = Modifier.size(26.dp).testTag("btn_save_note_${message.id}")
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", tint = EchoCyan, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Multimodal Attachment Badge inside bubble if message has attached media
                if (message.mediaUri != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoCyan.copy(alpha = 0.12f))
                            .border(0.5.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = if (message.mediaType == "audio") Icons.Default.Audiotrack else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = EchoCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (message.mediaType == "audio") "🎵 Audio Sample Examined" else "🖼️ Visual Exhibit Examined",
                                color = EchoCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Text Content
                Text(
                    text = message.text,
                    color = EchoText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Render Generated AI Image Preview Card
                if (message.contentType == "generated_image" && message.mediaUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = EchoDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onOpenMedia?.invoke(Uri.parse(message.mediaUri), MediaType.PHOTO, message.text)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = Uri.parse(message.mediaUri),
                                    contentDescription = message.text,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                        MediaVaultRepository.addItem(
                                            VaultMediaItem(
                                                title = message.text.lines().firstOrNull()?.take(28) ?: "Gemini Image",
                                                subtitle = "Gemini 2.5 Flash Art",
                                                uri = Uri.parse(message.mediaUri),
                                                type = MediaType.PHOTO,
                                                isAiGenerated = true
                                            )
                                        )
                                        Toast.makeText(context, "Saved to Media Vault!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save to Vault", fontSize = 10.sp, color = EchoCyan, fontWeight = FontWeight.Bold)
                                }
                                if (onOpenMedia != null) {
                                    Button(
                                        onClick = {
                                            HarmonicToneGenerator.SHARED.playChime(SoundEffectType.TAP)
                                            onOpenMedia(Uri.parse(message.mediaUri), MediaType.PHOTO, message.text)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan.copy(alpha = 0.2f), contentColor = EchoCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Fullscreen", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Render Veo Video Director Package Card
                if (message.contentType == "veo_storyboard") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, EchoAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = EchoDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(16.dp))
                                    Text("Veo 3.1 Video Director Package", color = EchoAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Surface(
                                    color = EchoGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, EchoGreen.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(12.dp))
                                        Text("Drive /ATHENA_AI_Videos", color = EchoGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (message.mediaUri != null && onOpenMedia != null) {
                                    Button(
                                        onClick = {
                                            HarmonicToneGenerator.SHARED.playChime(SoundEffectType.TAP)
                                            onOpenMedia(Uri.parse(message.mediaUri), MediaType.VIDEO, "Veo Video Preview")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoAmber, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Play", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                        val uri = if (message.mediaUri != null) Uri.parse(message.mediaUri) else Uri.EMPTY
                                        MediaVaultRepository.addItem(
                                            VaultMediaItem(
                                                title = message.text.lines().firstOrNull()?.take(28) ?: "Veo Video",
                                                subtitle = "Veo 3.1 Storyboard Package",
                                                uri = uri,
                                                type = MediaType.VIDEO,
                                                isAiGenerated = true,
                                                isVeoVideo = true
                                            )
                                        )
                                        Toast.makeText(context, "Saved Veo Video to Vault!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Vault", fontSize = 10.sp, color = EchoAmber, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        HarmonicToneGenerator.SHARED.playChime(SoundEffectType.SUCCESS)
                                        val driveItem = DriveVideoItem(
                                            id = "drive_vid_${System.currentTimeMillis()}",
                                            title = message.text.lines().firstOrNull()?.take(28) ?: "Veo Video",
                                            prompt = message.text,
                                            model = "Veo 3.1 Fast",
                                            aspectRatio = "16:9",
                                            durationSec = 6,
                                            videoUriString = message.mediaUri
                                        )
                                        val shareIntent = GoogleDriveVideoSyncManager.createExportToDriveIntent(context, driveItem)
                                        context.startActivity(shareIntent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("To Drive", fontSize = 10.sp, color = EchoGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Visual & Production Concept Tags
                if (message.visualTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(message.visualTags) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoCyan.copy(alpha = 0.12f))
                                    .border(0.5.dp, EchoCyan.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tag, color = EchoCyan, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (!isUser && onOpenBrowser != null && (message.contentType == "image_concept" || message.contentType == "video_script")) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                val query = message.text.lines().firstOrNull()?.replace("#", "")?.trim()?.take(25) ?: "visual concept"
                                onOpenBrowser("https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}", "Visual Web Explorer")
                            },
                            label = { Text("🌐 Search Visuals in Dola Browser", fontSize = 10.sp, color = EchoCyan) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                        )
                    }
                }

                // Grounding Search Sources
                if (message.searchSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sources & References:",
                        color = EchoMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    message.searchSources.take(3).forEach { source ->
                        Text(
                            text = "• $source",
                            color = EchoCyan,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data class representing system instruction persona presets.
 */
data class PersonaPreset(
    val name: String,
    val icon: String,
    val instruction: String,
    val defaultTemp: Float = 0.7f
)

/**
 * Utility to encode image or audio Uri into Base64 inlineData.
 */
fun getBase64FromUri(context: Context, uri: Uri): Pair<String, String>? {
    return try {
        val mime = context.contentResolver.getType(uri)
            ?: if (uri.toString().contains("audio")) "audio/mpeg" else "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        Pair(b64, mime)
    } catch (e: Exception) {
        null
    }
}

/**
 * Dialog displaying live Gemini token counts, round-trip latency, speed, and inference configuration.
 */
@Composable
fun TelemetryInspectorDialog(
    promptTokens: Int,
    responseTokens: Int,
    latencyMs: Long,
    tokensPerSec: Float,
    temperature: Float,
    topP: Float,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                Text("GEMINI TELEMETRY & TOKEN INSPECTOR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoBgAlt)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TelemetryRow("Active Model Engine", "gemini-3.5-flash (SSE Streaming)")
                        TelemetryRow("Generation Speed", "${"%.1f".format(tokensPerSec)} tokens / sec")
                        TelemetryRow("Inference Round-Trip", "$latencyMs ms")
                        TelemetryRow("Estimated Prompt Tokens", "$promptTokens tokens")
                        TelemetryRow("Generated Candidate Tokens", "$responseTokens tokens")
                        TelemetryRow("Total Context Consumed", "${promptTokens + responseTokens} tokens")
                        TelemetryRow("Sampling Temperature", "%.2f".format(temperature))
                        TelemetryRow("Top-P Nucleus Parameter", "%.2f".format(topP))
                        TelemetryRow("Max Context Capacity", "1,048,576 tokens")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = EchoCyan, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 10.sp, color = EchoMuted)
        Text(text = value, fontSize = 10.sp, color = EchoText, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

/**
 * Dialog for editing custom system instructions and fine-tuning temperature and top-p.
 */
@Composable
fun PersonaCustomizerDialog(
    currentInstruction: String,
    temperature: Float,
    topP: Float,
    onSave: (String, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var instructionText by remember { mutableStateOf(currentInstruction) }
    var tempVal by remember { mutableFloatStateOf(temperature) }
    var topPVal by remember { mutableFloatStateOf(topP) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                Text("SYSTEM INSTRUCTION & PERSONA PROFILES", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Tune how Gemini perceives queries, structures reasoning, and balances creativity:",
                    fontSize = 11.sp,
                    color = EchoMuted
                )

                OutlinedTextField(
                    value = instructionText,
                    onValueChange = { instructionText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    label = { Text("System Instructions", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Temperature: ${"%.2f".format(tempVal)}", fontSize = 10.sp, color = EchoText)
                        Text(if (tempVal < 0.4f) "Precise / STEM" else if (tempVal < 0.8f) "Balanced" else "Creative / Visionary", fontSize = 10.sp, color = EchoCyan)
                    }
                    Slider(
                        value = tempVal,
                        onValueChange = { tempVal = it },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = EchoCyan, activeTrackColor = EchoCyan)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Top-P Nucleus: ${"%.2f".format(topPVal)}", fontSize = 10.sp, color = EchoText)
                        Text("Sampling Diversity", fontSize = 10.sp, color = EchoMuted)
                    }
                    Slider(
                        value = topPVal,
                        onValueChange = { topPVal = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = EchoCyan, activeTrackColor = EchoCyan)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(instructionText, tempVal, topPVal) },
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
            ) {
                Text("Apply & Save", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EchoMuted)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun QuickImageGenDialog(
    toneGenerator: HarmonicToneGenerator,
    onDismiss: () -> Unit,
    onGenerated: (com.example.ai.GeneratedImageResult) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf(GeminiMediaGenerationService.IMAGE_STYLES.first()) }
    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(22.dp))
                Text("🎨 Gemini 2.5 Flash Art Studio", color = EchoText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Describe the image to synthesize...", color = EchoMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_quick_image_prompt"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            if (prompt.isNotBlank() && !isEnhancing) {
                                isEnhancing = true
                                coroutineScope.launch {
                                    val enhanced = GeminiMediaGenerationService.enhancePrompt(context, prompt, "image")
                                    prompt = enhanced
                                    isEnhancing = false
                                }
                            }
                        },
                        enabled = prompt.isNotBlank() && !isEnhancing,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (isEnhancing) "Enhancing..." else "✨ Enhance Prompt", fontSize = 10.sp, color = EchoCyan)
                    }
                }

                Text("Aspect Ratio", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(GeminiMediaGenerationService.ASPECT_RATIOS) { (ratio, label) ->
                        val isSelected = selectedAspectRatio == ratio
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAspectRatio = ratio },
                            label = { Text(ratio, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }

                Text("Visual Style", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(GeminiMediaGenerationService.IMAGE_STYLES) { style ->
                        val isSelected = selectedStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStyle = style },
                            label = { Text(style.take(18), fontSize = 10.sp) },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val promptToUse = prompt.trim().ifEmpty { "Cosmic landscape with nebulous aurora and chrome structures" }
                    if (!isGenerating) {
                        isGenerating = true
                        coroutineScope.launch {
                            val res = GeminiMediaGenerationService.generateImage(context, promptToUse, selectedAspectRatio, selectedStyle)
                            isGenerating = false
                            res.onSuccess {
                                onGenerated(it)
                            }.onFailure {
                                Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                modifier = Modifier.testTag("btn_confirm_quick_image_gen")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = EchoOnCyan, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Synthesizing...", fontSize = 11.sp)
                } else {
                    Text("Generate Art", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGenerating) {
                Text("Cancel", color = EchoMuted)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun QuickVideoGenDialog(
    toneGenerator: HarmonicToneGenerator,
    onDismiss: () -> Unit,
    onGenerated: (com.example.ai.VeoVideoResult) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(GeminiMediaGenerationService.VIDEO_GENRES.first()) }
    var selectedCameraMove by remember { mutableStateOf(GeminiMediaGenerationService.CAMERA_MOVEMENTS.first()) }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(22.dp))
                Text("🎬 Veo 3.1 Video Studio", color = EchoText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Describe the cinematic video concept...", color = EchoMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_quick_video_prompt"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            if (prompt.isNotBlank() && !isEnhancing) {
                                isEnhancing = true
                                coroutineScope.launch {
                                    val enhanced = GeminiMediaGenerationService.enhancePrompt(context, prompt, "video")
                                    prompt = enhanced
                                    isEnhancing = false
                                }
                            }
                        },
                        enabled = prompt.isNotBlank() && !isEnhancing,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (isEnhancing) "Enhancing..." else "✨ Enhance Scene", fontSize = 10.sp, color = EchoAmber)
                    }
                }

                Text("Cinematic Genre", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(GeminiMediaGenerationService.VIDEO_GENRES) { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = EchoBgAlt,
                                labelColor = EchoText
                            )
                        )
                    }
                }

                Text("Camera Dynamics", color = EchoMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(GeminiMediaGenerationService.CAMERA_MOVEMENTS) { move ->
                        val isSelected = selectedCameraMove == move
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCameraMove = move },
                            label = { Text(move.take(18), fontSize = 10.sp) },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val promptToUse = prompt.trim().ifEmpty { "Interstellar hyperspace corridor opening above Jupiter" }
                    if (!isGenerating) {
                        isGenerating = true
                        coroutineScope.launch {
                            val res = GeminiMediaGenerationService.generateVeoVideo(
                                context,
                                promptToUse,
                                selectedAspectRatio,
                                "1080p",
                                selectedGenre,
                                selectedCameraMove
                            )
                            isGenerating = false
                            res.onSuccess {
                                onGenerated(it)
                            }.onFailure {
                                Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = EchoAmber, contentColor = Color.Black),
                modifier = Modifier.testTag("btn_confirm_quick_video_gen")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directing...", fontSize = 11.sp)
                } else {
                    Text("Generate Video", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGenerating) {
                Text("Cancel", color = EchoMuted)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(14.dp)
    )
}


