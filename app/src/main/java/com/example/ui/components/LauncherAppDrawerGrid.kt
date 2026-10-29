package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.launcher.AppDrawerFilter
import com.example.launcher.LauncherAppItem
import com.example.launcher.LauncherAppsManager
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.launch

/**
 * Dedicated LazyVerticalGrid-based App Drawer Component for the ATHENA Custom Launcher.
 * Features:
 * - Adaptive grid column scaling (4 to 8 columns across phones, foldables, tablets, and landscape).
 * - Alphabetical A-Z grouping with spanning section headers.
 * - Interactive A-Z fast scroller jump rail on the edge.
 * - Real-time query search by app label or package.
 * - Instant app launching with acoustic cybernetic feedback.
 * - Long-press actions (Pin to Cockpit Favorites, App Info in OS settings).
 * - One-tap Default Home Launcher configuration prompt.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherAppDrawerGrid(
    onDismiss: () -> Unit,
    toneGenerator: HarmonicToneGenerator? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { LauncherAppsManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    val installedApps by manager.installedApps.collectAsState()
    val isLoading by manager.isLoading.collectAsState()
    val isDefaultLauncher by manager.isDefaultLauncher.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppDrawerFilter.ALL) }
    var selectedAppForOptions by remember { mutableStateOf<LauncherAppItem?>(null) }
    var activeScrollerLetter by remember { mutableStateOf<Char?>(null) }

    // Filter applications by search query and category
    val filteredApps = remember(installedApps, searchQuery, selectedFilter) {
        installedApps.filter { app ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                app.label.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            }
            val matchesFilter = when (selectedFilter) {
                AppDrawerFilter.ALL -> true
                AppDrawerFilter.FAVORITES -> app.isFavorite
                AppDrawerFilter.SYSTEM -> app.isSystemApp
                AppDrawerFilter.USER -> !app.isSystemApp
            }
            matchesSearch && matchesFilter
        }
    }

    // Group apps alphabetically by their initial character
    val groupedApps = remember(filteredApps) {
        val groups = filteredApps.groupBy { app ->
            val first = app.label.trim().firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first else '#'
        }
        val sortedLetters = groups.keys.sortedWith(Comparator { a, b ->
            when {
                a == '#' && b != '#' -> 1
                a != '#' && b == '#' -> -1
                else -> a.compareTo(b)
            }
        })
        sortedLetters.associateWith { groups[it] ?: emptyList() }
    }

    // Calculate grid item indices for fast alphabetical jump
    val letterToGridIndex = remember(groupedApps) {
        var currentIndex = 0
        val map = mutableMapOf<Char, Int>()
        for ((char, apps) in groupedApps) {
            map[char] = currentIndex
            // 1 header item + apps.size items
            currentIndex += (1 + apps.size)
        }
        map
    }

    val alphabetList = remember { ('A'..'Z').toList() + listOf('#') }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("launcher_app_drawer_grid_container")
    ) {
        // 1. Header Bar: Title, Default Launcher Indicator, Refresh & Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EchoCyan.copy(alpha = 0.15f))
                        .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "App Drawer Matrix",
                        tint = EchoCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ATHENA OS // APP DRAWER",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isDefaultLauncher) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoGreen.copy(alpha = 0.2f))
                                    .border(1.dp, EchoGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEFAULT HOME",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    Text(
                        text = "${filteredApps.size} apps available • ${if (isDefaultLauncher) "Sovereign shell active" else "Secondary launcher mode"}",
                        fontSize = 12.sp,
                        color = EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isDefaultLauncher) {
                    OutlinedButton(
                        onClick = {
                            toneGenerator?.playChime(SoundEffectType.HARMONIC_PING)
                            manager.openDefaultLauncherPrompt(context)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                        border = BorderStroke(1.dp, EchoCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("drawer_set_default_home_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SET AS DEFAULT HOME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = {
                        toneGenerator?.playChime(SoundEffectType.TAP)
                        manager.refreshInstalledApps()
                    },
                    modifier = Modifier.testTag("app_drawer_grid_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh App List",
                        tint = EchoCyan
                    )
                }

                IconButton(
                    onClick = {
                        toneGenerator?.playChime(SoundEffectType.TAP)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("app_drawer_grid_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close App Drawer",
                        tint = EchoText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search installed apps by name or package...",
                    color = EchoMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EchoCyan
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Search",
                            tint = EchoMuted
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EchoCyan,
                unfocusedBorderColor = EchoCardStroke,
                focusedContainerColor = EchoCard,
                unfocusedContainerColor = EchoCard,
                focusedTextColor = EchoText,
                unfocusedTextColor = EchoText
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_drawer_grid_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppDrawerFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                val count = when (filter) {
                    AppDrawerFilter.ALL -> installedApps.size
                    AppDrawerFilter.FAVORITES -> installedApps.count { it.isFavorite }
                    AppDrawerFilter.SYSTEM -> installedApps.count { it.isSystemApp }
                    AppDrawerFilter.USER -> installedApps.count { !it.isSystemApp }
                }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedFilter = filter
                        toneGenerator?.playChime(SoundEffectType.TAP)
                    },
                    label = {
                        Text("${filter.label} ($count)", fontSize = 12.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EchoCyan,
                        selectedLabelColor = EchoOnCyan,
                        containerColor = EchoCard,
                        labelColor = EchoText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = EchoCardStroke,
                        selectedBorderColor = EchoCyan
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Main Grid Area + A-Z Fast Scroller Jump Rail
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLoading && installedApps.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = EchoCyan, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Scanning installed applications on device...",
                        color = EchoMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else if (filteredApps.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = EchoMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "No applications match \"$searchQuery\"",
                        color = EchoText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Try a different search query or select 'All Apps'",
                        color = EchoMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // LazyVerticalGrid with Adaptive Columns (optimal for landscape & tablets)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 96.dp),
                        state = gridState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("lazy_vertical_grid_app_drawer"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 6.dp, bottom = 28.dp)
                    ) {
                        groupedApps.forEach { (char, apps) ->
                            // Full-width Alphabetical Section Header
                            item(
                                key = "section_$char",
                                span = { GridItemSpan(maxLineSpan) }
                            ) {
                                AlphabeticalSectionHeader(char = char, count = apps.size)
                            }

                            // Application items under this letter
                            items(apps, key = { it.packageName }) { app ->
                                AppGridTile(
                                    app = app,
                                    onClick = {
                                        toneGenerator?.playChime(SoundEffectType.TAP)
                                        manager.launchApp(context, app.packageName)
                                    },
                                    onLongClick = {
                                        toneGenerator?.playChime(SoundEffectType.CHIME)
                                        selectedAppForOptions = app
                                    }
                                )
                            }
                        }
                    }

                    // Interactive A-Z Fast Scroller Jump Rail
                    FastAlphabetScrollerRail(
                        alphabet = alphabetList,
                        availableLetters = groupedApps.keys,
                        activeLetter = activeScrollerLetter,
                        onLetterSelected = { char ->
                            val targetIndex = letterToGridIndex[char]
                            if (targetIndex != null) {
                                activeScrollerLetter = char
                                toneGenerator?.playChime(SoundEffectType.TAP)
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(targetIndex)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Long Press Options Dialog
    selectedAppForOptions?.let { app ->
        AppOptionsDialog(
            app = app,
            onDismiss = { selectedAppForOptions = null },
            onLaunch = {
                manager.launchApp(context, app.packageName)
                selectedAppForOptions = null
            },
            onToggleFavorite = {
                manager.toggleFavorite(app.packageName)
                selectedAppForOptions = null
            },
            onOpenAppInfo = {
                manager.openAppInfo(context, app.packageName)
                selectedAppForOptions = null
            }
        )
    }
}

/**
 * Spanning Section Header for Alphabetical Categories (A, B, C... #)
 */
