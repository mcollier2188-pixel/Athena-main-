package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.CreativePromptTemplate
import com.example.ai.PromptCategory
import com.example.ai.PromptTemplateLibrary
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText

/**
 * Creative Brainstorming Prompt Studio Dialog:
 * Filterable master template browser across 6 creative disciplines.
 * Allows users to preview, copy, or immediately send rich prompts to Gemini 3.5 Flash.
 */
@Composable
fun PromptTemplateDialog(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onSelectPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<PromptCategory?>(null) }
    var selectedTemplate by remember { mutableStateOf<CreativePromptTemplate?>(null) }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == null) PromptTemplateLibrary.templates
        else PromptTemplateLibrary.getByCategory(selectedCategory!!)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EchoCyan, RoundedCornerShape(16.dp))
                .testTag("prompt_template_dialog"),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EchoOnCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Creative Brainstorming Studio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = "Master Prompts • 6 Creative Disciplines",
                                fontSize = 11.sp,
                                color = EchoCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_prompt_studio_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoText)
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCategory = null
                            },
                            label = { Text("✨ All Disciplines (${PromptTemplateLibrary.templates.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoText
                            )
                        )
                    }
                    items(PromptCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCategory = category
                            },
                            label = { Text("${category.badge} ${category.displayName}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoText
                            )
                        )
                    }
                }

                // Templates List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTemplates) { template ->
                        val isSelected = selectedTemplate?.id == template.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    selectedTemplate = if (isSelected) null else template
                                }
                                .testTag("template_item_${template.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) EchoDarkSurface else EchoCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EchoCyan else EchoCardStroke
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(template.category.badge, fontSize = 14.sp)
                                        Text(
                                            text = template.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EchoText
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EchoCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = template.suggestedTone,
                                            fontSize = 9.sp,
                                            color = EchoCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Text(
                                    text = template.tagline,
                                    fontSize = 11.sp,
                                    color = EchoMuted
                                )

                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoBgAlt)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = template.detailedPrompt,
                                            fontSize = 12.sp,
                                            color = EchoText,
                                            lineHeight = 17.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                cm.setPrimaryClip(ClipData.newPlainText("Template Prompt", template.detailedPrompt))
                                                Toast.makeText(context, "Copied prompt to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Copy", fontSize = 11.sp)
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                onSelectPrompt(template.detailedPrompt)
                                                onDismiss()
                                            },
                                            modifier = Modifier.height(32.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = EchoCyan,
                                                contentColor = EchoOnCyan
                                            )
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Send to AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Hint Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap any template to preview and inject into Gemini 3.5 Flash",
                        fontSize = 10.sp,
                        color = EchoMuted
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Close", fontSize = 11.sp, color = EchoText)
                    }
                }
            }
        }
    }
}
