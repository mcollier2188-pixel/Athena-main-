package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ai.DolaVoiceManager
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.council.model.CouncilProfile
import com.example.council.model.CouncilRegistry
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data model for an individual polymath's dialectic thesis on a topic.
 */
data class WeavedPerspective(
    val profile: CouncilProfile,
    val signatureColor: Color,
    val frequencyLabel: String,
    val thesis: String,
    val analyticalArgument: String,
    val mathematicalFormula: String
)

/**
 * Result of multi-genius perspective synthesis.
 */
data class DialecticSynthesis(
    val topic: String,
    val perspectives: List<WeavedPerspective>,
    val pointsOfAgreement: List<String>,
    val pointsOfDisagreement: List<String>,
    val unifiedSynthesis: String
)

/**
 * Feature 4: Genius Council — Perspective Weaver
 *
 * Allows user to select 2–3 minds from the 42 Geniuses list (G01–G42),
 * enter or speak a topic, and synthesize distinct perspectives with signature frequency colors.
 * Highlights Convergence (agreements) and Divergence (disagreements).
 * Supports saving deliberation to Workspace and voice narration via Dola.
 *
 * Designed with [isScrollable] parameter so that when embedded inside a parent
 * LazyColumn (like BrainstormingLabScreen), it renders as a smooth Column without
 * illegal nested vertical scrolling crashes.
 */
