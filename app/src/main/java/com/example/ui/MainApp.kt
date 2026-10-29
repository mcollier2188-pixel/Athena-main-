package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.DolaAiMode
import com.example.audio.HarmonicToneGenerator
import com.example.ui.components.DolaAiDialog
import androidx.compose.material.icons.filled.Apps
import com.example.ui.components.AppDrawerSheet
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.FrequencyScreen
import com.example.ui.screens.NetworkScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText
import com.example.ui.theme.EchoRed
import com.example.ui.viewmodel.WorkspaceViewModel

import android.app.Activity
import android.app.Application
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import com.example.auth.ConnectedAccountsManager
import com.example.system.update.AthenaUpdateManager
import com.example.ui.components.AthenaSystemUpdateConsole
import com.example.ui.components.ConnectedAccountSheet
import com.example.ui.theme.EchoAmber
import com.example.ui.components.DeviceMirroringDialog
import com.example.ui.components.DolaBrowserDialog
import com.example.ui.components.QuickAudioControlsDialog
import com.example.util.AppOrientation
import com.example.util.OrientationManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModelProvider

import com.example.ui.screens.AthenaScreen
import com.example.ui.screens.GoogleGatewayScreen
import com.example.ui.screens.GoogleSuiteScreen
import com.example.ui.screens.BrainstormingLabScreen
import com.example.ui.components.SettingsDialog
import com.example.ui.components.DockedMiniPlayerBar
import com.example.ui.components.GlobalVideoPlaybackManager
import com.example.audio.SoundEffectType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DocumentScanner
import com.example.ui.components.DocumentScannerDialog
import com.example.ui.components.SystemSettingsDrawerSheet
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.EchoAmber
import kotlinx.coroutines.launch

enum class MainTab(val index: Int, val title: String) {
    ARCHIVE(0, "Archive"),
    ATHENA(1, "ATHENA"),
    WORKSPACE(2, "Workspace"),
    BRAIN(3, "Brainstorming"),
    GOOGLE(4, "Creative Hub")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    currentUserId: String? = null,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    isDarkTheme: Boolean = true,
    onToggleDarkTheme: () -> Unit = {},
    onReturnToEntrance: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as Application

