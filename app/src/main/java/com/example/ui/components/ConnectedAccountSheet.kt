package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.auth.CloudSyncStatus
import com.example.auth.ConnectedAccountsManager
import com.example.auth.ConnectedUser
import com.example.ui.theme.EchoAmber
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Google & Connected Accounts Profile Dialog Sheet.
 * Provides live account sign-in, service link toggles, and Google Drive cloud sync.
 */
@Composable
fun ConnectedAccountSheet(
    accountsManager: ConnectedAccountsManager,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onDismiss: () -> Unit
) {
    val currentUser by accountsManager.currentUser.collectAsState()
    val isSyncing by accountsManager.isSyncing.collectAsState()
    val lastSyncTime by accountsManager.lastSyncTime.collectAsState()

    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (currentUser != null) EchoGreen else EchoAmber)
                    )
                    Text(
                        text = "GOOGLE & CONNECTED HUB",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoCyan,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (currentUser != null) {
                    val user = currentUser!!
                    // Active Account Profile Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(EchoCyan.copy(alpha = 0.2f))
                                        .border(1.5.dp, EchoCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.displayName.take(1).uppercase(),
                                        color = EchoCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        color = EchoText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = user.email,
                                        color = EchoMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = EchoGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Google Account Verified",
                                            color = EchoGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Connected Cloud Services Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "INTERNET & CLOUD BRIDGES",
                                color = EchoCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            // Google Drive Sync row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = "Drive", tint = EchoGreen, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Google Drive & Workspace", color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Cloud notes & waveform backup", color = EchoMuted, fontSize = 9.sp)
                                    }
                                }
                                Switch(
                                    checked = user.isDriveConnected,
                                    onCheckedChange = { isChecked ->
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        accountsManager.toggleServiceLink("drive", isChecked)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoGreen, checkedTrackColor = EchoGreen.copy(alpha = 0.3f))
                                )
                            }

                            // YouTube Bridge row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "YouTube", tint = EchoRed, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("YouTube Explorer & Playlists", color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Live video searches & playlists", color = EchoMuted, fontSize = 9.sp)
                                    }
                                }
                                Switch(
                                    checked = user.isYouTubeConnected,
                                    onCheckedChange = { isChecked ->
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        accountsManager.toggleServiceLink("youtube", isChecked)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EchoRed, checkedTrackColor = EchoRed.copy(alpha = 0.3f))
                                )
                            }

                            // Live Web Search & Grounding row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = "Live Search", tint = EchoCyan, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Live Web Search Grounding", color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Real-time Gemini internet grounding", color = EchoMuted, fontSize = 9.sp)
                                    }
                                }
                                Text("ONLINE", color = EchoCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    // Cloud Sync Engine Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Cloud Backup Status", color = EchoText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    val formattedDate = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(lastSyncTime))
                                    Text("Last synced: $formattedDate", color = EchoMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }

                                Button(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        accountsManager.triggerCloudSync { success, msg ->
                                            if (success) toneGenerator.playChime(SoundEffectType.SUCCESS)
                                            syncFeedbackMessage = msg
                                        }
                                    },
                                    enabled = !isSyncing,
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_trigger_cloud_sync")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(color = EchoOnCyan, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Syncing...", fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.CloudSync, contentDescription = "Sync", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (syncFeedbackMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = syncFeedbackMessage!!,
                                    color = EchoGreen,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Sign-in Call to Action
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account", tint = EchoCyan, modifier = Modifier.size(48.dp))
                            Text("Sign In with Google", color = EchoText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Sign into your Google account to connect Google Drive workspace sync, YouTube video collections, and live web research.",
                                color = EchoMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.SUCCESS)
                                    accountsManager.signInWithGoogle(
                                        onSuccess = { syncFeedbackMessage = "Welcome, ${it.displayName}!" },
                                        onError = { syncFeedbackMessage = it }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_google_signin_action")
                            ) {
                                Text("Sign In as mcollier2188@gmail.com", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentUser != null) {
                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        accountsManager.signOut {
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out", fontSize = 11.sp, color = EchoRed)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Done", fontSize = 11.sp)
                }
            }
        },
        containerColor = EchoBg,
        textContentColor = EchoText
    )
}
