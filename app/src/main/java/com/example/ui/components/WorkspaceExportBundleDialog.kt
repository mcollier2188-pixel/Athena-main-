package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.WorkspaceItem
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Workspace Archive & Full Backup Bundle Dialog.
 * Compiles all Workspace notes, Dola voice journals, and scientific observations
 * into an exportable backup bundle with Android system share sheet and clipboard copying.
 */
@Composable
fun WorkspaceExportBundleDialog(
    workspaceViewModel: WorkspaceViewModel,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val allItems by workspaceViewModel.allItems.collectAsState()
    val notes = allItems.filter { it.kind != "folder" && !it.isDeleted }
    val folders = allItems.filter { it.kind == "folder" && !it.isDeleted }

    val timestampStr = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) }

    val backupContent = remember(allItems) {
        buildString {
            appendLine("# ATHENA WORKSPACE BACKUP ARCHIVE")
            appendLine("**Export Generated**: $timestampStr")
            appendLine("**Total Items**: ${allItems.size} (${notes.size} Notes/Files, ${folders.size} Folders)")
            appendLine("**Carrier Frequency Master**: 42.600 Hz (Bedrock Reference)")
            appendLine("**Acoustic Network**: 11 Nodes (Portsmouth N1 Master)")
            appendLine()
            appendLine("---")
            appendLine("## 1. WORKSPACE FOLDERS")
            appendLine()
            if (folders.isEmpty()) {
                appendLine("_No custom folders created._")
            } else {
                folders.forEachIndexed { index, folder ->
                    appendLine("- 📁 **${folder.name}** (ID: ${folder.id})")
                }
            }
            appendLine()
            appendLine("---")
            appendLine("## 2. WORKSPACE NOTES, VOICE JOURNALS & LOGS")
            appendLine()
            if (notes.isEmpty()) {
                appendLine("_No notes found in workspace._")
            } else {
                notes.forEachIndexed { index, item ->
                    val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.modifiedTimestamp))
                    appendLine("### ${index + 1}. ${item.name.ifBlank { "Untitled Note" }}")
                    appendLine("- **Kind**: ${item.kind.uppercase()}")
                    appendLine("- **Last Modified**: $dateFormatted")
                    if (item.tags.isNotBlank()) {
                        appendLine("- **Tags**: ${item.tags}")
                    }
                    appendLine()
                    appendLine("```markdown")
                    appendLine(item.text)
                    appendLine("```")
                    appendLine()
                }
            }

            appendLine("---")
            appendLine("_End of ATHENA Workspace Backup Bundle._")
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, EchoCyan.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .testTag("workspace_export_dialog"),
            color = EchoBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EchoCyan.copy(alpha = 0.2f))
                                .border(1.dp, EchoCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(
                                text = "WORKSPACE BACKUP & EXPORT BUNDLE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${notes.size} Notes & ${folders.size} Folders compiled for export",
                                fontSize = 11.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                    }
                }

                // Stats overview card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NOTES & LOGS", fontSize = 9.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                            Text("${notes.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FOLDERS", fontSize = 9.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                            Text("${folders.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EchoGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CARRIER LOCK", fontSize = 9.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                            Text("42.600 Hz", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EchoText, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Scrollable Bundle Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EchoDarkSurface)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = backupContent,
                            fontSize = 11.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Export Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy to Clipboard
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ATHENA Workspace Backup", backupContent)
                            clipboard.setPrimaryClip(clip)
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            Toast.makeText(context, "Full Workspace backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_copy_backup_clipboard"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                        border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy to Clipboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Share via Android Sheet
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, "ATHENA Workspace Backup ($timestampStr)")
                                putExtra(Intent.EXTRA_TEXT, backupContent)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export ATHENA Workspace Backup")
                            context.startActivity(shareIntent)
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("btn_share_backup_sheet"),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = EchoBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share / Export Backup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
