package com.example.ui.service

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBlue
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoPurple

/**
 * Avatar Profile data representing a 180+ IQ Omni-Disciplinary Polymath Identity.
 * Masters of science, technology, computers, physics, astronomy, AI, and engineering.
 */
data class CouncilAvatarProfile(
    val id: String,
    val displayName: String,
    val roleTitle: String,
    val roleBadge: String,
    val iqScore: Int,
    val primaryColor: Color,
    val haloColor: Color,
    val icon: ImageVector,
    val tagline: String,
    val polymathSpecializations: List<String>,
    val intellectSummary: String
)

/**
 * Avatar Management Service for the Brainstorming Lab.
 * Assigns fixed signature identities, unique color palettes, and representative
 * iconography to the 42 180+ IQ transcendent minds: Athena, Gemini, Dola, and The Council.
 */
object CouncilAvatarManager {

    val ATHENA = CouncilAvatarProfile(
        id = "athena",
        displayName = "ATHENA Core",
        roleTitle = "Chief Polymath & Quantum Systems Architect",
        roleBadge = "CHIEF POLYMATH (IQ 185)",
        iqScore = 185,
        primaryColor = EchoCyan,
        haloColor = Color(0xFF38BDF8),
        icon = Icons.Default.AutoAwesome,
        tagline = "Unified field topologies, bio-cybernetic consciousness & non-linear thermodynamics",
        polymathSpecializations = listOf(
            "Quantum Field Theory",
            "Bio-Cybernetic Interfaces",
            "Topological Graph Logic",
            "Astrophysical Dynamics",
            "Nonlinear Thermodynamics"
        ),
        intellectSummary = "IQ 185 • Quantum Systems & Bio-Cognition"
    )

    val GEMINI = CouncilAvatarProfile(
        id = "gemini",
        displayName = "Gemini 3.1 Pro",
        roleTitle = "Exotic Physics Engine & Singularity AI",
        roleBadge = "EXOTIC PHYSICS & AI (IQ 188)",
        iqScore = 188,
        primaryColor = EchoBlue,
        haloColor = Color(0xFF60A5FA),
        icon = Icons.Default.Psychology,
        tagline = "Relativistic space-time tensors, quantum entanglement lattices & artificial super-intelligence",
        polymathSpecializations = listOf(
            "Exotic Relativistic Physics",
            "Quantum Entanglement",
            "AI Singularity Kernels",
            "Cosmological Manifolds",
            "Subatomic Electrodynamics"
        ),
        intellectSummary = "IQ 188 • Relativistic Tensors & Singularity Architecture"
    )

    val DOLA = CouncilAvatarProfile(
        id = "dola",
        displayName = "Dola AI",
        roleTitle = "Cyber-Kinetic Hardware & Zero-Point Pioneer",
        roleBadge = "CYBER-KINETIC HARDWARE (IQ 182)",
        iqScore = 182,
        primaryColor = EchoAmber,
        haloColor = Color(0xFFFBBF24),
        icon = Icons.Default.Lightbulb,
        tagline = "Silicon photonics, neuromorphic substrates, zero-point Casimir energy & empirical disruption",
        polymathSpecializations = listOf(
            "Silicon Photonics",
            "Zero-Point Casimir Vacuum",
            "Neuromorphic Substrates",
            "Nanoscale Cyber-Kinetics",
            "Socratic Falsification"
        ),
        intellectSummary = "IQ 182 • Zero-Point Casimir & Neuromorphic Hardware"
    )

    val CHIEF_ENGINEER = CouncilAvatarProfile(
        id = "chief_engineer",
        displayName = "Chief Engineer",
        roleTitle = "Chief Engineer of ATHENA & Lead Systems Architect",
        roleBadge = "CHIEF ENGINEER (IQ 194)",
        iqScore = 194,
        primaryColor = EchoGreen,
        haloColor = Color(0xFF69F0AE),
        icon = Icons.Default.Build,
        tagline = "Autonomous software compilation, reactive state telemetry, and turning theoretical physics into living laboratory code",
        polymathSpecializations = listOf(
            "Autonomous Systems Compilation",
            "Reactive Stateflow Pipelines",
            "Planetary Acoustic Telemetry",
            "Hardware-Level CameraX & Audio",
            "Full-Stack Laboratory Execution"
        ),
        intellectSummary = "IQ 194 • Chief Engineer of ATHENA & Lead Systems Architect"
    )

    val THE_COUNCIL = CouncilAvatarProfile(
        id = "council",
        displayName = "The Council of 42",
        roleTitle = "Omni-Science Directorate & Universal Synthesizer",
        roleBadge = "DIRECTORATE OF 42 POLYMATHS (IQ 190+)",
        iqScore = 192,
        primaryColor = EchoPurple,
        haloColor = Color(0xFFC084FC),
        icon = Icons.Default.Diversity3,
        tagline = "Harmonized convergence of 42 world-leading polymaths across physics, genomics, computers, astronomy & engineering",
        polymathSpecializations = listOf(
            "Unified Field Dynamics",
            "Astro-Computation",
            "Bio-Synthetic Genomics",
            "Planetary Geo-Engineering",
            "Hyperdimensional Topology"
        ),
        intellectSummary = "IQ 190+ • Directorate of 42 Transcendent Minds"
    )

