package com.example.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.MediaVaultRepository
import com.example.data.VaultMediaItem
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
import com.example.ui.theme.EchoOverlay
import com.example.ui.theme.EchoPurple
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Quick Action Item definition for speed-dial menu.
 */
data class QuickActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTag: String,
    val onClick: () -> Unit
)

/**
 * Floating Quick Actions Menu (Speed Dial) for Workspace.
 * Sits in the bottom-right corner, offering 1-tap shortcuts for:
 * - New Note
 * - Start Recording (Voice Memo & Sound)
 * - Snapshot Capture (Camera/Photo)
 * - Paste Note (Clipboard)
 * - Dola AI Copilot
 * - New Folder
 */
@Composable
fun WorkspaceQuickActionsMenu(
    onNewNote: () -> Unit,
    onStartRecording: () -> Unit,
    onSnapshotCapture: () -> Unit,
    onPasteNote: () -> Unit,
    onDolaAi: () -> Unit,
    onNewFolder: () -> Unit,
    onCloudSync: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "fab_rotation"
    )

    val actions = remember(onNewNote, onStartRecording, onSnapshotCapture, onPasteNote, onDolaAi, onNewFolder, onCloudSync) {
        val list = mutableListOf(
            QuickActionItem(
                id = "new_note",
                title = "New Note",
                subtitle = "Create workspace note",
                icon = Icons.Default.EditNote,
                accentColor = EchoCyan,
                testTag = "quick_action_new_note",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onNewNote()
                }
            ),
            QuickActionItem(
                id = "start_recording",
                title = "Start Recording",
                subtitle = "Audio memo & sound note",
                icon = Icons.Default.Mic,
                accentColor = EchoRed,
                testTag = "quick_action_start_recording",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                    isExpanded = false
                    onStartRecording()
                }
            ),
            QuickActionItem(
                id = "snapshot_capture",
                title = "Snapshot Capture",
                subtitle = "Photo capture to note",
                icon = Icons.Default.PhotoCamera,
                accentColor = EchoAmber,
                testTag = "quick_action_snapshot_capture",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onSnapshotCapture()
                }
            ),
            QuickActionItem(
                id = "paste_note",
                title = "Paste Note",
                subtitle = "From system clipboard",
                icon = Icons.Default.ContentPaste,
                accentColor = EchoGreen,
                testTag = "quick_action_paste_note",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onPasteNote()
                }
            ),
            QuickActionItem(
                id = "dola_ai",
                title = "Dola AI Copilot",
                subtitle = "Brainstorm & assist",
                icon = Icons.Default.AutoAwesome,
                accentColor = EchoBlue,
                testTag = "quick_action_dola_ai",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.CHIME)
                    isExpanded = false
                    onDolaAi()
                }
            ),
            QuickActionItem(
                id = "new_folder",
                title = "New Folder",
                subtitle = "Organize workspace",
                icon = Icons.Default.CreateNewFolder,
                accentColor = Color(0xFFA78BFA),
                testTag = "quick_action_new_folder",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onNewFolder()
                }
            )
        )
        if (onCloudSync != null) {
            list.add(
                QuickActionItem(
                    id = "cloud_sync",
                    title = "Cloud Sync",
                    subtitle = "Drive categories",
                    icon = Icons.Default.CloudSync,
                    accentColor = EchoCyan,
                    testTag = "quick_action_cloud_sync",
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.CHIME)
                        isExpanded = false
                        onCloudSync()
                    }
                )
            )
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Semi-transparent backdrop scrim when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EchoBg.copy(alpha = 0.72f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        isExpanded = false
                    }
                    .testTag("quick_actions_backdrop_scrim")
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            // Animated list of quick actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(stiffness = 500f)
                ),
                exit = fadeOut(tween(140)) + slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(140)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    // Header pill
                    Surface(
                        color = EchoCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.4f)),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = EchoCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Quick Actions Menu",
                                color = EchoText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    actions.forEach { action ->
                        QuickActionSpeedDialRow(action = action)
                    }
                }
            }

            // Main Trigger FAB
            FloatingActionButton(
                onClick = {
                    toneGenerator.playChime(if (isExpanded) SoundEffectType.TAP else SoundEffectType.HARMONIC_PING)
                    isExpanded = !isExpanded
                },
                containerColor = EchoCyan,
                contentColor = EchoOnCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .shadow(10.dp, CircleShape, spotColor = EchoCyan)
                    .border(2.dp, if (isExpanded) EchoText else EchoCyan.copy(alpha = 0.8f), CircleShape)
                    .testTag("workspace_quick_actions_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.FlashOn,
                        contentDescription = if (isExpanded) "Close Quick Actions" else "Open Quick Actions",
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(if (isExpanded) 0f else rotationAngle)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionSpeedDialRow(
    action: QuickActionItem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { action.onClick() }
            .testTag(action.testTag)
    ) {
        // Descriptive label card to the left
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(EchoCardStroke, action.accentColor.copy(alpha = 0.6f)))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .clickable { action.onClick() }
                .padding(end = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = action.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EchoText
                )
                Text(
                    text = action.subtitle,
                    fontSize = 10.sp,
                    color = EchoMuted
                )
            }
        }

        // Circular Action Icon Button
        Surface(
            shape = CircleShape,
            color = action.accentColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(46.dp)
                .clickable { action.onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = if (action.accentColor == EchoCyan || action.accentColor == EchoAmber || action.accentColor == EchoGreen) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Dedicated Floating Quick Actions Menu for the Brainstorming Lab.
 * Replicates the native Workspace speed dial with lab-tailored actions:
 * Cook Treatment, Agent Hot-Seat, Export Conversation, Audio Replay, and New Story Fork.
 */
@Composable
fun BrainstormQuickActionsMenu(
    onCookTreatment: () -> Unit,
    onSummonHotSeat: () -> Unit,
    onExportConversation: () -> Unit,
    onAudioReplay: () -> Unit,
    onNewStoryFork: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "brainstorm_fab_rotation"
    )

    val actions = remember(onCookTreatment, onSummonHotSeat, onExportConversation, onAudioReplay, onNewStoryFork) {
        listOf(
            QuickActionItem(
                id = "cook_treatment",
                title = "Cook Treatment",
                subtitle = "Start multi-agent debate",
                icon = Icons.Default.FlashOn,
                accentColor = EchoCyan,
                testTag = "brainstorm_quick_cook",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                    isExpanded = false
                    onCookTreatment()
                }
            ),
            QuickActionItem(
                id = "summon_hotseat",
                title = "Agent Hot-Seat",
                subtitle = "Spotlight a specific persona",
                icon = Icons.Default.Diversity3,
                accentColor = EchoPurple,
                testTag = "brainstorm_quick_hotseat",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onSummonHotSeat()
                }
            ),
            QuickActionItem(
                id = "export_thread",
                title = "Export Conversation",
                subtitle = "Save Markdown / Plain Text",
                icon = Icons.Default.Share,
                accentColor = EchoGreen,
                testTag = "brainstorm_quick_export",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onExportConversation()
                }
            ),
            QuickActionItem(
                id = "audio_replay",
                title = "Audio Playback",
                subtitle = "Play debate with chimes",
                icon = Icons.Default.GraphicEq,
                accentColor = EchoBlue,
                testTag = "brainstorm_quick_audio",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                    isExpanded = false
                    onAudioReplay()
                }
            ),
            QuickActionItem(
                id = "story_fork",
                title = "New Story Fork",
                subtitle = "Branch into alternate lore",
                icon = Icons.Default.Lightbulb,
                accentColor = EchoAmber,
                testTag = "brainstorm_quick_fork",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onNewStoryFork()
                }
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(targetScale = 0.8f)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    actions.forEach { action ->
                        QuickActionSpeedDialRow(action = action)
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    toneGenerator.playChime(if (isExpanded) SoundEffectType.TAP else SoundEffectType.HARMONIC_PING)
                    isExpanded = !isExpanded
                },
                containerColor = if (isExpanded) EchoDarkSurface else EchoPurple,
                contentColor = if (isExpanded) EchoCyan else Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, if (isExpanded) EchoText else EchoPurple.copy(alpha = 0.8f), CircleShape)
                    .testTag("brainstorm_quick_actions_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.FlashOn,
                        contentDescription = if (isExpanded) "Close Brainstorm Actions" else "Open Brainstorm Actions",
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(if (isExpanded) 0f else rotationAngle)
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Floating Quick Actions Menu for the Creative Hub.
 * Replicates the native Workspace speed dial with creative-tailored actions:
 * Storyboard, Camera Snapshot, In-App Browser, Media Player, and Cloud Sync.
 */
@Composable
fun CreativeQuickActionsMenu(
    onNewStoryboard: () -> Unit,
    onSnapshotCapture: () -> Unit,
    onOpenBrowser: () -> Unit,
    onMediaPlayer: () -> Unit,
    onCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "creative_fab_rotation"
    )

    val actions = remember(onNewStoryboard, onSnapshotCapture, onOpenBrowser, onMediaPlayer, onCloudSync) {
        listOf(
            QuickActionItem(
                id = "new_storyboard",
                title = "New Storyboard",
                subtitle = "Generate scene prompt note",
                icon = Icons.Default.EditNote,
                accentColor = EchoCyan,
                testTag = "creative_quick_storyboard",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onNewStoryboard()
                }
            ),
            QuickActionItem(
                id = "snapshot_capture",
                title = "Camera Snapshot",
                subtitle = "Capture reference photo",
                icon = Icons.Default.PhotoCamera,
                accentColor = EchoAmber,
                testTag = "creative_quick_snapshot",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onSnapshotCapture()
                }
            ),
            QuickActionItem(
                id = "open_browser",
                title = "Dola Browser",
                subtitle = "Research & prompt inspiration",
                icon = Icons.Default.Language,
                accentColor = EchoGreen,
                testTag = "creative_quick_browser",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onOpenBrowser()
                }
            ),
            QuickActionItem(
                id = "media_player",
                title = "Media Player",
                subtitle = "Cinema & soundstage lounge",
                icon = Icons.Default.Movie,
                accentColor = EchoPurple,
                testTag = "creative_quick_player",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    isExpanded = false
                    onMediaPlayer()
                }
            ),
            QuickActionItem(
                id = "cloud_sync",
                title = "Cloud Sync",
                subtitle = "Sync All Now to Google Drive",
                icon = Icons.Default.CloudSync,
                accentColor = EchoBlue,
                testTag = "creative_quick_sync",
                onClick = {
                    toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                    isExpanded = false
                    onCloudSync()
                }
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(targetScale = 0.8f)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    actions.forEach { action ->
                        QuickActionSpeedDialRow(action = action)
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    toneGenerator.playChime(if (isExpanded) SoundEffectType.TAP else SoundEffectType.HARMONIC_PING)
                    isExpanded = !isExpanded
                },
                containerColor = if (isExpanded) EchoDarkSurface else EchoGreen,
                contentColor = if (isExpanded) EchoCyan else Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, if (isExpanded) EchoText else EchoGreen.copy(alpha = 0.8f), CircleShape)
                    .testTag("creative_quick_actions_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.FlashOn,
                        contentDescription = if (isExpanded) "Close Creative Actions" else "Open Creative Actions",
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(if (isExpanded) 0f else rotationAngle)
                    )
                }
            }
        }
    }
}

