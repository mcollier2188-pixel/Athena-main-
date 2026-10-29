package com.example.council.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.council.model.CanonStatus
import com.example.council.model.CouncilDomain
import com.example.council.model.CouncilProfile
import com.example.council.model.CouncilRegistry
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

@Composable
fun CouncilProfileBrowser(
    onSelectProfileForRole: ((CouncilProfile) -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDomainFilter by remember { mutableStateOf<CouncilDomain?>(null) }
    var inspectingProfile by remember { mutableStateOf<CouncilProfile?>(null) }

    val filteredProfiles = remember(searchQuery, selectedDomainFilter) {
        CouncilRegistry.search(searchQuery).filter { profile ->
            selectedDomainFilter == null || profile.primaryDomain == selectedDomainFilter
        }
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
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = EchoCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "G01–G42 COUNCIL REGISTRY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "VERSION v3.3-PROPOSED · 42 SCIENTIFIC PROFILES",
                            fontSize = 10.sp,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("council_browser_search_input"),
                    placeholder = { Text("Search by code, name, domain, equation...", fontSize = 12.sp, color = EchoMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = EchoMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Domain Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedDomainFilter == null,
                            onClick = { selectedDomainFilter = null },
                            label = { Text("All Domains (${CouncilRegistry.profiles.size})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoMuted
                            )
                        )
                    }
                    items(CouncilDomain.values()) { domain ->
                        val count = CouncilRegistry.profiles.count { it.primaryDomain == domain }
                        FilterChip(
                            selected = selectedDomainFilter == domain,
                            onClick = { selectedDomainFilter = if (selectedDomainFilter == domain) null else domain },
                            label = { Text("${domain.displayName} ($count)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoOnCyan,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoMuted
                            )
                        )
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                // Profile Cards List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProfiles) { profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inspectingProfile = profile }
                                .testTag("council_item_${profile.code.lowercase()}"),
                            colors = CardDefaults.cardColors(containerColor = EchoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                            shape = RoundedCornerShape(8.dp)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EchoCyan.copy(alpha = 0.2f))
                                                .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = profile.code,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EchoCyan,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Text(
                                            text = profile.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoText
                                        )
                                    }
                                    Text(
                                        text = profile.historicalEra,
                                        fontSize = 10.sp,
                                        color = EchoMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = profile.primaryDomain.displayName,
                                    fontSize = 11.sp,
                                    color = EchoGreen,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = profile.coreParadigm,
                                    fontSize = 11.sp,
                                    color = EchoMuted,
                                    maxLines = 2
                                )

                                // CANON_REQUIRED provenance badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EchoAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CANON_REQUIRED (Unmapped Clone Lineage)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = EchoAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Directory", color = EchoCyan)
            }
        },
        containerColor = EchoBg,
        modifier = Modifier.testTag("council_profile_browser_dialog")
    )

    // Detailed Profile Inspection Dialog
    inspectingProfile?.let { profile ->
        CouncilProfileDetailDialog(
            profile = profile,
            onSelect = {
                onSelectProfileForRole?.invoke(profile)
                inspectingProfile = null
            },
            onDismiss = { inspectingProfile = null }
        )
    }
}

@Composable
fun CouncilProfileDetailDialog(
    profile: CouncilProfile,
    onSelect: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(EchoCyan)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(profile.code, fontWeight = FontWeight.Bold, color = EchoOnCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(profile.name, fontWeight = FontWeight.Bold, color = EchoText, fontSize = 15.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = EchoMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Domain: ${profile.primaryDomain.displayName} (${profile.historicalEra})", color = EchoGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                Card(colors = CardDefaults.cardColors(containerColor = EchoCard), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("THEORETICAL PARADIGM", fontSize = 10.sp, color = EchoCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(profile.coreParadigm, fontSize = 12.sp, color = EchoText)
                    }
                }

                if (profile.primaryEquations.isNotEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = EchoDarkSurface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("GOVERNING EQUATIONS", fontSize = 10.sp, color = EchoCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            profile.primaryEquations.forEach { eq ->
                                Text("• $eq", fontSize = 12.sp, color = EchoText, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = EchoCard), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("FALSIFICATION STANDARD", fontSize = 10.sp, color = EchoAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(profile.falsificationStandard, fontSize = 12.sp, color = EchoText)
                    }
                }

                // Canon Ledger Provenance
                Card(colors = CardDefaults.cardColors(containerColor = EchoAmber.copy(alpha = 0.1f)), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, EchoAmber.copy(alpha = 0.4f))) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("PROVENANCE: CANON_REQUIRED", fontSize = 10.sp, color = EchoAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(profile.canonNotes, fontSize = 11.sp, color = EchoText)
                    }
                }
            }
        },
        confirmButton = {
            if (onSelect != null) {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                ) {
                    Text("Assign to Council Slot", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = EchoMuted)
            }
        },
        containerColor = EchoBgAlt,
        modifier = Modifier.testTag("council_profile_detail_dialog")
    )
}
