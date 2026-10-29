package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.news.PositiveArticle
import com.example.news.PositiveCategory
import com.example.news.PositiveNewsService
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText
import kotlinx.coroutines.launch

/**
 * Positive Tech, Entertainment, Space & Culture News Feed Tab in Workspace.
 */
@Composable
fun PositiveNewsTab(
    toneGenerator: HarmonicToneGenerator,
    onSaveToWorkspaceNote: (title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(PositiveCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    val articles = remember { mutableStateListOf<PositiveArticle>().apply { addAll(PositiveNewsService.curatedArticles) } }

    var selectedArticleForDetails by remember { mutableStateOf<PositiveArticle?>(null) }
    var isExpandingGemini by remember { mutableStateOf(false) }
    var geminiExpandedAnalysis by remember { mutableStateOf<String?>(null) }

    // Filtered articles
    val displayedArticles = articles.filter { article ->
        (selectedCategory == PositiveCategory.ALL || article.category == selectedCategory) &&
        (searchQuery.isBlank() ||
                article.title.contains(searchQuery, ignoreCase = true) ||
                article.summary.contains(searchQuery, ignoreCase = true) ||
                article.tags.any { it.contains(searchQuery, ignoreCase = true) })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
    ) {
        // Top Uplifting News Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EchoCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = EchoCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Optimistic Horizons Feed",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = "High-impact tech breakthroughs, joyful comedy, & arts",
                                fontSize = 11.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    // Refresh Button
                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            isRefreshing = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(600)
                                // Shuffle or add fresh perspective
                                articles.clear()
                                articles.addAll(PositiveNewsService.curatedArticles.shuffled())
                                isRefreshing = false
                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                Toast.makeText(context, "Feed refreshed with latest positive headlines!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoCyan,
                            contentColor = EchoOnCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_refresh_positive_news")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = EchoOnCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search positive tech, comedy, space breakthroughs...", fontSize = 12.sp, color = EchoMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = EchoCyan, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = EchoMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("input_positive_news_search")
                )
            }
        }

        // Category Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(PositiveCategory.values()) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        selectedCategory = cat
                    },
                    leadingIcon = {
                        Text(cat.icon, fontSize = 12.sp)
                    },
                    label = {
                        Text(cat.displayName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EchoCyan,
                        selectedLabelColor = EchoOnCyan,
                        containerColor = EchoBgAlt,
                        labelColor = EchoText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = EchoCardStroke,
                        selectedBorderColor = EchoCyan,
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("filter_news_${cat.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // News Headline List
        if (displayedArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("☀️", fontSize = 42.sp)
                    Text("No matching positive headlines found", color = EchoText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Try clearing the search query or selecting 'All Horizons'.", color = EchoMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedArticles, key = { it.id }) { article ->
                    PositiveArticleCard(
                        article = article,
                        onOpen = {
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            selectedArticleForDetails = article
                            geminiExpandedAnalysis = null
                        },
                        onSaveNote = {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            val noteContent = buildString {
                                appendLine("☀️ POSITIVE NEWS HIGHLIGHT")
                                appendLine("Headline: ${article.title}")
                                appendLine("Category: ${article.category.displayName} (${article.category.icon})")
                                appendLine("Date/Source: ${article.timestamp} • ${article.sourceName}")
                                appendLine("Uplift Score: ${article.upliftScore}%")
                                appendLine()
                                appendLine("Summary:")
                                appendLine(article.summary)
                                appendLine()
                                appendLine("Full Story:")
                                appendLine(article.fullContent)
                                appendLine()
                                appendLine("Why It Matters:")
                                appendLine(article.whyItMatters)
                                appendLine()
                                appendLine("Tags: ${article.tags.joinToString(", ") { "#$it" }}")
                            }
                            onSaveToWorkspaceNote("News: ${article.title.take(30)}...", noteContent)
                            Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, article.title)
                                putExtra(Intent.EXTRA_TEXT, "☀️ ${article.title}\n\n${article.summary}\n\nVia ATHENA Optimistic Horizons Feed")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Positive Story"))
                        }
                    )
                }
            }
        }
    }

    // Full Story & Gemini Deep-Dive Modal
    selectedArticleForDetails?.let { article ->
        Dialog(
            onDismissRequest = {
                selectedArticleForDetails = null
                geminiExpandedAnalysis = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxSize(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                color = EchoBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(article.emoji, fontSize = 24.sp)
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(article.category.badgeColorHex).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = article.category.displayName.uppercase(),
                                        color = Color(article.category.badgeColorHex),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "${article.sourceName} • ${article.timestamp}",
                                    fontSize = 11.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                selectedArticleForDetails = null
                                geminiExpandedAnalysis = null
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoText)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Article Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = article.title,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            lineHeight = 25.sp
                        )

                        // Uplift Score Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoGreen.copy(alpha = 0.15f))
                                    .border(1.dp, EchoGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✨ ${article.upliftScore}% Uplift Index",
                                    color = EchoGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "⏱️ ${article.readTimeMinutes} min read",
                                color = EchoMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Summary Callout Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EchoBgAlt)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = article.summary,
                                color = EchoText,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Full Narrative Body
                        Text(
                            text = article.fullContent,
                            color = EchoText.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        // Why It Matters Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EchoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🌟 Why It Matters for Society",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.whyItMatters,
                                    fontSize = 12.sp,
                                    color = EchoText,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // Tags
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            article.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EchoBgAlt)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 10.sp,
                                        color = EchoCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                        // Gemini Expansion Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Gemini 2.5 Flash Deep Dive",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoCyan
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                                            isExpandingGemini = true
                                            coroutineScope.launch {
                                                val analysis = PositiveNewsService.expandArticleWithGemini(context, article)
                                                geminiExpandedAnalysis = analysis
                                                isExpandingGemini = false
                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                            }
                                        },
                                        enabled = !isExpandingGemini,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EchoCyan,
                                            contentColor = EchoOnCyan
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        if (isExpandingGemini) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(13.dp),
                                                color = EchoOnCyan,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(
                                                text = if (geminiExpandedAnalysis != null) "Regenerate" else "Expand Story",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (isExpandingGemini) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = EchoCyan,
                                        trackColor = EchoBg
                                    )
                                    Text(
                                        text = "Connecting to Gemini 2.5 Flash for optimistic horizon analysis...",
                                        fontSize = 11.sp,
                                        color = EchoMuted,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                } else if (geminiExpandedAnalysis != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EchoCard)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = geminiExpandedAnalysis!!,
                                            fontSize = 12.sp,
                                            color = EchoText,
                                            lineHeight = 18.sp
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tap 'Expand Story' to generate an in-depth optimistic breakdown, exploring 5-year societal ripples and technical breakthroughs.",
                                        fontSize = 11.sp,
                                        color = EchoMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Positive Story", "${article.title}\n\n${article.fullContent}")
                                clipboard.setPrimaryClip(clip)
                                toneGenerator.playChime(SoundEffectType.TAP)
                                Toast.makeText(context, "Copied story to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = EchoText)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 11.sp, color = EchoText)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                val noteContent = buildString {
                                    appendLine("☀️ POSITIVE NEWS STORY")
                                    appendLine("Headline: ${article.title}")
                                    appendLine("Category: ${article.category.displayName}")
                                    appendLine("Source: ${article.sourceName} • ${article.timestamp}")
                                    appendLine("Uplift Score: ${article.upliftScore}%")
                                    appendLine()
                                    appendLine("Story:")
                                    appendLine(article.fullContent)
                                    appendLine()
                                    appendLine("Why It Matters:")
                                    appendLine(article.whyItMatters)
                                    if (geminiExpandedAnalysis != null) {
                                        appendLine()
                                        appendLine("--- GEMINI 2.5 FLASH OPTIMISTIC DEEP DIVE ---")
                                        appendLine(geminiExpandedAnalysis)
                                    }
                                }
                                onSaveToWorkspaceNote("News: ${article.title.take(30)}...", noteContent)
                                Toast.makeText(context, "Saved story into Workspace Notes!", Toast.LENGTH_SHORT).show()
                                selectedArticleForDetails = null
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan,
                                contentColor = EchoOnCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PositiveArticleCard(
    article: PositiveArticle,
    onOpen: () -> Unit,
    onSaveNote: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("card_positive_article_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category Badge & Uplift Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(article.category.badgeColorHex).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${article.category.icon} ${article.category.displayName}",
                            color = Color(article.category.badgeColorHex),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "• ${article.timestamp}",
                        fontSize = 10.sp,
                        color = EchoMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EchoGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${article.upliftScore}% Uplift",
                        color = EchoGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = EchoText,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = article.summary,
                fontSize = 12.sp,
                color = EchoMuted,
                lineHeight = 17.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = article.sourceName,
                        fontSize = 10.sp,
                        color = EchoMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = EchoMuted, modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = onSaveNote,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Note", tint = EchoCyan, modifier = Modifier.size(15.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoCyan.copy(alpha = 0.12f))
                            .clickable { onOpen() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("Read", color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
