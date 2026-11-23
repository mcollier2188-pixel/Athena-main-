package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.auth.AccountAuthSource
import com.example.auth.ConnectedAccountsManager
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
 * Provides AndroidX Credential Manager OAuth 2.0 Sign-In, runtime Web Client ID configuration,
 * service link toggles, and Google Drive cloud sync with graceful fallback.
 */
@Composable
fun ConnectedAccountSheet(
    accountsManager: ConnectedAccountsManager,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by accountsManager.currentUser.collectAsState()
    val isSigningIn by accountsManager.isSigningIn.collectAsState()
    val isSyncing by accountsManager.isSyncing.collectAsState()
    val lastSyncTime by accountsManager.lastSyncTime.collectAsState()
    val oauthClientId by accountsManager.oauthClientId.collectAsState()
    val authStatusMessage by accountsManager.authStatusMessage.collectAsState()

    val isOAuthConfigured = oauthClientId.isNotBlank()
    val scrollState = rememberScrollState()
    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var showOAuthConfig by remember { mutableStateOf(!isOAuthConfigured && currentUser == null) }
    var clientIdInput by remember(oauthClientId) { mutableStateOf(oauthClientId) }

    LaunchedEffect(oauthClientId) {
        clientIdInput = oauthClientId
    }

    LaunchedEffect(currentUser?.uid) {
        scrollState.scrollTo(0)
    }

    val effectiveBannerMessage = syncFeedbackMessage
        ?: authStatusMessage
        ?: if (currentUser == null && !isOAuthConfigured) {
            "No OAuth 2.0 Web Client ID configured — configure a *.apps.googleusercontent.com client ID below for live Google Sign-In, or continue with a Local Workspace Profile."
        } else {
            null
        }

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
                    val statusDotColor = when {
                        currentUser?.isGoogleConnected == true -> EchoGreen
                        currentUser != null -> EchoAmber
                        else -> EchoCyan
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (effectiveBannerMessage != null) {
                    val isWarningOrError = effectiveBannerMessage.contains("cancelled", ignoreCase = true) ||
                        effectiveBannerMessage.contains("invalid", ignoreCase = true) ||
                        effectiveBannerMessage.contains("unavailable", ignoreCase = true) ||
                        effectiveBannerMessage.contains("not configured", ignoreCase = true) ||
                        effectiveBannerMessage.contains("No OAuth", ignoreCase = true) ||
                        effectiveBannerMessage.contains("API key", ignoreCase = true)
                    val bannerAccent = if (isWarningOrError) EchoAmber else EchoGreen

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_auth_status"),
                        colors = CardDefaults.cardColors(containerColor = bannerAccent.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, bannerAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = bannerAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = effectiveBannerMessage,
                                color = EchoText,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                if (currentUser != null) {
                    val user = currentUser!!
                    val isVerifiedGoogle = user.isGoogleConnected &&
                        user.authSource != AccountAuthSource.LOCAL_FALLBACK
                    val badgeColor = if (isVerifiedGoogle) EchoGreen else EchoAmber

                    // Active Account Profile Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .testTag("badge_account_auth_source")
                                    ) {
                                        Icon(
                                            imageVector = if (isVerifiedGoogle) {
                                                Icons.Default.VerifiedUser
                                            } else {
                                                Icons.Default.CheckCircle
                                            },
                                            contentDescription = user.authSource.label,
                                            tint = badgeColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = user.authSource.label,
                                            color = badgeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            if (!user.isGoogleConnected && isOAuthConfigured) {
                                Button(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        syncFeedbackMessage = null
                                        accountsManager.signInWithGoogle(
                                            activityContext = context,
                                            allowLocalFallback = false,
                                            onSuccess = {
                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                syncFeedbackMessage = "Upgraded to ${it.authSource.label}: ${it.email}"
                                            },
                                            onError = { syncFeedbackMessage = it }
                                        )
                                    },
                                    enabled = !isSigningIn,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EchoCyan,
                                        contentColor = EchoOnCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_upgrade_google_oauth")
                                ) {
                                    Text(
                                        text = if (isSigningIn) {
                                            "Connecting Google Credential Manager..."
                                        } else {
                                            "Upgrade to Live Google OAuth Sign-In"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Connected Cloud Services Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                                    Icon(
                                        Icons.Default.Storage,
                                        contentDescription = "Drive",
                                        tint = EchoGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            "Google Drive & Workspace",
                                            color = EchoText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Cloud notes & waveform backup",
                                            color = EchoMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = user.isDriveConnected,
                                    onCheckedChange = { isChecked ->
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        accountsManager.toggleServiceLink("drive", isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = EchoGreen,
                                        checkedTrackColor = EchoGreen.copy(alpha = 0.3f)
                                    )
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
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = "YouTube",
                                        tint = EchoRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            "YouTube Explorer & Playlists",
                                            color = EchoText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Live video searches & playlists",
                                            color = EchoMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = user.isYouTubeConnected,
                                    onCheckedChange = { isChecked ->
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        accountsManager.toggleServiceLink("youtube", isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = EchoRed,
                                        checkedTrackColor = EchoRed.copy(alpha = 0.3f)
                                    )
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
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = "Live Search",
                                        tint = EchoCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            "Live Web Search Grounding",
                                            color = EchoText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Real-time Gemini internet grounding",
                                            color = EchoMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    "ONLINE",
                                    color = EchoCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Cloud Sync Engine Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Cloud Backup Status",
                                        color = EchoText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val formattedDate = SimpleDateFormat(
                                        "MMM d, HH:mm",
                                        Locale.getDefault()
                                    ).format(Date(lastSyncTime))
                                    Text(
                                        "Last synced: $formattedDate",
                                        color = EchoMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
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
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EchoCyan,
                                        contentColor = EchoOnCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_trigger_cloud_sync")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            color = EchoOnCyan,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Syncing...", fontSize = 11.sp)
                                    } else {
                                        Icon(
                                            Icons.Default.CloudSync,
                                            contentDescription = "Sync",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Sign-in Call to Action
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Account",
                                tint = EchoCyan,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                "Sign In with Google",
                                color = EchoText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Authenticate via AndroidX Credential Manager for verified Google OpenID identity, or fall back to an offline workspace profile when unconfigured.",
                                color = EchoMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    syncFeedbackMessage = null
                                    accountsManager.signInWithGoogle(
                                        activityContext = context,
                                        allowLocalFallback = true,
                                        onSuccess = {
                                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                                            if (it.isGoogleConnected) {
                                                syncFeedbackMessage = "Welcome, ${it.displayName}!"
                                            }
                                        },
                                        onError = { syncFeedbackMessage = it }
                                    )
                                },
                                enabled = !isSigningIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EchoCyan,
                                    contentColor = EchoOnCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_google_signin_action")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        color = EchoOnCyan,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Signing In...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Text(
                                        text = if (isOAuthConfigured) {
                                            "Sign In with Google Credential Manager"
                                        } else {
                                            "Sign In to ATHENA Workspace Profile"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Collapsible OAuth 2.0 Web Client ID Configuration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showOAuthConfig = !showOAuthConfig }
                                .testTag("btn_toggle_oauth_config"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = "OAuth Client ID",
                                    tint = EchoCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "OAuth 2.0 Web Client ID",
                                        color = EchoText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isOAuthConfigured) "Configured" else "Unconfigured (Local Fallback)",
                                        color = if (isOAuthConfigured) EchoGreen else EchoAmber,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showOAuthConfig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showOAuthConfig) "Collapse" else "Expand",
                                tint = EchoMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(visible = showOAuthConfig) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = clientIdInput,
                                    onValueChange = { clientIdInput = it },
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            "123456789-abc.apps.googleusercontent.com",
                                            color = EchoMuted,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EchoCyan,
                                        unfocusedBorderColor = EchoCardStroke,
                                        focusedTextColor = EchoText,
                                        unfocusedTextColor = EchoText,
                                        focusedContainerColor = EchoBgAlt,
                                        unfocusedContainerColor = EchoBgAlt
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_oauth_client_id")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            syncFeedbackMessage = null
                                            accountsManager.saveCustomOAuthClientId(clientIdInput)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EchoCyan,
                                            contentColor = EchoOnCyan
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_save_oauth_client_id")
                                    ) {
                                        Text("Save Client ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (oauthClientId.isNotBlank() || clientIdInput.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                clientIdInput = ""
                                                syncFeedbackMessage = null
                                                accountsManager.saveCustomOAuthClientId("")
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, EchoCardStroke),
                                            modifier = Modifier.testTag("btn_clear_oauth_client_id")
                                        ) {
                                            Text("Clear", fontSize = 11.sp, color = EchoMuted)
                                        }
                                    }
                                }
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
                        syncFeedbackMessage = null
                        accountsManager.signOut {
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoRed),
                    border = BorderStroke(1.dp, EchoRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_sign_out_account")
                ) {
                    Text("Sign Out", fontSize = 11.sp, color = EchoRed)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoCyan,
                        contentColor = EchoOnCyan
                    ),
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
