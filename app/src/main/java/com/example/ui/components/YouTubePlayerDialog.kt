package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.VideoPlayerPreferencesManager
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

/**
 * Dedicated In-App YouTube & HTML5 Video Player Modal.
 *
 * Interactive Fullscreen & Rotation Features:
 * 1. Fully interactive fullscreen mode where YouTube/HTML5 iframe controls (play, pause, timeline,
 *    scrubber, CC, quality) receive direct touch interactions without blocking parent click listeners.
 * 2. Double-tap gesture directly toggles between portrait (0°) and hardware-accelerated 90° landscape rotation.
 * 3. Settings Preferences integration: respects user toggle for double-tap rotation, default orientation,
 *    and gesture hint badges.
 * 4. Clear input affordances with subtle icon pills and gesture labels:
 *    - ⛶ Press: Full Screen / Exit Full Screen
 *    - 🔄 Press / Double-Tap: Landscape 90°
 *    - 👆 HTML5 Controls Active badge
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerDialog(
    videoId: String,
    videoTitle: String,
    channelName: String = "",
    playlistId: String? = null,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onSaveToWorkspaceNote: ((String, String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    remember(context) { VideoPlayerPreferencesManager.initialize(context) }

    val doubleTapRotateEnabled by VideoPlayerPreferencesManager.doubleTapRotateEnabled.collectAsState()
    val defaultOrientationDegrees by VideoPlayerPreferencesManager.defaultOrientationDegrees.collectAsState()
    val gestureHintBadgesVisible by VideoPlayerPreferencesManager.gestureHintBadgesVisible.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }
    var rotationDegrees by remember(defaultOrientationDegrees) { mutableFloatStateOf(defaultOrientationDegrees) }
    var showOverlayControls by remember { mutableStateOf(true) }

    val isPlaylistMode = !playlistId.isNullOrBlank()
    val youtubeWatchUrl = if (isPlaylistMode) {
        "https://www.youtube.com/playlist?list=$playlistId"
    } else {
        "https://www.youtube.com/watch?v=$videoId"
    }

    val embedHtml = remember(videoId, playlistId) {
        val embedUrl = if (isPlaylistMode) {
            "https://www.youtube.com/embed/videoseries?list=$playlistId&autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1"
        } else {
            "https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1"
        }
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { background-color: #000000; display: flex; align-items: center; justify-content: center; height: 100vh; width: 100vw; overflow: hidden; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <iframe
                src="$embedUrl"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """.trimIndent()
    }

    fun toggleRotation() {
        toneGenerator.playChime(SoundEffectType.TAP)
        rotationDegrees = if (rotationDegrees == 0f) 90f else (rotationDegrees + 90f) % 360f
    }

    fun openInNativeApp() {
        toneGenerator.playChime(SoundEffectType.TAP)
        val appUri = if (isPlaylistMode) {
            Uri.parse("vnd.youtube:playlist/$playlistId")
        } else {
            Uri.parse("vnd.youtube:$videoId")
        }
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeWatchUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.loadUrl("about:blank")
                webViewRef?.stopLoading()
                webViewRef?.destroy()
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        if (isFullscreen) {
            // =========================================================================
            // IMMERSIVE FULLSCREEN MODE (Interactive WebView with Double-Tap Rotation)
            // =========================================================================
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .testTag("youtube_fullscreen_surface"),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Fully Interactive Rotatable Video Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = rotationDegrees
                                if (rotationDegrees == 90f || rotationDegrees == 270f) {
                                    val aspect = size.width / size.height.coerceAtLeast(1f)
                                    scaleX = 1f / aspect.coerceAtLeast(0.7f)
                                    scaleY = 1f / aspect.coerceAtLeast(0.7f)
                                } else {
                                    scaleX = 1.0f
                                    scaleY = 1.0f
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Direct interactive HTML5 player view - touch events reach WebView smoothly
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.cacheMode = WebSettings.LOAD_DEFAULT

                                    webChromeClient = WebChromeClient()
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            isLoading = true
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            isLoading = false
                                        }
                                    }
                                    loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
                                    webViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = EchoCyan,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    // Floating Double-Tap Gesture Sensor Pill (Non-blocking edge tap areas)
                    if (doubleTapRotateEnabled) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(width = 64.dp, height = 120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            toggleRotation()
                                            Toast.makeText(
                                                context,
                                                "Landscape Toggle: ${rotationDegrees.toInt()}°",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onTap = {
                                            showOverlayControls = !showOverlayControls
                                        }
                                    )
                                }
                                .testTag("fullscreen_gesture_detector_strip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ScreenRotation,
                                    contentDescription = "Double-Tap to Rotate",
                                    tint = EchoCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "2x TAP\nROTATE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }

                    // Floating Top Overlay Controls Bar (Auto-Hidable with explicit gesture labels)
                    AnimatedVisibility(
                        visible = showOverlayControls,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (isPlaylistMode) Icons.Default.PlaylistPlay else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = EchoRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = videoTitle.ifBlank { "YouTube Fullscreen" },
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Orientation: ${rotationDegrees.toInt()}°",
                                            color = EchoCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (gestureHintBadgesVisible) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(EchoGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(10.dp))
                                                    Text("HTML5 Interactive", color = EchoGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 90° Rotate Button with gesture label
                                Button(
                                    onClick = { toggleRotation() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (rotationDegrees != 0f) EchoGreen else EchoDarkSurface,
                                        contentColor = if (rotationDegrees != 0f) EchoOnCyan else EchoCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_rotate_youtube_fullscreen")
                                ) {
                                    Icon(Icons.Default.CropRotate, contentDescription = "Rotate", modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${rotationDegrees.toInt()}°",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Exit Fullscreen Button
                                Button(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        isFullscreen = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EchoDarkSurface,
                                        contentColor = EchoCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_exit_youtube_fullscreen")
                                ) {
                                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Exit", fontSize = 11.sp)
                                }

                                // Close Dialog Button
                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        onDismiss()
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Floating Bottom Overlay Controls
                    AnimatedVisibility(
                        visible = showOverlayControls,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { openInNativeApp() },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open in YouTube App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (onSaveToWorkspaceNote != null) {
                                OutlinedButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                                        onSaveToWorkspaceNote(videoTitle, youtubeWatchUrl)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                                ) {
                                    Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Save Note", fontSize = 11.sp)
                                }
                            }

                            // Quick Toggle Bar Visibility Button
                            IconButton(
                                onClick = { showOverlayControls = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Hide Bar", tint = EchoMuted)
                            }
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // STANDARD MODAL DIALOG MODE (Framed Card with Controls, Rotate & Fullscreen)
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EchoCyan, RoundedCornerShape(16.dp))
                    .testTag("youtube_player_dialog"),
                colors = CardDefaults.cardColors(containerColor = EchoBg)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Bar with Title, Rotate, Fullscreen, and Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaylistMode) Icons.Default.PlaylistPlay else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = videoTitle.ifBlank { if (isPlaylistMode) "YouTube Playlist" else "YouTube Video" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isPlaylistMode) "YouTube Playlist • In-App HTML5 Player" else channelName.ifBlank { "YouTube Stream" },
                                    fontSize = 11.sp,
                                    color = EchoCyan
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 90° Video Rotate Toggle with explicit input label
                            Button(
                                onClick = { toggleRotation() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (rotationDegrees != 0f) EchoGreen else EchoDarkSurface,
                                    contentColor = if (rotationDegrees != 0f) EchoOnCyan else EchoCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("rotate_youtube_button")
                            ) {
                                Icon(Icons.Default.CropRotate, contentDescription = "Rotate 90°", modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("${rotationDegrees.toInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Fullscreen Toggle Button with explicit input label
                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    isFullscreen = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoDarkSurface,
                                    contentColor = EchoCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("fullscreen_youtube_button")
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("Full", fontSize = 11.sp)
                            }

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_youtube_player_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Player", tint = EchoText)
                            }
                        }
                    }

                    // Interactive Gesture & Input Guide Pill
                    if (gestureHintBadgesVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoBgAlt)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                                Text("Interactive HTML5 Controls Active", fontSize = 10.sp, color = EchoGreen, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(12.dp))
                                Text("Double-tap or Press to Rotate", fontSize = 10.sp, color = EchoMuted)
                            }
                        }
                    }

                    HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                    // Video Player Container with 90° Rotation Transform and Double-Tap Detection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp))
                            .pointerInput(doubleTapRotateEnabled) {
                                if (doubleTapRotateEnabled) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            toggleRotation()
                                            Toast.makeText(
                                                context,
                                                "Landscape Toggle: ${rotationDegrees.toInt()}°",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = rotationDegrees
                                    if (rotationDegrees == 90f || rotationDegrees == 270f) {
                                        val aspect = size.width / size.height.coerceAtLeast(1f)
                                        scaleX = 1f / aspect.coerceAtLeast(0.7f)
                                        scaleY = 1f / aspect.coerceAtLeast(0.7f)
                                    } else {
                                        scaleX = 1.0f
                                        scaleY = 1.0f
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.mediaPlaybackRequiresUserGesture = false
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        settings.cacheMode = WebSettings.LOAD_DEFAULT

                                        webChromeClient = WebChromeClient()
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                isLoading = true
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isLoading = false
                                            }
                                        }
                                        loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
                                        webViewRef = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = EchoCyan,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy Link Button
                        OutlinedButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("YouTube URL", youtubeWatchUrl))
                                Toast.makeText(context, "YouTube URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(40.dp).testTag("copy_youtube_url_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoText)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Link", fontSize = 11.sp)
                        }

                        // Open in Native YouTube App
                        Button(
                            onClick = { openInNativeApp() },
                            modifier = Modifier.weight(1.3f).height(40.dp).testTag("open_in_youtube_app_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Watch on YouTube App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Save to Workspace Note Button if Callback provided
                    if (onSaveToWorkspaceNote != null) {
                        OutlinedButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                onSaveToWorkspaceNote(videoTitle, youtubeWatchUrl)
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("save_yt_to_workspace_note"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save to Athena Workspace Notes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
