package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gateway.GatewayRequester
import com.example.gateway.GatewayResponse
import com.example.gateway.GoogleGatewayService
import com.example.util.NetworkConnectivityMonitor
import com.example.util.NetworkStatus
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Embeddable GoogleGateway Composable component.
 *
 * Provides an isolated external-services interface for ATHENA, DOLA, and approved AI integrations.
 * Strictly adheres to zero-trust air-gap architecture:
 * - Has NO access to Archive, Workspace notes, Frequency history, SQLite, or internal memory.
 * - Outbound payload preview provides 100% transparency of transmitted data.
 * - Responses are treated as unprivileged external data and are never auto-persisted.
 */
@Composable
fun GoogleGateway(
    modifier: Modifier = Modifier,
    initialRequester: GatewayRequester = GatewayRequester.ATHENA,
    isMasterEnabledInitial: Boolean = true,
    onResultReceived: ((GatewayResponse) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Master Switch & Configuration State
    var isGatewayMasterEnabled by rememberSaveable { mutableStateOf(isMasterEnabledInitial) }
    var selectedRequester by rememberSaveable { mutableStateOf(initialRequester) }
    var enableGoogleSearch by rememberSaveable { mutableStateOf(true) }
    var enableGoogleMaps by rememberSaveable { mutableStateOf(false) }
    var requestQueryText by rememberSaveable { mutableStateOf("") }

    // Execution & Transient Result State
    var isLoading by remember { mutableStateOf(false) }
    var activeJob by remember { mutableStateOf<Job?>(null) }
    var currentResponse by remember { mutableStateOf<GatewayResponse?>(null) }

    // Real-Time Network Connectivity Monitor using Android ConnectivityManager NetworkCallback
    val networkStatusFlow = remember(context) {
        NetworkConnectivityMonitor.observeNetworkStatus(context)
    }
    val networkStatus by networkStatusFlow.collectAsState(
        initial = remember(context) { NetworkConnectivityMonitor.getInitialStatus(context) }
    )
    val isDeviceOnline = networkStatus.isConnected

    // Dynamic Outbound Payload Preview (Computed live based on explicit inputs)
    val outboundPreview = remember(selectedRequester, requestQueryText, enableGoogleSearch, enableGoogleMaps) {
        GoogleGatewayService.generateOutboundPayloadPreview(
            requester = selectedRequester,
            requestText = requestQueryText,
            enableGoogleSearch = enableGoogleSearch,
            enableGoogleMaps = enableGoogleMaps
        )
    }

    Column(
        modifier = modifier.testTag("google_gateway_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Status Indicator & Gateway Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gateway_header_card"),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Gateway",
                                tint = EchoCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "GOOGLE GATEWAY",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "ISOLATED EXTERNAL SERVICES INTERFACE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = EchoMuted
                            )
                        }
                    }

                    // Online / Offline Badge with Android Callback details
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isDeviceOnline) EchoGreen.copy(alpha = 0.15f)
                                else EchoAmber.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isDeviceOnline) EchoGreen.copy(alpha = 0.5f)
                                else EchoAmber.copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("gateway_online_status_badge"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDeviceOnline) EchoGreen else EchoAmber)
                        )
                        Icon(
                            imageVector = when {
                                !isDeviceOnline -> Icons.Default.WifiOff
                                networkStatus.isCellular && !networkStatus.isWifi -> Icons.Default.SignalCellularAlt
                                else -> Icons.Default.Wifi
                            },
                            contentDescription = null,
                            tint = if (isDeviceOnline) EchoGreen else EchoAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when {
                                !isDeviceOnline -> "OFFLINE"
                                networkStatus.isWifi -> "ONLINE (WIFI)"
                                networkStatus.isCellular -> "ONLINE (CELL)"
                                else -> "ONLINE"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDeviceOnline) EchoGreen else EchoAmber
                        )
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                // Air-Gap Zero-Trust Notice
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Isolation",
                        tint = EchoCyan,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = "Zero-Trust Air-Gap Active: Gateway has NO access to Archive, Workspace notes, Frequency records, SQLite, or internal memory. Only explicit request text leaves the sandbox.",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Disable Gateway Master Switch
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gateway_master_switch_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isGatewayMasterEnabled) EchoCard else EchoDarkSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isGatewayMasterEnabled) EchoCardStroke else EchoRed.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isGatewayMasterEnabled) Icons.Default.VpnLock else Icons.Default.Block,
                        contentDescription = "Master Switch",
                        tint = if (isGatewayMasterEnabled) EchoCyan else EchoRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Gateway Master Switch",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText
                        )
                        Text(
                            text = if (isGatewayMasterEnabled)
                                "Gateway armed. Outbound requests permitted."
                            else
                                "GATEWAY DISABLED: External transmissions strictly blocked.",
                            fontSize = 11.sp,
                            color = if (isGatewayMasterEnabled) EchoMuted else EchoRed
                        )
                    }
                }

                Switch(
                    checked = isGatewayMasterEnabled,
                    onCheckedChange = { isGatewayMasterEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EchoCyan,
                        checkedTrackColor = EchoCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = EchoRed,
                        uncheckedTrackColor = EchoRed.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("gateway_master_switch")
                )
            }
        }

        // Warning banner when master switch is disabled
        AnimatedVisibility(
            visible = !isGatewayMasterEnabled,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoRed.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoRed.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = EchoRed, modifier = Modifier.size(18.dp))
                    Text(
                        text = "External gateway is hard-locked. All existing ATHENA and local workspace tools continue offline without disruption.",
                        fontSize = 12.sp,
                        color = EchoText
                    )
                }
            }
        }

        // 3. Requester AI & Google Grounding Toggles
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gateway_config_card"),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "REQUESTER IDENTITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GatewayRequester.values().forEach { requester ->
                        val isSelected = selectedRequester == requester
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRequester = requester },
                            label = {
                                Text(
                                    text = requester.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
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
                                borderColor = if (isSelected) EchoCyan else EchoCardStroke
                            ),
                            modifier = Modifier.testTag("requester_chip_${requester.id.lowercase()}")
                        )
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                Text(
                    text = "GOOGLE SERVICES GROUNDING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoCyan
                )

                // Google Search toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Google Search", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                            Text("Ground query in live Google Search results", fontSize = 11.sp, color = EchoMuted)
                        }
                    }
                    Switch(
                        checked = enableGoogleSearch,
                        onCheckedChange = { enableGoogleSearch = it },
                        enabled = isGatewayMasterEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EchoCyan,
                            checkedTrackColor = EchoCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("google_search_toggle")
                    )
                }

                // Google Maps toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Google Maps", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EchoText)
                            Text("Ground query in geographic & places data", fontSize = 11.sp, color = EchoMuted)
                        }
                    }
                    Switch(
                        checked = enableGoogleMaps,
                        onCheckedChange = { enableGoogleMaps = it },
                        enabled = isGatewayMasterEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EchoGreen,
                            checkedTrackColor = EchoGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("google_maps_toggle")
                    )
                }
            }
        }

        // 4. Request / Question Field (Free-form input)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gateway_input_card"),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPLICIT REQUEST PAYLOAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = EchoCyan
                    )
                    if (requestQueryText.isNotBlank()) {
                        Text(
                            text = "${requestQueryText.length} chars",
                            fontSize = 11.sp,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                OutlinedTextField(
                    value = requestQueryText,
                    onValueChange = { requestQueryText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gateway_request_input"),
                    placeholder = {
                        Text(
                            "Enter the explicit inquiry for Google services (e.g. 'Recent seismic surveys in Solent channel' or 'Tesla Colorado Springs frequency papers')...",
                            color = EchoMuted,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(8.dp),
                    enabled = isGatewayMasterEnabled && !isLoading
                )

                Text(
                    text = "🔒 Privacy: Only characters typed above will leave the device. No project notes, history, or tokens are attached.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        // 5. Exact "Data Being Sent" Preview Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gateway_preview_card"),
            colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
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
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EchoCyan)
                        )
                        Text(
                            text = "EXACT DATA BEING SENT (AUDIT PREVIEW)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EchoCyan
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Payload Preview", outboundPreview))
                            Toast.makeText(context, "Payload preview copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy payload preview",
                            tint = EchoMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EchoBg)
                        .border(1.dp, EchoCardStroke.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = outboundPreview,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EchoText.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 6. Action Controls: Send and Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (requestQueryText.isBlank()) {
                        Toast.makeText(context, "Please enter a query first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    currentResponse = null
                    activeJob = coroutineScope.launch {
                        try {
                            val result = GoogleGatewayService.executeGatewayRequest(
                                context = context,
                                requester = selectedRequester,
                                requestText = requestQueryText,
                                enableGoogleSearch = enableGoogleSearch,
                                enableGoogleMaps = enableGoogleMaps,
                                isGatewayMasterEnabled = isGatewayMasterEnabled
                            )
                            currentResponse = result
                            onResultReceived?.invoke(result)
                        } finally {
                            isLoading = false
                            activeJob = null
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("gateway_send_button"),
                enabled = isGatewayMasterEnabled && requestQueryText.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EchoCyan,
                    contentColor = EchoOnCyan,
                    disabledContainerColor = EchoCardStroke,
                    disabledContentColor = EchoMuted
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = EchoOnCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("TRANSMITTING...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("SEND TO GOOGLE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Cancel Button
            OutlinedButton(
                onClick = {
                    activeJob?.cancel()
                    activeJob = null
                    isLoading = false
                    Toast.makeText(context, "Gateway transmission cancelled", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("gateway_cancel_button"),
                enabled = isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EchoRed,
                    disabledContentColor = EchoMuted
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLoading) EchoRed else EchoCardStroke
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // 7. Results Display Area (External Untrusted Data Sandbox)
        currentResponse?.let { resp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gateway_results_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (resp.isSuccess) EchoCard else EchoRed.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (resp.isSuccess) EchoCyan.copy(alpha = 0.5f) else EchoRed.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with untrusted label & Clear Results button
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
                                imageVector = if (resp.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (resp.isSuccess) EchoCyan else EchoRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "EXTERNAL UNTRUSTED RESULT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (resp.isSuccess) EchoCyan else EchoRed
                            )
                        }

                        // Temporary Clear Results control
                        Button(
                            onClick = { currentResponse = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoBgAlt,
                                contentColor = EchoMuted
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("gateway_clear_results_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear", fontSize = 11.sp)
                        }
                    }

                    // Disclaimer Tag
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoDarkSurface)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Sandbox Protection: This response is untrusted external data. It will NOT alter canon, write to memory, execute functions, or access local files.",
                            fontSize = 10.sp,
                            color = EchoMuted,
                            lineHeight = 14.sp
                        )
                    }

                    // Returned Content or Error
                    if (resp.errorMessage != null) {
                        Text(
                            text = resp.errorMessage,
                            color = EchoRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        androidx.compose.foundation.text.selection.SelectionContainer {
                            Text(
                                text = resp.rawText,
                                color = EchoText,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Sources / Citations
                    if (resp.sources.isNotEmpty()) {
                        HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)
                        Text(
                            text = "SOURCES & CITATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EchoCyan
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            resp.sources.forEach { source ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = EchoCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = source,
                                        fontSize = 11.sp,
                                        color = EchoCyan,
                                        modifier = Modifier.clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Source", source))
                                            Toast.makeText(context, "Copied citation", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Map Location if grounded
                    resp.mapLocation?.let { loc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                            Text(text = loc, fontSize = 11.sp, color = EchoGreen)
                        }
                    }
                }
            }
        }
    }
}
