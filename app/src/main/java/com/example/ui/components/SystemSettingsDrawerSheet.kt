package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import com.example.util.AppOrientation
import com.example.util.OrientationManager

import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.launcher.LauncherAppsManager

/**
 * System Settings Navigation Drawer Panel:
 * Consolidates interface controls (Top Bar, Bottom Navigation Bar, Fullscreen Mode,
 * Screen Orientation), Quick Tools (Document Scanner, Dola AI, Browser, Audio, Cast),
 * Sovereign Launcher Controls, and System Preferences into a persistent drawer.
 * Completely eliminates floating widgets that obstruct UI content.
 */
@Composable
fun SystemSettingsDrawerSheet(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    isTopBarVisible: Boolean,
    onToggleTopBar: () -> Unit,
    isBottomBarVisible: Boolean,
    onToggleBottomBar: () -> Unit,
    onToggleFullscreen: () -> Unit,
    orientationManager: OrientationManager,
    currentOrientation: AppOrientation,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onOpenDocumentScanner: () -> Unit,
    onOpenDolaAssistant: () -> Unit,
    onOpenAccountSheet: () -> Unit,
    onOpenMirroring: () -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenAudioControls: () -> Unit,
    onOpenFullSettings: () -> Unit,
    onRequestExitApp: () -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenAppDrawer: (() -> Unit)? = null,
    onOpenSystemUpdateConsole: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val launcherManager = remember { LauncherAppsManager.getInstance(context) }
    val isDefaultLauncher by launcherManager.isDefaultLauncher.collectAsState()
    val isFullscreen = !isTopBarVisible && !isBottomBarVisible

    ModalDrawerSheet(
        drawerContainerColor = EchoBgAlt,
        drawerContentColor = EchoText,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("system_settings_drawer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Close Button
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
                            .background(EchoCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "SYSTEM SETTINGS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "INTERFACE & CONTROLS",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EchoCyan
                        )
                    }
                }

                IconButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        onCloseDrawer()
                    },
                    modifier = Modifier.testTag("btn_close_settings_drawer")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = EchoMuted)
                }
            }

            HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

            // Section 1: In-App Document & Image Scanner (CameraX)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCyan.copy(alpha = 0.10f)),
                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "CAMERA DOCUMENT SCANNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Capture physical notes, paper equations, and diagrams via CameraX optics and feed directly into the Council debate.",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenDocumentScanner()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("drawer_btn_launch_camera_scanner"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Scanner Viewfinder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 2: Consolidated Interface Controls (Top Bar, Bottom Bar, Fullscreen)
            Text(
                text = "INTERFACE VISIBILITY & LAYOUT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EchoCyan,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Bar Toggle Switch
                    DrawerSettingToggleRow(
                        icon = if (isTopBarVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        title = "Top App Bar",
                        subtitle = if (isTopBarVisible) "Visible" else "Hidden (clean view)",
                        checked = isTopBarVisible,
                        onCheckedChange = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onToggleTopBar()
                        },
                        testTag = "drawer_switch_top_bar"
                    )

                    HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Bottom Navigation Bar Toggle Switch
                    DrawerSettingToggleRow(
                        icon = if (isBottomBarVisible) Icons.Default.VerticalAlignBottom else Icons.Default.VisibilityOff,
                        title = "Bottom Navigation Bar",
                        subtitle = if (isBottomBarVisible) "Visible" else "Hidden (gesture edge restore)",
                        checked = isBottomBarVisible,
                        onCheckedChange = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onToggleBottomBar()
                        },
                        testTag = "drawer_switch_bottom_bar"
                    )

                    HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Fullscreen Mode (Both bars toggled)
                    DrawerSettingToggleRow(
                        icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        title = "Immersive Fullscreen",
                        subtitle = if (isFullscreen) "Fullscreen active (both bars hidden)" else "Standard layout",
                        checked = isFullscreen,
                        onCheckedChange = {
                            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                            onToggleFullscreen()
                        },
                        testTag = "drawer_switch_fullscreen"
                    )

                    HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Screen Orientation Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val next = orientationManager.toggle90Degrees(context as? Activity)
                                toneGenerator.playChime(SoundEffectType.TAP)
                                Toast.makeText(context, "Orientation: ${next.title}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = null,
                                tint = if (currentOrientation == AppOrientation.LANDSCAPE) EchoCyan else EchoGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("Screen Orientation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText)
                                Text("Current: ${currentOrientation.title}", fontSize = 10.sp, color = EchoMuted)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, EchoCardStroke, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Rotate 90°",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan
                            )
                        }
                    }
                }
            }

            // Section 3: Theme & Quick System Tools
            Text(
                text = "SYSTEM CONTROLS & HUBS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EchoCyan,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sovereign OS App Drawer Matrix
                    DrawerNavigationActionRow(
                        icon = Icons.Default.Apps,
                        iconTint = EchoCyan,
                        title = "Installed App Drawer Matrix",
                        subtitle = "Access all device applications & launch",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenAppDrawer?.invoke()
                        },
                        testTag = "drawer_action_app_drawer"
                    )

                    // Default Android Home Launcher Setting
                    DrawerNavigationActionRow(
                        icon = Icons.Default.Home,
                        iconTint = if (isDefaultLauncher) EchoGreen else EchoCyan,
                        title = if (isDefaultLauncher) "Default Home Shell: ACTIVE" else "Set as Default Home Launcher",
                        subtitle = if (isDefaultLauncher) "ATHENA is active system boot shell" else "One-tap prompt to configure Android Home",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                            onCloseDrawer()
                            launcherManager.openDefaultLauncherPrompt(context)
                        },
                        testTag = "drawer_action_default_home"
                    )

                    // Theme Switcher
                    DrawerNavigationActionRow(
                        icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        iconTint = if (isDarkTheme) EchoCyan else EchoGreen,
                        title = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                        subtitle = "Toggle UI appearance",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onToggleDarkTheme()
                        },
                        testTag = "drawer_action_toggle_theme"
                    )

                    // Audio & Sound Controls
                    DrawerNavigationActionRow(
                        icon = Icons.Default.GraphicEq,
                        iconTint = EchoCyan,
                        title = "Audio & Sound Controls",
                        subtitle = "Master volume, button sounds & tone synth",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenAudioControls()
                        },
                        testTag = "drawer_action_audio_controls"
                    )

                    // Dola AI Copilot
                    DrawerNavigationActionRow(
                        icon = Icons.Default.AutoAwesome,
                        iconTint = EchoCyan,
                        title = "Dola AI Assistant",
                        subtitle = "Fast multimodal agent & workspace tool",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenDolaAssistant()
                        },
                        testTag = "drawer_action_dola_assistant"
                    )

                    // Connected Accounts Hub
                    DrawerNavigationActionRow(
                        icon = Icons.Default.AccountCircle,
                        iconTint = EchoGreen,
                        title = "Connected Accounts Hub",
                        subtitle = "Manage Google and local user profiles",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenAccountSheet()
                        },
                        testTag = "drawer_action_account_hub"
                    )

                    // Dola Browser
                    DrawerNavigationActionRow(
                        icon = Icons.Default.Language,
                        iconTint = EchoCyan,
                        title = "Dola Web Browser",
                        subtitle = "In-app secure research browser",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenBrowser()
                        },
                        testTag = "drawer_action_browser"
                    )

                    // Device Mirroring & Cast
                    DrawerNavigationActionRow(
                        icon = Icons.Default.Cast,
                        iconTint = EchoCyan,
                        title = "Device Mirroring & Cast Hub",
                        subtitle = "Mirror interface to external displays",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenMirroring()
                        },
                        testTag = "drawer_action_mirroring"
                    )

                    // System Updates & OTA Console
                    DrawerNavigationActionRow(
                        icon = Icons.Default.SystemUpdate,
                        iconTint = EchoAmber,
                        title = "System Update & Recovery Console",
                        subtitle = "Bypass USB blocks, accept OTA packages & reboot",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenSystemUpdateConsole?.invoke()
                        },
                        testTag = "drawer_action_system_update"
                    )

                    // Advanced App Preferences
                    DrawerNavigationActionRow(
                        icon = Icons.Default.Settings,
                        iconTint = EchoText,
                        title = "Advanced Preferences",
                        subtitle = "Cloud sync, diagnostics & cache",
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            onCloseDrawer()
                            onOpenFullSettings()
                        },
                        testTag = "drawer_action_advanced_settings"
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Section 4: Exit Application
            Button(
                onClick = {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    onCloseDrawer()
                    onRequestExitApp()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EchoRed.copy(alpha = 0.2f), contentColor = EchoRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("drawer_btn_exit_app"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, EchoRed.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Exit ATHENA Application", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DrawerSettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) EchoCyan else EchoMuted,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText)
                Text(text = subtitle, fontSize = 10.sp, color = EchoMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EchoOnCyan,
                checkedTrackColor = EchoCyan,
                uncheckedThumbColor = EchoMuted,
                uncheckedTrackColor = EchoDarkSurface
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DrawerNavigationActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
            Text(text = subtitle, fontSize = 9.sp, color = EchoMuted)
        }
    }
}
