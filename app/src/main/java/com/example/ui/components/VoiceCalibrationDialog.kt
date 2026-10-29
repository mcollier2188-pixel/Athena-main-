package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.DolaVoiceManager
import com.example.ai.VoiceCalibration
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText

/**
 * Dedicated Voice Calibration Dialog for configuring global pitch, rate, and volume.
 * Persists settings via the VoiceCalibration singleton across all ATHENA suite modules.
 */
@Composable
fun VoiceCalibrationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val voiceCalibration = remember { VoiceCalibration.getInstance(context) }
    val currentSettings by voiceCalibration.settings.collectAsState()
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }

    var pitch by remember(currentSettings) { mutableFloatStateOf(currentSettings.pitch) }
    var rate by remember(currentSettings) { mutableFloatStateOf(currentSettings.rate) }
    var volume by remember(currentSettings) { mutableFloatStateOf(currentSettings.volume) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_calibration_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = EchoBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = EchoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Voice Calibration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )
                            Text(
                                text = "Persisted across all ATHENA speech modules",
                                fontSize = 11.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("btn_close_voice_calibration")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                    }
                }

                // Tuning Sliders Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EchoCardStroke)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rate Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Speech Rate (Speed)", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                Text("${"%.2f".format(rate)}x", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = EchoCyan)
                            }
                            Slider(
                                value = rate,
                                onValueChange = { rate = it },
                                valueRange = VoiceCalibration.MIN_RATE..VoiceCalibration.MAX_RATE,
                                colors = SliderDefaults.colors(thumbColor = EchoCyan, activeTrackColor = EchoCyan)
                            )
                        }

                        // Pitch Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Speech Pitch", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                Text("${"%.2f".format(pitch)}x", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = EchoGreen)
                            }
                            Slider(
                                value = pitch,
                                onValueChange = { pitch = it },
                                valueRange = VoiceCalibration.MIN_PITCH..VoiceCalibration.MAX_PITCH,
                                colors = SliderDefaults.colors(thumbColor = EchoGreen, activeTrackColor = EchoGreen)
                            )
                        }

                        // Volume Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Speech Volume", fontSize = 12.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                                Text("${(volume * 100).toInt()}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = EchoAmber)
                            }
                            Slider(
                                value = volume,
                                onValueChange = { volume = it },
                                valueRange = VoiceCalibration.MIN_VOLUME..VoiceCalibration.MAX_VOLUME,
                                colors = SliderDefaults.colors(thumbColor = EchoAmber, activeTrackColor = EchoAmber)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            voiceCalibration.resetToDefaults()
                            pitch = VoiceCalibration.DEFAULT_PITCH
                            rate = VoiceCalibration.DEFAULT_RATE
                            volume = VoiceCalibration.DEFAULT_VOLUME
                            Toast.makeText(context, "Reset to optimal Dola defaults", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(0.9f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = EchoMuted)
                        Spacer(Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp, color = EchoMuted)
                    }

                    OutlinedButton(
                        onClick = {
                            // Test speech with current dialog settings
                            voiceCalibration.calibrate(pitch = pitch, rate = rate, volume = volume)
                            voiceManager.speak("Hello, ATHENA voice calibrated to ${"%.2f".format(rate)} speed, ${"%.2f".format(pitch)} pitch, and ${(volume * 100).toInt()} percent volume.")
                        },
                        modifier = Modifier.weight(1.1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp), tint = EchoCyan)
                        Spacer(Modifier.width(4.dp))
                        Text("Test Voice", fontSize = 11.sp, color = EchoCyan)
                    }

                    Button(
                        onClick = {
                            voiceCalibration.calibrate(pitch = pitch, rate = rate, volume = volume)
                            Toast.makeText(context, "Voice calibration saved across all ATHENA modules!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.4f).height(40.dp).testTag("btn_save_voice_calibration_dialog"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoGreen,
                            contentColor = EchoBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Apply Global", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
