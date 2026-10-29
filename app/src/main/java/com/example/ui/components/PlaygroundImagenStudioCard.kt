package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.CreativeMessage
import com.example.ai.GeminiMediaGenerationService
import com.example.ai.GeneratedImageResult
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.MediaVaultRepository
import com.example.data.VaultMediaItem
import kotlinx.coroutines.launch

/**
 * PlaygroundImagenStudioCard
 *
 * Collapsible Studio Card situated above the chat feed in Gemini Playground.
 * Provides interactive AI image generation powered by Google Imagen 3 with
 * Gemini 2.5 Flash fallback, text prompts, aspect ratio selectors (16:9, 1:1, 9:16),
 * sample count controls, aesthetic style presets, and prompt enhancement.
 */
@Composable
fun PlaygroundImagenStudioCard(
    modifier: Modifier = Modifier,
    toneGenerator: HarmonicToneGenerator? = null,
    onPostToChat: (GeneratedImageResult) -> Unit = {},
    onAttachToWorkspaceNote: ((String, Uri) -> Unit)? = null,
    onSaveToVault: ((GeneratedImageResult) -> Unit)? = null,
    onOpenMedia: ((Uri, MediaType, String) -> Unit)? = null,
    onOpenMediaPreview: ((Uri, MediaType) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }
    var prompt by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf("Cinematic 35mm Film") }
    var selectedSampleCount by remember { mutableIntStateOf(1) }

    var isGenerating by remember { mutableStateOf(false) }
    var isEnhancing by remember { mutableStateOf(false) }
    var generatedResults by remember { mutableStateOf<List<GeneratedImageResult>>(emptyList()) }
    var activeResultIndex by remember { mutableIntStateOf(0) }

    val aspectRatios = listOf(
        "1:1" to "Square (1:1)",
        "16:9" to "Landscape (16:9)",
        "9:16" to "Portrait / Reel (9:16)",
        "4:3" to "Standard (4:3)"
    )

    val stylePresets = listOf(
        "Cinematic 35mm Film",
        "Photorealistic Studio",
        "Cyberpunk Neon",
        "Anime Studio Masterpiece",
        "Oil Painting Classical",
        "Minimalist Bauhaus Vector"
    )

    val sampleCounts = listOf(1, 2, 4)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("playground_imagen_studio_card")
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF0F1522)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar: Always visible, clickable to toggle collapse/expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        toneGenerator?.playChime(SoundEffectType.TAP)
                        isExpanded = !isExpanded
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Imagen Studio",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Imagen 3 Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF241442))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚡ Imagen 3 • Flash Fallback",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB388FF)
                                )
                            }
                        }
                        Text(
                            text = if (isExpanded) "Text-to-image synthesis, ratios & styles" else "Tap to open AI image generator",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = {
                        toneGenerator?.playChime(SoundEffectType.TAP)
                        isExpanded = !isExpanded
                    },
                    modifier = Modifier.testTag("imagen_card_expand_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // Expandable Studio Controls
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    // Prompt Input
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("imagen_prompt_input"),
                        label = { Text("Visual Prompt Description") },
                        placeholder = { Text("e.g. A solitary astronaut standing near ancient neon megaliths on Europa, 35mm film...") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (prompt.isNotBlank()) {
                                IconButton(onClick = { prompt = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Prompt")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF070B13),
                            unfocusedContainerColor = Color(0xFF070B13),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Magic Wand Prompt Enhancer Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (prompt.isBlank()) {
                                    Toast.makeText(context, "Enter a concept first to enhance", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                scope.launch {
                                    isEnhancing = true
                                    toneGenerator?.playChime(SoundEffectType.TAP)
                                    try {
                                        val enhanced = GeminiMediaGenerationService.enhancePrompt(context, prompt, "image")
                                        prompt = enhanced
                                        toneGenerator?.playChime(SoundEffectType.SUCCESS)
                                        Toast.makeText(context, "Prompt enhanced with cinematography details", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isEnhancing = false
                                    }
                                }
                            },
                            enabled = !isEnhancing && !isGenerating,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("imagen_enhance_button")
                        ) {
                            if (isEnhancing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF00E5FF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enhancing...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("✨ Enhance with Gemini", fontSize = 12.sp, color = Color(0xFF00E5FF))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Aspect Ratio Selector
                    Text(
                        text = "Aspect Ratio",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        aspectRatios.forEach { (ratio, label) ->
                            FilterChip(
                                selected = selectedAspectRatio == ratio,
                                onClick = {
                                    toneGenerator?.playChime(SoundEffectType.TAP)
                                    selectedAspectRatio = ratio
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFF00E5FF)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Style Presets Carousel
                    Text(
                        text = "Style Preset",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stylePresets) { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = {
                                    toneGenerator?.playChime(SoundEffectType.TAP)
                                    selectedStyle = style
                                },
                                label = { Text(style, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(0xFFB388FF)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sample Count Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sample Count",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            sampleCounts.forEach { count ->
                                FilterChip(
                                    selected = selectedSampleCount == count,
                                    onClick = {
                                        toneGenerator?.playChime(SoundEffectType.TAP)
                                        selectedSampleCount = count
                                    },
                                    label = { Text("$count", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                        selectedLabelColor = Color(0xFF00E5FF)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Generate Button
                    Button(
                        onClick = {
                            val activePrompt = prompt.ifBlank { "Futuristic creative workspace with holographic displays and warm ambient illumination" }
                            scope.launch {
                                isGenerating = true
                                toneGenerator?.playChime(SoundEffectType.TAP)
                                try {
                                    val genResult = GeminiMediaGenerationService.generateWithImagen(
                                        context = context,
                                        prompt = activePrompt,
                                        aspectRatio = selectedAspectRatio,
                                        sampleCount = selectedSampleCount,
                                        style = selectedStyle
                                    )

                                    if (genResult.isSuccess) {
                                        val items = genResult.getOrThrow()
                                        generatedResults = items
                                        activeResultIndex = 0
                                        toneGenerator?.playChime(SoundEffectType.SUCCESS)
                                        Toast.makeText(context, "Generated ${items.size} image(s) with Imagen", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Generation failed: ${genResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } finally {
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("imagen_generate_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF070B13)
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF070B13)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing Image with Imagen 3...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🎨 Generate with Imagen 3", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // Showcase Result Preview if available
                    if (generatedResults.isNotEmpty()) {
                        val currentItem = generatedResults.getOrNull(activeResultIndex) ?: generatedResults.first()
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF070B13),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Generated Output (${activeResultIndex + 1}/${generatedResults.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF00E5FF)
                                    )

                                    if (generatedResults.size > 1) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            generatedResults.indices.forEach { index ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (activeResultIndex == index) Color(0xFF00E5FF) else Color(0xFF334155))
                                                        .clickable {
                                                            toneGenerator?.playChime(SoundEffectType.TAP)
                                                            activeResultIndex = index
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Image Container
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 180.dp, max = 280.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black)
                                        .clickable {
                                            if (onOpenMedia != null) {
                                                onOpenMedia(currentItem.localUri, MediaType.PHOTO, currentItem.prompt)
                                            } else {
                                                onOpenMediaPreview?.invoke(currentItem.localUri, MediaType.PHOTO)
                                            }
                                        }
                                ) {
                                    AsyncImage(
                                        model = currentItem.localUri,
                                        contentDescription = currentItem.prompt,
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Fullscreen action overlay
                                    IconButton(
                                        onClick = {
                                            if (onOpenMedia != null) {
                                                onOpenMedia(currentItem.localUri, MediaType.PHOTO, currentItem.prompt)
                                            } else {
                                                onOpenMediaPreview?.invoke(currentItem.localUri, MediaType.PHOTO)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Fullscreen,
                                            contentDescription = "Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentItem.prompt,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Save to Vault
                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator?.playChime(SoundEffectType.SUCCESS)
                                            MediaVaultRepository.addItem(
                                                VaultMediaItem(
                                                    title = currentItem.prompt.take(30),
                                                    subtitle = "Imagen 3 • ${currentItem.style}",
                                                    uri = currentItem.localUri,
                                                    type = MediaType.PHOTO,
                                                    isAiGenerated = true
                                                )
                                            )
                                            onSaveToVault?.invoke(currentItem)
                                            Toast.makeText(context, "Saved to Media Vault", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Vault", fontSize = 11.sp)
                                    }

                                    // Post to Chat
                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator?.playChime(SoundEffectType.TAP)
                                            onPostToChat(currentItem)
                                            Toast.makeText(context, "Posted image to Playground chat", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("To Chat", fontSize = 11.sp)
                                    }

                                    // Attach to Note
                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator?.playChime(SoundEffectType.SUCCESS)
                                            onAttachToWorkspaceNote?.invoke(currentItem.prompt, currentItem.localUri)
                                            Toast.makeText(context, "Attached image to Workspace Note", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("To Note", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
