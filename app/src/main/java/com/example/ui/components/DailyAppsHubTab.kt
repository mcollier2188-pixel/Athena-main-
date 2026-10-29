package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyAppsRepository
import com.example.model.DailyApp
import com.example.model.DailyAppCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAppsHubTab(
    modifier: Modifier = Modifier,
    initialCategory: DailyAppCategory = DailyAppCategory.ALL,
    headerTitle: String = "Daily Apps & Tools Hub",
    headerSubtitle: String = "20 daily utilities, tools, games, and on-device applications"
) {
    val context = LocalContext.current
    val repository = remember { DailyAppsRepository.getInstance(context) }
    val pinnedIds by repository.pinnedAppIds.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by rememberSaveable { mutableStateOf(initialCategory.name) }
    val selectedCategory = remember(selectedCategoryName) {
        try { DailyAppCategory.valueOf(selectedCategoryName) } catch (e: Exception) { DailyAppCategory.ALL }
    }

    var isCompactGridView by rememberSaveable { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf<List<DailyApp>>(emptyList()) }
    var isLoadingInstalled by remember { mutableStateOf(false) }
    var activeModalApp by remember { mutableStateOf<DailyApp?>(null) }

    // Lazy load installed apps when On Device or Games category is selected
    LaunchedEffect(selectedCategory) {
        if ((selectedCategory == DailyAppCategory.INSTALLED || selectedCategory == DailyAppCategory.GAMES) && installedApps.isEmpty()) {
            isLoadingInstalled = true
            withContext(Dispatchers.IO) {
                val list = repository.getInstalledDeviceApps()
                withContext(Dispatchers.Main) {
                    installedApps = list
                    isLoadingInstalled = false
                }
            }
        }
    }

    val allAvailableApps = remember(repository.dailyCatalog, installedApps) {
        repository.dailyCatalog + installedApps
    }

    // Filtered apps
    val filteredApps = remember(allAvailableApps, selectedCategory, searchQuery, pinnedIds) {
        allAvailableApps.filter { app ->
            val matchesCategory = when (selectedCategory) {
                DailyAppCategory.ALL -> !app.isSystemInstalled
                DailyAppCategory.INSTALLED -> app.isSystemInstalled
                DailyAppCategory.GAMES -> app.category == DailyAppCategory.GAMES || app.tags.contains("game")
                DailyAppCategory.UTILITIES -> app.category == DailyAppCategory.UTILITIES
                DailyAppCategory.TOOLS -> app.category == DailyAppCategory.TOOLS
                else -> app.category == selectedCategory
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                app.name.lowercase().contains(q) ||
                    app.summary.lowercase().contains(q) ||
                    app.tags.any { it.lowercase().contains(q) } ||
                    app.category.displayName.lowercase().contains(q)
            }

            matchesCategory && matchesSearch
        }
    }

    val pinnedApps = remember(allAvailableApps, pinnedIds) {
        allAvailableApps.filter { pinnedIds.contains(it.id) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_apps_hub_scrollable"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Description
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 2. Search & Scan Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search utilities, tools, games, system apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("apps_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                FilledTonalIconButton(
                    onClick = {
                        isLoadingInstalled = true
                        installedApps = repository.getInstalledDeviceApps()
                        isLoadingInstalled = false
                        Toast.makeText(context, "Scanned ${installedApps.size} device apps", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Scan Device Apps")
                }
            }
        }

        // 3. Category Filter Chips (Utilities, Tools, Games, Productivity, Creative, Lifestyle, On Device)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DailyAppCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryName = category.name },
                        label = { Text(category.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // 4. Pinned / Quick Launch Bar
        if (pinnedApps.isNotEmpty() && searchQuery.isBlank() && selectedCategory != DailyAppCategory.INSTALLED) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⭐ Pinned Quick Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(pinnedApps, key = { it.id }) { app ->
                            ElevatedCard(
                                onClick = { activeModalApp = app },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.width(160.dp)
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
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    app.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Pinned",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = app.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Open tool",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Apps Count Indicator & Grid / List View Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${filteredApps.size} Apps in ${selectedCategory.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isLoadingInstalled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    }
                }

                // Layout Switcher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isCompactGridView = false }) {
                        Icon(
                            Icons.Default.ViewList,
                            contentDescription = "List View",
                            tint = if (!isCompactGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isCompactGridView = true }) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = "Grid View",
                            tint = if (isCompactGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 6. Apps Rendering (Compact Grid or Detailed Cards)
        if (filteredApps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No apps found", fontWeight = FontWeight.Bold)
                        Text("Try searching for another keyword or switch category filters.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (searchQuery.isNotEmpty()) {
                            Button(onClick = { searchQuery = "" }) {
                                Text("Clear Search")
                            }
                        }
                    }
                }
            }
        } else if (isCompactGridView) {
            // Render 2 items per row in Compact Grid
            val chunked = filteredApps.chunked(2)
            items(chunked) { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (app in rowApps) {
                        val isPinned = pinnedIds.contains(app.id)
                        ElevatedCard(
                            onClick = { activeModalApp = app },
                            modifier = Modifier.weight(1f).testTag("app_card_${app.id}"),
                            shape = RoundedCornerShape(12.dp)
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
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(app.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                    IconButton(
                                        onClick = { repository.togglePin(app.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            if (isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Pin",
                                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    if (rowApps.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Detailed List View
            items(filteredApps, key = { it.id }) { app ->
                val isPinned = pinnedIds.contains(app.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeModalApp = app }
                        .testTag("app_card_${app.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = app.icon,
                                    contentDescription = app.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (app.badge != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = app.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = app.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { repository.togglePin(app.id) }
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isPinned) "Unpin" else "Pin",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { activeModalApp = app },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open")
                        }
                    }
                }
            }
        }
    }

    // Modal popup when clicking any app
    activeModalApp?.let { app ->
        DailyAppMiniToolModal(
            app = app,
            onDismiss = { activeModalApp = null }
        )
    }
}