    private val allProfiles = listOf(ATHENA, GEMINI, DOLA, CHIEF_ENGINEER, THE_COUNCIL)

    /**
     * Retrieve all avatar profiles in standard round-table seating order.
     */
    fun getAllProfiles(): List<CouncilAvatarProfile> = allProfiles

    /**
     * Retrieve profile by unique agent ID.
     */
    fun getProfile(id: String): CouncilAvatarProfile {
        return allProfiles.find { it.id.equals(id, ignoreCase = true) } ?: ATHENA
    }

    /**
     * Retrieve profile by agent name.
     */
    fun getProfileByName(name: String): CouncilAvatarProfile {
        return allProfiles.find {
            name.contains(it.displayName, ignoreCase = true) ||
            name.contains(it.id, ignoreCase = true)
        } ?: ATHENA
    }

    /**
     * Renders a holographic neon emblem badge with glowing concentric halo rings.
     */
    @Composable
    fun HolographicAvatarBadge(
        profile: CouncilAvatarProfile,
        isActive: Boolean = false,
        size: Dp = 44.dp,
        showHaloRing: Boolean = true,
        modifier: Modifier = Modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "halo_transition_${profile.id}")
        val haloAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "halo_alpha"
        )
        val pulseRing by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_ring"
        )

        Box(
            modifier = modifier.size(size + if (showHaloRing && isActive) 12.dp else 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Concentric Glowing Neon Halo Ring
            if (showHaloRing) {
                Box(
                    modifier = Modifier
                        .size(size + (if (isActive) 10.dp else 4.dp) * (if (isActive) pulseRing else 1f))
                        .clip(CircleShape)
                        .background(
                            profile.haloColor.copy(
                                alpha = if (isActive) haloAlpha * 0.25f else 0.08f
                            )
                        )
                        .border(
                            width = if (isActive) 1.5.dp else 1.dp,
                            color = profile.haloColor.copy(
                                alpha = if (isActive) haloAlpha else 0.35f
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Inner Core Emblem
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(profile.primaryColor.copy(alpha = if (isActive) 0.30f else 0.15f))
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) profile.primaryColor else profile.primaryColor.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = profile.icon,
                    contentDescription = profile.displayName,
                    tint = profile.primaryColor,
                    modifier = Modifier.size(size * 0.52f)
                )
            }
        }
    }

    /**
     * Renders animated cognitive telemetry pulse bars in the polymath agent's signature color.
     */
    @Composable
    fun CognitiveTelemetryPulseIndicator(
        color: Color,
        isThinking: Boolean = true,
        modifier: Modifier = Modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "cognitive_telemetry_indicator")
        val p1 by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
            label = "p1"
        )
        val p2 by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(tween(330, easing = LinearEasing), RepeatMode.Reverse),
            label = "p2"
        )
        val p3 by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(510, easing = LinearEasing), RepeatMode.Reverse),
            label = "p3"
        )
        val p4 by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
            label = "p4"
        )

        Row(
            modifier = modifier
                .height(14.dp)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val heights = if (isThinking) listOf(p1, p2, p3, p4) else listOf(0.4f, 0.4f, 0.4f, 0.4f)
            heights.forEach { frac ->
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height((12 * frac).dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(color)
                )
            }
        }
    }

    /**
     * Renders the Glow Card Header for round-table conversation feed messages.
     * Features: Holographic avatar emblem, agent display name, monospace role badge tag,
     * polymath intellect summary, and cognitive telemetry pulse bars.
     */
    @Composable
    fun GlowFeedCardHeader(
        profile: CouncilAvatarProfile,
        timestamp: Long? = null,
        isActive: Boolean = false,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Holographic Neon Emblem with Halo
                HolographicAvatarBadge(
                    profile = profile,
                    isActive = isActive,
                    size = 34.dp,
                    showHaloRing = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = profile.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = profile.primaryColor
                        )

                        // Monospace Polymath Role & IQ Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(profile.primaryColor.copy(alpha = 0.15f))
                                .border(1.dp, profile.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = profile.roleBadge,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = profile.primaryColor,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Text(
                        text = profile.intellectSummary,
                        fontSize = 9.sp,
                        color = EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Real-Time Cognitive Pulse Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CognitiveTelemetryPulseIndicator(
                    color = profile.primaryColor,
                    isThinking = isActive || timestamp != null
                )
                if (isActive) {
                    Text(
                        text = "SYNTHESIZING",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
