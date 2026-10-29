package com.example.ui.components

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText

@Composable
fun ProNotepadTab(
    toneGenerator: HarmonicToneGenerator,
    onSaveToWorkspaceNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("athena_pro_notepad", Context.MODE_PRIVATE) }

    var docTitle by remember {
        mutableStateOf(prefs.getString("draft_title", "Athena Field Notes & Research Manuscript") ?: "Athena Field Notes & Research Manuscript")
    }
    var docContent by remember {
        mutableStateOf(
            prefs.getString(
                "draft_content",
                "ATHENA RESEARCH & CREATIVE MANUSCRIPT\n\n" +
                        "1. EXECUTIVE OVERVIEW\n" +
                        "The Athena workspace merges high-capacity manuscript authoring, audio frequency synthesis, and local media vault curation into a unified, privacy-first interface. All document generation runs 100% locally and offline on your device, ensuring complete data sovereignty without mandatory online dependencies.\n\n" +
                        "2. SYSTEM ARCHITECTURE & CAPABILITIES\n" +
                        "• Word Processing Limit: Unbounded text capacity engineered for manuscripts, technical logs, essays, and screenplays.\n" +
                        "• Document Compilation: Direct on-device PDF generation utilizing native Android graphics pipelines with dynamic pagination and header formatting.\n" +
                        "• Multi-Format Interoperability: Export to Plain Text (.txt) or styled HTML5 (.html) with responsive dark theme styling.\n" +
                        "• Hybrid Storage: Continuous local persistence with optional cloud sync and snapshot bundling.\n\n" +
                        "3. METHODOLOGY & OBSERVATIONS\n" +
                        "Paste or author your thoughts here without word limits. Use the action bar below to format, track statistics, copy, paste, or export directly to PDF, Plain Text, or HTML."
            ) ?: ""
        )
    }

    var stats by remember(docContent) { mutableStateOf(NotepadExportManager.calculateStats(docContent)) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var isCloudSyncEnabled by remember {
        mutableStateOf(prefs.getBoolean("cloud_sync_enabled", false))
    }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    // Auto-save draft locally on change
    LaunchedEffect(docTitle, docContent, isCloudSyncEnabled) {
        prefs.edit()
            .putString("draft_title", docTitle)
            .putString("draft_content", docContent)
            .putBoolean("cloud_sync_enabled", isCloudSyncEnabled)
            .apply()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("pro_notepad_tab")
    ) {
        // Document Title & Live Stats Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title", fontSize = 11.sp, color = EchoCyan) },
                        placeholder = { Text("Untitled Manuscript...", color = EchoMuted) },
                        modifier = Modifier.weight(1f).testTag("notepad_title_input"),
                        singleLine = true,
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

                    Spacer(modifier = Modifier.width(8.dp))

                    // Offline / Hybrid Mode Pill Button
                    OutlinedButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            showCloudSyncDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isCloudSyncEnabled) EchoCyan else EchoMuted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_notepad_sync_status")
                    ) {
                        Icon(
                            imageVector = if (isCloudSyncEnabled) Icons.Default.CloudDone else Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isCloudSyncEnabled) "Hybrid Online" else "100% Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Live Text Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stats.wordCount} words",
                            color = EchoCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "•",
                            color = EchoMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${stats.charCount} chars",
                            color = EchoText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "•",
                            color = EchoMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${stats.paragraphCount} paragraphs",
                            color = EchoMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "~${stats.readingTimeMinutes} min read",
                        color = EchoCyan.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Editing Toolbar (Copy, Paste, Clear, Markdown Helpers)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        NotepadExportManager.copyToClipboard(context, docContent, docTitle)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_notepad_copy")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy All", modifier = Modifier.size(14.dp), tint = EchoCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("Copy All", fontSize = 11.sp)
                }
            }

            item {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        val pasted = NotepadExportManager.getClipboardText(context)
                        if (!pasted.isNullOrBlank()) {
                            docContent = if (docContent.isBlank()) pasted else "$docContent\n$pasted"
                            Toast.makeText(context, "Pasted text from clipboard", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_notepad_paste")
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(14.dp), tint = EchoCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("Paste", fontSize = 11.sp)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        docContent = "$docContent\n• "
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet", modifier = Modifier.size(14.dp), tint = EchoCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("Bullet", fontSize = 11.sp, color = EchoText)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        docContent = "$docContent\n### "
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Heading", modifier = Modifier.size(14.dp), tint = EchoCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("Heading", fontSize = 11.sp, color = EchoText)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        docContent = "$docContent\n> "
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FormatQuote, contentDescription = "Quote", modifier = Modifier.size(14.dp), tint = EchoCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("Quote", fontSize = 11.sp, color = EchoText)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        showClearConfirmDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_notepad_clear")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp), tint = EchoRed)
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", fontSize = 11.sp, color = EchoRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // High-Capacity Document Editor Viewport
        OutlinedTextField(
            value = docContent,
            onValueChange = { docContent = it },
            placeholder = { Text("Write or paste your manuscripts, articles, logs, or screenplays here without word limits...", color = EchoMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("notepad_content_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EchoCyan,
                unfocusedBorderColor = EchoCardStroke,
                focusedTextColor = EchoText,
                unfocusedTextColor = EchoText,
                focusedContainerColor = EchoDarkSurface,
                unfocusedContainerColor = EchoDarkSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Export Toolbar: PDF, TXT, HTML, and Save to Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Export PDF
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                    NotepadExportManager.exportPdf(context, docTitle, docContent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).height(40.dp).testTag("btn_export_pdf")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // 2. Export TXT
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                    NotepadExportManager.exportPlainText(context, docTitle, docContent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).height(40.dp).testTag("btn_export_txt")
            ) {
                Icon(Icons.Default.Description, contentDescription = "TXT", modifier = Modifier.size(16.dp), tint = EchoCyan)
                Spacer(Modifier.width(4.dp))
                Text("Plain Text", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // 3. Export HTML
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                    NotepadExportManager.exportHtml(context, docTitle, docContent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).height(40.dp).testTag("btn_export_html")
            ) {
                Icon(Icons.Default.Html, contentDescription = "HTML", modifier = Modifier.size(16.dp), tint = EchoCyan)
                Spacer(Modifier.width(4.dp))
                Text("HTML Doc", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // 4. Save to Workspace Notes
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                    onSaveToWorkspaceNote(docTitle, docContent)
                    Toast.makeText(context, "Saved manuscript to Workspace Notes!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan.copy(alpha = 0.2f), contentColor = EchoCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1.1f).height(40.dp).testTag("btn_save_to_notes")
            ) {
                Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save to Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Clear confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Notepad Content?", color = EchoCyan, fontWeight = FontWeight.Bold) },
            text = { Text("This will erase the current text in this editor. (Saved notes and exported files remain safe).", color = EchoText) },
            confirmButton = {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        docContent = ""
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Cloud / Offline Sync Configuration Dialog
    if (showCloudSyncDialog) {
        AlertDialog(
            onDismissRequest = { showCloudSyncDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (isCloudSyncEnabled) Icons.Default.CloudDone else Icons.Default.Sync,
                        contentDescription = null,
                        tint = EchoCyan
                    )
                    Text("Storage & Privacy Mode", color = EchoCyan, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isCloudSyncEnabled)
                            "• Current: Hybrid Mode (Enabled)\nYour notes and documents are saved offline locally on device, with optional cloud backup and sync bundles available."
                        else
                            "• Current: 100% Offline Mode\nYour notes, document exports (PDF, TXT, HTML), and drafts NEVER leave your device. Zero internet connection or account required.",
                        color = EchoText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            isCloudSyncEnabled = !isCloudSyncEnabled
                            Toast.makeText(
                                context,
                                if (isCloudSyncEnabled) "Switched to Hybrid Mode" else "Switched to 100% Offline Mode",
                                Toast.LENGTH_SHORT
                            ).show()
                            showCloudSyncDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCloudSyncEnabled) EchoRed.copy(alpha = 0.8f) else EchoCyan,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isCloudSyncEnabled) "Switch to 100% Offline" else "Enable Hybrid Cloud Sync")
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showCloudSyncDialog = false }) {
                    Text("Done", color = EchoCyan)
                }
            },
            containerColor = EchoDarkSurface
        )
    }
}
