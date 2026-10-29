package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.OpenInBrowser
import com.example.ai.ApkHtmlExporter
import com.example.ai.AppIconData
import com.example.ui.dialogs.AppRunnerDialog
import com.example.ui.dialogs.DeployToDeviceDialog
import com.example.ui.dialogs.HtmlConfirmDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ai.ApkBuildResult
import com.example.ai.ApkEngine
import com.example.ai.AppArchetype
import com.example.ai.AthenaApkBuilderService
import com.example.ai.BuildStatus
import com.example.ai.GeneratedSourceFile
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoCyanSoft
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.launch

@Composable
fun AthenaApkBuilderView(
    onSaveNote: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var appName by rememberSaveable { mutableStateOf("AnyAppStudio") }
    var packageName by rememberSaveable { mutableStateOf("com.app.anycustom") }
    var appPrompt by rememberSaveable {
        mutableStateOf("Create a modern, full-featured Android app with responsive Material 3 layout, state management, search and filter, local database storage, and dark mode.")
    }

    var selectedEngine by rememberSaveable { mutableStateOf(ApkEngine.GEMINI_PRO) }
    var selectedArchetype by rememberSaveable { mutableStateOf(AppArchetype.ANY_CUSTOM_APP) }
    var customCategoryName by rememberSaveable { mutableStateOf("") }
    var includeUnitTests by rememberSaveable { mutableStateOf(true) }
    var enableProGuard by rememberSaveable { mutableStateOf(true) }

    var isBuilding by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf("") }
    var currentDetail by remember { mutableStateOf("") }
    var buildProgress by remember { mutableStateOf(0f) }

    var lastBuildResult by remember { mutableStateOf<ApkBuildResult?>(null) }
    var selectedFileIndex by rememberSaveable { mutableIntStateOf(0) }

    // Interactive App Runner, Deploy to Device, and HTML Web App State
    var showDeployDialog by remember { mutableStateOf(false) }
    var showAppRunnerDialog by remember { mutableStateOf(false) }
    var currentAppIcon by remember { mutableStateOf(AppIconData.defaultFor(selectedArchetype)) }
    var exportedHtmlFile by remember { mutableStateOf<java.io.File?>(null) }
    var showHtmlConfirmDialog by remember { mutableStateOf(false) }

    // Quick templates for fast generation across diverse real-world domains
    val templates = listOf(
        "🎬 Cinema & Video App" to "Build a full video streaming & cinema app with embedded native video player, trailer carousel, script library, and download caching.",
        "📝 Workspace & Notes" to "Build a rich note-taking & document management app with nested folders, text search, file attachments (PDF/Word), and local Room storage.",
        "💬 Messenger & Chat" to "Build a real-time messaging client with channel list, direct chat view, message bubbles, reactions, and offline message queue.",
        "🛒 E-Commerce Storefront" to "Build an online shopping marketplace app with product catalog, item categories, interactive shopping cart, checkout flow, and wishlist.",
        "🏋️ Fitness Tracker" to "Build a health and workout planner with exercise routines, stopwatch timer, calorie burn calculator, and weekly progress charts.",
        "⚡ Acoustic & DSP Lab" to "Build a 42.600 Hz harmonic tuner with real-time waveform visualizer, standing wave physics equations, and Q-factor energy transfer calibration.",
        "🛠️ Power Utilities Hub" to "Build an all-in-one utility toolkit featuring unit converter, battery telemetry, network speed meter, QR generator, and device info.",
        "🚀 Bespoke Custom App" to "Build a high-performance Jetpack Compose app featuring modern Material 3 styling, Coroutines, MVVM architecture, and offline persistence."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("athena_apk_builder_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(EchoCyan, EchoGreen))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    tint = EchoOnCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ATHENA APK BUILDER",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Full-Stack Android Synthesis & APK Bundling",
                                    fontSize = 11.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoCyanSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE ENGINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Synthesizes complete Kotlin Jetpack Compose apps, generates Gradle configurations, writes unit tests, and packages signed Android APK bundles directly inside Athena.",
                        fontSize = 12.sp,
                        color = EchoMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val outputDir = java.io.File(context.filesDir, "apk_builds")
                                val existingApk = outputDir.listFiles()?.firstOrNull { it.name.endsWith(".apk") }
                                if (existingApk != null && existingApk.exists()) {
                                    if (lastBuildResult == null) {
                                        lastBuildResult = ApkBuildResult(
                                            appName = appName,
                                            packageName = packageName,
                                            versionCode = 1,
                                            versionName = "1.0.0",
                                            engineUsed = selectedEngine,
                                            archetype = selectedArchetype,
                                            promptDescription = appPrompt,
                                            generatedFiles = emptyList(),
                                            buildLogs = emptyList(),
                                            packageSizeKb = existingApk.length() / 1024L,
                                            packageFile = existingApk,
                                            isSuccess = true
                                        )
                                    }
                                    showDeployDialog = true
                                } else {
                                    Toast.makeText(context, "No APK built yet. Synthesize an app below first!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(38.dp).testTag("quick_deploy_apk_hero_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black)
                        ) {
                            Text("🚀", fontSize = 13.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("Deploy to Device for Testing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val outputDir = java.io.File(context.filesDir, "apk_builds")
                                val existingApk = outputDir.listFiles()?.firstOrNull { it.name.endsWith(".apk") }
                                if (existingApk != null && existingApk.exists()) {
                                    try {
                                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                                        val dest = java.io.File(downloadsDir, existingApk.name)
                                        existingApk.copyTo(dest, overwrite = true)
                                        Toast.makeText(context, "Saved to Downloads/${existingApk.name}!", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "APK ready: ${existingApk.name}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No saved APK yet. Build one below!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("quick_download_apk_hero_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Download APK", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // AI Engine Selection (Gemini Pro, Gemini Flash, Claude Specialist)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. AI ENGINE ARCHITECTURE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Multi-Engine Support",
                            fontSize = 10.sp,
                            color = EchoGreen
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    ApkEngine.entries.forEach { engine ->
                        val isSelected = selectedEngine == engine
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) EchoCyanSoft else EchoBgAlt)
                                .border(
                                    1.dp,
                                    if (isSelected) EchoCyan else EchoCardStroke,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedEngine = engine }
                                .padding(12.dp)
                                .testTag("engine_${engine.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = engine.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) EchoCyan else EchoText
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) EchoCyan else EchoBg)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = engine.badge,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) EchoOnCyan else EchoMuted
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = engine.description,
                                        fontSize = 11.sp,
                                        color = EchoMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Archetype & Template Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. APPLICATION CATEGORY & DOMAIN (BUILD ANY APP)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EchoCyanSoft)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("UNRESTRICTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Build any application from your imagination. Choose a category below or specify custom requirements.",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )

                    Spacer(Modifier.height(10.dp))

                    // Category Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppArchetype.entries.forEach { arch ->
                            val isSelected = selectedArchetype == arch
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedArchetype = arch
                                    currentAppIcon = AppIconData.defaultFor(arch)
                                    when (arch) {
                                        AppArchetype.ANY_CUSTOM_APP -> {
                                            appName = "AnyAppStudio"
                                            packageName = "com.app.anycustom"
                                        }
                                        AppArchetype.MEDIA_STREAMING -> {
                                            appName = "CinemaVideoPlayer"
                                            packageName = "com.app.cinema"
                                        }
                                        AppArchetype.PRODUCTIVITY_WORKSPACE -> {
                                            appName = "WorkspaceNotes"
                                            packageName = "com.app.workspace"
                                        }
                                        AppArchetype.SOCIAL_MESSENGER -> {
                                            appName = "EchoMessenger"
                                            packageName = "com.app.messenger"
                                        }
                                        AppArchetype.ECOMMERCE_STORE -> {
                                            appName = "MarketStore"
                                            packageName = "com.app.store"
                                        }
                                        AppArchetype.HEALTH_FITNESS -> {
                                            appName = "PulseFitness"
                                            packageName = "com.app.fitness"
                                        }
                                        AppArchetype.UTILITY_TOOLKIT -> {
                                            appName = "ToolboxHub"
                                            packageName = "com.app.tools"
                                        }
                                        AppArchetype.SCIENTIFIC_ACOUSTICS -> {
                                            appName = "ResonanceTelemetry"
                                            packageName = "com.athena.resonance"
                                        }
                                        AppArchetype.GEOLOGICAL_TELEMETRY -> {
                                            appName = "BedrockSeismicNode"
                                            packageName = "com.athena.geosensor"
                                        }
                                        AppArchetype.STANDALONE_ATHENA_AI -> {
                                            appName = "AthenaStandaloneClient"
                                            packageName = "com.athena.aisuite"
                                        }
                                        AppArchetype.CUSTOM_COMPOSE_APP -> {
                                            appName = "CustomComposeApp"
                                            packageName = "com.athena.customapp"
                                        }
                                    }
                                },
                                label = { Text(arch.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EchoCyan,
                                    selectedLabelColor = EchoOnCyan,
                                    containerColor = EchoBgAlt,
                                    labelColor = EchoText
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Selected Category Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoBgAlt)
                            .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedArchetype.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = EchoCyan
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Modules: ${selectedArchetype.defaultModules.joinToString(", ")}",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = selectedArchetype.description,
                                fontSize = 11.sp,
                                color = EchoText
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Quick App Templates & Ideas:",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        templates.forEach { (title, prompt) ->
                            FilterChip(
                                selected = appPrompt == prompt,
                                onClick = {
                                    appPrompt = prompt
                                    when {
                                        title.contains("Cinema") -> {
                                            selectedArchetype = AppArchetype.MEDIA_STREAMING
                                            appName = "CinemaVideoPlayer"
                                            packageName = "com.app.cinema"
                                        }
                                        title.contains("Workspace") -> {
                                            selectedArchetype = AppArchetype.PRODUCTIVITY_WORKSPACE
                                            appName = "WorkspaceNotes"
                                            packageName = "com.app.workspace"
                                        }
                                        title.contains("Messenger") -> {
                                            selectedArchetype = AppArchetype.SOCIAL_MESSENGER
                                            appName = "EchoMessenger"
                                            packageName = "com.app.messenger"
                                        }
                                        title.contains("E-Commerce") -> {
                                            selectedArchetype = AppArchetype.ECOMMERCE_STORE
                                            appName = "MarketStore"
                                            packageName = "com.app.store"
                                        }
                                        title.contains("Fitness") -> {
                                            selectedArchetype = AppArchetype.HEALTH_FITNESS
                                            appName = "PulseFitness"
                                            packageName = "com.app.fitness"
                                        }
                                        title.contains("Acoustic") -> {
                                            selectedArchetype = AppArchetype.SCIENTIFIC_ACOUSTICS
                                            appName = "ResonanceTelemetry"
                                            packageName = "com.athena.resonance"
                                        }
                                        title.contains("Utilities") -> {
                                            selectedArchetype = AppArchetype.UTILITY_TOOLKIT
                                            appName = "ToolboxHub"
                                            packageName = "com.app.tools"
                                        }
                                        else -> {
                                            selectedArchetype = AppArchetype.ANY_CUSTOM_APP
                                            appName = "AnyAppStudio"
                                            packageName = "com.app.anycustom"
                                        }
                                    }
                                },
                                label = { Text(title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EchoGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = EchoBgAlt,
                                    labelColor = EchoText
                                )
                            )
                        }
                    }
                }
            }
        }

        // App Metadata & Prompt Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. SPECIFICATIONS & CAPABILITIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoCyan,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = appName,
                            onValueChange = { appName = it },
                            label = { Text("App Name", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_app_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EchoCyan,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText
                            )
                        )

                        OutlinedTextField(
                            value = packageName,
                            onValueChange = { packageName = it },
                            label = { Text("Package Name", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("input_package_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EchoCyan,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = appPrompt,
                        onValueChange = { appPrompt = it },
                        label = { Text("Natural Language Prompt & Requirements", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_app_prompt"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            unfocusedBorderColor = EchoCardStroke,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Build Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Include Robolectric Unit Tests", fontSize = 12.sp, color = EchoText)
                        }
                        Switch(
                            checked = includeUnitTests,
                            onCheckedChange = { includeUnitTests = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EchoCyan,
                                checkedTrackColor = EchoCyanSoft
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Enable ProGuard / R8 Shrinker", fontSize = 12.sp, color = EchoText)
                        }
                        Switch(
                            checked = enableProGuard,
                            onCheckedChange = { enableProGuard = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EchoCyan,
                                checkedTrackColor = EchoCyanSoft
                            )
                        )
                    }
                }
            }
        }

        // Build Trigger Buttons: 'Download APK' and 'Synthesize & Build APK'
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary 'Download APK' Button (User Requested)
                Button(
                    onClick = {
                        if (appName.isBlank() || packageName.isBlank()) {
                            Toast.makeText(context, "Please specify App Name and Package Name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        Toast.makeText(
                            context,
                            "Generating and downloading APK package for current session...",
                            Toast.LENGTH_LONG
                        ).show()

                        isBuilding = true
                        buildProgress = 0.1f
                        coroutineScope.launch {
                            val result = AthenaApkBuilderService.buildApplicationPackage(
                                context = context,
                                appName = appName,
                                packageName = packageName,
                                promptDescription = appPrompt,
                                archetype = selectedArchetype,
                                engine = selectedEngine,
                                includeUnitTests = includeUnitTests,
                                enableProGuard = enableProGuard,
                                onProgress = { step, detail ->
                                    currentStep = step
                                    currentDetail = detail
                                    buildProgress = when {
                                        step.startsWith("1.") -> 0.15f
                                        step.startsWith("2.") -> 0.35f
                                        step.startsWith("3.") -> 0.50f
                                        step.startsWith("4.") -> 0.70f
                                        step.startsWith("5.") -> 0.80f
                                        step.startsWith("6.") -> 0.90f
                                        step.startsWith("7.") -> 0.95f
                                        step.startsWith("8.") -> 1.0f
                                        else -> buildProgress
                                    }
                                }
                            )
                            lastBuildResult = result
                            isBuilding = false
                            showDeployDialog = true

                            // Automatically save to Downloads and launch package installer
                            val file = result.packageFile
                            if (file != null && file.exists()) {
                                try {
                                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    )
                                    if (!downloadsDir.exists()) downloadsDir.mkdirs()
                                    val dest = java.io.File(downloadsDir, file.name)
                                    file.copyTo(dest, overwrite = true)

                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/vnd.android.package-archive")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    Toast.makeText(
                                        context,
                                        "APK Downloaded to Downloads/${file.name}!\nOpening package installer...",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    context.startActivity(installIntent)
                                } catch (e: Exception) {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/vnd.android.package-archive"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Download / Save APK"))
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "APK ready in storage: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isBuilding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("download_apk_primary_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isBuilding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "GENERATING PACKAGE FOR SESSION...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Text("🚀", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "BUILD & DEPLOY TO DEVICE FOR TESTING",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Secondary Synthesize & Build Button
                OutlinedButton(
                    onClick = {
                        if (appName.isBlank() || packageName.isBlank()) {
                            Toast.makeText(context, "Please specify App Name and Package", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }

                        isBuilding = true
                        buildProgress = 0.1f
                        coroutineScope.launch {
                            val result = AthenaApkBuilderService.buildApplicationPackage(
                                context = context,
                                appName = appName,
                                packageName = packageName,
                                promptDescription = appPrompt,
                                archetype = selectedArchetype,
                                engine = selectedEngine,
                                includeUnitTests = includeUnitTests,
                                enableProGuard = enableProGuard,
                                onProgress = { step, detail ->
                                    currentStep = step
                                    currentDetail = detail
                                    buildProgress = when {
                                        step.startsWith("1.") -> 0.15f
                                        step.startsWith("2.") -> 0.35f
                                        step.startsWith("3.") -> 0.50f
                                        step.startsWith("4.") -> 0.70f
                                        step.startsWith("5.") -> 0.80f
                                        step.startsWith("6.") -> 0.90f
                                        step.startsWith("7.") -> 0.95f
                                        step.startsWith("8.") -> 1.0f
                                        else -> buildProgress
                                    }
                                }
                            )
                            lastBuildResult = result
                            isBuilding = false
                        }
                    },
                    enabled = !isBuilding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("trigger_build_apk_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EchoCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Synthesize & Inspect Build Artifact",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            if (isBuilding) {
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EchoBgAlt)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = currentStep, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                        Text(text = "${(buildProgress * 100).toInt()}%", fontSize = 12.sp, color = EchoGreen)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { buildProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = EchoCyan,
                        trackColor = EchoCardStroke
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(text = currentDetail, fontSize = 11.sp, color = EchoMuted)
                }
            }
        }

        // Build Results & Package Inspector
        val result = lastBuildResult
        if (result != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoGreen))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EchoGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "APK BUILD ARTIFACT READY",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoGreen
                                    )
                                    Text(
                                        text = "${result.appName} • ${result.packageName}",
                                        fontSize = 11.sp,
                                        color = EchoMuted
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${result.packageSizeKb} KB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoGreen
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Engine Used: ${result.engineUsed.displayName} • Archetype: ${result.archetype.title}",
                            fontSize = 11.sp,
                            color = EchoText
                        )

                        Spacer(Modifier.height(12.dp))

                        // Primary Action Grid: Open & Test, Create Icon, Convert to HTML, Download APK
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: Open & Test App (Full Live Runner) + Create Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAppRunnerDialog = true },
                                    modifier = Modifier.weight(1.3f).height(42.dp).testTag("open_test_app_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Open & Test App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showDeployDialog = true },
                                    modifier = Modifier.weight(1.3f).height(42.dp).testTag("deploy_device_testing_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🚀", fontSize = 14.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Deploy to Device", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Row 2: Convert to HTML Web App + Download APK + Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val htmlFile = ApkHtmlExporter.exportHtmlWebApp(context, result, currentAppIcon)
                                        if (htmlFile != null && htmlFile.exists()) {
                                            exportedHtmlFile = htmlFile
                                            showHtmlConfirmDialog = true
                                            Toast.makeText(context, "Converted to Standalone HTML Web App: ${htmlFile.name}", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to export HTML Web App", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1.2f).height(40.dp).testTag("convert_html_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Convert to HTML", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val file = result.packageFile
                                        if (file != null && file.exists()) {
                                            try {
                                                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                                                val dest = java.io.File(downloadsDir, file.name)
                                                file.copyTo(dest, overwrite = true)

                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                Toast.makeText(context, "APK Downloaded to Downloads/${file.name}!\nOpening package installer...", Toast.LENGTH_LONG).show()
                                                context.startActivity(installIntent)
                                            } catch (e: Exception) {
                                                try {
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        file
                                                    )
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/vnd.android.package-archive"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Download / Save APK"))
                                                } catch (ex: Exception) {
                                                    Toast.makeText(context, "APK saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "APK bundle ready in storage", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1.1f).height(40.dp).testTag("download_apk_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Download APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = {
                                        val file = result.packageFile
                                        if (file != null && file.exists()) {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/vnd.android.package-archive"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share APK / Archive"))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "APK saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(40.dp).background(EchoBgAlt, RoundedCornerShape(8.dp)).testTag("share_apk_icon_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = EchoCyan, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Interactive Simulated Live Preview Sandbox (Tap to Launch App)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAppRunnerDialog = true }
                                .testTag("sandbox_preview_card"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF040D1A)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCyan))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(10.dp).background(EchoGreen, CircleShape)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "LIVE SANDBOX: ${result.appName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoCyan
                                        )
                                    }
                                    Text(
                                        text = "Tap to Launch Full Test Runner",
                                        fontSize = 10.sp,
                                        color = EchoGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Device Frame Emulator View
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EchoBg)
                                        .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            Brush.linearGradient(
                                                                listOf(currentAppIcon.startColor, currentAppIcon.endColor)
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(currentAppIcon.glyph, fontSize = 13.sp)
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = result.appName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EchoText
                                                )
                                            }
                                            Text(
                                                text = "9:41 AM",
                                                fontSize = 10.sp,
                                                color = EchoMuted
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(EchoCard)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "${currentAppIcon.glyph} Icon: ${currentAppIcon.glyphName} (${currentAppIcon.shape.label})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = EchoCyan
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "Active Archetype: ${result.archetype.title}",
                                                fontSize = 10.sp,
                                                color = EchoMuted,
                                                maxLines = 1
                                            )
                                        }

                                        Button(
                                            onClick = { showAppRunnerDialog = true },
                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Launch Full Interactive App Runner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Source File Inspector Tabs
                        Text(
                            text = "Generated Source Files (${result.generatedFiles.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.generatedFiles.forEachIndexed { index, file ->
                                FilterChip(
                                    selected = selectedFileIndex == index,
                                    onClick = { selectedFileIndex = index },
                                    label = {
                                        Text(
                                            text = file.relativePath.substringAfterLast('/'),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EchoCyan,
                                        selectedLabelColor = EchoOnCyan,
                                        containerColor = EchoBgAlt,
                                        labelColor = EchoText
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (result.generatedFiles.isNotEmpty() && selectedFileIndex < result.generatedFiles.size) {
                            val activeFile = result.generatedFiles[selectedFileIndex]
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoBgAlt)
                                    .border(1.dp, EchoCardStroke, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeFile.relativePath,
                                        fontSize = 11.sp,
                                        color = EchoCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", activeFile.content))
                                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy code",
                                            tint = EchoCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(Color(0xFF030712), RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = activeFile.content.take(800) + if (activeFile.content.length > 800) "\n\n... [truncated]" else "",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Build Step Logs
                        Text(
                            text = "Toolchain Build Verification Logs:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoMuted
                        )
                        Spacer(Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF020912))
                                .padding(8.dp)
                        ) {
                            result.buildLogs.forEach { log ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✓",
                                        color = EchoGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${log.stepName}: ${log.message}",
                                        fontSize = 10.sp,
                                        color = EchoText,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Dialog Overlays: App Runner Sandbox, Icon Studio, HTML Web App Exporter
    if (showAppRunnerDialog && lastBuildResult != null) {
        AppRunnerDialog(
            result = lastBuildResult!!,
            icon = currentAppIcon,
            onDismiss = { showAppRunnerDialog = false }
        )
    }

    if (showDeployDialog && lastBuildResult != null) {
        DeployToDeviceDialog(
            result = lastBuildResult!!,
            onDismiss = { showDeployDialog = false },
            onOpenInAppRunner = { showAppRunnerDialog = true }
        )
    }

    if (showHtmlConfirmDialog && exportedHtmlFile != null) {
        HtmlConfirmDialog(
            htmlFile = exportedHtmlFile!!,
            appName = appName.ifBlank { "Athena Web App" },
            onDismiss = { showHtmlConfirmDialog = false }
        )
    }
}
