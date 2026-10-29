package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Dola In-App Browser Dialog:
 * Ultra-lightweight, native web modal built with Android WebView.
 * Features an integrated Gemini AI Copilot drawer for live research, webpage summarization,
 * creative video scriptwriting, visual prompt generation, and voice dictation via Android TTS.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DolaBrowserDialog(
    initialUrl: String,
    title: String = "Web Exploration",
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    workspaceViewModel: WorkspaceViewModel? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf(title) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Gemini Copilot Drawer State
    var showCopilotDrawer by remember { mutableStateOf(false) }
    var copilotPrompt by remember { mutableStateOf("") }
    var isCopilotThinking by remember { mutableStateOf(false) }
    var copilotResponse by remember { mutableStateOf<String?>(null) }
    var isAutoDictateEnabled by remember { mutableStateOf(true) }

    // Text To Speech
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.0f)
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

    fun cleanTextForSpeech(raw: String): String {
        return raw.replace(Regex("[\"\'#*_`~\\[\\]]"), "")
            .replace(Regex("\\bhttps?://\\S+"), "web link")
            .replace(Regex("\n+"), ". ")
            .trim()
    }

    fun speakText(text: String) {
        ttsInstance?.let { tts ->
            tts.stop()
            val clean = cleanTextForSpeech(text)
            isSpeaking = true
            tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "copilot_speech")
        }
    }

    fun stopSpeaking() {
        ttsInstance?.stop()
        isSpeaking = false
    }

    fun executeCopilotCommand(prompt: String) {
        if (isCopilotThinking) return
        toneGenerator.playChime(SoundEffectType.TAP)
        isCopilotThinking = true
        showCopilotDrawer = true

        coroutineScope.launch {
            try {
                val fullContextPrompt = """
                    Current Webpage Title: $pageTitle
                    Current Webpage URL: $currentUrl
                    
                    USER REQUEST:
                    $prompt
                """.trimIndent()

                val result = CreativeHubAiService.askAnything(
                    context = context,
                    prompt = fullContextPrompt,
                    model = "gemini-3.5-flash",
                    enableGoogleSearch = true,
                    creativeTone = "Visionary & Entertainment Focused"
                )
                copilotResponse = result.text
                toneGenerator.playChime(SoundEffectType.SUCCESS)

                if (isAutoDictateEnabled && isTtsReady) {
                    speakText(result.text)
                }
            } catch (e: Exception) {
                copilotResponse = "Copilot update: Could not retrieve online insight (${e.localizedMessage})."
            } finally {
                isCopilotThinking = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 18.dp)
                .testTag("dola_browser_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EchoBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Browser Navigation & Address Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back / Forward controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        if (webViewInstance?.canGoBack() == true) {
                                            webViewInstance?.goBack()
                                        }
                                    },
                                    enabled = canGoBack,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = if (canGoBack) EchoCyan else EchoMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        if (webViewInstance?.canGoForward() == true) {
                                            webViewInstance?.goForward()
                                        }
                                    },
                                    enabled = canGoForward,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Forward",
                                        tint = if (canGoForward) EchoCyan else EchoMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        webViewInstance?.reload()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Reload",
                                        tint = EchoCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Address Pill Display
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoCard)
                                    .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Secure",
                                        tint = EchoGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = currentUrl.take(40),
                                        color = EchoText,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Action Menu (Gemini Copilot, Copy, Share, Close)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // ⚡ Gemini AI Copilot Trigger
                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        showCopilotDrawer = !showCopilotDrawer
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .testTag("btn_dola_gemini_copilot")
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini Copilot",
                                        tint = if (showCopilotDrawer) EchoAmber else EchoCyan,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("URL", currentUrl))
                                        Toast.makeText(context, "URL Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = EchoMuted, modifier = Modifier.size(14.dp))
                                }

                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Unable to launch external browser", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "Open External", tint = EchoCyan, modifier = Modifier.size(14.dp))
                                }

                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        stopSpeaking()
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Web Loading Progress Bar
                        if (isLoading && pageProgress < 1.0f) {
                            LinearProgressIndicator(
                                progress = { pageProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = EchoCyan,
                                trackColor = Color.Transparent
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }

                // Native WebView Embedded Canvas
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (showCopilotDrawer) 0.55f else 1f),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    url?.let { currentUrl = it }
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    url?.let { currentUrl = it }
                                    pageTitle = view?.title ?: title
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val reqUrl = request?.url?.toString() ?: return false
                                    if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                                        return false
                                    }
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                                        context.startActivity(intent)
                                        true
                                    } catch (e: Exception) {
                                        true
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress / 100f
                                    if (newProgress >= 100) isLoading = false
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }

                                override fun onReceivedTitle(view: WebView?, t: String?) {
                                    super.onReceivedTitle(view, t)
                                    if (!t.isNullOrBlank()) pageTitle = t
                                }
                            }

                            webViewInstance = this
                            loadUrl(initialUrl)
                        }
                    },
                    update = { view ->
                        webViewInstance = view
                    }
                )

                // ⚡ Embedded Gemini AI Copilot Drawer (Creative Entertainment & Production Controller)
                AnimatedVisibility(visible = showCopilotDrawer) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .testTag("dola_copilot_drawer"),
                        colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Copilot Header
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
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = EchoCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Gemini Copilot",
                                        color = EchoCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "• Live Web Inspector",
                                        color = EchoMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Auto Dictate Switch
                                    Icon(
                                        imageVector = if (isAutoDictateEnabled) Icons.Default.RecordVoiceOver else Icons.Default.VolumeOff,
                                        contentDescription = null,
                                        tint = if (isAutoDictateEnabled) EchoCyan else EchoMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Switch(
                                        checked = isAutoDictateEnabled,
                                        onCheckedChange = {
                                            isAutoDictateEnabled = it
                                            if (!it) stopSpeaking()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EchoCyan,
                                            checkedTrackColor = EchoCyan.copy(alpha = 0.3f),
                                            uncheckedThumbColor = EchoMuted,
                                            uncheckedTrackColor = EchoBgAlt
                                        ),
                                        modifier = Modifier.size(width = 34.dp, height = 20.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            stopSpeaking()
                                            showCopilotDrawer = false
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Copilot", tint = EchoMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Quick Tool Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            executeCopilotCommand("Summarize this webpage thoroughly. Highlight the main concepts, standout facts, and artistic or entertainment potential.")
                                        },
                                        label = { Text("💡 Summarize Page", fontSize = 10.sp, color = EchoText) },
                                        colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            executeCopilotCommand("Generate a viral 60-second video script based on this topic. Include a 3-second opening hook, high-retention premise, visual camera cues, and background soundtrack style.")
                                        },
                                        label = { Text("🎬 Video Script Hook", fontSize = 10.sp, color = EchoText) },
                                        colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            executeCopilotCommand("Design 3 cinematic image generation prompts inspired by this content: specify camera lens, dynamic lighting, color palette, and visual composition.")
                                        },
                                        label = { Text("🎨 Image Art Prompts", fontSize = 10.sp, color = EchoText) },
                                        colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            executeCopilotCommand("Create a complete entertainment production blueprint inspired by this page. Format with scene ideas, storyline arc, and actionable next steps.")
                                        },
                                        label = { Text("⚡ Production Blueprint", fontSize = 10.sp, color = EchoText) },
                                        colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                                    )
                                }
                            }

                            // Copilot Output Bubble
                            if (isCopilotThinking) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(color = EchoCyan, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini is analyzing webpage content...", color = EchoCyan, fontSize = 11.sp)
                                }
                            } else if (copilotResponse != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Analysis & Blueprint", color = EchoAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Read Aloud / Stop button
                                                IconButton(
                                                    onClick = {
                                                        if (isSpeaking) stopSpeaking() else speakText(copilotResponse ?: "")
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                        contentDescription = if (isSpeaking) "Stop" else "Speak",
                                                        tint = if (isSpeaking) EchoRed else EchoCyan,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                // Copy Button
                                                IconButton(
                                                    onClick = {
                                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        cm.setPrimaryClip(ClipData.newPlainText("Copilot Analysis", copilotResponse ?: ""))
                                                        Toast.makeText(context, "Copied analysis!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EchoMuted, modifier = Modifier.size(13.dp))
                                                }

                                                // Save to Workspace Notes
                                                IconButton(
                                                    onClick = {
                                                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                        val noteTitle = "Web: ${pageTitle.take(30)}"
                                                        workspaceViewModel?.createNote(
                                                            name = noteTitle,
                                                            initialText = "PAGE: $pageTitle\nURL: $currentUrl\n\nGEMINI INSIGHTS:\n${copilotResponse ?: ""}",
                                                            tags = "WebResearch, GeminiCopilot"
                                                        )
                                                        Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp).testTag("btn_save_copilot_note")
                                                ) {
                                                    Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", tint = EchoCyan, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = copilotResponse ?: "",
                                            color = EchoText,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }

                            // Custom Prompt Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = copilotPrompt,
                                    onValueChange = { copilotPrompt = it },
                                    modifier = Modifier.weight(1f).testTag("input_dola_copilot"),
                                    placeholder = { Text("Ask Gemini about this webpage...", color = EchoMuted, fontSize = 11.sp) },
                                    maxLines = 2,
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

                                Button(
                                    onClick = {
                                        val p = copilotPrompt.trim()
                                        if (p.isNotBlank()) {
                                            copilotPrompt = ""
                                            executeCopilotCommand(p)
                                        }
                                    },
                                    enabled = copilotPrompt.isNotBlank() && !isCopilotThinking,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EchoCyan,
                                        contentColor = EchoOnCyan,
                                        disabledContainerColor = EchoCardStroke
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