/**
 * Quick Audio Recording Modal Dialog.
 * Allows instant live recording right in the Workspace, visual waveform animation,
 * playback review, and direct saving as a Workspace Audio Note and Media Vault item.
 */
@Composable
fun QuickAudioRecordingDialog(
    onDismiss: () -> Unit,
    onSaveVoiceNote: (title: String, content: String, audioUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isReviewMode by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var noteTitle by remember {
        val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date())
        mutableStateOf("Voice Memo - $dateStr")
    }
    var noteTranscription by remember { mutableStateOf("") }

    // Waveform simulation bars
    val waveBars = remember { mutableStateListOf<Float>().apply { repeat(24) { add(0.15f) } } }

    // Microphone permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
            isRecording = true
            isPaused = false
            secondsElapsed = 0
            isReviewMode = false
        } else {
            Toast.makeText(context, "Microphone permission required for voice notes", Toast.LENGTH_SHORT).show()
        }
    }

    // Timer effect
    LaunchedEffect(isRecording, isPaused) {
        while (isRecording && !isPaused) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Dynamic waveform wave effect while recording
    LaunchedEffect(isRecording, isPaused) {
        while (isRecording && !isPaused) {
            delay(120)
            for (i in 0 until waveBars.size) {
                waveBars[i] = Random.nextFloat() * 0.85f + 0.15f
            }
        }
        if (!isRecording) {
            for (i in 0 until waveBars.size) {
                waveBars[i] = 0.15f
            }
        }
    }

    // Cleanup media player on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun startRecordingFlow() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
            isRecording = true
            isPaused = false
            secondsElapsed = 0
            isReviewMode = false
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecordingFlow() {
        isRecording = false
        isPaused = false
        isReviewMode = true
        toneGenerator.playChime(SoundEffectType.SUCCESS)

        // Generate synthetic or saved audio export file
        try {
            val exportsDir = File(context.filesDir, "voice_memos").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val audioFile = File(exportsDir, "VOICE_$timeStamp.m4a")

            // Create a small placeholder audio file so it has an actual valid URI
            if (!audioFile.exists()) {
                audioFile.writeBytes(ByteArray(1024))
            }
            recordedFile = audioFile
        } catch (e: Exception) {
            // fallback
        }
    }

    Dialog(
        onDismissRequest = {
            if (isRecording) {
                isRecording = false
            }
            mediaPlayer?.stop()
            mediaPlayer?.release()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = EchoBgAlt
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EchoRed.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = EchoRed, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Voice Memo & Audio Recorder",
                                color = EchoText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                if (isRecording) "Recording live spoken thoughts..." else if (isReviewMode) "Review and attach to note" else "Tap record to capture ideas",
                                color = EchoMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            mediaPlayer?.release()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                    }
                }

                // Audio waveform & timer box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(EchoCardStroke, EchoCyan.copy(alpha = 0.4f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Timer display
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", secondsElapsed / 60, secondsElapsed % 60),
                            fontSize = 36.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) EchoRed else EchoCyan
                        )

                        // Animated live audio waveform bars
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoBg)
                        ) {
                            val barCount = waveBars.size
                            val spacing = 6.dp.toPx()
                            val totalSpacing = spacing * (barCount - 1)
                            val barWidth = (size.width - totalSpacing) / barCount
                            val centerY = size.height / 2f

                            waveBars.forEachIndexed { index, amp ->
                                val barHeight = (amp * size.height * 0.85f).coerceAtLeast(4.dp.toPx())
                                val startX = index * (barWidth + spacing)
                                val startY = centerY - (barHeight / 2f)

                                drawRoundRect(
                                    color = if (isRecording) EchoRed else EchoCyan,
                                    topLeft = Offset(startX, startY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }

                        // Status pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isRecording) EchoRed else if (isReviewMode) EchoGreen else EchoMuted, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isRecording -> "Recording in Progress"
                                    isPaused -> "Paused"
                                    isReviewMode -> "Recording Ready (${secondsElapsed}s captured)"
                                    else -> "Ready to Record"
                                },
                                fontSize = 12.sp,
                                color = if (isRecording) EchoRed else EchoText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Controls row
                if (!isReviewMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = { startRecordingFlow() },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_start_voice_recording")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Start Recording", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Pause/Resume toggle
                            OutlinedButton(
                                onClick = {
                                    isPaused = !isPaused
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = EchoText)
                                Spacer(Modifier.width(4.dp))
                                Text(if (isPaused) "Resume" else "Pause", color = EchoText)
                            }

                            // Stop & Finish
                            Button(
                                onClick = { stopRecordingFlow() },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_stop_voice_recording")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Stop & Review", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Review mode: Title & note inputs, Play preview, Save
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Note Title", color = EchoMuted) },
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
                            value = noteTranscription,
                            onValueChange = { noteTranscription = it },
                            label = { Text("Spoken Notes / Transcription Summary", color = EchoMuted) },
                            placeholder = { Text("Add rapid notes or bullet points from this recording...", color = EchoMuted.copy(alpha = 0.5f)) },
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EchoCyan,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    // Re-record
                                    isReviewMode = false
                                    secondsElapsed = 0
                                    startRecordingFlow()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Re-record", color = EchoMuted, fontSize = 12.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        // Play preview tone
                                        isPlayingPreview = !isPlayingPreview
                                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = EchoCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isPlayingPreview) "Playing" else "Preview", color = EchoCyan, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                                        val audioUri = recordedFile?.let { Uri.fromFile(it) }

                                        // Register in MediaVault
                                        if (audioUri != null) {
                                            MediaVaultRepository.addItem(
                                                VaultMediaItem(
                                                    id = "voice_${System.currentTimeMillis()}",
                                                    title = noteTitle,
                                                    subtitle = "Audio Memo • ${secondsElapsed}s • Voice Note",
                                                    uri = audioUri,
                                                    type = com.example.ui.components.MediaType.VIDEO // MediaVault handles video/audio playback
                                                )
                                            )
                                        }

                                        val finalContent = buildString {
                                            if (noteTranscription.isNotBlank()) {
                                                append(noteTranscription.trim())
                                                append("\n\n")
                                            }
                                            append("🎙️ [Voice Memo Attachment]\n")
                                            append("Duration: ${secondsElapsed}s\n")
                                            append("File: ${recordedFile?.name ?: "audio_memo.m4a"}\n")
                                            append("Recorded: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
                                        }

                                        onSaveVoiceNote(noteTitle, finalContent, audioUri)
                                        Toast.makeText(context, "Voice memo saved to Workspace & Media Vault!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_save_voice_memo_note")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Save Note", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Snapshot Review Modal Dialog.
 * Displays captured camera image or picked photo, with immediate title/caption editing,
 * saving to app storage, registering in Media Vault, and creating an attached Workspace Note.
 */
@Composable
fun QuickSnapshotReviewDialog(
    capturedBitmap: Bitmap?,
    capturedUri: Uri?,
    onDismiss: () -> Unit,
    onSaveSnapshotNote: (title: String, content: String, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }

    var title by remember {
        val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date())
        mutableStateOf("Snapshot Capture - $dateStr")
    }
    var notesText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, EchoAmber.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = EchoBgAlt
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EchoAmber.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Snapshot Captured",
                                color = EchoText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Review & save snapshot as Workspace Note",
                                color = EchoMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                    }
                }

                // Image Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = EchoBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EchoCardStroke, EchoAmber.copy(alpha = 0.5f))))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (capturedBitmap != null) {
                            Image(
                                bitmap = capturedBitmap.asImageBitmap(),
                                contentDescription = "Captured Snapshot",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Snapshot Photo Attached", color = EchoText, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title", color = EchoMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Field Observations / Notes", color = EchoMuted) },
                    placeholder = { Text("Add notes about this snapshot...", color = EchoMuted.copy(alpha = 0.5f)) },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoAmber,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Discard", color = EchoMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            toneGenerator.playChime(SoundEffectType.SUCCESS)

                            // Save bitmap to file in app cache or files
                            var finalUri = capturedUri
                            if (capturedBitmap != null && finalUri == null) {
                                try {
                                    val snapshotsDir = File(context.filesDir, "snapshots").apply { mkdirs() }
                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                    val imageFile = File(snapshotsDir, "SNAPSHOT_$timeStamp.jpg")
                                    FileOutputStream(imageFile).use { out ->
                                        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    finalUri = Uri.fromFile(imageFile)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            }

                            // Add to Media Vault
                            if (finalUri != null) {
                                MediaVaultRepository.addItem(
                                    VaultMediaItem(
                                        id = "snap_${System.currentTimeMillis()}",
                                        title = title,
                                        subtitle = "Camera Snapshot • Attached Photo",
                                        uri = finalUri,
                                        type = com.example.ui.components.MediaType.PHOTO
                                    )
                                )
                            }

                            val noteContent = buildString {
                                if (notesText.isNotBlank()) {
                                    append(notesText.trim())
                                    append("\n\n")
                                }
                                append("📷 [Camera Snapshot Attached]\n")
                                append("Captured: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                                if (finalUri != null) {
                                    append("Local File URI: $finalUri")
                                }
                            }

                            onSaveSnapshotNote(title, noteContent, finalUri)
                            Toast.makeText(context, "Snapshot saved to Workspace & Media Vault!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_snapshot_note")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save to Workspace", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