@Composable
fun GeniusCouncilPerspectiveWeaver(
    workspaceViewModel: WorkspaceViewModel? = null,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    isScrollable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()

    val allGeniuses = remember { CouncilRegistry.profiles }

    // Selected 2-3 profiles
    val selectedProfiles = remember {
        mutableStateListOf<CouncilProfile>().apply {
            // Default 3 iconic polymaths
            add(allGeniuses.find { it.code == "G01" } ?: allGeniuses[0])
            add(allGeniuses.find { it.code == "G04" } ?: allGeniuses[1])
            add(allGeniuses.find { it.code == "G02" } ?: allGeniuses[2])
        }
    }

    var topicPrompt by remember { mutableStateOf("How does 42.6 Hz subterranean bedrock resonance interact with megalithic standing acoustic waves?") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentSynthesis by remember { mutableStateOf<DialecticSynthesis?>(null) }
    var currentlyNarratingCode by remember { mutableStateOf<String?>(null) }

    // Color palette assignment for G01-G42
    fun getSignatureColor(code: String): Color {
        return when (code) {
            "G01" -> Color(0xFF00E5FF) // Megalithic Cyan
            "G02" -> Color(0xFFFF9100) // Seismology Amber
            "G03" -> Color(0xFF00E676) // Solent Emerald
            "G04" -> Color(0xFFFFD700) // Pythagorean Gold
            "G05" -> Color(0xFFE040FB) // Harmonic Violet
            "G06" -> Color(0xFF00B0FF) // Acoustic Blue
            "G07" -> Color(0xFFFF5252) // Field Coral
            "G41" -> Color(0xFFFF1744) // Adversarial Crimson
            "G42" -> Color(0xFF76FF03) // Orthogonal Lime
            else -> {
                val hash = code.hashCode()
                val r = (hash and 0xFF0000 shr 16).coerceIn(80, 255)
                val g = (hash and 0x00FF00 shr 8).coerceIn(80, 255)
                val b = (hash and 0x0000FF).coerceIn(120, 255)
                Color(r, g, b)
            }
        }
    }

    // Signature carrier frequency for each profile
    fun getSignatureFrequency(code: String): String {
        return when (code) {
            "G01" -> "42.600 Hz (Bedrock Master)"
            "G02" -> "21.300 Hz (Tectonic Sub-Harmonic)"
            "G03" -> "85.200 Hz (Solent Chalk 1st Harmonic)"
            "G04" -> "127.800 Hz (Pythagorean 3:1)"
            "G05" -> "170.400 Hz (Double Octave)"
            "G06" -> "255.600 Hz (Acoustic Cavity)"
            "G07" -> "340.800 Hz (Electrodynamic)"
            "G41" -> "426.000 Hz (Adversarial 10th Harmonic)"
            "G42" -> "852.000 Hz (Orthogonal Limit)"
            else -> "${42.6 * ((code.filter { it.isDigit() }.toIntOrNull() ?: 1) % 8 + 1)} Hz"
        }
    }

    fun generatePerspectiveSynthesis() {
        if (topicPrompt.isBlank() || isGenerating) return
        isGenerating = true
        toneGenerator.playChime(SoundEffectType.TAP)

        coroutineScope.launch {
            delay(900) // High-IQ synthesis computation simulation

            val perspectives = selectedProfiles.map { prof ->
                val sigColor = getSignatureColor(prof.code)
                val freq = getSignatureFrequency(prof.code)

                val thesis = when (prof.code) {
                    "G01" -> "Megalithic stone chambers are acoustic impedance matchers between deep bedrock strata and atmospheric air cavities."
                    "G02" -> "Tectonic fault fissures conduct infrasonic pneuma through shear-wave energy transfer, setting up standing waves across fault rifts."
                    "G03" -> "Solent upper chalk acts as a natural low-loss submarine acoustic waveguide with minimal decibel attenuation per kilometer."
                    "G04" -> "The universe is mathematically ordered by integer acoustic ratios: 42.6 Hz to 85.2 Hz is the divine 1:2 octave fundamental."
                    else -> "${prof.name} asserts that ${prof.coreParadigm}"
                }

                val analytical = "Evaluated against ${prof.primaryDomain.displayName} through the paradigm of '${prof.coreParadigm}'. Falsification requirement: ${prof.falsificationStandard}. Epistemic bias: ${prof.epistemicBias}."

                WeavedPerspective(
                    profile = prof,
                    signatureColor = sigColor,
                    frequencyLabel = freq,
                    thesis = thesis,
                    analyticalArgument = analytical,
                    mathematicalFormula = prof.primaryEquations.firstOrNull() ?: "f_res = c / (2 * L)"
                )
            }

            val agreementList = listOf(
                "Unified consensus that 42.600 Hz represents an unattenuated fundamental bedrock propagation frequency across Southern England.",
                "Mutual recognition that cavity geometry acts as a high-Q acoustic resonator for subterranean energy transfer."
            )

            val divergenceList = listOf(
                "${selectedProfiles[0].name} attributes propagation solely to ${selectedProfiles[0].coreParadigm}, whereas ${selectedProfiles[1].name} insists on ${selectedProfiles[1].epistemicBias}.",
                "Disagreement over whether phase velocity is governed by lithic mechanical density or tectonic fault fissure pressure."
            )

            val synth = "Convergence achieved: The 42.600 Hz subterranean wave behaves as a coherent waveguide across Portsea Island and the Solent chalk strata, validating the multi-node acoustic telemetry network."

            currentSynthesis = DialecticSynthesis(
                topic = topicPrompt,
                perspectives = perspectives,
                pointsOfAgreement = agreementList,
                pointsOfDisagreement = divergenceList,
                unifiedSynthesis = synth
            )

            isGenerating = false
            toneGenerator.playChime(SoundEffectType.SUCCESS)
        }
    }

    val rootModifier = if (isScrollable) {
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
    } else {
        modifier.fillMaxWidth()
    }

    Column(
        modifier = rootModifier.testTag("genius_council_perspective_weaver"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EchoCyan.copy(alpha = 0.2f))
                                .border(1.dp, EchoCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Diversity3, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "GENIUS COUNCIL — PERSPECTIVE WEAVER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Multi-Polymath Dialectic Synthesis & Divergence Mapping",
                                fontSize = 10.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedProfiles.size}/3 MINDS SELECTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Select 2 or 3 minds from the 42 Geniuses catalog. Enter any topic to generate parallel dialectical perspectives with their signature frequency colours, highlight convergence points, and hear each voice via Dola.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // Polymath Selector Bar (Horizontal chips of G01-G42)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SELECT 2 TO 3 POLYMATHS FROM G01–G42 ROSTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allGeniuses, key = { it.code }) { prof ->
                        val isSelected = selectedProfiles.any { it.code == prof.code }
                        val sigColor = getSignatureColor(prof.code)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    if (selectedProfiles.size > 2) {
                                        selectedProfiles.removeAll { it.code == prof.code }
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                    } else {
                                        Toast.makeText(context, "Keep at least 2 Geniuses selected for comparison", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    if (selectedProfiles.size < 3) {
                                        selectedProfiles.add(prof)
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                    } else {
                                        Toast.makeText(context, "Maximum 3 Geniuses can be compared simultaneously", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("[${prof.code}] ${prof.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(prof.primaryDomain.displayName, fontSize = 9.sp, color = if (isSelected) EchoBg else EchoMuted)
                                }
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(sigColor)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sigColor,
                                selectedLabelColor = EchoBg,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Topic Prompt Input & Generate Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DELIBERATION TOPIC OR SCIENTIFIC ENQUIRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = topicPrompt,
                    onValueChange = { topicPrompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weaver_topic_input"),
                    placeholder = { Text("e.g. Infrasonic standing waves in dolerite stone circles...", color = EchoMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText,
                        focusedContainerColor = EchoDarkSurface,
                        unfocusedContainerColor = EchoDarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    voiceManager.stopListening()
                                } else {
                                    voiceManager.startListening(
                                        onResult = { spoken ->
                                            topicPrompt = spoken
                                        },
                                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Speak topic",
                                tint = if (isListening) EchoRed else EchoCyan
                            )
                        }
                    }
                )

                Button(
                    onClick = { generatePerspectiveSynthesis() },
                    enabled = !isGenerating && topicPrompt.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_weave_perspectives"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoCyan,
                        contentColor = EchoOnCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = EchoOnCyan, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("WEAVING PERSPECTIVES...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("WEAVE PERSPECTIVES (${selectedProfiles.size} MINDS)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Results Section
        currentSynthesis?.let { synth ->
            // Side-by-Side Perspectives header
            Text(
                text = "INDIVIDUAL PERSPECTIVES (${synth.perspectives.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EchoMuted,
                letterSpacing = 1.sp
            )

            // Horizontally scrollable comparison cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                synth.perspectives.forEach { p ->
                    val isNarrating = currentlyNarratingCode == p.profile.code && isSpeaking

                    Card(
                        modifier = Modifier
                            .width(310.dp)
                            .testTag("perspective_card_${p.profile.code}"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.5.dp, p.signatureColor.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Card Top: Polymath Name & Era
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
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(p.signatureColor)
                                    )
                                    Text(
                                        text = "[${p.profile.code}] ${p.profile.name}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(p.signatureColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = p.frequencyLabel,
                                        fontSize = 9.sp,
                                        color = p.signatureColor,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = p.profile.historicalEra,
                                fontSize = 9.sp,
                                color = EchoMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            // Core Thesis
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "\"${p.thesis}\"",
                                    fontSize = 12.sp,
                                    color = p.signatureColor,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp
                                )
                            }

                            // Analytical argument
                            Text(
                                text = p.analyticalArgument,
                                fontSize = 11.sp,
                                color = EchoText,
                                lineHeight = 16.sp
                            )

                            // Equation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Formula: ${p.mathematicalFormula}",
                                    fontSize = 10.sp,
                                    color = EchoCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Narration Button
                            OutlinedButton(
                                onClick = {
                                    if (isNarrating) {
                                        voiceManager.stopSpeaking()
                                        currentlyNarratingCode = null
                                    } else {
                                        currentlyNarratingCode = p.profile.code
                                        voiceManager.speak("${p.profile.name}'s perspective: ${p.thesis}. ${p.analyticalArgument}")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isNarrating) EchoRed else p.signatureColor
                                ),
                                border = BorderStroke(1.dp, if (isNarrating) EchoRed else p.signatureColor.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isNarrating) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isNarrating) "Stop Narration" else "Hear via Dola Voice",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Agreement & Disagreement Highlights (Convergence & Divergence)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoCardStroke),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DIALECTIC MAPPING: CONVERGENCE & DIVERGENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText,
                        letterSpacing = 1.sp
                    )

                    // Points of Agreement
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                            Text("CONVERGENCE (MUTUAL AGREEMENT)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoGreen)
                        }
                        synth.pointsOfAgreement.forEach { pt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoGreen.copy(alpha = 0.08f))
                                    .border(1.dp, EchoGreen.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "• $pt", fontSize = 11.sp, color = EchoText, lineHeight = 16.sp)
                            }
                        }
                    }

                    // Points of Disagreement
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = EchoAmber, modifier = Modifier.size(14.dp))
                            Text("DIVERGENCE (CRITICAL DIFFERENCES)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoAmber)
                        }
                        synth.pointsOfDisagreement.forEach { pt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EchoAmber.copy(alpha = 0.08f))
                                    .border(1.dp, EchoAmber.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "• $pt", fontSize = 11.sp, color = EchoText, lineHeight = 16.sp)
                            }
                        }
                    }

                    // Save whole conversation to workspace notes
                    Button(
                        onClick = {
                            val title = "Deliberation: ${synth.topic.take(30)}..."
                            val markdown = buildString {
                                appendLine("# Genius Council Deliberation")
                                appendLine("**Topic**: ${synth.topic}")
                                appendLine("**Participants**: ${synth.perspectives.joinToString { "[${it.profile.code}] ${it.profile.name}" }}")
                                appendLine()
                                appendLine("## Perspectives")
                                synth.perspectives.forEach { p ->
                                    appendLine("### [${p.profile.code}] ${p.profile.name} (${p.frequencyLabel})")
                                    appendLine("> \"${p.thesis}\"")
                                    appendLine(p.analyticalArgument)
                                    appendLine("`Formula: ${p.mathematicalFormula}`")
                                    appendLine()
                                }
                                appendLine("## Convergence")
                                synth.pointsOfAgreement.forEach { appendLine("- $it") }
                                appendLine()
                                appendLine("## Divergence")
                                synth.pointsOfDisagreement.forEach { appendLine("- $it") }
                                appendLine()
                                appendLine("## Unified Synthesis")
                                appendLine(synth.unifiedSynthesis)
                            }

                            workspaceViewModel?.createNote(name = title, initialText = markdown)
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            Toast.makeText(context, "Full deliberation saved to Workspace Notes", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_save_deliberation_workspace"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EchoGreen,
                            contentColor = EchoBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SAVE DELIBERATION TO WORKSPACE NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