    val workspaceViewModel: WorkspaceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WorkspaceViewModel(app, null, null) as T
            }
        }
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(MainTab.ARCHIVE.index) }
    var isTopBarVisible by rememberSaveable { mutableStateOf(true) }
    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showDocumentScannerDialog by remember { mutableStateOf(false) }
    var scannedCouncilTopic by remember { mutableStateOf<String?>(null) }
    var scannedDocumentAnalysis by remember { mutableStateOf<String?>(null) }

    val isTonePlaying by toneGenerator.isPlayingState.collectAsState()
    var showDolaAssistantDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGlobalAppDrawer by remember { mutableStateOf(false) }
    var showSystemUpdateConsole by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showQuickAudioControls by remember { mutableStateOf(false) }
    var activeGlobalBrowserUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val recovery = AthenaUpdateManager.checkAndConsumeUpdateRecovery(context)
        if (recovery != null) {
            android.widget.Toast.makeText(
                context,
                "ATHENA Reboot Successful: State restored from update checkpoint (${recovery.updateTitle})",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    var showMirroringDialog by remember { mutableStateOf(false) }
    val orientationManager = remember { OrientationManager.getInstance(context) }
    val currentOrientation by orientationManager.currentOrientation.collectAsState()

    val accountsManager = remember { ConnectedAccountsManager.getInstance(context) }
    val currentUser by accountsManager.currentUser.collectAsState()

    val saveableStateHolder = rememberSaveableStateHolder()
    val visitedTabs = remember { mutableStateListOf(MainTab.ARCHIVE.index) }
    LaunchedEffect(selectedTab) {
        if (!visitedTabs.contains(selectedTab)) {
            visitedTabs.add(selectedTab)
        }
    }

    val isMiniplayerVisible by GlobalVideoPlaybackManager.isMiniplayerVisible.collectAsState()
    val isFullPlayerRequested by GlobalVideoPlaybackManager.isFullPlayerRequested.collectAsState()

    LaunchedEffect(isFullPlayerRequested) {
        if (isFullPlayerRequested) {
            selectedTab = MainTab.GOOGLE.index
            GlobalVideoPlaybackManager.consumeFullPlayerRequest()
        }
    }

    // Standard Launcher & APK Back Navigation: dismiss drawer, dialogs, miniplayer, or return to Home tab
    val canGoBackInApp = drawerState.isOpen || showGlobalAppDrawer || showSystemUpdateConsole || showDocumentScannerDialog || isMiniplayerVisible ||
        activeGlobalBrowserUrl != null || showQuickAudioControls || showAccountSheet ||
        showMirroringDialog || showExitConfirmationDialog || showSettingsDialog || showDolaAssistantDialog ||
        selectedTab != MainTab.ARCHIVE.index
    BackHandler(enabled = canGoBackInApp) {
        when {
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
            showGlobalAppDrawer -> showGlobalAppDrawer = false
            showSystemUpdateConsole -> showSystemUpdateConsole = false
            showDocumentScannerDialog -> showDocumentScannerDialog = false
            isMiniplayerVisible -> GlobalVideoPlaybackManager.dismissMiniplayer()
            activeGlobalBrowserUrl != null -> activeGlobalBrowserUrl = null
            showQuickAudioControls -> showQuickAudioControls = false
            showAccountSheet -> showAccountSheet = false
            showMirroringDialog -> showMirroringDialog = false
            showExitConfirmationDialog -> showExitConfirmationDialog = false
            showSettingsDialog -> showSettingsDialog = false
            showDolaAssistantDialog -> showDolaAssistantDialog = false
            selectedTab != MainTab.ARCHIVE.index -> selectedTab = MainTab.ARCHIVE.index
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SystemSettingsDrawerSheet(
                toneGenerator = toneGenerator,
                isTopBarVisible = isTopBarVisible,
                onToggleTopBar = { isTopBarVisible = !isTopBarVisible },
                isBottomBarVisible = isBottomBarVisible,
                onToggleBottomBar = { isBottomBarVisible = !isBottomBarVisible },
                onToggleFullscreen = {
                    val next = !(isTopBarVisible && isBottomBarVisible)
                    isTopBarVisible = next
                    isBottomBarVisible = next
                },
                orientationManager = orientationManager,
                currentOrientation = currentOrientation,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                onOpenDocumentScanner = { showDocumentScannerDialog = true },
                onOpenDolaAssistant = { showDolaAssistantDialog = true },
                onOpenAccountSheet = { showAccountSheet = true },
                onOpenMirroring = { showMirroringDialog = true },
                onOpenBrowser = { activeGlobalBrowserUrl = "https://www.google.com" },
                onOpenAudioControls = { showQuickAudioControls = true },
                onOpenFullSettings = { showSettingsDialog = true },
                onRequestExitApp = { showExitConfirmationDialog = true },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                onOpenAppDrawer = { showGlobalAppDrawer = true },
                onOpenSystemUpdateConsole = { showSystemUpdateConsole = true }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = EchoBg,
            topBar = {
                AnimatedVisibility(
                    visible = isTopBarVisible,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                coroutineScope.launch { drawerState.open() }
                            },
                            modifier = Modifier.testTag("top_navigation_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "System Settings Drawer",
                                tint = EchoCyan
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EchoCyan)
                            )
                            Column {
                                Text(
                                    text = "ATHENA",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                                Text(
                                    text = "42 GENIUSES • ECHO-INTERFACE",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = EchoCyan
                                )
                            }
                        }
                    },
                actions = {
                    // System Update & OTA Recovery Console Trigger
                    IconButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            showSystemUpdateConsole = true
                        },
                        modifier = Modifier.testTag("top_system_update_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "System Update & Recovery",
                            tint = EchoAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sovereign Android OS Installed App Drawer Trigger
                    IconButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            showGlobalAppDrawer = true
                        },
                        modifier = Modifier.testTag("top_app_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Installed Applications Matrix",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // In-App CameraX Document Scanner Trigger
                    IconButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            showDocumentScannerDialog = true
                        },
                        modifier = Modifier.testTag("top_document_scanner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan Document",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick Dola AI Assistant Trigger
                    IconButton(
                        onClick = { showDolaAssistantDialog = true },
                        modifier = Modifier.testTag("top_dola_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Dola AI",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick 90° Screen Rotation Toggle (Landscape Default <-> Portrait)
                    IconButton(
                        onClick = {
                            val next = orientationManager.toggle90Degrees(context as? Activity)
                            android.widget.Toast.makeText(
                                context,
                                "Orientation: ${next.title}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("top_orientation_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Rotate 90 Degrees (${currentOrientation.title})",
                            tint = if (currentOrientation == AppOrientation.LANDSCAPE) EchoCyan else EchoGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Native Device Mirroring & Cast Hub Trigger
                    IconButton(
                        onClick = { showMirroringDialog = true },
                        modifier = Modifier.testTag("top_device_mirroring_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Device Mirroring & Cast",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick Dark / Light Mode Toggle
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier.testTag("top_theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) EchoCyan else EchoGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Google & Connected Accounts Hub Trigger
                    IconButton(
                        onClick = { showAccountSheet = true },
                        modifier = Modifier.testTag("top_account_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Connected Accounts",
                            tint = if (currentUser != null) EchoGreen else EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Lightweight In-App Dola Browser Modal Trigger
                    IconButton(
                        onClick = { activeGlobalBrowserUrl = "https://www.google.com" },
                        modifier = Modifier.testTag("top_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Dola Browser",
                            tint = EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings Dialog Trigger
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = EchoText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick Audio & Sound Controls Trigger (Master Volume, Button Tap Sounds & Synthesizer)
                    IconButton(
                        onClick = { showQuickAudioControls = true },
                        modifier = Modifier.testTag("top_sound_indicator_button")
                    ) {
                        Icon(
                            imageVector = if (isTonePlaying) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isTonePlaying) "Sound Playing • Quick Audio Controls" else "Quick Audio Controls",
                            tint = if (isTonePlaying) EchoGreen else EchoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // In-App Custom "Exit App" Button
                    IconButton(
                        onClick = { showExitConfirmationDialog = true },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("top_exit_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Exit App",
                            tint = EchoRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EchoBgAlt)
            )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                // Split line at the bottom and safe navigationBarsPadding
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Docked YouTube-style Miniplayer Bar
                DockedMiniPlayerBar(
                    toneGenerator = toneGenerator,
                    onExpandFullPlayer = {
                        selectedTab = MainTab.GOOGLE.index
                    }
                )

                // Crisp split divider separating content from bottom navigation
                HorizontalDivider(
                    color = EchoCardStroke,
                    thickness = 1.dp
                )

                // 5-Tab Material 3 Navigation Bar
                NavigationBar(
                    containerColor = EchoBgAlt,
                    contentColor = EchoCyan,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.ARCHIVE.index,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedTab = MainTab.ARCHIVE.index
                        },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Archive") },
                        label = { Text("Archive", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EchoOnCyan,
                            selectedTextColor = EchoCyan,
                            indicatorColor = EchoCyan,
                            unselectedIconColor = EchoMuted,
                            unselectedTextColor = EchoMuted
                        ),
                        modifier = Modifier.testTag("nav_archive")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.ATHENA.index,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedTab = MainTab.ATHENA.index
                        },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "ATHENA") },
                        label = { Text("ATHENA", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EchoOnCyan,
                            selectedTextColor = EchoCyan,
                            indicatorColor = EchoCyan,
                            unselectedIconColor = EchoMuted,
                            unselectedTextColor = EchoMuted
                        ),
                        modifier = Modifier.testTag("nav_athena")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.WORKSPACE.index,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedTab = MainTab.WORKSPACE.index
                        },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Workspace") },
                        label = { Text("Workspace", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EchoOnCyan,
                            selectedTextColor = EchoCyan,
                            indicatorColor = EchoCyan,
                            unselectedIconColor = EchoMuted,
                            unselectedTextColor = EchoMuted
                        ),
                        modifier = Modifier.testTag("nav_workspace")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.BRAIN.index,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedTab = MainTab.BRAIN.index
                        },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "Brainstorming") },
                        label = { Text("Brainstorm", fontSize = 10.sp, fontWeight = if (selectedTab == MainTab.BRAIN.index) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EchoOnCyan,
                            selectedTextColor = EchoCyan,
                            indicatorColor = EchoCyan,
                            unselectedIconColor = EchoMuted,
                            unselectedTextColor = EchoMuted
                        ),
                        modifier = Modifier.testTag("nav_brainstorming")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.GOOGLE.index,
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            selectedTab = MainTab.GOOGLE.index
                        },
                        icon = { Icon(Icons.Default.Movie, contentDescription = "Creative Hub") },
                        label = { Text("Creative Hub", fontSize = 10.sp, fontWeight = if (selectedTab == MainTab.GOOGLE.index) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EchoOnCyan,
                            selectedTextColor = EchoCyan,
                            indicatorColor = EchoCyan,
                            unselectedIconColor = EchoMuted,
                            unselectedTextColor = EchoMuted
                        ),
                        modifier = Modifier.testTag("nav_creative_hub")
                    )
                }
            }
        }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startX = down.position.x
                        val width = size.width
                        var totalDragX = 0f
                        var gestureHandled = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val deltaX = change.position.x - change.previousPosition.x
                            totalDragX += deltaX

                            if (!gestureHandled) {
                                // Swipe left from right edge (starts near right bezel, drags left): Hides bars
                                if (startX >= width * 0.80f && totalDragX < -45f) {
                                    if (isTopBarVisible || isBottomBarVisible) {
                                        isTopBarVisible = false
                                        isBottomBarVisible = false
                                        toneGenerator.playChime(SoundEffectType.CHIME)
                                        change.consume()
                                        gestureHandled = true
                                    }
                                }
                                // Swipe right from left edge (starts near left bezel, drags right): Restores bars
                                else if (startX <= width * 0.20f && totalDragX > 45f) {
                                    if (!isTopBarVisible || !isBottomBarVisible) {
                                        isTopBarVisible = true
                                        isBottomBarVisible = true
                                        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                                        change.consume()
                                        gestureHandled = true
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // Persistent Multi-Tab Host: Preserves exact state, scroll, and inputs across tab switches
            saveableStateHolder.SaveableStateProvider(key = selectedTab) {
                when (selectedTab) {
                    MainTab.ARCHIVE.index -> {
                        ArchiveScreen(
                            onNavigateToFrequency = { selectedTab = MainTab.WORKSPACE.index },
                            onNavigateToNetwork = { selectedTab = MainTab.WORKSPACE.index },
                            onNavigateToWorkspace = { selectedTab = MainTab.WORKSPACE.index },
                            onSaveQuoteToFieldJournal = { quote, chapterTitle ->
                                val firstLine = quote.trim().lines().firstOrNull()?.take(36) ?: "Archived Quote"
                                workspaceViewModel.createNote(
                                    name = "Quote: $firstLine...",
                                    initialText = "“$quote”\n\n— Source: $chapterTitle\n[Saved via Archive Reading Companion]",
                                    tags = "Quote, FieldJournal, Archive"
                                )
                            }
                        )
                    }
                    MainTab.ATHENA.index -> {
                        AthenaScreen(
                            onSaveToWorkspaceNote = { noteContent ->
                                val firstLine = noteContent.trim().lines().firstOrNull()?.take(40) ?: "Athena Note"
                                workspaceViewModel.createNote(name = firstLine, initialText = noteContent)
                                selectedTab = MainTab.WORKSPACE.index
                            }
                        )
                    }
                    MainTab.WORKSPACE.index -> {
                        WorkspaceScreen(
                            viewModel = workspaceViewModel,
                            onNavigateToEcho = { selectedTab = MainTab.GOOGLE.index },
                            onOpenAppDrawer = { showGlobalAppDrawer = true }
                        )
                    }
                    MainTab.BRAIN.index -> {
                        BrainstormingLabScreen(
                            workspaceViewModel = workspaceViewModel,
                            toneGenerator = toneGenerator,
                            initialCouncilTopic = scannedCouncilTopic,
                            initialDocumentAnalysis = scannedDocumentAnalysis,
                            onNavigateToWorkspace = { selectedTab = MainTab.WORKSPACE.index }
                        )
                    }
                    MainTab.GOOGLE.index -> {
                        GoogleSuiteScreen(
                            workspaceViewModel = workspaceViewModel,
                            toneGenerator = toneGenerator,
                            onOpenPreferences = { showSettingsDialog = true }
                        )
                    }
                }
            }

            // Subtle non-intrusive restore hint shown ONLY when navigation bars are hidden (No blocking floating widget)
            AnimatedVisibility(
                visible = !isTopBarVisible || !isBottomBarVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EchoCard.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.4f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier.clickable {
                        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                        isTopBarVisible = true
                        isBottomBarVisible = true
                    }.testTag("subtle_restore_bars_hint")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EchoCyan)
                        )
                        Text(
                            text = "Swipe edge or tap to restore bars",
                            fontSize = 10.sp,
                            color = EchoCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Global Dola AI Dialog
            if (showDolaAssistantDialog) {
                DolaAiDialog(
                    initialText = "",
                    initialMode = DolaAiMode.CHAT,
                    firestoreRepository = null,
                    currentUserId = null,
                    onSaveAsNewNote = { noteText ->
                        val firstLine = noteText.trim().lines().firstOrNull()?.take(40) ?: "Dola AI Note"
                        workspaceViewModel.createNote(name = firstLine, initialText = noteText)
                        selectedTab = MainTab.WORKSPACE.index
                    },
                    onDismiss = { showDolaAssistantDialog = false }
                )
            }

            // Global Connected Accounts Dialog Sheet
            if (showAccountSheet) {
                ConnectedAccountSheet(
                    accountsManager = accountsManager,
                    toneGenerator = toneGenerator,
                    onDismiss = { showAccountSheet = false }
                )
            }

            // Quick Audio & Sound Controls Dialog Overlay
            if (showQuickAudioControls) {
                QuickAudioControlsDialog(
                    toneGenerator = toneGenerator,
                    isTopBarVisible = isTopBarVisible,
                    onToggleTopBar = { isTopBarVisible = !isTopBarVisible },
                    isBottomBarVisible = isBottomBarVisible,
                    onToggleBottomBar = { isBottomBarVisible = !isBottomBarVisible },
                    onToggleFullscreen = {
                        val next = !(isTopBarVisible && isBottomBarVisible)
                        isTopBarVisible = next
                        isBottomBarVisible = next
                    },
                    onOpenSettings = {
                        showQuickAudioControls = false
                        showSettingsDialog = true
                    },
                    onOpenBrowser = {
                        showQuickAudioControls = false
                        activeGlobalBrowserUrl = "https://www.google.com"
                    },
                    onDismiss = { showQuickAudioControls = false }
                )
            }

            // Global Lightweight Dola In-App Browser Modal with Integrated Gemini Copilot
            if (activeGlobalBrowserUrl != null) {
                DolaBrowserDialog(
                    initialUrl = activeGlobalBrowserUrl!!,
                    title = "Dola In-App Browser",
                    toneGenerator = toneGenerator,
                    workspaceViewModel = workspaceViewModel,
                    onDismiss = { activeGlobalBrowserUrl = null }
                )
            }

            // Device Mirroring & Cast Testing Dialog
            if (showMirroringDialog) {
                DeviceMirroringDialog(
                    onDismiss = { showMirroringDialog = false },
                    orientationManager = orientationManager
                )
            }

            // In-App Settings Dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onRequestExitApp = {
                        showSettingsDialog = false
                        showExitConfirmationDialog = true
                    },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // In-App Exit Confirmation Dialog (Non-native navigation termination)
            if (showExitConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showExitConfirmationDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = EchoRed)
                            Text("Exit ATHENA Application?", fontWeight = FontWeight.Bold, color = EchoText)
                        }
                    },
                    text = {
                        Text(
                            "Are you sure you want to terminate the application? All active audio sessions, resonance presets, and workspace notes are securely preserved.",
                            color = EchoMuted,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitConfirmationDialog = false
                                toneGenerator.stop()
                                (context as? Activity)?.finishAffinity()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                            modifier = Modifier.testTag("confirm_exit_app_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Exit App", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showExitConfirmationDialog = false },
                            modifier = Modifier.testTag("cancel_exit_app_button")
                        ) {
                            Text("Cancel", color = EchoText)
                        }
                    },
                    containerColor = EchoCard,
                    modifier = Modifier.testTag("exit_app_dialog")
                )
            }

            // In-App CameraX Document Scanner Dialog
            if (showDocumentScannerDialog) {
                DocumentScannerDialog(
                    toneGenerator = toneGenerator,
                    onDocumentProcessed = { extractedTopic, fullAnalysis ->
                        showDocumentScannerDialog = false
                        scannedCouncilTopic = extractedTopic
                        scannedDocumentAnalysis = fullAnalysis
                        selectedTab = MainTab.BRAIN.index
                    },
                    onDismiss = { showDocumentScannerDialog = false }
                )
            }

            // Sovereign Android OS Installed App Drawer Matrix
            if (showGlobalAppDrawer) {
                AppDrawerSheet(
                    onDismiss = { showGlobalAppDrawer = false },
                    toneGenerator = toneGenerator
                )
            }

            // ATHENA System Update & OTA Recovery Console
            if (showSystemUpdateConsole) {
                AthenaSystemUpdateConsole(
                    onDismiss = { showSystemUpdateConsole = false },
                    toneGenerator = toneGenerator
                )
            }
        }
    }
}
}
