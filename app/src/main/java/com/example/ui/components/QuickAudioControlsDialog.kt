package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoCyanSoft
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText

import androidx.compose.material.icons.filled.Tune

/**
 * Quick Audio Controls Dialog:
 * Master Output Gain, Sound Effects, Tactile Chimes,
 * and Interactive Interface Chrome Toggles (Top Header Bar & Bottom Nav Bar).
 */
@Composable
fun QuickAudioControlsDialog(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    isTopBarVisible: Boolean = true,
    onToggleTopBar: (() -> Unit)? = null,
    isBottomBarVisible: Boolean = true,
    onToggleBottomBar: (() -> Unit)? = null,
    onToggleFullscreen: (() -> Unit)? = null,
    onOpenSettings: () -> Unit,
    onOpenBrowser: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val volume by toneGenerator.volumeState.collectAsState()
    val isButtonSoundEnabled by toneGenerator.isButtonSoundEnabledState.collectAsState()
    val isMuted by toneGenerator.isMutedState.collectAsState()
    val isTonePlaying by toneGenerator.isPlayingState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, EchoCyan, RoundedCornerShape(16.dp))
                .testTag("quick_audio_controls_dialog"),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = EchoOnCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Quick Sound Controls",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = "DSP Master Output & Haptic Chimes",
                                fontSize = 11.sp,
                                color = EchoCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_quick_audio_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoText)
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                // Master Volume Slider Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = if (isMuted) EchoRed else EchoCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Media & Master Output Gain",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EchoText
                                )
                            }
                            Text(
                                text = if (isMuted) "MUTED" else "${(volume * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMuted) EchoRed else EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = volume,
                            onValueChange = { newVol ->
                                if (isMuted) {
                                    toneGenerator.setMuted(false)
                                }
                                toneGenerator.setVolumeLevel(newVol)
                            },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = EchoCyan,
                                activeTrackColor = EchoCyan,
                                inactiveTrackColor = EchoDarkSurface
                            ),
                            modifier = Modifier.testTag("quick_volume_slider")
                        )

                        // Mute All Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isMuted) "Audio Output is Muted" else "Audio Output is Active",
                                fontSize = 11.sp,
                                color = if (isMuted) EchoRed else EchoMuted
                            )
                            OutlinedButton(
                                onClick = {
                                    toneGenerator.toggleMute()
                                },
                                modifier = Modifier.height(32.dp).testTag("quick_mute_toggle_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isMuted) EchoGreen else EchoRed
                                )
                            ) {
                                Text(
                                    text = if (isMuted) "Unmute Audio" else "Mute All",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Button Tap Sound Effects Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isButtonSoundEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (isButtonSoundEnabled) EchoCyan else EchoMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Button Tap Sound Effects",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EchoText
                                )
                                Text(
                                    text = if (isButtonSoundEnabled) "Harmonic feedback active on touch" else "Silent interface button taps",
                                    fontSize = 10.sp,
                                    color = EchoMuted
                                )
                            }
                        }

                        Switch(
                            checked = isButtonSoundEnabled,
                            onCheckedChange = { toneGenerator.setButtonSoundEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EchoCyan,
                                checkedTrackColor = EchoCyanSoft,
                                uncheckedThumbColor = EchoMuted,
                                uncheckedTrackColor = EchoBgAlt
                            ),
                            modifier = Modifier.testTag("quick_button_sound_switch")
                        )
                    }
                }

                // Test Chimes & Synthesis Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Acoustic Synthesizer Chimes:",
                            fontSize = 11.sp,
                            color = EchoMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { toneGenerator.playChime(SoundEffectType.TAP) },
                                modifier = Modifier.weight(1f).height(34.dp).testTag("quick_test_tap_button")
                            ) {
                                Text("Tap Chime", fontSize = 10.sp, color = EchoText)
                            }
                            OutlinedButton(
                                onClick = { toneGenerator.playChime(SoundEffectType.CHIME) },
                                modifier = Modifier.weight(1f).height(34.dp).testTag("quick_test_chime_button")
                            ) {
                                Text("528Hz Bell", fontSize = 10.sp, color = EchoCyan)
                            }
                            OutlinedButton(
                                onClick = { toneGenerator.playChime(SoundEffectType.HARMONIC_PING) },
                                modifier = Modifier.weight(1f).height(34.dp).testTag("quick_test_ping_button")
                            ) {
                                Text("432Hz Ping", fontSize = 10.sp, color = EchoGreen)
                            }
                        }

                        // Continuous Carrier Tone Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isTonePlaying) EchoGreen else EchoMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isTonePlaying) "42.6 Hz Carrier Active" else "Carrier Generator Idle",
                                    fontSize = 11.sp,
                                    color = if (isTonePlaying) EchoGreen else EchoMuted
                                )
                            }

                            Button(
                                onClick = {
                                    if (isTonePlaying) {
                                        toneGenerator.stop()
                                    } else {
                                        toneGenerator.start(listOf(42.6))
                                    }
                                },
                                modifier = Modifier.height(32.dp).testTag("quick_toggle_carrier_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTonePlaying) EchoRed else EchoDarkSurface,
                                    contentColor = if (isTonePlaying) Color.White else EchoCyan
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTonePlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isTonePlaying) "Stop" else "Start 42.6Hz",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Quick WAV Audio Recorder & Phone Exporter
                WavRecorderCard(
                    toneGenerator = toneGenerator,
                    currentFrequency = 42.6,
                    isPlaying = isTonePlaying,
                    onStartAudio = { toneGenerator.start(listOf(42.6)) }
                )

                // Interactive Top & Bottom Chrome Bar Toggles (Requirement 6)
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_chrome_toggles"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icon(Icons.Default.Tune, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "INTERFACE CHROME VISIBILITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoCyan,
                                    letterSpacing = 1.sp
                                )
                            }
                            if (onToggleFullscreen != null) {
                                TextButton(
                                    onClick = onToggleFullscreen,
                                    modifier = Modifier.height(28.dp).testTag("btn_toggle_fullscreen")
                                ) {
                                    Text(
                                        text = if (!isTopBarVisible && !isBottomBarVisible) "RESTORE ALL" else "FULL CANVAS",
                                        fontSize = 10.sp,
                                        color = EchoCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Top Bar Row with Status Badge & Direct Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Top Header Bar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isTopBarVisible) EchoCyan.copy(alpha = 0.2f) else EchoCardStroke.copy(alpha = 0.3f))
                                            .border(1.dp, if (isTopBarVisible) EchoCyan else EchoCardStroke, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isTopBarVisible) "VISIBLE" else "HIDDEN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTopBarVisible) EchoCyan else EchoMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text("Status, mirroring & settings controls", fontSize = 10.sp, color = EchoMuted)
                            }

                            // Direct quick-action button
                            Button(
                                onClick = { onToggleTopBar?.invoke() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTopBarVisible) EchoDarkSurface else EchoCyan,
                                    contentColor = if (isTopBarVisible) EchoText else EchoOnCyan
                                ),
                                border = BorderStroke(1.dp, if (isTopBarVisible) EchoCardStroke else EchoCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_quick_toggle_top_bar")
                            ) {
                                Icon(
                                    imageVector = if (isTopBarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isTopBarVisible) "Hide Top" else "Show Top",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f))

                        // Bottom Nav Bar Row with Status Badge & Direct Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Bottom Nav Bar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isBottomBarVisible) EchoCyan.copy(alpha = 0.2f) else EchoCardStroke.copy(alpha = 0.3f))
                                            .border(1.dp, if (isBottomBarVisible) EchoCyan else EchoCardStroke, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isBottomBarVisible) "VISIBLE" else "HIDDEN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBottomBarVisible) EchoCyan else EchoMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text("5-Tab primary destination switcher", fontSize = 10.sp, color = EchoMuted)
                            }

                            // Direct quick-action button
                            Button(
                                onClick = { onToggleBottomBar?.invoke() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBottomBarVisible) EchoDarkSurface else EchoCyan,
                                    contentColor = if (isBottomBarVisible) EchoText else EchoOnCyan
                                ),
                                border = BorderStroke(1.dp, if (isBottomBarVisible) EchoCardStroke else EchoCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_quick_toggle_bottom_nav")
                            ) {
                                Icon(
                                    imageVector = if (isBottomBarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isBottomBarVisible) "Hide Nav" else "Show Nav",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Horizontal gesture guidance pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoCyan.copy(alpha = 0.08f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚡ Edge Gestures: Swipe LEFT from the right edge to hide both bars; swipe RIGHT from the left edge to restore them.",
                                fontSize = 10.sp,
                                color = EchoCyan,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Bottom Navigation Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onOpenBrowser != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenBrowser()
                            },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("quick_open_browser_button")
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Dola Browser", fontSize = 11.sp, color = EchoText)
                        }
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenSettings()
                        },
                        modifier = Modifier.weight(1f).height(38.dp).testTag("quick_open_settings_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoCyan,
                            contentColor = EchoOnCyan
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("All Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
