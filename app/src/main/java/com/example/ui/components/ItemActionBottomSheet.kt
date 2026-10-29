package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.launch

/**
 * Data model describing an item targeted by long-press.
 */
data class LongPressItemPayload(
    val id: String,
    val title: String,
    val textContent: String,
    val subtitle: String = "",
    val category: String = "Item",
    val isPinned: Boolean = false,
    val isFolder: Boolean = false,
    val isMedia: Boolean = false,
    val mediaUriString: String? = null
)

/**
 * Categorized Modal Bottom Sheet for rich interactive operations on long-pressed items.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemActionBottomSheet(
    item: LongPressItemPayload,
    isSpeaking: Boolean = false,
    onDismiss: () -> Unit,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onCopyText: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onSaveToNotes: ((title: String, content: String) -> Unit)? = null,
    onSpeakAloud: (() -> Unit)? = null,
    onStopSpeaking: (() -> Unit)? = null,
    onRemixWithGemini: ((prompt: String) -> Unit)? = null,
    onExpandToScript: ((title: String, prompt: String) -> Unit)? = null,
    onTogglePin: (() -> Unit)? = null,
    onRename: (() -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onOpenInViewer: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = EchoBg,
        tonalElevation = 8.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(EchoCardStroke)
            )
        },
        modifier = Modifier.testTag("item_action_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Item preview & Type badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                item.isFolder -> Icons.Default.NoteAdd
                                item.isMedia -> Icons.Default.Movie
                                item.category.contains("Gemini", ignoreCase = true) -> Icons.Default.AutoAwesome
                                else -> Icons.AutoMirrored.Filled.MenuBook
                            },
                            contentDescription = null,
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = EchoText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.category.uppercase(),
                                    color = EchoCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (item.subtitle.isNotBlank()) {
                            Text(
                                text = item.subtitle,
                                color = EchoMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                }
            }

            // Excerpt snippet preview
            if (item.textContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoBgAlt)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = item.textContent.take(160) + if (item.textContent.length > 160) "..." else "",
                        color = EchoText.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY 1: AI CREATIVE & REMIX (GEMINI & DOLA)
            ActionCategoryHeader(title = "AI REMIX & ENTERTAINMENT", icon = Icons.Default.AutoAwesome, tint = EchoAmber)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionTile(
                    label = "Remix & Brainstorm",
                    subtitle = "Generate 3 variations",
                    icon = Icons.Default.ElectricBolt,
                    color = EchoAmber,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        val remixPrompt = "Remix and creatively expand on this concept: \"${item.title}\"\n\nContext:\n${item.textContent}"
                        onRemixWithGemini?.invoke(remixPrompt)
                        onDismiss()
                    }
                )

                ActionTile(
                    label = "Draft Video Script",
                    subtitle = "3-Beat visual hook",
                    icon = Icons.Default.Movie,
                    color = EchoCyan,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        val scriptPrompt = "Transform this into a dramatic 3-beat viral video script with camera angles and sound effects:\n\"${item.title}\"\n\nContent:\n${item.textContent}"
                        onExpandToScript?.invoke("Script: ${item.title.take(25)}", scriptPrompt)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY 2: CONTENT & EXPORT
            ActionCategoryHeader(title = "CONTENT & EXPORT", icon = Icons.Default.Share, tint = EchoCyan)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionTile(
                    label = "Copy Text",
                    subtitle = "To clipboard",
                    icon = Icons.Default.ContentCopy,
                    color = EchoText,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val copyContent = "${item.title}\n\n${item.textContent}"
                        clipboard.setPrimaryClip(ClipData.newPlainText(item.title, copyContent))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        onCopyText?.invoke()
                        onDismiss()
                    }
                )

                ActionTile(
                    label = "Share System-Wide",
                    subtitle = "Apps, SMS, Drive",
                    icon = Icons.Default.Share,
                    color = EchoGreen,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TITLE, item.title)
                            putExtra(Intent.EXTRA_TEXT, "${item.title}\n\n${item.textContent}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share \"${item.title}\""))
                        onShare?.invoke()
                        onDismiss()
                    }
                )

                if (onSaveToNotes != null) {
                    ActionTile(
                        label = "Save to Notes",
                        subtitle = "Workspace log",
                        icon = Icons.Default.NoteAdd,
                        color = EchoCyan,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            onSaveToNotes.invoke(item.title, item.textContent)
                            Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )
                }

                if (item.isMedia && onOpenInViewer != null) {
                    ActionTile(
                        label = "Launch Player",
                        subtitle = "90° rotation view",
                        icon = Icons.Default.PlayArrow,
                        color = EchoCyan,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onOpenInViewer.invoke()
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY 3: VOICE & DICTATION
            ActionCategoryHeader(title = "VOICE & DICTATION", icon = Icons.Default.VolumeUp, tint = EchoCyan)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSpeaking) {
                    ActionTile(
                        label = "Stop Speaking",
                        subtitle = "Pause Dola voice",
                        icon = Icons.Default.Stop,
                        color = EchoRed,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onStopSpeaking?.invoke()
                            onDismiss()
                        }
                    )
                } else {
                    ActionTile(
                        label = "Speak Aloud",
                        subtitle = "Read with Dola voice",
                        icon = Icons.Default.VolumeUp,
                        color = EchoCyan,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onSpeakAloud?.invoke()
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY 4: ITEM MANAGEMENT & ORGANIZATION
            ActionCategoryHeader(title = "ORGANIZATION & MANAGEMENT", icon = Icons.Default.Edit, tint = EchoMuted)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onTogglePin != null) {
                    ActionTile(
                        label = if (item.isPinned) "Unpin" else "Pin to Top",
                        subtitle = if (item.isPinned) "Remove from top" else "Keep at top",
                        icon = Icons.Default.PushPin,
                        color = if (item.isPinned) EchoAmber else EchoText,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onTogglePin.invoke()
                            onDismiss()
                        }
                    )
                }

                if (onDuplicate != null) {
                    ActionTile(
                        label = "Duplicate",
                        subtitle = "Create copy",
                        icon = Icons.Default.ContentPaste,
                        color = EchoText,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onDuplicate.invoke()
                            onDismiss()
                        }
                    )
                }

                if (onRename != null) {
                    ActionTile(
                        label = "Rename",
                        subtitle = "Edit title",
                        icon = Icons.Default.Edit,
                        color = EchoText,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onRename.invoke()
                            onDismiss()
                        }
                    )
                }

                if (onDelete != null) {
                    ActionTile(
                        label = "Delete / Trash",
                        subtitle = "Remove item",
                        icon = Icons.Default.Delete,
                        color = EchoRed,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.ALERT)
                            onDelete.invoke()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCategoryHeader(title: String, icon: ImageVector, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Text(
            text = title,
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ActionTile(
    label: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 168.dp, height = 72.dp)
            .clickable(onClick = onClick)
            .testTag("action_tile_${label.replace(" ", "_").lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = EchoText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = EchoMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
