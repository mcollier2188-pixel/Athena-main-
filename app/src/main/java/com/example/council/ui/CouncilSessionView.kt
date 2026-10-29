package com.example.council.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import com.example.council.engine.AdversarialAuditEngine
import com.example.council.engine.CouncilDeliberationEngine
import com.example.council.engine.CouncilSelectorEngine
import com.example.council.model.ConsensusVote
import com.example.council.model.CouncilExhibit
import com.example.council.model.CouncilProfile
import com.example.council.model.CouncilProtocol
import com.example.council.model.CouncilRegistry
import com.example.council.model.CouncilRole
import com.example.council.model.CouncilSession
import com.example.council.model.CouncilState
import com.example.council.model.CrossExamExchange
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
import kotlinx.coroutines.launch

/**
 * Interactive Workbench for the ATHENA Genius Council under v3.3-PROPOSED.
 * Supports manual tripartite slotting, explainable automatic scoring,
 * blind first-pass execution, disagreement preservation, and adversarial review.
 */
@Composable
fun CouncilSessionView(
    onSaveToWorkspaceNote: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Tripartite Member State (Defaults: G42 Hilsea Lead, G37 Tesla Comparator, G04 Pythagoras Verification)
    var domainLead by remember { mutableStateOf(CouncilRegistry.getByCode("G42")!!) }
    var comparator by remember { mutableStateOf(CouncilRegistry.getByCode("G37")!!) }
    var verification by remember { mutableStateOf(CouncilRegistry.getByCode("G04")!!) }

    // Deliberation Protocol & Visual Exhibit State
    var selectedProtocol by rememberSaveable { mutableStateOf(CouncilProtocol.SOCRATIC_CROSS_EXAM) }
    var attachedExhibit by remember { mutableStateOf<CouncilExhibit?>(null) }

    var userInquiry by rememberSaveable {
        mutableStateOf("What are the harmonic standing wave equations governing the 42.600 Hz bedrock velocity in the Solent basin?")
    }

    var activeSession by remember { mutableStateOf<CouncilSession?>(null) }
    var isDeliberating by remember { mutableStateOf(false) }

    // Dialog state for profile browser
    var showProfileBrowser by remember { mutableStateOf(false) }
    var roleSelectingFor by remember { mutableStateOf<CouncilRole?>(null) }
    var explainableScoreReport by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.testTag("council_session_workbench"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Council Header Banner & Provenance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(EchoCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "ATHENA GENIUS COUNCIL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = EchoText,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "TRIPARTITE DELIBERATION WORKBENCH · v3.3",
                                fontSize = 10.sp,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EchoGreen.copy(alpha = 0.15f))
                            .border(1.dp, EchoGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AIR-GAPPED OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                Text(
                    text = "Zero-Assumption Canon Protection: G01–G42 registry operates in an isolated namespace. Identity lineages remain CANON_REQUIRED pending source verification.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 15.sp
                )
            }
        }

        // 2. Tripartite Role Assignment Slots
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANDATORY TRIPARTITE ASSIGNMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = EchoCyan
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                val (l, c, v) = CouncilSelectorEngine.selectExplainableCouncil(userInquiry)
                                domainLead = l
                                comparator = c
                                verification = v
                                val scores = CouncilSelectorEngine.scoreAllProfiles(userInquiry).take(3)
                                explainableScoreReport = scores.joinToString("\n") {
                                    "[${it.profileCode}] ${it.profileName}: Total ${"%.1f".format(it.totalWeightedScore)} | ${it.selectionRationale}"
                                }
                                Toast.makeText(context, "Tripartite Council auto-selected via v3.3 weights", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("council_auto_select_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Auto-Select (v3.3)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                roleSelectingFor = null
                                showProfileBrowser = true
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("council_browse_directory_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoText),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Browse All 42", fontSize = 11.sp)
                        }
                    }
                }

                // 3 Role Cards
                RoleSlotCard(
                    role = CouncilRole.DOMAIN_LEAD,
                    profile = domainLead,
                    onPick = {
                        roleSelectingFor = CouncilRole.DOMAIN_LEAD
                        showProfileBrowser = true
                    }
                )
                RoleSlotCard(
                    role = CouncilRole.INDEPENDENT_COMPARATOR,
                    profile = comparator,
                    onPick = {
                        roleSelectingFor = CouncilRole.INDEPENDENT_COMPARATOR
                        showProfileBrowser = true
                    }
                )
                RoleSlotCard(
                    role = CouncilRole.VERIFICATION_SPECIALIST,
                    profile = verification,
                    onPick = {
                        roleSelectingFor = CouncilRole.VERIFICATION_SPECIALIST
                        showProfileBrowser = true
                    }
                )

                // Explainable Scoring Breakdown
                explainableScoreReport?.let { report ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoDarkSurface)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("v3.3 EXPLAINABLE SELECTION METRICS", fontSize = 9.sp, color = EchoCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(report, fontSize = 10.sp, color = EchoMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 3. Deliberation Protocol Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DELIBERATION PROTOCOL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = EchoCyan
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EchoCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = selectedProtocol.badge,
                            fontSize = 9.sp,
                            color = EchoCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CouncilProtocol.values()) { proto ->
                        val isSelected = selectedProtocol == proto
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedProtocol = proto },
                            label = {
                                Text(
                                    text = proto.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

                Text(
                    text = selectedProtocol.summary,
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 15.sp
                )
            }
        }

        // 4. Visual Exhibit Attachment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = "VISUAL EXHIBIT EXAMINATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EchoCyan
                        )
                    }

                    if (attachedExhibit != null) {
                        IconButton(
                            onClick = { attachedExhibit = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Exhibit", tint = EchoRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (attachedExhibit == null) {
                    Text(
                        text = "Attach diagrams, waveforms, or apparatus schematics for the Council to inspect collectively:",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    attachedExhibit = CouncilExhibit(
                                        title = "Chladni 42.6 Hz Bedrock Plate",
                                        description = "2D modal symmetry nodal lines locked at 42.600 Hz harmonic resonance.",
                                        technicalDomain = "Acoustic Cymatics"
                                    )
                                },
                                label = { Text("📐 Chladni 42.6 Hz Plate", fontSize = 10.sp, color = EchoText) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    attachedExhibit = CouncilExhibit(
                                        title = "Solent Geological Waveguide",
                                        description = "Cross-sectional bedrock stratigraphy with inverted chalk cladding.",
                                        technicalDomain = "Geological Telemetry"
                                    )
                                },
                                label = { Text("🗺️ Solent Waveguide", fontSize = 10.sp, color = EchoText) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    attachedExhibit = CouncilExhibit(
                                        title = "Tesla Wardenclyffe Resonator",
                                        description = "1899 Colorado Springs high-Q telluric standing wave schematic.",
                                        technicalDomain = "Electromagnetic Acoustics"
                                    )
                                },
                                label = { Text("⚡ Tesla Resonator", fontSize = 10.sp, color = EchoText) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = EchoBgAlt)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoDarkSurface)
                            .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📐 EXHIBIT: ${attachedExhibit?.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = attachedExhibit?.technicalDomain ?: "",
                                    fontSize = 9.sp,
                                    color = EchoGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = attachedExhibit?.description ?: "",
                                fontSize = 10.sp,
                                color = EchoText
                            )
                        }
                    }
                }
            }
        }

        // 5. User Inquiry Input Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "COUNCIL SCIENTIFIC INQUIRY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoCyan
                )

                OutlinedTextField(
                    value = userInquiry,
                    onValueChange = { userInquiry = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("council_inquiry_input"),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedContainerColor = EchoBgAlt,
                        unfocusedContainerColor = EchoBgAlt
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        isDeliberating = true
                        coroutineScope.launch {
                            try {
                                val session = CouncilDeliberationEngine.executeDeliberation(
                                    inquiry = userInquiry,
                                    domainLead = domainLead,
                                    comparator = comparator,
                                    verification = verification,
                                    protocol = selectedProtocol,
                                    exhibit = attachedExhibit
                                )
                                activeSession = session
                            } finally {
                                isDeliberating = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("council_convene_button"),
                    enabled = userInquiry.isNotBlank() && !isDeliberating,
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isDeliberating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = EchoOnCyan, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("EXECUTING ${selectedProtocol.badge} DELIBERATION...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CONVENE ${selectedProtocol.displayName.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 6. Session Results Section
        activeSession?.let { session ->
            // Consensus Voting Matrix Card
            if (session.consensusMatrix.isNotEmpty()) {
                val overallAgreement = session.consensusMatrix.map { it.agreementPercent }.average()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "CONSENSUS VOTING MATRIX",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = EchoGreen
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${"%.1f".format(overallAgreement)}% CONVERGENCE",
                                    fontSize = 10.sp,
                                    color = EchoGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        session.consensusMatrix.forEach { vote ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[${vote.profileCode}] ${vote.profileName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText
                                    )
                                    Text(
                                        text = "${vote.agreementPercent}% · ${vote.verdict}",
                                        fontSize = 10.sp,
                                        color = if (vote.agreementPercent >= 80) EchoGreen else EchoAmber,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { vote.agreementPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (vote.agreementPercent >= 80) EchoGreen else EchoAmber,
                                    trackColor = EchoBgAlt
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Alignment: ${vote.ideologicalAlignment}",
                                        fontSize = 9.sp,
                                        color = EchoMuted
                                    )
                                    Text(
                                        text = "Confidence: ${vote.epistemicConfidence}%",
                                        fontSize = 9.sp,
                                        color = EchoCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dialectic Cross-Examination Rounds Card
            if (session.crossExamRounds.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${session.protocol.displayName.uppercase()} ROUNDS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = EchoCyan
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${session.crossExamRounds.size} ROUNDS COMPLETED",
                                    fontSize = 9.sp,
                                    color = EchoCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        session.crossExamRounds.forEach { round ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, EchoCardStroke),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ROUND ${round.roundNumber} · ${round.speakerName} ➔ ${round.targetName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "+${round.scoreDelta} pts",
                                            fontSize = 10.sp,
                                            color = EchoGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoDarkSurface)
                                            .padding(8.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "DIALECTIC CHALLENGE:",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EchoAmber,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = round.challengeOrQuestion,
                                                fontSize = 11.sp,
                                                color = EchoText,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoCyan.copy(alpha = 0.08f))
                                            .padding(8.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "DEFENSE & RESOLUTION:",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EchoCyan,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = round.responseOrDefense,
                                                fontSize = 11.sp,
                                                color = EchoText,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Blind Outputs Cards
            Text(
                text = "INDEPENDENT BLIND FIRST-PASS OUTPUTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = EchoCyan
            )

            session.blindOutputs.forEach { output ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${output.role.title}: [${output.profileCode}] ${output.profileName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(output.evidenceClass.tag, fontSize = 10.sp, color = EchoGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(text = output.hypothesis, fontSize = 12.sp, color = EchoText)
                        Text(text = output.theoreticalRationale, fontSize = 11.sp, color = EchoMuted)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Uncertainty: ${"%.2f".format(output.uncertaintyScore)}", fontSize = 10.sp, color = EchoAmber, fontFamily = FontFamily.Monospace)
                            Text("Falsification Defined", fontSize = 10.sp, color = EchoGreen, fontFamily = FontFamily.Monospace)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(EchoDarkSurface)
                                .padding(6.dp)
                        ) {
                            Text("Refutation Criteria: \"${output.falsificationCriterion}\"", fontSize = 10.sp, color = EchoText.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            // Preserved Disagreements Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoAmber.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(16.dp))
                        Text("PRESERVED SCIENTIFIC DISAGREEMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoAmber, fontFamily = FontFamily.Monospace)
                    }

                    session.disagreements.forEach { dis ->
                        Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("• ${dis.topic} (${dis.tensionClass})", fontSize = 11.sp, color = EchoText, fontWeight = FontWeight.SemiBold)
                            Text(dis.perspectiveA, fontSize = 10.sp, color = EchoMuted)
                            Text(dis.perspectiveB, fontSize = 10.sp, color = EchoMuted)
                        }
                    }
                }
            }

            // Traceable ATHENA Synthesis Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TRACEABLE ATHENA SYNTHESIS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("Synthesis", session.athenaSynthesis))
                                    Toast.makeText(context, "Synthesis copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EchoMuted, modifier = Modifier.size(16.dp))
                            }

                            Button(
                                onClick = {
                                    onSaveToWorkspaceNote(session.athenaSynthesis)
                                    Toast.makeText(context, "Saved to Workspace Field Journal Note", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save Note", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(text = session.athenaSynthesis, fontSize = 12.sp, color = EchoText, lineHeight = 18.sp)

                    HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                    // Adversarial Review & Lifecycle State
                    session.auditReport?.let { audit ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (audit.isIntegrityIntact) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (audit.isIntegrityIntact) EchoGreen else EchoRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Audit Score: ${"%.1f".format(audit.evidenceAuditScore)}% · State: ${session.state.displayName}",
                                    fontSize = 11.sp,
                                    color = if (audit.isIntegrityIntact) EchoGreen else EchoRed,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = "SHA-256: ${session.sessionSha256Hash.take(12)}...",
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

    // Profile browser dialog for role assignment
    if (showProfileBrowser) {
        CouncilProfileBrowser(
            onSelectProfileForRole = { profile ->
                when (roleSelectingFor) {
                    CouncilRole.DOMAIN_LEAD -> domainLead = profile
                    CouncilRole.INDEPENDENT_COMPARATOR -> comparator = profile
                    CouncilRole.VERIFICATION_SPECIALIST -> verification = profile
                    null -> {}
                }
                showProfileBrowser = false
            },
            onDismiss = { showProfileBrowser = false }
        )
    }
}

@Composable
private fun RoleSlotCard(
    role: CouncilRole,
    profile: CouncilProfile,
    onPick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPick)
            .testTag("role_slot_${role.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
        border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = role.title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EchoCyan
                )
                Text(
                    text = "[${profile.code}] ${profile.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoText
                )
                Text(
                    text = "${profile.primaryDomain.displayName} · ${profile.historicalEra}",
                    fontSize = 11.sp,
                    color = EchoMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EchoDarkSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text("Change", fontSize = 11.sp, color = EchoCyan, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