@Composable
private fun AlphabeticalSectionHeader(
    char: Char,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(EchoCyan.copy(alpha = 0.2f))
                .border(1.dp, EchoCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = char.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EchoCyan,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$count ${if (count == 1) "app" else "apps"}",
            fontSize = 11.sp,
            color = EchoMuted,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(EchoCardStroke)
        )
    }
}

/**
 * High-Density Adaptive App Tile in LazyVerticalGrid
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridTile(
    app: LauncherAppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                1.dp,
                if (app.isFavorite) EchoCyan.copy(alpha = 0.5f) else EchoCardStroke,
                RoundedCornerShape(12.dp)
            )
            .testTag("app_tile_${app.packageName}"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(EchoBgAlt)
                    .border(1.dp, EchoCardStroke, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = EchoCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (app.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pinned Favorite",
                        tint = EchoCyan,
                        modifier = Modifier
                            .size(15.dp)
                            .align(Alignment.TopEnd)
                            .padding(1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = EchoText,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            if (app.isSystemApp) {
                Text(
                    text = "SYSTEM",
                    fontSize = 8.sp,
                    color = EchoMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Vertical A-Z Fast Scroller Rail
 */
@Composable
private fun FastAlphabetScrollerRail(
    alphabet: List<Char>,
    availableLetters: Set<Char>,
    activeLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        alphabet.forEach { char ->
            val isAvailable = availableLetters.contains(char)
            val isActive = activeLetter == char

            Text(
                text = char.toString(),
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else if (isAvailable) FontWeight.SemiBold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                color = when {
                    isActive -> EchoCyan
                    isAvailable -> EchoText
                    else -> EchoMuted.copy(alpha = 0.3f)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = isAvailable) { onLetterSelected(char) }
                    .padding(vertical = 1.dp, horizontal = 2.dp)
            )
        }
    }
}

/**
 * Long-Press Options Dialog for Quick Management
 */
@Composable
private fun AppOptionsDialog(
    app: LauncherAppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenAppInfo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = app.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoText
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Package: ${app.packageName}",
                    fontSize = 12.sp,
                    color = EchoMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Type: ${if (app.isSystemApp) "System Android App" else "User Installed App"}",
                    fontSize = 12.sp,
                    color = EchoMuted,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onLaunch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                ) {
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch Application")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoText)
                ) {
                    Icon(
                        imageVector = if (app.isFavorite) Icons.Default.StarBorder else Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (app.isFavorite) EchoRed else EchoCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (app.isFavorite) "Unpin from Cockpit Favorites" else "Pin to Cockpit Favorites")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenAppInfo,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoMuted)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System App Details (Android Settings)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = EchoCyan)
            }
        },
        containerColor = EchoCard,
        shape = RoundedCornerShape(14.dp)
    )
}
