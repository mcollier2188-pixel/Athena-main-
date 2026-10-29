package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.NetworkNode
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import java.util.Locale

private val Gold85Hz = Color(0xFFFFD700)
private val AlertYellow = Color(0xFFFFD54F)
private val AlertRed = Color(0xFFFF5252)

data class NodeTelemetryState(
    val node: NetworkNode,
    val signalLockPercent: Int,
    val lastSeenSecondsAgo: Int,
    val batteryPercent: Int,
    val uptimeHours: Int,
    val healthStatus: NodeHealth
)

enum class NodeHealth(val label: String, val color: Color) {
    OPTIMAL("Locked", EchoGreen),
    CALIBRATING("Phase Drift", AlertYellow),
    WARNING("Signal Attenuation", AlertRed)
}

/**
 * NodeSyncDashboardView Composable
 *
 * Displays live status for all 11 UK bedrock nodes with Portsmouth as Master,
 * showing signal lock quality, frequency comparison, uptime, and detailed profile modal.
 */
@Composable
fun NodeSyncDashboardView(
    modifier: Modifier = Modifier
) {
    val nodes = NetworkNode.ALL_NODES
    var selectedNodeForModal by remember { mutableStateOf<NetworkNode?>(null) }

    // Pre-calculate deterministic status for the 11 nodes
    val telemetryList = remember {
        nodes.map { node ->
            val isMaster = node.id == "N1"
            val lockPct = if (isMaster) 100 else (94 + (node.baseRatio * 3) % 6)
            val lastSeen = if (isMaster) 0 else ((node.baseRatio * 2) % 8)
            val battery = if (isMaster) 98 else (85 + (node.batteryVoltage.toInt() * 3) % 13)
            val uptime = 120 + node.baseRatio * 48
            val health = when {
                lockPct >= 96 -> NodeHealth.OPTIMAL
                lockPct >= 90 -> NodeHealth.CALIBRATING
                else -> NodeHealth.WARNING
            }
            NodeTelemetryState(
                node = node,
                signalLockPercent = lockPct,
                lastSeenSecondsAgo = lastSeen,
                batteryPercent = battery,
                uptimeHours = uptime,
                healthStatus = health
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("node_sync_dashboard_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, Gold85Hz.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Gold85Hz.copy(alpha = 0.2f))
                                .border(1.dp, Gold85Hz, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Gold85Hz, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(
                                text = "NODE STATUS & SYNC DASHBOARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Master Clock: N1 Portsmouth (85.200 Hz) · 11 Nodes Online",
                                fontSize = 9.sp,
                                color = Gold85Hz,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SYNC 100%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "All 10 field nodes are continuously synchronized against the N1 Portsmouth 85.200 Hz master clock baseline. Tap any node to inspect geological telemetry and battery health.",
                    fontSize = 10.sp,
                    color = EchoMuted,
                    lineHeight = 14.sp
                )
            }
        }

        // 11 Node Telemetry Cards
        telemetryList.forEach { state ->
            val node = state.node
            val isMaster = node.id == "N1"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedNodeForModal = node }
                    .testTag("node_status_card_${node.id}"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (isMaster) Gold85Hz.copy(alpha = 0.7f) else EchoCardStroke),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Health dot + ID + Name + Setting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Health Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(state.healthStatus.color)
                        )

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${node.id} ${node.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMaster) Gold85Hz else EchoText
                                )
                                if (isMaster) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Gold85Hz.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("MASTER", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Gold85Hz, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                            Text(
                                text = "${node.setting} · Last seen: ${if (state.lastSeenSecondsAgo == 0) "Live" else "${state.lastSeenSecondsAgo}s ago"}",
                                fontSize = 9.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    // Right: Frequency + Lock % + Comparison to Master
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "%.1f Hz", node.frequency),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Lock ${state.signalLockPercent}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = state.healthStatus.color,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "· ${state.batteryPercent}% (${state.uptimeHours}h)",
                                fontSize = 9.sp,
                                color = EchoMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // Detailed Node Profile Modal
    selectedNodeForModal?.let { node ->
        AlertDialog(
            onDismissRequest = { selectedNodeForModal = null },
            confirmButton = {
                Button(
                    onClick = { selectedNodeForModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${node.id} — ${node.name} Profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )
                    IconButton(onClick = { selectedNodeForModal = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Geological Setting: ${node.setting}",
                        fontSize = 11.sp,
                        color = EchoCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Coordinates: ${node.latitude}° N, ${node.longitude}° W\n" +
                                "• Acoustic Frequency: ${String.format(Locale.US, "%.2f", node.frequency)} Hz (${node.baseRatio}x Base)\n" +
                                "• Ratio to Portsmouth: ${String.format(Locale.US, "%.2f", node.comparedToMaster)}x Master\n" +
                                "• Daily Energy Harvest: ${node.dailyEnergyWh} Wh\n" +
                                "• Storage Battery: ${node.batteryVoltage}V · ${node.batteryCapacityAh} Ah (${node.totalBatteryWh.toInt()} Wh total)",
                        fontSize = 10.sp,
                        color = EchoMuted,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            containerColor = EchoCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
