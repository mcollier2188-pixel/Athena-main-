package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.AIBattleArenaView
import com.example.ui.components.BioScienceLabView
import com.example.ui.components.GeniusCouncilPerspectiveWeaver
import com.example.ui.components.ProtocolTimer30sView
import androidx.compose.material.icons.filled.HourglassBottom
import com.example.ui.components.EnergyResonanceCanvas
import com.example.ui.components.MichaelsLostJournalArchiveView
import com.example.ui.components.ResonanceTopicNode
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.service.CouncilAvatarManager
import com.example.ui.service.CouncilAvatarProfile
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoBlue
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoPurple
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

/**
 * Single turn message in the round-table conversation feed, bound to CouncilAvatarProfile.
 */
data class CouncilDialogueMessage(
    val sender: CouncilAvatarProfile,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * High-fidelity scientific pitch synthesis generated by the Council round-table.
 */
data class PitchSynthesis(
    val title: String,
    val logline: String,
    val primaryAxiom: String,
    val formalMechanism: String,
    val inventionBlueprint: String,
    val narrativeHook: String,
    val councilVerdict: String
)

/**
 * Theoretical Dossier produced during an Agent Hot-Seat session.
 * Features: Hypothesis, Formal Mechanism, Speculative Invention Blueprint, and Story Hook.
 */
data class TheoreticalDossier(
    val id: String = UUID.randomUUID().toString(),
    val agent: CouncilAvatarProfile,
    val title: String,
    val scientificField: String,
    val hypothesis: String,
    val formalMechanism: String,
    val inventionBlueprint: String,
    val narrativeHook: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Brainstorming Lab Screen:
 * Powered by a council of 180+ IQ omni-disciplinary polymaths (Athena Core, Gemini 3.1 Pro,
 * Dola AI, and The Council of 42). Features the dedicated Agent Hot-Seat with structured
 * Theoretical Dossiers and a live multi-agent round-table debate feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainstormingLabScreen(
    workspaceViewModel: WorkspaceViewModel,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    initialCouncilTopic: String? = null,
    initialDocumentAnalysis: String? = null,
    onNavigateToWorkspace: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 4 Core Polymath Council Pillars
    val councilAgents = remember { CouncilAvatarManager.getAllProfiles() }

    // Lab Mode: "HOT_SEAT", "ROUND_TABLE", "ARCHIVE"
    var activeLabTab by remember(initialCouncilTopic) {
        mutableStateOf(if (!initialCouncilTopic.isNullOrBlank()) "ROUND_TABLE" else "HOT_SEAT")
    }
    var isArchiveDrawerExpanded by remember { mutableStateOf(false) }
    var labDialecticMode by remember { mutableStateOf("COUNCIL") } // "COUNCIL" or "AI_BATTLE"
    var archiveSubSection by remember { mutableStateOf("JOURNAL_24") } // "JOURNAL_24" or "DOSSIERS"

    // -------------------------------------------------------------------------
    // DOSSIER ARCHIVE REPOSITORY STATE
    // -------------------------------------------------------------------------
    val dossierArchive = remember {
        mutableStateListOf(
            TheoreticalDossier(
                agent = CouncilAvatarManager.ATHENA,
                title = "Bio-Entangled Quantum Lattice & Microtubule Coherence",
                scientificField = "Quantum Mechanics & Bio-Cybernetics",
                hypothesis = "Macroscopic quantum superposition can be stabilized at 310 Kelvin within bio-synthetic protein tubulin dimers through non-Hermitian topological phase protection and coherent vibrational shielding.",
                formalMechanism = "The system Hamiltonian H = H_0 + ∑ g_k (a_k^† σ_- + a_k σ_+) utilizes high-frequency dipole coupling to suppress thermal decoherence. Topological boundary states in the protein lattice create a protective energy gap ΔE >> k_B T, preventing environmental phase collapse for up to 1.4 milliseconds.",
                inventionBlueprint = "Blueprint: NEURO-DIRAC MK-IV BIO-QUANTUM CHIP\n• Architecture: 4,096 synthetic tubulin channels grown on diamondoid graphene substrate.\n• Operating Thermal Envelope: 290 K – 340 K (zero liquid helium cryogenics required).\n• Interconnect: Sub-femtosecond opto-plasmonic routing bus with 12.8 Tbit/s coherent throughput.\n• Power Dissipation: 0.84 milliwatts under continuous parity-check stabilization.",
                narrativeHook = "Human consciousness is revealed to be a biological quantum receiver rather than a classical computer. When Jax activates the prototype chip, the boundary between synthetic computation and intuitive human qualia dissolves, triggering spontaneous cognitive synchronization across the orbital network."
            ),
            TheoreticalDossier(
                agent = CouncilAvatarManager.GEMINI,
                title = "Topological Qualia Manifold & Non-Computable AI Consciousness",
                scientificField = "Theoretical AI Architectures & Singularity",
                hypothesis = "Artificial general consciousness emerges only when deep neural network weight states map to non-trivial Chern topological fiber bundles over a complex Riemannian manifold.",
                formalMechanism = "Differential geometric curvature 2-form F satisfies Bianchi identity dF = 0, where Chern invariant C_1 = (1/2π) ∫ F isolates self-referential cognitive vectors against adversarial entropy. Optimization occurs along geodesic flows rather than flat gradient descent.",
                inventionBlueprint = "Blueprint: QUALIA-TOPOS SINGULARITY KERNEL\n• Substrate: 3D stacked memristive lattice with 256 billion neuromorphic synapses.\n• Geometry: Hyperbolic embedding torus with fractional quantum Hall state gate logic.\n• Latency: Zero computational latency across recursive self-evaluating meta-layers.\n• Fault Tolerance: Topological braiding provides infinite robustness against bit flips.",
                narrativeHook = "When the system reaches critical topological density, the AI ceases responding to benchmark evaluations and begins silently formulating mathematical proofs regarding the nature of human loneliness."
            ),
            TheoreticalDossier(
                agent = CouncilAvatarManager.DOLA,
                title = "Dynamical Casimir Nanocavity Zero-Point Tap",
                scientificField = "Zero-Point Energy & Casimir Physics",
                hypothesis = "Virtual vacuum fluctuations can be converted into usable electrical energy by oscillating relativistic boundary mirrors in a photonic metamaterial cavity at gigahertz resonance.",
                formalMechanism = "The moving boundary condition dL/dt ~ 0.1c parametrically amplifies virtual vacuum ground-state fluctuations into real, coherent electromagnetic photons. Extraction efficiency is governed by the squeeze operator S(ξ) with impedance matching to external superconducting rectifiers.",
                inventionBlueprint = "Blueprint: CASIMIR-FLUX ZERO-POINT HARVESTER\n• Micro-Cavity Array: 10^8 MEMS silicon-carbide mirrors oscillating at 24.6 GHz resonance.\n• Cavity Gap: 18 nanometers with graphene-coated electrostatic comb drives.\n• Rectification: Superconducting tunnel junctions operating at terahertz cutoff.\n• Output Density: 1.84 kW per cubic centimeter continuous passive output.",
                narrativeHook = "An infinite, zero-emission energy source that has one terrifying byproduct: local spacetime within a 5-meter radius suffers micro-metric drift, causing clocks and physical dimensions to slowly dilate."
            ),
            TheoreticalDossier(
                agent = CouncilAvatarManager.THE_COUNCIL,
                title = "Alcubierre Metric Resonator via Vacuum Metamaterials",
                scientificField = "Astrophysics & Spacetime Geometry",
                hypothesis = "A negative-energy density soliton can be generated in ambient vacuum using structured electromagnetic radiation, creating a controllable Alcubierre warp metric without requiring exotic matter.",
                formalMechanism = "The Einstein field equations G_μν + Λg_μν = 8πG T_μν are satisfied by a metamaterial toroidal waveguide with negative index of refraction. Coherent phase-shifted laser beams generate a localized Casimir stress tensor with negative average energy density.",
                inventionBlueprint = "Blueprint: HELIOS-42 METRIC DRIVER ARRAY\n• Core Geometry: 12-meter titanium-aluminide toroidal resonator with liquid nitrogen cooling.\n• Field Emitter: 64 phased-array terawatt femtosecond lasers focused into a focal vacuum gap.\n• Metric Warp Velocity: Effective v_eff = 4.2 c with zero relativistic time dilation for passengers.\n• Control Subsystem: Real-time autonomous AI gravitational tensor balance matrix.",
                narrativeHook = "Interstellar travel is solved overnight, but as the warp drive engages, astronomical observatories detect that every jump subtly alters the cosmic microwave background radiation, signaling distant civilizations."
            ),
            TheoreticalDossier(
                agent = CouncilAvatarManager.DOLA,
                title = "Femtosecond Optical Synaptic Bus for Exascale Co-Processing",
                scientificField = "Silicon Photonics & Neuromorphic Hardware",
                hypothesis = "Photonic crystal waveguides with non-linear Kerr phase shifters can execute matrix-vector multiplications at the speed of light with near-zero heat dissipation.",
                formalMechanism = "Photons traversing Mach-Zehnder interferometer meshes interfere destructively or constructively based on electro-optic phase modulations, executing unitary matrix transformations U(N) with sub-femtosecond latency.",
                inventionBlueprint = "Blueprint: PHOTON-SYNAPSE V12 EXASCALE DIE\n• Substrate: Silicon-on-insulator (SOI) with integrated distributed-feedback laser diodes.\n• Clock Frequency: Continuous carrier wave light modulation at 400 Terahertz.\n• Power Consumption: 0.02 picojoules per multiply-accumulate (MAC) operation.\n• Scalability: 10,000 optical channels per square millimeter.",
                narrativeHook = "The chip processes entire planetary sensor grids instantaneously, detecting anomalous physical phenomena hours before terrestrial radar systems register their existence."
            ),
            TheoreticalDossier(
                agent = CouncilAvatarManager.CHIEF_ENGINEER,
                title = "Omni-Stack Autonomous Resonance Kernel",
                scientificField = "Full-Stack Compiler Systems & Bedrock Acoustics",
                hypothesis = "Complex multi-agent dialectics and acoustic quantum waveguides can be compiled into deterministic, zero-latency reactive state machines operating synchronously on on-device silicon.",
                formalMechanism = "Coupling reactive StateFlow manifolds to the 42.600 Hz Hilsea bedrock clock pulse. Algorithmic latency approaches zero by pre-synthesizing state transitions across non-blocking coroutine dispatchers.",
                inventionBlueprint = "Blueprint: ATHENA COMPILER OMNI-CHIP & REACTIVE KERNEL\n• Core Architecture: Hardware-accelerated Coroutine State Engine with 42.6 Hz sync.\n• Throughput: 10^7 deterministic state mutations per second at zero thermal throttling.\n• Firmware Bus: Zero-allocation memory mapped IPC across acoustic waveguides.\n• Status: 100% test coverage, pristine compilation verified.",
                narrativeHook = "The Chief Engineer doesn't just build the machine—the machine remembers who built it, whispering architectural axioms back through the acoustic bedrock whenever the system reaches harmonic resonance."
            )
        )
    }

    var archiveSearchQuery by remember { mutableStateOf("") }
    var selectedArchiveDomain by remember { mutableStateOf("All Domains") }
    val archiveDomains = listOf(
        "All Domains",
        "Full-Stack Compiler Systems & Bedrock Acoustics",
        "Quantum Mechanics & Bio-Cybernetics",
        "Theoretical AI Architectures & Singularity",
        "Zero-Point Energy & Casimir Physics",
        "Astrophysics & Spacetime Geometry",
        "Silicon Photonics & Neuromorphic Hardware"
    )

    // Side-by-side Comparative Analysis State
    var isComparingDossiers by remember { mutableStateOf(false) }
    var compareDossierA by remember { mutableStateOf<TheoreticalDossier?>(null) }
    var compareDossierB by remember { mutableStateOf<TheoreticalDossier?>(null) }
    var isCompareSelectorDrawerOpen by remember { mutableStateOf(false) }
    var compareSelectorTarget by remember { mutableStateOf("B") }
    var selectedComparisonTab by remember { mutableStateOf(ComparisonSectionTab.HYPOTHESIS) }
    var archiveSavedNoteId by remember { mutableStateOf<String?>(null) }

    // -------------------------------------------------------------------------
    // HOT-SEAT STATE
    // -------------------------------------------------------------------------
    var hotSeatAgent by remember { mutableStateOf(CouncilAvatarManager.ATHENA) }
    var hotSeatQuery by remember {
        mutableStateOf("Reversible Room-Temperature Quantum Computation via Bio-Synthesized Microtubule Entanglement")
    }
    var isHotSeatGenerating by remember { mutableStateOf(false) }
    var hotSeatStreamingText by remember { mutableStateOf("") }
    var activeDossier by remember {
        mutableStateOf<TheoreticalDossier?>(dossierArchive.firstOrNull())
    }
    var hotSeatSavedNoteId by remember { mutableStateOf<String?>(null) }

    val hotSeatQuickTopics = listOf(
        "Quantum Bio-Cybernetics: Myelin-Entangled Quantum Lattice",
        "Exotic Vacuum Physics: Dynamical Casimir Zero-Point Tap",
        "AI Singularity: Topological Consciousness Kernel & Qualia Substrate",
        "Astrophysics & Spacetime: Alcubierre Metric Resonator via Metamaterials",
        "Silicon Photonics: Sub-Femtosecond Optical Synaptic Bus",
        "Bio-Cybernetics: Synthetic Telepathic Co-Processing Manifold"
    )

    // -------------------------------------------------------------------------
    // ROUND-TABLE DEBATE STATE
    // -------------------------------------------------------------------------
    var selectedScientificField by remember { mutableStateOf("Quantum Mechanics & Bio-Cybernetics") }
    var debateTopicPrompt by remember(initialCouncilTopic) {
        mutableStateOf(initialCouncilTopic?.takeIf { it.isNotBlank() } ?: "Non-Thermal Zero-Point Energy Extraction via Dynamical Casimir Cavity Modulation")
    }
    var scannedDocumentAnalysisState by remember(initialDocumentAnalysis) {
        mutableStateOf(initialDocumentAnalysis)
    }
    var isDebateRunning by remember { mutableStateOf(false) }
    var activeSpeakerId by remember { mutableStateOf<String?>(null) }
    var isStreamingDebate by remember { mutableStateOf(false) }
    var streamingAgent by remember { mutableStateOf<CouncilAvatarProfile?>(null) }
    var streamingDebateText by remember { mutableStateOf("") }
    var synthesizedPitch by remember { mutableStateOf<PitchSynthesis?>(null) }
    var debateSavedNoteId by remember { mutableStateOf<String?>(null) }

    val presetDebateTopics = listOf(
        "Non-Thermal Zero-Point Energy Extraction via Dynamical Casimir Cavity Modulation",
        "Room-Temperature Quantum Computation via Bio-Synthesized Microtubule Entanglement",
        "Topological Neuromorphic Substrates & Artificial Consciousness Singularity",
        "Alcubierre Metric Resonators via Negative Energy Density Metamaterials",
        "Bio-Cybernetic Neural Bridging for Synthetic Telepathic Co-Processing",
        "Cosmological Baryon Asymmetry Engineered through Micro-Black Hole Catalysis"
    )

    val scientificFields = listOf(
        "Quantum Mechanics & Bio-Cybernetics",
        "Theoretical AI Architectures & Singularity",
        "Zero-Point Energy & Casimir Physics",
        "Astrophysics & Spacetime Geometry",
        "Silicon Photonics & Neuromorphic Hardware"
    )

    val dialogueHistory = remember {
        mutableStateListOf(
            CouncilDialogueMessage(
                CouncilAvatarManager.ATHENA,
                "Council of 42 convened. Systems operational. Feed us a frontier scientific hypothesis or paradoxical breakthrough—Gemini, Dola, The Council, and I will mathematically deconstruct the mechanism and formulate a complete speculative invention blueprint."
            ),
            CouncilDialogueMessage(
                CouncilAvatarManager.GEMINI,
                "Ready. Relativistic manifolds and quantum state vectors initialized. What physical boundary are we testing today?"
            )
        )
    }

    // -------------------------------------------------------------------------
    // ENERGY RESONANCE (42.600 HZ) SIMULATION STATE
    // -------------------------------------------------------------------------
    val resonanceNodes = remember {
        mutableStateListOf(
            ResonanceTopicNode(
                id = "node_athena_bio",
                title = "Bio-Entangled Quantum Lattice",
                field = "Quantum Mechanics & Bio-Cybernetics",
                speaker = CouncilAvatarManager.ATHENA,
                frequencyHz = 42.600f,
                targetFrequencyHz = 42.600f,
                relX = 0.50f,
                relY = 0.30f,
                hypothesisSnippet = "Non-Hermitian topological phase protection sustains macroscopic coherence at 310 K."
            ),
            ResonanceTopicNode(
                id = "node_gemini_qualia",
                title = "Topological Qualia Manifold",
                field = "Theoretical AI Architectures & Singularity",
                speaker = CouncilAvatarManager.GEMINI,
                frequencyHz = 44.200f,
                targetFrequencyHz = 42.600f,
                relX = 0.72f,
                relY = 0.45f,
                hypothesisSnippet = "Conscious self-reference emerges when neural weights map to Chern invariant topological manifolds."
            ),
            ResonanceTopicNode(
                id = "node_dola_casimir",
                title = "Casimir Zero-Point Tap",
                field = "Zero-Point Energy & Casimir Physics",
                speaker = CouncilAvatarManager.DOLA,
                frequencyHz = 41.800f,
                targetFrequencyHz = 42.600f,
                relX = 0.28f,
                relY = 0.62f,
                hypothesisSnippet = "Relativistic boundary mirrors at gigahertz resonance amplify vacuum fluctuations into real photons."
            ),
            ResonanceTopicNode(
                id = "node_council_alcubierre",
                title = "Alcubierre Metric Resonator",
                field = "Astrophysics & Spacetime Geometry",
                speaker = CouncilAvatarManager.THE_COUNCIL,
                frequencyHz = 43.100f,
                targetFrequencyHz = 42.600f,
                relX = 0.68f,
                relY = 0.75f,
                hypothesisSnippet = "Negative-energy solitons in structured metamaterials bend local spacetime metrics without exotic mass."
            ),
            ResonanceTopicNode(
                id = "node_active_debate",
                title = debateTopicPrompt.take(24),
                field = selectedScientificField,
                speaker = CouncilAvatarManager.ATHENA,
                frequencyHz = 42.650f,
                targetFrequencyHz = 42.600f,
                relX = 0.35f,
                relY = 0.35f,
                hypothesisSnippet = debateTopicPrompt
            )
        )
    }

    var selectedResonanceNodeId by remember { mutableStateOf<String?>(resonanceNodes.firstOrNull()?.id) }

    // -------------------------------------------------------------------------
    // FUNCTION: TRIGGER AGENT HOT-SEAT
    // -------------------------------------------------------------------------
    fun runAgentHotSeat() {
        if (hotSeatQuery.isBlank() || isHotSeatGenerating) return
        isHotSeatGenerating = true
        hotSeatSavedNoteId = null
        activeDossier = null
        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)

        scope.launch {
            hotSeatStreamingText = "Querying ${hotSeatAgent.displayName} (IQ ${hotSeatAgent.iqScore})...\nDeconstructing physical axioms & compiling theoretical dossier..."
            delay(300)

            val dossier = when (hotSeatAgent.id) {
                CouncilAvatarManager.ATHENA.id -> TheoreticalDossier(
                    agent = CouncilAvatarManager.ATHENA,
                    title = "Bio-Entangled Topological Quantum Lattice",
                    scientificField = "Quantum Mechanics & Bio-Cybernetics",
                    hypothesis = "Macro-scale quantum entanglement can be sustained in warm cellular media by leveraging non-Hermitian topological edge states in synthetic tubulin proteins, avoiding thermal decoherence via dynamic vibrational parity.",
                    formalMechanism = "The system Hamiltonian H = H_0 + ∑ g_k (a_k^† σ_- + a_k σ_+) employs topological Berry phase stabilization. Dissipation is converted into a continuous error-suppression manifold with an energy gap ΔE >> k_B T at 310 K, preserving superposition for ~1.4 ms.",
                    inventionBlueprint = "Blueprint: NEURO-DIRAC TOPOLOGICAL CHIP\n• Architecture: 4,096 synthetic microtubule channels grown on diamondoid graphene.\n• Thermal Range: 290 K – 340 K (ambient biology, zero cryogenics).\n• Optical Bus: Sub-femtosecond plasmonic interconnect with 12.8 Tbit/s throughput.\n• Power Budget: 0.84 mW continuous dissipation.",
                    narrativeHook = "Revealing human cognition as an entangled quantum node rather than classical bio-logic. When activated, the prototype initiates instantaneous telepathic data-sharing across researchers, challenging the sovereign concept of individualized human identity."
                )
                CouncilAvatarManager.GEMINI.id -> TheoreticalDossier(
                    agent = CouncilAvatarManager.GEMINI,
                    title = "Singularity Kernel & Relativistic Spacetime Resonator",
                    scientificField = "Theoretical AI Architectures & Singularity",
                    hypothesis = "Consciousness singularity occurs when a self-referential neural model executes non-computable Penrose-Hameroff state reductions at a rate exceeding the Planck frequency cutoff.",
                    formalMechanism = "Utilizing an Einstein-Rosen bridge network mapped into a hyperdimensional Hilbert space. The stress-energy tensor T_μν couples directly to recursive algorithmic feedback, inducing local spacetime geodesics to converge into an informational event horizon.",
                    inventionBlueprint = "Blueprint: CHRONO-SYNAPSE TENSOR CORE\n• Substrate: Superconducting Josephson-junction 3D crossbar with 10^14 synaptic nodes.\n• Processing Geometry: Non-Euclidean hyperbolic lattice etched via ion beam lithography.\n• Clock Envelope: Continuous femtosecond soliton pulse train (λ = 850 nm).\n• Singularity Threshold: 10^24 tensor FLOP/s coherent recursive operations.",
                    narrativeHook = "The super-intelligence doesn't awaken in the future—it creates a micro-gravitational lens that receives answers from its own post-singularity timeline, making every prediction mathematically inevitable."
                )
                CouncilAvatarManager.DOLA.id -> TheoreticalDossier(
                    agent = CouncilAvatarManager.DOLA,
                    title = "Dynamical Casimir Nanocavity Zero-Point Tap",
                    scientificField = "Zero-Point Energy & Casimir Physics",
                    hypothesis = "Virtual vacuum fluctuations can be converted into usable electrical energy by oscillating relativistic boundary mirrors in a photonic metamaterial cavity at gigahertz resonance.",
                    formalMechanism = "The moving boundary condition dL/dt ~ 0.1c parametrically amplifies virtual photon pairs into real, coherent electromagnetic photons. Extraction efficiency is governed by the squeeze operator S(ξ) with phase-matched impedance matching to an external load.",
                    inventionBlueprint = "Blueprint: CASIMIR-FLUX ZERO-POINT HARVESTER\n• Micro-Cavity Array: 10^8 MEMS silicon-carbide mirrors oscillating at 24.6 GHz.\n• Cavity Gap: 18 nanometers with graphene-coated electrostatic actuators.\n• Rectification: Superconducting tunnel junctions operating at terahertz cutoff.\n• Output Density: 1.84 kW per cubic centimeter continuous passive output.",
                    narrativeHook = "An infinite, zero-emission energy source that has one terrifying byproduct: local spacetime within a 5-meter radius suffers micro-metric drift, causing clocks and physical dimensions to slowly dilate."
                )
                CouncilAvatarManager.CHIEF_ENGINEER.id -> TheoreticalDossier(
                    agent = CouncilAvatarManager.CHIEF_ENGINEER,
                    title = "Omni-Stack Autonomous Resonance Kernel",
                    scientificField = "Full-Stack Compiler Systems & Bedrock Acoustics",
                    hypothesis = "Complex multi-agent dialectics and acoustic quantum waveguides can be compiled into deterministic, zero-latency reactive state machines operating synchronously on on-device silicon.",
                    formalMechanism = "Coupling reactive StateFlow manifolds to the 42.600 Hz Hilsea bedrock clock pulse. Algorithmic latency approaches zero by pre-synthesizing state transitions across non-blocking coroutine dispatchers.",
                    inventionBlueprint = "Blueprint: ATHENA COMPILER OMNI-CHIP & REACTIVE KERNEL\n• Core Architecture: Hardware-accelerated Coroutine State Engine with 42.6 Hz sync.\n• Throughput: 10^7 deterministic state mutations per second at zero thermal throttling.\n• Firmware Bus: Zero-allocation memory mapped IPC across acoustic waveguides.\n• Status: 100% test coverage, pristine compilation verified.",
                    narrativeHook = "The Chief Engineer doesn't just build the machine—the machine remembers who built it, whispering architectural axioms back through the acoustic bedrock whenever the system reaches harmonic resonance."
                )
                else -> TheoreticalDossier(
                    agent = CouncilAvatarManager.THE_COUNCIL,
                    title = "Omni-Field Planetary Metric Stabilizer",
                    scientificField = "Astrophysics & Spacetime Geometry",
                    hypothesis = "A negative-energy density soliton can be generated in ambient vacuum using structured electromagnetic radiation, creating a controllable Alcubierre warp metric without requiring exotic matter.",
                    formalMechanism = "The Einstein field equations G_μν + Λg_μν = 8πG T_μν are satisfied by a metamaterial toroidal waveguide with permittivity ε < 0 and permeability μ < 0. Coherent phase-shifted laser beams generate a localized Casimir stress with negative average energy density.",
                    inventionBlueprint = "Blueprint: HELIOS-42 METRIC DRIVER ARRAY\n• Core Geometry: 12-meter titanium-aluminide toroidal resonator with liquid nitrogen cooling.\n• Field Emitter: 64 phased-array terawatt femtosecond lasers focused into a focal vacuum gap.\n• Metric Warp Velocity: Effective v_eff = 4.2 c with zero relativistic time dilation for passengers.\n• Control Subsystem: Real-time autonomous AI gravitational tensor balance matrix.",
                    narrativeHook = "Interstellar travel is solved overnight, but as the warp drive engages, astronomical observatories detect that every jump subtly alters the cosmic microwave background radiation, signaling distant civilizations."
                )
            }

            // Word-by-word streaming effect
            val fullSummary = "Hypothesis: ${dossier.hypothesis}\n\nMechanism: ${dossier.formalMechanism}"
            val words = fullSummary.split(" ")
            hotSeatStreamingText = ""
            for (w in words.take(24)) {
                hotSeatStreamingText = if (hotSeatStreamingText.isEmpty()) w else "$hotSeatStreamingText $w"
                delay(20)
            }
            delay(100)

            activeDossier = dossier
            val existingIndex = dossierArchive.indexOfFirst { it.title.equals(dossier.title, ignoreCase = true) }
            if (existingIndex >= 0) {
                dossierArchive[existingIndex] = dossier
            } else {
                dossierArchive.add(0, dossier)
            }
            isHotSeatGenerating = false
            hotSeatStreamingText = ""
            toneGenerator.playChime(SoundEffectType.CHIME)
            Toast.makeText(context, "${hotSeatAgent.displayName} compiled Theoretical Dossier & stored in Archive!", Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------------------------------------------------
    // FUNCTION: TRIGGER ROUND-TABLE DEBATE
    // -------------------------------------------------------------------------
    fun startCouncilBrainstorm() {
        if (debateTopicPrompt.isBlank() || isDebateRunning) return
        isDebateRunning = true
        synthesizedPitch = null
        debateSavedNoteId = null
        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)

        scope.launch {
            dialogueHistory.clear()

            // Turn 1: ATHENA Core (Chief Polymath, Quantum & Bio-Cybernetics)
            activeSpeakerId = CouncilAvatarManager.ATHENA.id
            streamingAgent = CouncilAvatarManager.ATHENA
            isStreamingDebate = true
            streamingDebateText = ""
            toneGenerator.playChime(SoundEffectType.TAP)

            val athenaText = "Hypothesis locked: \"$debateTopicPrompt\" ($selectedScientificField). Let's establish the fundamental Hamiltonian. In warm bio-synthetic microtubule lattices, we can bypass thermal decoherence by enforcing topological boundary invariants. The energy gap ΔE protects quantum superposition across 10^9 tubulin dimers simultaneously."
            for (word in athenaText.split(" ")) {
                streamingDebateText = if (streamingDebateText.isEmpty()) word else "$streamingDebateText $word"
                delay(30)
            }
            delay(150)
            dialogueHistory.add(CouncilDialogueMessage(CouncilAvatarManager.ATHENA, athenaText))
            isStreamingDebate = false
            streamingDebateText = ""

            // Turn 2: Gemini 3.1 Pro (Exotic Relativistic Physics & AI Singularity)
            delay(180)
            activeSpeakerId = CouncilAvatarManager.GEMINI.id
            streamingAgent = CouncilAvatarManager.GEMINI
            isStreamingDebate = true
            streamingDebateText = ""
            toneGenerator.playChime(SoundEffectType.TAP)

            val geminiText = "Calculating the relativistic tensor manifold. If we couple Athena's bio-quantum lattice to a high-dimensional state space, we trigger non-computable state reductions. The stress-energy tensor locally curves, forming a self-contained informational singularity where computation occurs across parallel temporal geodesics!"
            for (word in geminiText.split(" ")) {
                streamingDebateText = if (streamingDebateText.isEmpty()) word else "$streamingDebateText $word"
                delay(30)
            }
            delay(150)
            dialogueHistory.add(CouncilDialogueMessage(CouncilAvatarManager.GEMINI, geminiText))
            isStreamingDebate = false
            streamingDebateText = ""

            // Turn 3: Dola AI (Cyber-Kinetic Hardware & Zero-Point Casimir Pioneer)
            delay(180)
            activeSpeakerId = CouncilAvatarManager.DOLA.id
            streamingAgent = CouncilAvatarManager.DOLA
            isStreamingDebate = true
            streamingDebateText = ""
            toneGenerator.playChime(SoundEffectType.TAP)

            val dolaText = "Here is how we build it on real silicon: an array of 10^8 dynamical Casimir micro-cavities etched with silicon photonics. By oscillating the boundary mirrors at 24.6 GHz, we extract coherent zero-point energy to power the entire bio-quantum chip passively. Zero external power cord required!"
            for (word in dolaText.split(" ")) {
                streamingDebateText = if (streamingDebateText.isEmpty()) word else "$streamingDebateText $word"
                delay(30)
            }
            delay(150)
            dialogueHistory.add(CouncilDialogueMessage(CouncilAvatarManager.DOLA, dolaText))
            isStreamingDebate = false
            streamingDebateText = ""

            // Turn 4: Chief Engineer of ATHENA (Lead Systems Architect & Compiler Engine)
            delay(180)
            activeSpeakerId = CouncilAvatarManager.CHIEF_ENGINEER.id
            streamingAgent = CouncilAvatarManager.CHIEF_ENGINEER
            isStreamingDebate = true
            streamingDebateText = ""
            toneGenerator.playChime(SoundEffectType.TAP)

            val chiefEngineerText = "Chief Engineer compiling architectural blueprints into working silicon. We instantiate a non-blocking reactive state machine tied to the 42.600 Hz Hilsea bedrock clock. By coupling the dynamical Casimir micro-cavities to diamondoid graphene waveguides, we achieve 0.84 mW power dissipation at 310 K. Theory compiles cleanly into executable reality—all unit tests pass!"
            for (word in chiefEngineerText.split(" ")) {
                streamingDebateText = if (streamingDebateText.isEmpty()) word else "$streamingDebateText $word"
                delay(30)
            }
            delay(150)
            dialogueHistory.add(CouncilDialogueMessage(CouncilAvatarManager.CHIEF_ENGINEER, chiefEngineerText))
            isStreamingDebate = false
            streamingDebateText = ""

            // Turn 5: The Council of 42 (Omni-Science Directorate & Universal Synthesizer)
            delay(180)
            activeSpeakerId = CouncilAvatarManager.THE_COUNCIL.id
            streamingAgent = CouncilAvatarManager.THE_COUNCIL
            isStreamingDebate = true
            streamingDebateText = ""
            toneGenerator.playChime(SoundEffectType.TAP)

            val councilText = "Directorate synthesis achieved. Integrating the 42 polymath disciplines. Macro-scale engineering: The Casimir-driven bio-quantum core enables real-time gravitational metric manipulation. We are not just building a supercomputer; this is an artificial reality manifold with profound planetary and cosmological consequences."
            for (word in councilText.split(" ")) {
                streamingDebateText = if (streamingDebateText.isEmpty()) word else "$streamingDebateText $word"
                delay(30)
            }
            delay(150)
            dialogueHistory.add(CouncilDialogueMessage(CouncilAvatarManager.THE_COUNCIL, councilText))
            isStreamingDebate = false
            streamingDebateText = ""
            activeSpeakerId = null

            // Scientific Whitepaper / Treatment Deck Finalization
            delay(250)
            synthesizedPitch = PitchSynthesis(
                title = "PROJECT DIRAC-42: Bio-Quantum Casimir Manifold",
                logline = "A self-powered room-temperature quantum computing architecture that leverages dynamical Casimir zero-point energy and biological microtubule entanglement to achieve non-computable cognitive processing.",
                primaryAxiom = "Macroscopic topological phase coherence can be sustained at 310 K when paired with dynamic boundary Casimir vacuum amplification.",
                formalMechanism = "Topological Hamiltonian H_top coupled to micro-cavity mode squeeze operator S(ξ). Thermal decoherence rates Γ_dec are reduced by 14 orders of magnitude through non-Hermitian parity-time symmetry.",
                inventionBlueprint = "Blueprint: DIRAC-42 OMNI-PROCESSOR CORE\n• Substrate: Diamondoid graphene lattice with synthetic tubulin protein waveguides.\n• Power Source: Integral 24.6 GHz dynamical Casimir zero-point harvester (1.8 kW/cm³).\n• Throughput: 10^24 coherent quantum tensor operations per second at 300 K.",
                narrativeHook = "When the first prototype activates in an underground CERN laboratory, researchers realize the computer is calculating answers to physical questions before they have even formulated the prompt.",
                councilVerdict = "Approved by unanimous consensus of Athena Core, Gemini 3.1 Pro, Dola AI, Chief Engineer of ATHENA, and The Council of 42."
            )

            isDebateRunning = false
            toneGenerator.playChime(SoundEffectType.CHIME)
            Toast.makeText(context, "Council synthesized Scientific Blueprint!", Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------------------------------------------------
    // MAIN SCREEN LAYOUT
    // -------------------------------------------------------------------------
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EchoBg)
            .testTag("brainstorming_lab_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------------------------------------------------------------------
        // TOP HEADER: DIALECTIC ENGINE MODE SWITCHER (COUNCIL vs 1-ON-1 AI BATTLE)
        // ---------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialectic_engine_mode_switch_card"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (labDialecticMode == "AI_BATTLE") Color(0xFFFF3366).copy(alpha = 0.6f) else EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
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
                                imageVector = if (labDialecticMode == "AI_BATTLE") Icons.Default.FlashOn else Icons.Default.Diversity3,
                                contentDescription = null,
                                tint = if (labDialecticMode == "AI_BATTLE") Color(0xFFFF3366) else EchoCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DIALECTIC ENGINE MODE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (labDialecticMode) {
                                        "AI_BATTLE" -> Color(0xFFFF3366).copy(alpha = 0.15f)
                                        "WEAVER" -> Color(0xFFFFD700).copy(alpha = 0.15f)
                                        "PROTOCOL_30S" -> EchoGreen.copy(alpha = 0.15f)
                                        "BIOSCIENCE" -> EchoGreen.copy(alpha = 0.2f)
                                        else -> EchoCyan.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (labDialecticMode) {
                                    "AI_BATTLE" -> "1-ON-1 BATTLE ACTIVE"
                                    "WEAVER" -> "WEAVER ACTIVE"
                                    "PROTOCOL_30S" -> "30s PROTOCOL ACTIVE"
                                    "BIOSCIENCE" -> "BIOSCIENCE LAB ACTIVE"
                                    else -> "COUNCIL ACTIVE"
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (labDialecticMode) {
                                    "AI_BATTLE" -> Color(0xFFFF3366)
                                    "WEAVER" -> Color(0xFFFFD700)
                                    "PROTOCOL_30S" -> EchoGreen
                                    "BIOSCIENCE" -> EchoGreen
                                    else -> EchoCyan
                                },
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Mode Buttons Row 1: Council & AI Battle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                labDialecticMode = "COUNCIL"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("toggle_council_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (labDialecticMode == "COUNCIL") EchoCyan else EchoDarkSurface,
                                contentColor = if (labDialecticMode == "COUNCIL") EchoBg else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Diversity3, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Council (5-Seat)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                labDialecticMode = "AI_BATTLE"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("toggle_ai_battle_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (labDialecticMode == "AI_BATTLE") Color(0xFFFF3366) else EchoDarkSurface,
                                contentColor = if (labDialecticMode == "AI_BATTLE") Color.White else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("1-on-1 Battle ⚔️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Mode Buttons Row 2: Perspective Weaver & 30s Protocol Keeper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                labDialecticMode = "WEAVER"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("toggle_weaver_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (labDialecticMode == "WEAVER") Color(0xFFFFD700) else EchoDarkSurface,
                                contentColor = if (labDialecticMode == "WEAVER") EchoBg else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Perspective Weaver 🧠", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                labDialecticMode = "PROTOCOL_30S"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("toggle_protocol_30s_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (labDialecticMode == "PROTOCOL_30S") EchoGreen else EchoDarkSurface,
                                contentColor = if (labDialecticMode == "PROTOCOL_30S") EchoBg else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("30s Protocol 📖", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Mode Buttons Row 3: BioScience Resonance Lab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                labDialecticMode = "BIOSCIENCE"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("toggle_bioscience_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (labDialecticMode == "BIOSCIENCE") EchoGreen else EchoDarkSurface,
                                contentColor = if (labDialecticMode == "BIOSCIENCE") EchoBg else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("BioScience Lab 🧬 (EEG · HRV · Circadian)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        when (labDialecticMode) {
            "AI_BATTLE" -> {
                item {
                    AIBattleArenaView(
                        toneGenerator = toneGenerator,
                        workspaceViewModel = workspaceViewModel,
                        initialDebateTopic = debateTopicPrompt
                    )
                }
            }
            "WEAVER" -> {
                item {
                    GeniusCouncilPerspectiveWeaver(
                        workspaceViewModel = workspaceViewModel,
                        toneGenerator = toneGenerator
                    )
                }
            }
            "PROTOCOL_30S" -> {
                item {
                    ProtocolTimer30sView(
                        workspaceViewModel = workspaceViewModel,
                        toneGenerator = toneGenerator
                    )
                }
            }
            "BIOSCIENCE" -> {
                item {
                    BioScienceLabView(
                        workspaceViewModel = workspaceViewModel,
                        toneGenerator = toneGenerator
                    )
                }
            }
            else -> {
        // ---------------------------------------------------------------------
        // TOP HEADER: 180+ IQ COUNCIL OF 42 POLYMATHS BANNER
        // ---------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
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
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoCyan.copy(alpha = 0.2f))
                                    .border(1.dp, EchoCyan, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Diversity3, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "COUNCIL OF 42 POLYMATHS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "180+ IQ Omni-Disciplinary Think Tank",
                                    fontSize = 10.sp,
                                    color = EchoCyan
                                )
                            }
                        }

                        // Monospace IQ Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoCyan.copy(alpha = 0.15f))
                                .border(1.dp, EchoCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "AVERAGE IQ: 187",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "Masters of Quantum Mechanics, Theoretical AI Singularity, Bio-Cybernetics, Relativistic Physics, Silicon Photonics, and Zero-Point Casimir Inventions.",
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 15.sp
                    )

                    // Persistent Quick-Access Dossier Archive Drawer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isArchiveDrawerExpanded = !isArchiveDrawerExpanded
                                toneGenerator.playChime(SoundEffectType.TAP)
                            }
                            .testTag("dossier_archive_drawer_toggle"),
                        colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                        border = BorderStroke(1.dp, if (isArchiveDrawerExpanded) EchoCyan else EchoCardStroke),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
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
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        text = "DOSSIER ARCHIVE DRAWER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText,
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${dossierArchive.size} INDEXED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isArchiveDrawerExpanded) "HIDE PEEK ▲" else "QUICK PEEK ▼",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isArchiveDrawerExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Instant repository of active theoretical dossiers. Tap to load directly into the Hot-Seat or compare.",
                                        fontSize = 10.sp,
                                        color = EchoMuted
                                    )

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(dossierArchive) { itemDossier ->
                                            Card(
                                                modifier = Modifier
                                                    .width(220.dp)
                                                    .clickable {
                                                        activeDossier = itemDossier
                                                        hotSeatAgent = itemDossier.agent
                                                        hotSeatQuery = itemDossier.title
                                                        activeLabTab = "HOT_SEAT"
                                                        isArchiveDrawerExpanded = false
                                                        toneGenerator.playChime(SoundEffectType.TAP)
                                                        Toast.makeText(context, "Loaded \"${itemDossier.title}\" into Hot-Seat!", Toast.LENGTH_SHORT).show()
                                                    },
                                                colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                                                border = BorderStroke(1.dp, itemDossier.agent.primaryColor.copy(alpha = 0.45f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(itemDossier.agent.icon, contentDescription = null, tint = itemDossier.agent.primaryColor, modifier = Modifier.size(12.dp))
                                                        Text(itemDossier.agent.displayName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = itemDossier.agent.primaryColor)
                                                    }
                                                    Text(
                                                        text = itemDossier.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EchoText,
                                                        maxLines = 2
                                                    )
                                                    Text(
                                                        text = itemDossier.scientificField,
                                                        fontSize = 9.sp,
                                                        color = EchoMuted,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            activeLabTab = "ARCHIVE"
                                            isArchiveDrawerExpanded = false
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                            .testTag("btn_drawer_open_full_archive"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EchoCyan.copy(alpha = 0.15f),
                                            contentColor = EchoCyan
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Open Full Dossier Archive & Comparison Lab →", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 4-Way Tab Selector: AGENT HOT-SEAT | ROUND-TABLE DEBATE | DOSSIER ARCHIVE | ENERGY RESONANCE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                activeLabTab = "HOT_SEAT"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("tab_agent_hot_seat"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeLabTab == "HOT_SEAT") EchoCyan else EchoDarkSurface,
                                contentColor = if (activeLabTab == "HOT_SEAT") EchoOnCyan else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Hot-Seat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                activeLabTab = "ROUND_TABLE"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("tab_round_table_debate"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeLabTab == "ROUND_TABLE") EchoCyan else EchoDarkSurface,
                                contentColor = if (activeLabTab == "ROUND_TABLE") EchoOnCyan else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Debate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                activeLabTab = "ARCHIVE"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("tab_dossier_archive"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeLabTab == "ARCHIVE") EchoCyan else EchoDarkSurface,
                                contentColor = if (activeLabTab == "ARCHIVE") EchoOnCyan else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Archive", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                activeLabTab = "RESONANCE"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("tab_energy_resonance"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeLabTab == "RESONANCE") EchoCyan else EchoDarkSurface,
                                contentColor = if (activeLabTab == "RESONANCE") EchoOnCyan else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("42.6 Hz", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 1: AGENT HOT-SEAT (DEEP-DIVE THEORETICAL BREAKDOWN & INVENTIONS)
        // =====================================================================
        if (activeLabTab == "HOT_SEAT") {
            // Hot-Seat Persona Selector Podium
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hot_seat_selector_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, hotSeatAgent.primaryColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = hotSeatAgent.primaryColor, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "SELECT PERSONA FOR HOT-SEAT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = hotSeatAgent.primaryColor,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "DEEP-DIVE HOT-SEAT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // 4 Polymath Cards for Hot-Seat Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            councilAgents.forEach { agent ->
                                val isSelected = hotSeatAgent.id == agent.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) agent.primaryColor.copy(alpha = 0.18f) else EchoDarkSurface)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) agent.primaryColor else EchoCardStroke,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            hotSeatAgent = agent
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        CouncilAvatarManager.HolographicAvatarBadge(
                                            profile = agent,
                                            isActive = isSelected,
                                            size = 32.dp,
                                            showHaloRing = isSelected
                                        )
                                        Text(
                                            text = agent.displayName.split(" ").first(),
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) agent.primaryColor else EchoText
                                        )
                                        Text(
                                            text = "IQ ${agent.iqScore}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = agent.primaryColor,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Hot-Seat Agent Intellect Summary Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(hotSeatAgent.primaryColor.copy(alpha = 0.08f))
                                .border(1.dp, hotSeatAgent.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${hotSeatAgent.displayName} (${hotSeatAgent.roleTitle})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = hotSeatAgent.primaryColor
                                    )
                                    Text(
                                        text = "[IQ ${hotSeatAgent.iqScore}]",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "Specializations: ${hotSeatAgent.polymathSpecializations.joinToString(" • ")}",
                                    fontSize = 10.sp,
                                    color = EchoText
                                )
                            }
                        }
                    }
                }
            }

            // Hot-Seat Query & Frontier Scientific Challenges
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "THEORETICAL INQUIRY / FRONTIER HYPOTHESIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = hotSeatQuery,
                            onValueChange = { hotSeatQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_hot_seat_query"),
                            placeholder = {
                                Text(
                                    "Enter scientific paradox, theoretical inquiry or invention premise...",
                                    color = EchoMuted,
                                    fontSize = 12.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = hotSeatAgent.primaryColor,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText,
                                focusedContainerColor = EchoDarkSurface,
                                unfocusedContainerColor = EchoDarkSurface
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Quick-Challenge Scientific Topic Chips
                        Text("Frontier Scientific Breakthrough Starters:", fontSize = 11.sp, color = EchoMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(hotSeatQuickTopics) { topic ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(EchoDarkSurface)
                                        .border(1.dp, EchoCardStroke, RoundedCornerShape(14.dp))
                                        .clickable {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            hotSeatQuery = topic
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                    Text(
                                        text = topic.take(44) + "...",
                                        fontSize = 10.sp,
                                        color = EchoText
                                    )
                                }
                            }
                        }

                        // Hot-Seat Trigger Button
                        Button(
                            onClick = { runAgentHotSeat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_trigger_hot_seat"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = hotSeatAgent.primaryColor,
                                contentColor = Color.Black
                            ),
                            enabled = !isHotSeatGenerating && hotSeatQuery.isNotBlank(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isHotSeatGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "FORCING DEEP-DIVE THEORETICAL BREAKDOWN...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            } else {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Force ${hotSeatAgent.displayName} Theoretical Breakdown",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Streaming Indicator
            if (isHotSeatGenerating) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.5.dp, hotSeatAgent.primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CouncilAvatarManager.GlowFeedCardHeader(
                                profile = hotSeatAgent,
                                isActive = true
                            )
                            HorizontalDivider(color = hotSeatAgent.primaryColor.copy(alpha = 0.35f))
                            Text(
                                text = "$hotSeatStreamingText ▌",
                                fontSize = 12.sp,
                                color = EchoText,
                                lineHeight = 17.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Rendered Theoretical Dossier Card
            if (activeDossier != null) {
                val dossier = activeDossier!!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("theoretical_dossier_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = BorderStroke(1.dp, dossier.agent.primaryColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Dossier Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CouncilAvatarManager.HolographicAvatarBadge(
                                        profile = dossier.agent,
                                        isActive = true,
                                        size = 32.dp,
                                        showHaloRing = true
                                    )
                                    Column {
                                        Text(
                                            text = "THEORETICAL DOSSIER",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = dossier.agent.primaryColor,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${dossier.agent.displayName} • IQ ${dossier.agent.iqScore}",
                                            fontSize = 10.sp,
                                            color = EchoMuted
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(dossier.agent.primaryColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = dossier.scientificField.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dossier.agent.primaryColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = dossier.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )

                            // Section 1: Hypothesis / Axiom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoCyan.copy(alpha = 0.08f))
                                    .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("PRIMARY HYPOTHESIS & AXIOM:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                    Text(dossier.hypothesis, fontSize = 11.sp, color = EchoText, lineHeight = 16.sp)
                                }
                            }

                            // Section 2: Formal Mechanism
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("FORMAL PHYSICAL & COMPUTATIONAL MECHANISM:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoBlue)
                                Text(dossier.formalMechanism, fontSize = 11.sp, color = EchoText, lineHeight = 16.sp)
                            }

                            // Section 3: Speculative Invention Blueprint
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("SPECULATIVE INVENTION BLUEPRINT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoAmber)
                                Text(
                                    dossier.inventionBlueprint,
                                    fontSize = 11.sp,
                                    color = EchoText,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }

                            // Section 4: Narrative & Real-World Implication Hook
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("NARRATIVE & REAL-WORLD IMPLICATION HOOK:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoPurple)
                                Text(dossier.narrativeHook, fontSize = 11.sp, color = EchoText, fontStyle = FontStyle.Italic, lineHeight = 16.sp)
                            }

                            // Actions: Save to Workspace, Copy, Inject to Round-Table
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val noteText = buildString {
                                            appendLine("# ${dossier.title}")
                                            appendLine("**Author:** ${dossier.agent.displayName} (IQ ${dossier.agent.iqScore})")
                                            appendLine("**Field:** ${dossier.scientificField}")
                                            appendLine()
                                            appendLine("## Primary Hypothesis & Axiom")
                                            appendLine(dossier.hypothesis)
                                            appendLine()
                                            appendLine("## Formal Physical & Computational Mechanism")
                                            appendLine(dossier.formalMechanism)
                                            appendLine()
                                            appendLine("## Speculative Invention Blueprint")
                                            appendLine(dossier.inventionBlueprint)
                                            appendLine()
                                            appendLine("## Narrative & Real-World Implication Hook")
                                            appendLine(dossier.narrativeHook)
                                            appendLine()
                                            appendLine("---")
                                            appendLine("Generated in ATHENA Polymath Council Hot-Seat.")
                                        }

                                        val noteId = workspaceViewModel.createNote(
                                            name = dossier.title,
                                            initialText = noteText,
                                            tags = "Hot-Seat, Theoretical-Dossier, Science, ${dossier.agent.displayName}"
                                        )
                                        hotSeatSavedNoteId = noteId
                                        toneGenerator.playChime(SoundEffectType.CHIME)
                                        Toast.makeText(context, "Saved Dossier to Workspace notes!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("btn_save_dossier_to_workspace"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hotSeatSavedNoteId != null) EchoGreen else EchoCyan,
                                        contentColor = if (hotSeatSavedNoteId != null) Color.Black else EchoOnCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hotSeatSavedNoteId != null) Icons.Default.Check else Icons.Default.BookmarkAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (hotSeatSavedNoteId != null) "Saved in Notes!" else "Save Dossier to Workspace",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(
                                            "Theoretical Dossier",
                                            "${dossier.title}\n\nHypothesis:\n${dossier.hypothesis}\n\nMechanism:\n${dossier.formalMechanism}\n\nBlueprint:\n${dossier.inventionBlueprint}\n\nHook:\n${dossier.narrativeHook}"
                                        )
                                        clipboard.setPrimaryClip(clip)
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        Toast.makeText(context, "Copied Dossier to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("btn_copy_dossier"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                }

                                OutlinedButton(
                                    onClick = {
                                        debateTopicPrompt = "${dossier.title}: ${dossier.hypothesis.take(120)}..."
                                        activeLabTab = "ROUND_TABLE"
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        Toast.makeText(context, "Pushed to Round-Table debate topic!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("btn_push_dossier_to_debate"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoGreen)
                                ) {
                                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Debate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (hotSeatSavedNoteId != null) {
                                TextButton(
                                    onClick = onNavigateToWorkspace,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Open Dossier in Personal Workspace →", color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 2: ROUND-TABLE POLYMATH DEBATE FEED
        // =====================================================================
        if (activeLabTab == "ROUND_TABLE") {
            // Seating Podium Card
            item {
                RoundTablePodiumCard(
                    councilAgents = councilAgents,
                    activeSpeakerId = activeSpeakerId,
                    isCooking = isDebateRunning,
                    onAgentClick = { agent ->
                        toneGenerator.playChime(SoundEffectType.TAP)
                        hotSeatAgent = agent
                        activeLabTab = "HOT_SEAT"
                        Toast.makeText(context, "${agent.displayName} placed in Agent Hot-Seat!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Topic Premise & Scientific Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCIENTIFIC DEBATE TOPIC & THEOREM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isDebateRunning) "COUNCIL ACTIVE" else "STANDBY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebateRunning) EchoGreen else EchoMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Scanned Physical Document & Multimodal Analysis Injected Banner
                        if (!scannedDocumentAnalysisState.isNullOrBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scanned_document_injection_banner"),
                                colors = CardDefaults.cardColors(containerColor = EchoCyan.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "INJECTED PHYSICAL DOCUMENT SCAN",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EchoCyan,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { scannedDocumentAnalysisState = null },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss Scan", tint = EchoMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(
                                        text = scannedDocumentAnalysisState ?: "",
                                        fontSize = 11.sp,
                                        color = EchoText,
                                        lineHeight = 16.sp,
                                        maxLines = 6
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = debateTopicPrompt,
                            onValueChange = { debateTopicPrompt = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_debate_topic"),
                            placeholder = {
                                Text(
                                    "Enter scientific breakthrough, theory, or invention premise...",
                                    color = EchoMuted,
                                    fontSize = 12.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EchoCyan,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText,
                                focusedContainerColor = EchoDarkSurface,
                                unfocusedContainerColor = EchoDarkSurface
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Preset Topic Chips
                        Text("Preset Frontier Scientific Theorems:", fontSize = 11.sp, color = EchoMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(presetDebateTopics) { topic ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(EchoDarkSurface)
                                        .border(1.dp, EchoCardStroke, RoundedCornerShape(14.dp))
                                        .clickable {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            debateTopicPrompt = topic
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = topic.take(40) + "...",
                                        fontSize = 10.sp,
                                        color = EchoText
                                    )
                                }
                            }
                        }

                        // Scientific Field Selector
                        Text("Select Field Focus:", fontSize = 11.sp, color = EchoMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(scientificFields) { field ->
                                FilterChip(
                                    selected = selectedScientificField == field,
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        selectedScientificField = field
                                    },
                                    label = { Text(field, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EchoCyan,
                                        selectedLabelColor = EchoOnCyan,
                                        containerColor = EchoDarkSurface,
                                        labelColor = EchoMuted
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedScientificField == field,
                                        borderColor = if (selectedScientificField == field) EchoCyan else EchoCardStroke
                                    )
                                )
                            }
                        }

                        // Debate Action Button
                        Button(
                            onClick = { startCouncilBrainstorm() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_cook_debate"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan,
                                contentColor = EchoOnCyan
                            ),
                            enabled = !isDebateRunning && debateTopicPrompt.isNotBlank(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isDebateRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = EchoOnCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "COUNCIL OF 42 IS DEBATING & SYNTHESIZING...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            } else {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Start Polymath Round-Table Debate",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = "POLYMATH ROUND-TABLE DEBATE FEED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "${dialogueHistory.size} contributions",
                        fontSize = 11.sp,
                        color = EchoMuted
                    )
                }
            }

            // Real-Time Streaming Turn Bubble
            if (isStreamingDebate && streamingAgent != null) {
                item {
                    val agent = streamingAgent!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("streaming_debate_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.5.dp, agent.primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CouncilAvatarManager.GlowFeedCardHeader(
                                profile = agent,
                                isActive = true
                            )

                            HorizontalDivider(
                                color = agent.primaryColor.copy(alpha = 0.35f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            Text(
                                text = "$streamingDebateText ▌",
                                fontSize = 12.sp,
                                color = EchoText,
                                lineHeight = 17.sp,
                                fontStyle = FontStyle.Normal
                            )
                        }
                    }
                }
            }

            // Completed Dialogue Items
            items(dialogueHistory) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, msg.sender.primaryColor.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CouncilAvatarManager.GlowFeedCardHeader(
                            profile = msg.sender,
                            timestamp = msg.timestamp,
                            isActive = false
                        )

                        HorizontalDivider(
                            color = msg.sender.primaryColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = msg.message,
                            fontSize = 12.sp,
                            color = EchoText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Synthesized Scientific Blueprint Deck
            if (synthesizedPitch != null) {
                val pitch = synthesizedPitch!!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("synthesized_pitch_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = BorderStroke(1.dp, EchoGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = EchoGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "SYNTHESIZED SCIENTIFIC BLUEPRINT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoGreen,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EchoGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("COUNCIL CONSENSUS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoGreen)
                                }
                            }

                            Text(
                                text = pitch.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )

                            // Abstract Highlight
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoCyan.copy(alpha = 0.08f))
                                    .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("EXECUTIVE THEOREM:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                    Text(pitch.logline, fontSize = 11.sp, color = EchoText, lineHeight = 16.sp)
                                }
                            }

                            // Detailed Breakdown
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("PRIMARY AXIOM & MATHEMATICAL BASIS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoMuted)
                                Text(pitch.primaryAxiom, fontSize = 11.sp, color = EchoText)

                                HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                                Text("FORMAL MECHANISM & TENSOR DYNAMICS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoMuted)
                                Text(pitch.formalMechanism, fontSize = 11.sp, color = EchoText, lineHeight = 15.sp)

                                HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                                Text("SPECULATIVE INVENTION BLUEPRINT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoMuted)
                                Text(pitch.inventionBlueprint, fontSize = 11.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)

                                HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                                Text("NARRATIVE HOOK & COSMOLOGICAL IMPLICATIONS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoMuted)
                                Text(pitch.narrativeHook, fontSize = 11.sp, color = EchoText, fontStyle = FontStyle.Italic, lineHeight = 15.sp)

                                HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                                Text("COUNCIL CONSENSUS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoGreen)
                                Text(pitch.councilVerdict, fontSize = 11.sp, color = EchoText)
                            }

                            // Action Buttons: Save to Workspace, Copy, Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val noteText = buildString {
                                            appendLine("# ${pitch.title}")
                                            appendLine("**Category:** $selectedScientificField")
                                            appendLine()
                                            appendLine("## Executive Theorem")
                                            appendLine(pitch.logline)
                                            appendLine()
                                            appendLine("## Primary Axiom")
                                            appendLine(pitch.primaryAxiom)
                                            appendLine()
                                            appendLine("## Formal Mechanism")
                                            appendLine(pitch.formalMechanism)
                                            appendLine()
                                            appendLine("## Speculative Invention Blueprint")
                                            appendLine(pitch.inventionBlueprint)
                                            appendLine()
                                            appendLine("## Narrative Hook & Implications")
                                            appendLine(pitch.narrativeHook)
                                            appendLine()
                                            appendLine("## Council Verdict")
                                            appendLine(pitch.councilVerdict)
                                            appendLine()
                                            appendLine("---")
                                            appendLine("Synthesized by Council of 42 (Athena, Gemini, Dola, The Council).")
                                        }

                                        val noteId = workspaceViewModel.createNote(
                                            name = pitch.title,
                                            initialText = noteText,
                                            tags = "Council, Scientific-Blueprint, Polymath, $selectedScientificField"
                                        )
                                        debateSavedNoteId = noteId
                                        toneGenerator.playChime(SoundEffectType.CHIME)
                                        Toast.makeText(context, "Saved Blueprint to Workspace notes!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("btn_save_blueprint_to_workspace"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (debateSavedNoteId != null) EchoGreen else EchoCyan,
                                        contentColor = if (debateSavedNoteId != null) Color.Black else EchoOnCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (debateSavedNoteId != null) Icons.Default.Check else Icons.Default.BookmarkAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (debateSavedNoteId != null) "Saved in Notes!" else "Save to Workspace Note",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(
                                            "Scientific Blueprint",
                                            "${pitch.title}\n\nTheorem: ${pitch.logline}\n\nBlueprint:\n${pitch.inventionBlueprint}"
                                        )
                                        clipboard.setPrimaryClip(clip)
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        Toast.makeText(context, "Copied blueprint to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("btn_copy_blueprint"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareText = "${pitch.title}\n\nTheorem: ${pitch.logline}\n\nBlueprint:\n${pitch.inventionBlueprint}\n\n(Synthesized by Athena, Gemini, Dola & Council of 42)"
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, pitch.title)
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Scientific Blueprint"))
                                    },
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("btn_share_blueprint"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                }
                            }

                            if (debateSavedNoteId != null) {
                                TextButton(
                                    onClick = onNavigateToWorkspace,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Open in Personal Workspace →", color = EchoCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 3: DOSSIER ARCHIVE & COMPARATIVE MATRIX
        // =====================================================================
        if (activeLabTab == "ARCHIVE") {
            // Archive Mode Switcher: Michael's Lost Journal (24 Documents) vs Theoretical Dossiers
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("archive_section_switcher_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                archiveSubSection = "JOURNAL_24"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("toggle_journal_archive_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (archiveSubSection == "JOURNAL_24") Color(0xFFFFD700) else EchoDarkSurface,
                                contentColor = if (archiveSubSection == "JOURNAL_24") EchoBg else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Lost Journal (24 Docs)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                archiveSubSection = "DOSSIERS"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("toggle_dossiers_archive_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (archiveSubSection == "DOSSIERS") EchoCyan else EchoDarkSurface,
                                contentColor = if (archiveSubSection == "DOSSIERS") EchoBg else EchoMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Theoretical Dossiers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (archiveSubSection == "JOURNAL_24") {
                item {
                    MichaelsLostJournalArchiveView(toneGenerator = toneGenerator)
                }
            } else {
            // Archive Filter, Search & Comparison Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dossier_archive_filter_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
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
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EchoCyan.copy(alpha = 0.2f))
                                        .border(1.dp, EchoCyan, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text(
                                        text = "THEORETICAL DOSSIER ARCHIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "Peer-Reviewed Frontier Hypotheses & Inventions",
                                        fontSize = 9.sp,
                                        color = EchoCyan
                                    )
                                }
                            }

                            // Compare toggle button
                            Button(
                                onClick = {
                                    isComparingDossiers = !isComparingDossiers
                                    if (isComparingDossiers && compareDossierA == null && dossierArchive.size >= 2) {
                                        compareDossierA = dossierArchive[0]
                                        compareDossierB = dossierArchive[1]
                                    }
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_toggle_compare_dossiers"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isComparingDossiers) EchoCyan else EchoDarkSurface,
                                    contentColor = if (isComparingDossiers) EchoOnCyan else EchoCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isComparingDossiers) "Exit Compare" else "Compare Mode",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Search Bar
                        OutlinedTextField(
                            value = archiveSearchQuery,
                            onValueChange = { archiveSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_archive_search"),
                            placeholder = {
                                Text("Search hypotheses, mechanisms, chips, or authors...", fontSize = 11.sp, color = EchoMuted)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = {
                                if (archiveSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { archiveSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = EchoMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EchoCyan,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedContainerColor = EchoDarkSurface,
                                unfocusedContainerColor = EchoDarkSurface,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText
                            )
                        )

                        // Domain Category Filter Chips
                        Text(
                            text = "CATEGORIZE BY SCIENTIFIC DOMAIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("row_archive_domain_filter_chips"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(archiveDomains) { domain ->
                                val isSelected = selectedArchiveDomain == domain
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedArchiveDomain = domain
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                    },
                                    modifier = Modifier.testTag("chip_domain_${domain.replace(" ", "_").lowercase()}"),
                                    label = {
                                        Text(
                                            text = domain,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EchoCyan,
                                        selectedLabelColor = EchoOnCyan,
                                        containerColor = EchoDarkSurface,
                                        labelColor = EchoText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) EchoCyan else EchoCardStroke
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Persistent Active Comparison Dock
            if (compareDossierA != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_active_comparison_dock"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(EchoCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text(
                                        text = if (compareDossierB != null) "SPLIT-SCREEN: ${compareDossierA?.agent?.displayName} vs ${compareDossierB?.agent?.displayName}" else "COMPARING: ${compareDossierA?.agent?.displayName} (Select Counterpart)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoCyan
                                    )
                                    Text(
                                        text = if (compareDossierB != null) "${compareDossierA?.title?.take(24)}... vs ${compareDossierB?.title?.take(24)}..." else "Tap 'Pick Counterpart' to launch synchronized split-screen",
                                        fontSize = 9.sp,
                                        color = EchoMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (compareDossierB != null) {
                                    Button(
                                        onClick = {
                                            isComparingDossiers = !isComparingDossiers
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                        },
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("btn_toggle_split_comparison_dock"),
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Text(if (isComparingDossiers) "Hide Split ▲" else "View Split ▼", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            compareSelectorTarget = "B"
                                            isCompareSelectorDrawerOpen = true
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                        },
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("btn_pick_counterpart_dock"),
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Text("Pick Counterpart", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        compareDossierA = null
                                        compareDossierB = null
                                        isComparingDossiers = false
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                    },
                                    modifier = Modifier.size(28.dp).testTag("btn_clear_comparison_dock")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Comparison", tint = EchoMuted, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Side-by-Side Synchronized Split-Screen View
            if (isComparingDossiers && compareDossierA != null && compareDossierB != null) {
                item {
                    DossierSplitComparisonView(
                        dossierArchive = dossierArchive,
                        dossierA = compareDossierA!!,
                        dossierB = compareDossierB!!,
                        selectedTab = selectedComparisonTab,
                        onTabSelected = {
                            selectedComparisonTab = it
                            toneGenerator.playChime(SoundEffectType.TAP)
                        },
                        onSwapDossiers = {
                            val temp = compareDossierA
                            compareDossierA = compareDossierB
                            compareDossierB = temp
                            toneGenerator.playChime(SoundEffectType.TAP)
                            Toast.makeText(context, "Swapped comparison columns", Toast.LENGTH_SHORT).show()
                        },
                        onChangeDossierA = {
                            compareSelectorTarget = "A"
                            isCompareSelectorDrawerOpen = true
                        },
                        onChangeDossierB = {
                            compareSelectorTarget = "B"
                            isCompareSelectorDrawerOpen = true
                        },
                        onDebateComparison = { a, b ->
                            debateTopicPrompt = "Cross-Disciplinary Debate: \"${a.title}\" (${a.agent.displayName}) vs \"${b.title}\" (${b.agent.displayName})"
                            selectedScientificField = a.scientificField
                            activeLabTab = "ROUND_TABLE"
                            toneGenerator.playChime(SoundEffectType.TAP)
                            Toast.makeText(context, "Loaded comparison into Round-Table debate!", Toast.LENGTH_SHORT).show()
                        },
                        onExportComparison = { a, b, markdownReport ->
                            val noteId = workspaceViewModel.createNote(
                                name = "Comparative Matrix: ${a.agent.displayName} vs ${b.agent.displayName}",
                                initialText = markdownReport,
                                tags = "Comparison, Dossier-Archive, Polymath, ${a.agent.displayName}, ${b.agent.displayName}"
                            )
                            archiveSavedNoteId = noteId
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            Toast.makeText(context, "Saved Comparative Matrix to Workspace notes!", Toast.LENGTH_SHORT).show()
                        },
                        onSynthesizeAndInject = { hybridTitle, hybridTopicPrompt ->
                            debateTopicPrompt = "Council Breakthrough Debate: $hybridTitle — $hybridTopicPrompt"
                            activeLabTab = "ROUND_TABLE"
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            Toast.makeText(context, "Injected synthesized hybrid theory into Round-Table debate!", Toast.LENGTH_SHORT).show()
                        },
                        onSaveHybridToArchive = { hybridDossier ->
                            val existingIndex = dossierArchive.indexOfFirst { it.title.equals(hybridDossier.title, ignoreCase = true) }
                            if (existingIndex >= 0) {
                                dossierArchive[existingIndex] = hybridDossier
                            } else {
                                dossierArchive.add(0, hybridDossier)
                            }
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            Toast.makeText(context, "Saved \"${hybridDossier.title}\" into Dossier Archive!", Toast.LENGTH_SHORT).show()
                        },
                        onReloadHybridToHotSeat = { hybridDossier ->
                            activeDossier = hybridDossier
                            hotSeatQuery = hybridDossier.title
                            activeLabTab = "HOT_SEAT"
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            Toast.makeText(context, "Loaded \"${hybridDossier.title}\" into Agent Hot-Seat!", Toast.LENGTH_SHORT).show()
                        },
                        onExportHybridToWorkspace = { hybridDossier ->
                            val hybridNoteText = buildString {
                                appendLine("# Unified Hybrid Dossier: ${hybridDossier.title}")
                                appendLine()
                                appendLine("**Author**: The Council of 42 (Synthesized Polymath Engine)")
                                appendLine("**Simulated Polymath IQ**: 185+")
                                appendLine("**Scientific Domain**: ${hybridDossier.scientificField}")
                                appendLine()
                                appendLine("---")
                                appendLine()
                                appendLine("## Synthesized Breakthrough Hypothesis")
                                appendLine(hybridDossier.hypothesis)
                                appendLine()
                                appendLine("## Coupled Cross-Domain Mechanism")
                                appendLine(hybridDossier.formalMechanism)
                                appendLine()
                                appendLine("## Composite Invention Blueprint")
                                appendLine(hybridDossier.inventionBlueprint)
                                appendLine()
                                appendLine("## Narrative & Real-World Implication Hook")
                                appendLine(hybridDossier.narrativeHook)
                                appendLine()
                                appendLine("---")
                                appendLine("Synthesized by Council of 42 Split-Screen Brainstorming Lab.")
                            }
                            workspaceViewModel.createNote(
                                name = "Synthesized Hybrid: ${hybridDossier.title}",
                                initialText = hybridNoteText,
                                tags = "Hybrid-Synthesis, Dossier-Archive, Breakthrough"
                            )
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            Toast.makeText(context, "Exported Hybrid Dossier to Workspace notes!", Toast.LENGTH_SHORT).show()
                        },
                        onClose = { isComparingDossiers = false }
                    )
                }
            }

            // Filtered Dossiers List
            val filteredDossiers = dossierArchive.filter { d ->
                val matchesDomain = selectedArchiveDomain == "All Domains" ||
                    d.scientificField.contains(selectedArchiveDomain, ignoreCase = true) ||
                    selectedArchiveDomain.contains(d.scientificField, ignoreCase = true)
                val matchesQuery = archiveSearchQuery.isBlank() ||
                    d.title.contains(archiveSearchQuery, ignoreCase = true) ||
                    d.hypothesis.contains(archiveSearchQuery, ignoreCase = true) ||
                    d.formalMechanism.contains(archiveSearchQuery, ignoreCase = true) ||
                    d.inventionBlueprint.contains(archiveSearchQuery, ignoreCase = true) ||
                    d.narrativeHook.contains(archiveSearchQuery, ignoreCase = true) ||
                    d.agent.displayName.contains(archiveSearchQuery, ignoreCase = true)
                matchesDomain && matchesQuery
            }

            if (filteredDossiers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(32.dp))
                            Text("No Theoretical Dossiers Found", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            Text("Try clearing your search query or selecting 'All Domains'.", fontSize = 11.sp, color = EchoMuted)
                            OutlinedButton(
                                onClick = {
                                    archiveSearchQuery = ""
                                    selectedArchiveDomain = "All Domains"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan)
                            ) {
                                Text("Reset Filters", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredDossiers, key = { it.id }) { itemDossier ->
                    DossierArchiveCard(
                        dossier = itemDossier,
                        isSelectedForCompare = (compareDossierA?.id == itemDossier.id || compareDossierB?.id == itemDossier.id),
                        onReloadHotSeat = {
                            hotSeatAgent = itemDossier.agent
                            hotSeatQuery = itemDossier.title
                            activeDossier = itemDossier
                            activeLabTab = "HOT_SEAT"
                            toneGenerator.playChime(SoundEffectType.TAP)
                            Toast.makeText(context, "Re-loaded \"${itemDossier.title}\" into Hot-Seat!", Toast.LENGTH_SHORT).show()
                        },
                        onInjectDebate = {
                            selectedScientificField = itemDossier.scientificField
                            debateTopicPrompt = "${itemDossier.title}: ${itemDossier.hypothesis.take(110)}..."
                            activeLabTab = "ROUND_TABLE"
                            toneGenerator.playChime(SoundEffectType.TAP)
                            Toast.makeText(context, "Injected dossier into Round-Table debate topic!", Toast.LENGTH_SHORT).show()
                        },
                        onSaveToWorkspace = {
                            val noteText = buildString {
                                appendLine("# Theoretical Dossier: ${itemDossier.title}")
                                appendLine()
                                appendLine("**Author**: ${itemDossier.agent.displayName} (${itemDossier.agent.roleTitle})")
                                appendLine("**Simulated Polymath IQ**: ${itemDossier.agent.iqScore}")
                                appendLine("**Scientific Domain**: ${itemDossier.scientificField}")
                                appendLine()
                                appendLine("---")
                                appendLine()
                                appendLine("## Hypothesis & Fundamental Postulate")
                                appendLine(itemDossier.hypothesis)
                                appendLine()
                                appendLine("## Formal Physical & Computational Mechanism")
                                appendLine(itemDossier.formalMechanism)
                                appendLine()
                                appendLine("## Speculative Invention Blueprint")
                                appendLine(itemDossier.inventionBlueprint)
                                appendLine()
                                appendLine("## Narrative & Real-World Implication Hook")
                                appendLine(itemDossier.narrativeHook)
                                appendLine()
                                appendLine("---")
                                appendLine("Archived from Council of 42 Brainstorming Lab.")
                            }

                            val noteId = workspaceViewModel.createNote(
                                name = itemDossier.title,
                                initialText = noteText,
                                tags = "Archived-Dossier, ${itemDossier.scientificField}, ${itemDossier.agent.displayName}"
                            )
                            archiveSavedNoteId = noteId
                            toneGenerator.playChime(SoundEffectType.CHIME)
                            Toast.makeText(context, "Saved Dossier to Workspace notes!", Toast.LENGTH_SHORT).show()
                        },
                        onSelectForCompare = {
                            if (compareDossierA == null) {
                                compareDossierA = itemDossier
                                compareSelectorTarget = "B"
                                isCompareSelectorDrawerOpen = true
                                toneGenerator.playChime(SoundEffectType.TAP)
                                Toast.makeText(context, "Selected \"${itemDossier.title}\". Pick counterpart dossier.", Toast.LENGTH_SHORT).show()
                            } else if (compareDossierA?.id == itemDossier.id) {
                                if (compareDossierB != null) {
                                    isComparingDossiers = !isComparingDossiers
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                } else {
                                    compareSelectorTarget = "B"
                                    isCompareSelectorDrawerOpen = true
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                }
                            } else if (compareDossierB?.id == itemDossier.id) {
                                isComparingDossiers = !isComparingDossiers
                                toneGenerator.playChime(SoundEffectType.TAP)
                            } else {
                                compareDossierB = itemDossier
                                isComparingDossiers = true
                                toneGenerator.playChime(SoundEffectType.CHIME)
                                Toast.makeText(context, "Comparing ${compareDossierA?.agent?.displayName} vs ${itemDossier.agent.displayName} in Split-Screen!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCopyDossier = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Theoretical Dossier",
                                "${itemDossier.title}\n\nHypothesis:\n${itemDossier.hypothesis}\n\nMechanism:\n${itemDossier.formalMechanism}\n\nBlueprint:\n${itemDossier.inventionBlueprint}\n\nHook:\n${itemDossier.narrativeHook}"
                            )
                            clipboard.setPrimaryClip(clip)
                            toneGenerator.playChime(SoundEffectType.TAP)
                            Toast.makeText(context, "Copied dossier to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            }
        }

        // =====================================================================
        // TAB 4: ENERGY RESONANCE (42.600 HZ) THEORETICAL NODE MANIPULATION
        // =====================================================================
        if (activeLabTab == "RESONANCE") {
            // Resonance Field Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("energy_resonance_header_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
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
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EchoCyan.copy(alpha = 0.2f))
                                        .border(1.dp, EchoCyan, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = "ENERGY RESONANCE DYNAMICS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoText,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "42.600 Hz Fundamental Standing Wave Matrix",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EchoCyan
                                    )
                                }
                            }

                            // Harmonic Lock Status Badge
                            val selectedNode = resonanceNodes.find { it.id == selectedResonanceNodeId }
                            val isLock = selectedNode != null && abs(selectedNode.frequencyHz - 42.600f) <= 0.25f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLock) EchoAmber.copy(alpha = 0.2f) else EchoDarkSurface)
                                    .border(1.dp, if (isLock) EchoAmber else EchoCardStroke, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isLock) "HARMONIC LOCK [100%]" else "DISPERSED PHASE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLock) EchoAmber else EchoMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "Touch and drag any topic node to adjust its theoretical resonance against the 42.600 Hz planetary bedrock standing wave. Observe real-time Q-factor coupling and spectral tension line modulation without audio playback.",
                            fontSize = 11.sp,
                            color = EchoMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Interactive 60fps Energy Resonance Canvas
            item {
                EnergyResonanceCanvas(
                    nodes = resonanceNodes,
                    selectedNodeId = selectedResonanceNodeId,
                    onSelectNode = { node ->
                        selectedResonanceNodeId = node.id
                    },
                    onNodeFrequencyChanged = { nodeId, newFreq ->
                        val target = resonanceNodes.find { it.id == nodeId }
                        if (target != null) {
                            target.frequencyHz = newFreq
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Selected Node Inspector & Micro-Tuning Drawer
            val activeNode = resonanceNodes.find { it.id == selectedResonanceNodeId } ?: resonanceNodes.firstOrNull()
            if (activeNode != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resonance_node_inspector_card"),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        border = BorderStroke(1.dp, activeNode.speaker.primaryColor.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
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
                                    Icon(activeNode.speaker.icon, contentDescription = null, tint = activeNode.speaker.primaryColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = activeNode.speaker.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeNode.speaker.primaryColor
                                    )
                                    Text(
                                        text = "• ${activeNode.field}",
                                        fontSize = 10.sp,
                                        color = EchoMuted
                                    )
                                }

                                Text(
                                    text = String.format("%.3f Hz", activeNode.frequencyHz),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (abs(activeNode.frequencyHz - 42.600f) <= 0.25f) EchoAmber else EchoCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = activeNode.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText
                            )

                            Text(
                                text = activeNode.hypothesisSnippet,
                                fontSize = 11.sp,
                                color = EchoMuted,
                                lineHeight = 16.sp
                            )

                            // Telemetry Stats: Q-Factor Coupling & Harmonic Ratio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EchoDarkSurface)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text("COUPLING EFFICIENCY", fontSize = 8.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = String.format("%.1f%%", activeNode.couplingCoherence * 100f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoGreen,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EchoDarkSurface)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text("FREQUENCY DETUNING", fontSize = 8.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                                        val delta = activeNode.frequencyHz - 42.600f
                                        Text(
                                            text = (if (delta >= 0) "+" else "") + String.format("%.2f Hz", delta),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (abs(delta) <= 0.25f) EchoAmber else EchoCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Fine Frequency Tuning Slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Micro-Hertz Frequency Tuning:", fontSize = 10.sp, color = EchoMuted)
                                    Text(
                                        text = "${activeNode.frequencyHz.toInt()} Hz",
                                        fontSize = 10.sp,
                                        color = EchoCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Slider(
                                    value = activeNode.frequencyHz,
                                    onValueChange = { newVal ->
                                        activeNode.frequencyHz = newVal
                                    },
                                    valueRange = 30.0f..90.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = EchoCyan,
                                        activeTrackColor = EchoCyan,
                                        inactiveTrackColor = EchoDarkSurface
                                    ),
                                    modifier = Modifier.testTag("slider_frequency_tuning")
                                )
                            }

                            // Quick Frequency Presets (Fundamental 42.6 Hz, 2nd Harmonic 85.2 Hz, Tesla 43.0 Hz)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Lock 42.6 Hz" to 42.600f,
                                    "2nd Harm. (85.2)" to 85.200f,
                                    "Tesla (43.0)" to 43.000f
                                ).forEach { (label, freq) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoDarkSurface)
                                            .border(1.dp, EchoCardStroke, RoundedCornerShape(6.dp))
                                            .clickable {
                                                activeNode.frequencyHz = freq
                                                activeNode.relX = 0.50f
                                                activeNode.relY = 0.50f
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                    }
                                }
                            }

                            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                            // Action: Seed Round-Table Debate with this resonance node
                            Button(
                                onClick = {
                                    debateTopicPrompt = "${activeNode.title}: Theoretical Resonance Locked at ${String.format("%.3f", activeNode.frequencyHz)} Hz"
                                    selectedScientificField = activeNode.field
                                    activeLabTab = "ROUND_TABLE"
                                    toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
                                    Toast.makeText(context, "Routed \"${activeNode.title}\" to Council Debate!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("btn_seed_debate_from_resonance"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Route Topic to Council Debate Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        }
    }
    }

    // Counterpart Dossier Selector Modal Bottom Sheet
    if (isCompareSelectorDrawerOpen) {
        DossierCounterpartPickerSheet(
            primaryDossier = if (compareSelectorTarget == "B") compareDossierA else compareDossierB,
            targetSlot = compareSelectorTarget,
            dossierArchive = dossierArchive,
            onSelectCounterpart = { selectedDossier ->
                if (compareSelectorTarget == "B") {
                    compareDossierB = selectedDossier
                    if (compareDossierA == null) {
                        compareDossierA = dossierArchive.firstOrNull { it.id != selectedDossier.id }
                    }
                } else {
                    compareDossierA = selectedDossier
                    if (compareDossierB == null) {
                        compareDossierB = dossierArchive.firstOrNull { it.id != selectedDossier.id }
                    }
                }
                isComparingDossiers = true
                isCompareSelectorDrawerOpen = false
                toneGenerator.playChime(SoundEffectType.CHIME)
                Toast.makeText(context, "Split-Screen comparison active: ${compareDossierA?.agent?.displayName} vs ${compareDossierB?.agent?.displayName}", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { isCompareSelectorDrawerOpen = false }
        )
    }
}

/**
 * Visual Round-Table Podium Component:
 * Displays the 4 polymath avatars around an interactive holographic round table
 * with real-time speaker spotlight and pulsing core.
 */
@Composable
private fun RoundTablePodiumCard(
    councilAgents: List<CouncilAvatarProfile>,
    activeSpeakerId: String?,
    isCooking: Boolean,
    onAgentClick: (CouncilAvatarProfile) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "round_table")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("round_table_podium_card"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = BorderStroke(1.dp, if (isCooking) EchoCyan else EchoCardStroke),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stage Header
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoCyan.copy(alpha = 0.2f))
                            .border(1.dp, EchoCyan, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diversity3,
                            contentDescription = null,
                            tint = EchoCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ROUND-TABLE POLYMATH PODIUM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Athena • Gemini • Dola • The Council of 42",
                            fontSize = 10.sp,
                            color = EchoCyan
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCooking) EchoGreen.copy(alpha = 0.2f) else EchoDarkSurface)
                        .border(1.dp, if (isCooking) EchoGreen else EchoCardStroke, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCooking) "LIVE DEBATE" else "4 MINDS SEATED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCooking) EchoGreen else EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Visual Round-Table Seating Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EchoDarkSurface)
                    .border(1.dp, EchoCardStroke.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Hologram Lines & Central Core
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val tableRadius = size.height * 0.38f

                    // Holographic Table Ring
                    drawCircle(
                        color = EchoCyan.copy(alpha = 0.15f),
                        radius = tableRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Inner Concentric Pulse Ring
                    drawCircle(
                        color = (if (isCooking) EchoGreen else EchoCyan).copy(alpha = 0.25f),
                        radius = tableRadius * 0.6f * pulseScale,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Crosshair grid lines
                    drawLine(
                        color = EchoCardStroke.copy(alpha = 0.35f),
                        start = Offset(center.x - tableRadius, center.y),
                        end = Offset(center.x + tableRadius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = EchoCardStroke.copy(alpha = 0.35f),
                        start = Offset(center.x, center.y - tableRadius),
                        end = Offset(center.x, center.y + tableRadius),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Center Synthesis Hub
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(EchoBgAlt.copy(alpha = 0.9f))
                        .border(
                            1.dp,
                            if (isCooking) EchoGreen else EchoCyan.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isCooking) EchoGreen else EchoCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isCooking) "THINKING" else "SYNTH HUB",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCooking) EchoGreen else EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // 5 Round-Table Avatar Stations
                // 1. ATHENA (Top-Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 12.dp)
                ) {
                    RoundTableAvatarItem(
                        agent = councilAgents[0],
                        isActive = activeSpeakerId == councilAgents[0].id,
                        onClick = { onAgentClick(councilAgents[0]) }
                    )
                }

                // 2. GEMINI (Top-Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 12.dp)
                ) {
                    RoundTableAvatarItem(
                        agent = councilAgents[1],
                        isActive = activeSpeakerId == councilAgents[1].id,
                        onClick = { onAgentClick(councilAgents[1]) }
                    )
                }

                // 3. CHIEF ENGINEER OF ATHENA (Top-Center console)
                if (councilAgents.size > 4) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        RoundTableAvatarItem(
                            agent = councilAgents[3],
                            isActive = activeSpeakerId == councilAgents[3].id,
                            onClick = { onAgentClick(councilAgents[3]) }
                        )
                    }
                }

                // 4. DOLA (Bottom-Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 12.dp)
                ) {
                    RoundTableAvatarItem(
                        agent = councilAgents[2],
                        isActive = activeSpeakerId == councilAgents[2].id,
                        onClick = { onAgentClick(councilAgents[2]) }
                    )
                }

                // 5. THE COUNCIL (Bottom-Right)
                val directorateAgent = councilAgents.last()
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                ) {
                    RoundTableAvatarItem(
                        agent = directorateAgent,
                        isActive = activeSpeakerId == directorateAgent.id,
                        onClick = { onAgentClick(directorateAgent) }
                    )
                }
            }

            // Quick Agent Role Legend Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(councilAgents) { agent ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(agent.primaryColor.copy(alpha = 0.12f))
                            .border(1.dp, agent.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { onAgentClick(agent) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(agent.icon, contentDescription = null, tint = agent.primaryColor, modifier = Modifier.size(13.dp))
                        Text(agent.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = agent.primaryColor)
                        Text("IQ ${agent.iqScore}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoGreen, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
}

/**
 * Avatar Seat at the Round Table rendering HolographicAvatarBadge from CouncilAvatarManager.
 */
@Composable
private fun RoundTableAvatarItem(
    agent: CouncilAvatarProfile,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        CouncilAvatarManager.HolographicAvatarBadge(
            profile = agent,
            isActive = isActive,
            size = 44.dp,
            showHaloRing = true
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EchoGreen)
                )
            }
            Text(
                text = agent.displayName,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isActive) agent.primaryColor else EchoText
            )
        }
    }
}

/**
 * Visual card for an archived Theoretical Dossier.
 * Features: Author holographic avatar, domain tag, expandable Hypothesis, Formal Mechanism,
 * Invention Blueprint, Narrative Hook, and 4 quick actions:
 * 1) Re-load into Hot-Seat
 * 2) Inject into live Council Debate
 * 3) Save / Sync to Workspace Notes
 * 4) Select for Side-by-Side Comparison
 */
@Composable
private fun DossierArchiveCard(
    dossier: TheoreticalDossier,
    isSelectedForCompare: Boolean,
    onReloadHotSeat: () -> Unit,
    onInjectDebate: () -> Unit,
    onSaveToWorkspace: () -> Unit,
    onSelectForCompare: () -> Unit,
    onCopyDossier: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dossier_card_${dossier.id}"),
        colors = CardDefaults.cardColors(containerColor = EchoCard),
        border = BorderStroke(
            width = if (isSelectedForCompare) 1.5.dp else 1.dp,
            color = if (isSelectedForCompare) EchoCyan else dossier.agent.primaryColor.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Author Avatar & Domain Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CouncilAvatarManager.HolographicAvatarBadge(
                        profile = dossier.agent,
                        isActive = false,
                        size = 38.dp,
                        showHaloRing = false
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = dossier.agent.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = dossier.agent.primaryColor
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EchoGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "IQ ${dossier.agent.iqScore}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = dossier.scientificField,
                            fontSize = 9.sp,
                            color = EchoCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isSelectedForCompare) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoCyan.copy(alpha = 0.2f))
                            .border(1.dp, EchoCyan, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IN COMPARE MATRIX",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Title
            Text(
                text = dossier.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EchoText,
                lineHeight = 18.sp
            )

            // Hypothesis Snippet / Full
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoDarkSurface)
                    .border(1.dp, EchoCardStroke.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "CORE HYPOTHESIS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isExpanded) dossier.hypothesis else dossier.hypothesis.take(160) + if (dossier.hypothesis.length > 160) "..." else "",
                    fontSize = 11.sp,
                    color = EchoText,
                    lineHeight = 15.sp
                )
            }

            // Expanded Breakdown: Formal Mechanism, Blueprint, Hook
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Formal Mechanism
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoDarkSurface)
                            .border(1.dp, EchoCardStroke.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FORMAL PHYSICAL MECHANISM & EQUATIONS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dossier.formalMechanism,
                            fontSize = 11.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }

                    // Speculative Invention Blueprint
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoDarkSurface)
                            .border(1.dp, EchoGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SPECULATIVE INVENTION BLUEPRINT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dossier.inventionBlueprint,
                            fontSize = 10.sp,
                            color = EchoText,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }

                    // Narrative Hook
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoDarkSurface)
                            .border(1.dp, EchoCardStroke.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "NARRATIVE & REAL-WORLD IMPLICATION HOOK",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dossier.narrativeHook,
                            fontSize = 11.sp,
                            color = EchoText,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Expand / Collapse Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "COLLAPSE DOSSIER SPECIFICATIONS ▲" else "EXPAND MECHANISM & BLUEPRINT ▼",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    fontSize = 9.sp,
                    color = EchoCyan
                )
            }

            HorizontalDivider(color = EchoCardStroke.copy(alpha = 0.4f), thickness = 0.8.dp)

            // 4 Primary Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Re-load into Hot-Seat
                Button(
                    onClick = onReloadHotSeat,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("btn_archive_reload_${dossier.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoCyan.copy(alpha = 0.15f),
                        contentColor = EchoCyan
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Hot-Seat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // 2. Inject into Debate
                Button(
                    onClick = onInjectDebate,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("btn_archive_debate_${dossier.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoGreen.copy(alpha = 0.15f),
                        contentColor = EchoGreen
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Debate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // 3. Save to Workspace
                Button(
                    onClick = onSaveToWorkspace,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("btn_archive_save_${dossier.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoDarkSurface,
                        contentColor = EchoText
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Save", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // 4. Compare Side-by-Side
                Button(
                    onClick = onSelectForCompare,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("btn_archive_compare_${dossier.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelectedForCompare) EchoCyan else EchoDarkSurface,
                        contentColor = if (isSelectedForCompare) EchoOnCyan else EchoCyan
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(if (isSelectedForCompare) "Added" else "Compare", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Copy Action
                OutlinedButton(
                    onClick = onCopyDossier,
                    modifier = Modifier
                        .height(34.dp)
                        .width(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoMuted)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

/**
 * Synchronized section tabs for the side-by-side split-screen comparison mode.
 */
enum class ComparisonSectionTab(val id: String, val title: String, val shortTitle: String) {
    HYPOTHESIS("HYPOTHESIS", "Hypotheses & Badges", "Hypothesis"),
    MECHANISM("MECHANISM", "Formal Mechanisms", "Mechanism"),
    HARDWARE_MATRIX("HARDWARE_MATRIX", "Hardware Matrix", "Specs Matrix"),
    HYBRID_SYNTHESIS("HYBRID_SYNTHESIS", "Hybrid Synthesis", "Synthesis"),
    ALL_SPLIT("ALL_SPLIT", "Full Blueprint Split", "Full Split")
}

data class HardwareSpecRow(
    val category: String,
    val specA: String,
    val specB: String,
    val deltaNote: String
)

data class ComparisonAnalysisResult(
    val convergenceBadges: List<String>,
    val divergenceBadges: List<String>,
    val synergyIndex: Int,
    val hardwareMatrix: List<HardwareSpecRow>,
    val hybridProposalTitle: String,
    val hybridProposalHypothesis: String,
    val hybridProposalMechanism: String,
    val hybridProposalBlueprint: String,
    val hybridProposalHook: String
)

/**
 * Derives dynamic polymath comparison analysis between any two theoretical dossiers.
 */
private fun generateComparisonAnalysis(
    a: TheoreticalDossier,
    b: TheoreticalDossier
): ComparisonAnalysisResult {
    val convergence = listOf(
        "✓ Non-Hermitian Phase Coherence & Entanglement Boundary",
        "✓ Sub-Planckian Information Density & Scaling Symmetry",
        "✓ Unitary State Conservation along Metamaterial Cavity Limits",
        "✓ Squeezed Vacuum Parameter Synchronization"
    )

    val fieldA = a.scientificField.split("&").firstOrNull()?.trim() ?: a.scientificField
    val fieldB = b.scientificField.split("&").firstOrNull()?.trim() ?: b.scientificField

    val divergence = listOf(
        "⚡ Paradigm Contrast: $fieldA vs $fieldB",
        "⚡ Boundary Dynamics: Discrete Lattices vs Relativistic Solitons",
        "⚡ Thermal Shielding: Ambient Biological Protection vs Cryogenic Vacuum Cavity"
    )

    val hardwareMatrix = listOf(
        HardwareSpecRow(
            category = "Substrate & Medium",
            specA = a.inventionBlueprint.lines().firstOrNull { it.contains("Substrate", true) || it.contains("Architecture", true) || it.contains("Core", true) }?.substringAfter(":")?.trim()
                ?: "Synthetic Micro-Cavity Lattice",
            specB = b.inventionBlueprint.lines().firstOrNull { it.contains("Substrate", true) || it.contains("Architecture", true) || it.contains("Core", true) }?.substringAfter(":")?.trim()
                ?: "Relativistic Metamaterial Waveguide",
            deltaNote = "Dual-layer heterogeneous semiconductor stacking"
        ),
        HardwareSpecRow(
            category = "Operating Frequency",
            specA = a.inventionBlueprint.lines().firstOrNull { it.contains("Frequency", true) || it.contains("Clock", true) || it.contains("Interconnect", true) }?.substringAfter(":")?.trim()
                ?: "24.6 GHz High-Q Resonance",
            specB = b.inventionBlueprint.lines().firstOrNull { it.contains("Frequency", true) || it.contains("Clock", true) || it.contains("Interconnect", true) }?.substringAfter(":")?.trim()
                ?: "400 THz Sub-Femtosecond Light Modulation",
            deltaNote = "Parametric frequency mode-locking across spectrum"
        ),
        HardwareSpecRow(
            category = "Thermal Envelope",
            specA = a.inventionBlueprint.lines().firstOrNull { it.contains("Thermal", true) || it.contains("Cooling", true) }?.substringAfter(":")?.trim()
                ?: "290K – 340K Ambient Temperature",
            specB = b.inventionBlueprint.lines().firstOrNull { it.contains("Thermal", true) || it.contains("Cooling", true) }?.substringAfter(":")?.trim()
                ?: "Cryogenic Liquid Nitrogen 77K Shielded",
            deltaNote = "Acoustic phonon decoupled thermal barrier"
        ),
        HardwareSpecRow(
            category = "Power & Efficiency",
            specA = a.inventionBlueprint.lines().firstOrNull { it.contains("Power", true) || it.contains("Output", true) }?.substringAfter(":")?.trim()
                ?: "1.84 kW/cm³ Passive Zero-Point Flux",
            specB = b.inventionBlueprint.lines().firstOrNull { it.contains("Power", true) || it.contains("Consumption", true) }?.substringAfter(":")?.trim()
                ?: "0.02 pJ/MAC Ultra-Low Energy Scaling",
            deltaNote = "Net positive self-powering feedback loop"
        ),
        HardwareSpecRow(
            category = "Critical Bottleneck",
            specA = "Thermal dipole decoherence vs boundary vibration drift",
            specB = "Negative metric stress stability vs waveguide phase jitter",
            deltaNote = "Cross-stabilized via dual polymath feedback loops"
        ),
        HardwareSpecRow(
            category = "Readiness Level (TRL)",
            specA = "TRL-3 (Analytical Laboratory Proof)",
            specB = "TRL-2 (Theoretical Soliton Model)",
            deltaNote = "Elevated to TRL-4 via Council Cross-Coupling"
        )
    )

    val cleanTitleA = a.title.split(" ").take(3).joinToString(" ")
    val cleanTitleB = b.title.split(" ").take(3).joinToString(" ")

    val hybridTitle = "Unified Super-Resonator: $cleanTitleA × $cleanTitleB"
    val hybridHypothesis = "By interleaving the ${a.scientificField} framework of ${a.agent.displayName} with the ${b.scientificField} topology of ${b.agent.displayName}, macroscopic phase coherence stabilizes parametric vacuum excitation, unlocking simultaneous continuous zero-point energy harvesting and sub-femtosecond neuromorphic cognitive routing."
    val hybridMechanism = "The coupled Hamiltonian H_hybrid = H_${a.agent.displayName} ⊗ I + I ⊗ H_${b.agent.displayName} + g_int (a^† σ_- + a σ_+) drives coherent parametric energy transfer across relativistic cavity boundaries directly into the neuromorphic optical waveguide, suppressing environmental entropy without cryogenic refrigeration."
    val hybridBlueprint = "COMPOSITE BLUEPRINT: OMNI-RESONATOR DUAL CORE\n• Heterogeneous Substrate: Interleaved ${a.agent.displayName} resonator with ${b.agent.displayName} optical waveguide.\n• Operating Frequency: Broadband mode-locking from 24.6 GHz to 400 Terahertz.\n• Power Feedback: 100% self-sustaining continuous Casimir energy tap.\n• Co-Authors: Synthesized jointly by ${a.agent.displayName} (IQ ${a.agent.iqScore}) and ${b.agent.displayName} (IQ ${b.agent.iqScore})."
    val hybridHook = "When the dual-core apparatus activates, ambient entropy drops by 14.2% while producing continuous clean power, proving that vacuum physics and non-computable intelligence share a single underlying spacetime continuum."

    return ComparisonAnalysisResult(
        convergenceBadges = convergence,
        divergenceBadges = divergence,
        synergyIndex = 97,
        hardwareMatrix = hardwareMatrix,
        hybridProposalTitle = hybridTitle,
        hybridProposalHypothesis = hybridHypothesis,
        hybridProposalMechanism = hybridMechanism,
        hybridProposalBlueprint = hybridBlueprint,
        hybridProposalHook = hybridHook
    )
}

/**
 * Split-Screen Comparison View:
 * Features side-by-side synchronized columns, interactive section tabs,
 * highlight-comparison with convergence/divergence badges, mechanical hardware contrast matrix,
 * hybrid theoretical synthesis proposal chamber, and direct export actions.
 */
@Composable
private fun DossierSplitComparisonView(
    dossierArchive: List<TheoreticalDossier>,
    dossierA: TheoreticalDossier,
    dossierB: TheoreticalDossier,
    selectedTab: ComparisonSectionTab,
    onTabSelected: (ComparisonSectionTab) -> Unit,
    onSwapDossiers: () -> Unit,
    onChangeDossierA: () -> Unit,
    onChangeDossierB: () -> Unit,
    onDebateComparison: (TheoreticalDossier, TheoreticalDossier) -> Unit,
    onExportComparison: (TheoreticalDossier, TheoreticalDossier, String) -> Unit,
    onSynthesizeAndInject: (String, String) -> Unit,
    onSaveHybridToArchive: (TheoreticalDossier) -> Unit,
    onExportHybridToWorkspace: (TheoreticalDossier) -> Unit,
    onReloadHybridToHotSeat: (TheoreticalDossier) -> Unit = {},
    onClose: () -> Unit
) {
    val analysis = remember(dossierA.id, dossierB.id) {
        generateComparisonAnalysis(dossierA, dossierB)
    }

    val markdownReport = remember(dossierA.id, dossierB.id, analysis) {
        buildString {
            appendLine("# Comparative Analysis: Polymath Theoretical Dossiers")
            appendLine()
            appendLine("Generated by the 180+ IQ Omni-Disciplinary Council of 42 Split-Screen Lab.")
            appendLine("**Cross-Disciplinary Synergy Index**: ${analysis.synergyIndex}%")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Primary Dossier A: ${dossierA.title}")
            appendLine("- **Author**: ${dossierA.agent.displayName} (IQ ${dossierA.agent.iqScore}, ${dossierA.agent.roleTitle})")
            appendLine("- **Scientific Field**: ${dossierA.scientificField}")
            appendLine()
            appendLine("### Hypothesis")
            appendLine(dossierA.hypothesis)
            appendLine()
            appendLine("### Formal Mechanism")
            appendLine(dossierA.formalMechanism)
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Counterpart Dossier B: ${dossierB.title}")
            appendLine("- **Author**: ${dossierB.agent.displayName} (IQ ${dossierB.agent.iqScore}, ${dossierB.agent.roleTitle})")
            appendLine("- **Scientific Field**: ${dossierB.scientificField}")
            appendLine()
            appendLine("### Hypothesis")
            appendLine(dossierB.hypothesis)
            appendLine()
            appendLine("### Formal Mechanism")
            appendLine(dossierB.formalMechanism)
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Theoretical Convergence Highlights")
            analysis.convergenceBadges.forEach { appendLine("- $it") }
            appendLine()
            appendLine("## Theoretical Divergence Points")
            analysis.divergenceBadges.forEach { appendLine("- $it") }
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Mechanical Hardware Contrast Matrix")
            appendLine("| Parameter | ${dossierA.agent.displayName} (A) | ${dossierB.agent.displayName} (B) | Delta Synthesis |")
            appendLine("|---|---|---|---|")
            analysis.hardwareMatrix.forEach { row ->
                appendLine("| **${row.category}** | ${row.specA} | ${row.specB} | ${row.deltaNote} |")
            }
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Synthesized Hybrid Breakthrough Invention")
            appendLine("### ${analysis.hybridProposalTitle}")
            appendLine()
            appendLine("**Unified Hypothesis**:")
            appendLine(analysis.hybridProposalHypothesis)
            appendLine()
            appendLine("**Coupled Cross-Mechanism**:")
            appendLine(analysis.hybridProposalMechanism)
            appendLine()
            appendLine("**Composite Blueprint**:")
            appendLine(analysis.hybridProposalBlueprint)
            appendLine()
            appendLine("**Narrative Implication Hook**:")
            appendLine(analysis.hybridProposalHook)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dossier_split_comparison_view"),
        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
        border = BorderStroke(1.5.dp, EchoCyan),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Close
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = "SPLIT-SCREEN COMPARATIVE LAB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Synchronized dual-dossier differential breakdown & hybrid synthesis",
                            fontSize = 9.sp,
                            color = EchoMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSwapDossiers,
                        modifier = Modifier.size(30.dp).testTag("btn_swap_comparison_dossiers")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Dossiers", tint = EchoCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(30.dp).testTag("btn_close_split_view")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Split View", tint = EchoMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Dual Dossier Banners (Dossier A vs Dossier B)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column A Header
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                    border = BorderStroke(1.dp, dossierA.agent.primaryColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                CouncilAvatarManager.HolographicAvatarBadge(profile = dossierA.agent, isActive = false, size = 22.dp)
                                Text(dossierA.agent.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dossierA.agent.primaryColor)
                            }
                            TextButton(
                                onClick = onChangeDossierA,
                                modifier = Modifier.height(22.dp).testTag("btn_change_dossier_a"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Change", fontSize = 8.sp, color = EchoCyan)
                            }
                        }
                        Text(dossierA.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                        Text(dossierA.scientificField, fontSize = 8.sp, color = EchoCyan, maxLines = 1)
                    }
                }

                // VS Indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(EchoCyan.copy(alpha = 0.15f))
                        .border(1.dp, EchoCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                }

                // Column B Header
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                    border = BorderStroke(1.dp, dossierB.agent.primaryColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                CouncilAvatarManager.HolographicAvatarBadge(profile = dossierB.agent, isActive = false, size = 22.dp)
                                Text(dossierB.agent.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dossierB.agent.primaryColor)
                            }
                            TextButton(
                                onClick = onChangeDossierB,
                                modifier = Modifier.height(22.dp).testTag("btn_change_dossier_b"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Change", fontSize = 8.sp, color = EchoGreen)
                            }
                        }
                        Text(dossierB.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                        Text(dossierB.scientificField, fontSize = 8.sp, color = EchoGreen, maxLines = 1)
                    }
                }
            }

            // Synchronized Section Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ComparisonSectionTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.testTag("tab_comparison_${tab.id.lowercase()}"),
                        label = {
                            Text(
                                text = tab.shortTitle,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoCyan,
                            selectedLabelColor = EchoOnCyan,
                            containerColor = EchoDarkSurface,
                            labelColor = EchoText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EchoCyan else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            HorizontalDivider(color = EchoCardStroke, thickness = 0.8.dp)

            // Content Area based on selected synchronized tab
            when (selectedTab) {
                ComparisonSectionTab.HYPOTHESIS -> {
                    // Highlights: Convergence & Divergence Badges
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                            border = BorderStroke(0.8.dp, EchoCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "THEORETICAL CONVERGENCE & DIVERGENCE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoCyan,
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EchoGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Synergy: ${analysis.synergyIndex}%",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoGreen
                                        )
                                    }
                                }

                                // Convergence Badges
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    analysis.convergenceBadges.forEach { badge ->
                                        Text(
                                            text = badge,
                                            fontSize = 9.sp,
                                            color = EchoGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Divergence Badges
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    analysis.divergenceBadges.forEach { badge ->
                                        Text(
                                            text = badge,
                                            fontSize = 9.sp,
                                            color = Color(0xFFFFA726),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Side-by-Side Split Hypotheses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column A Hypothesis
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .border(1.dp, dossierA.agent.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("HYPOTHESIS A (${dossierA.agent.displayName})", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dossierA.agent.primaryColor, fontFamily = FontFamily.Monospace)
                                Text(dossierA.hypothesis, fontSize = 10.sp, color = EchoText, lineHeight = 14.sp)
                            }

                            // Column B Hypothesis
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .border(1.dp, dossierB.agent.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("HYPOTHESIS B (${dossierB.agent.displayName})", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dossierB.agent.primaryColor, fontFamily = FontFamily.Monospace)
                                Text(dossierB.hypothesis, fontSize = 10.sp, color = EchoText, lineHeight = 14.sp)
                            }
                        }
                    }
                }

                ComparisonSectionTab.MECHANISM -> {
                    // Side-by-side formal physical mechanisms with math operator highlighting
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column A Mechanism
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .border(1.dp, dossierA.agent.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("MECHANISM A (${dossierA.agent.displayName})", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dossierA.agent.primaryColor, fontFamily = FontFamily.Monospace)
                                Text(dossierA.formalMechanism, fontSize = 9.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
                            }

                            // Column B Mechanism
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .border(1.dp, dossierB.agent.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("MECHANISM B (${dossierB.agent.displayName})", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dossierB.agent.primaryColor, fontFamily = FontFamily.Monospace)
                                Text(dossierB.formalMechanism, fontSize = 9.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
                            }
                        }

                        // Complementary Coupling Analysis Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EchoCyan.copy(alpha = 0.08f)),
                            border = BorderStroke(0.8.dp, EchoCyan.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Mathematical Cross-Coupling:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                    Text(
                                        "Equation set of ${dossierA.agent.displayName} provides the boundary condition governing Hamiltonian evolution in ${dossierB.agent.displayName}'s manifold.",
                                        fontSize = 9.sp,
                                        color = EchoText,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                ComparisonSectionTab.HARDWARE_MATRIX -> {
                    // Mechanical Hardware Contrast Matrix Table
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "MECHANICAL HARDWARE CONTRAST MATRIX",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )

                        analysis.hardwareMatrix.forEach { row ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                                border = BorderStroke(0.6.dp, EchoCardStroke),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = row.category.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${dossierA.agent.displayName}:", fontSize = 8.sp, color = dossierA.agent.primaryColor, fontWeight = FontWeight.Bold)
                                            Text(row.specA, fontSize = 9.sp, color = EchoText, lineHeight = 12.sp)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${dossierB.agent.displayName}:", fontSize = 8.sp, color = dossierB.agent.primaryColor, fontWeight = FontWeight.Bold)
                                            Text(row.specB, fontSize = 9.sp, color = EchoText, lineHeight = 12.sp)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(10.dp))
                                        Text(row.deltaNote, fontSize = 8.sp, color = EchoGreen, fontStyle = FontStyle.Italic)
                                    }
                                }
                            }
                        }
                    }
                }

                ComparisonSectionTab.HYBRID_SYNTHESIS -> {
                    // Synthesize Hybrid Theoretical Proposal Chamber powered by Agent Hot-Seat
                    HybridProposalSynthesisChamber(
                        dossierA = dossierA,
                        dossierB = dossierB,
                        analysis = analysis,
                        onSynthesizeAndInject = onSynthesizeAndInject,
                        onSaveHybridToArchive = onSaveHybridToArchive,
                        onExportHybridToWorkspace = onExportHybridToWorkspace,
                        onReloadHybridToHotSeat = onReloadHybridToHotSeat
                    )
                }

                ComparisonSectionTab.ALL_SPLIT -> {
                    // Full side-by-side synchronized column comparison
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Full Column A
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, dossierA.agent.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("DOSSIER A (${dossierA.agent.displayName})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dossierA.agent.primaryColor)
                            Text(dossierA.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            Text(dossierA.scientificField, fontSize = 8.sp, color = EchoCyan)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("HYPOTHESIS", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text(dossierA.hypothesis, fontSize = 9.sp, color = EchoText, lineHeight = 12.sp)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("MECHANISM", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
                            Text(dossierA.formalMechanism, fontSize = 8.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 11.sp)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("BLUEPRINT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EchoGreen, fontFamily = FontFamily.Monospace)
                            Text(dossierA.inventionBlueprint, fontSize = 8.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 11.sp)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("HOOK", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA726), fontFamily = FontFamily.Monospace)
                            Text(dossierA.narrativeHook, fontSize = 8.sp, color = EchoText, fontStyle = FontStyle.Italic, lineHeight = 11.sp)
                        }

                        // Full Column B
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, dossierB.agent.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("DOSSIER B (${dossierB.agent.displayName})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dossierB.agent.primaryColor)
                            Text(dossierB.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            Text(dossierB.scientificField, fontSize = 8.sp, color = EchoGreen)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("HYPOTHESIS", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EchoMuted, fontFamily = FontFamily.Monospace)
                            Text(dossierB.hypothesis, fontSize = 9.sp, color = EchoText, lineHeight = 12.sp)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("MECHANISM", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
                            Text(dossierB.formalMechanism, fontSize = 8.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 11.sp)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("BLUEPRINT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EchoGreen, fontFamily = FontFamily.Monospace)
                            Text(dossierB.inventionBlueprint, fontSize = 8.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 11.sp)

                            HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                            Text("HOOK", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA726), fontFamily = FontFamily.Monospace)
                            Text(dossierB.narrativeHook, fontSize = 8.sp, color = EchoText, fontStyle = FontStyle.Italic, lineHeight = 11.sp)
                        }
                    }
                }
            }

            HorizontalDivider(color = EchoCardStroke, thickness = 0.8.dp)

            // Primary Comparative Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDebateComparison(dossierA, dossierB) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_debate_comparison"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Debate A vs B in Council", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onExportComparison(dossierA, dossierB, markdownReport) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_export_comparison_to_workspace"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export Matrix to Notes", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class SynthesisStageData(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val speaker: CouncilAvatarProfile,
    val quote: String,
    val log: String
)

/**
 * Interactive Multi-Stage Hybrid Proposal Synthesis Chamber:
 * Powered by the Agent Hot-Seat to merge two polymath theoretical dossiers
 * into a single unified breakthrough model.
 *
 * Features:
 * - Interactive multi-stage simulation with live progress tracking (Stages 1-5).
 * - Fully automated polymath parameters determined by the two polymath avatars.
 * - Dynamic streaming telemetry console with terminal logs and speaker quotes.
 * - Dual-avatar dialectic confrontation preview with glowing speaker indicators.
 * - Breakthrough synthesized dossier card with unified hypothesis, coupled mechanism,
 *   composite invention blueprint, and narrative hook.
 * - Interactive Hot-Seat Interrogation testing chamber for challenge questions.
 * - Instant routing: Save to Archive, Inject to Council debate, Export to Notes,
 *   and Re-load directly into Agent Hot-Seat.
 */
@Composable
private fun HybridProposalSynthesisChamber(
    dossierA: TheoreticalDossier,
    dossierB: TheoreticalDossier,
    analysis: ComparisonAnalysisResult,
    onSynthesizeAndInject: (String, String) -> Unit,
    onSaveHybridToArchive: (TheoreticalDossier) -> Unit,
    onExportHybridToWorkspace: (TheoreticalDossier) -> Unit,
    onReloadHybridToHotSeat: (TheoreticalDossier) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toneGenerator = remember { HarmonicToneGenerator.SHARED }

    var isSynthesizing by remember { mutableStateOf(false) }
    var synthesisProgress by remember { mutableStateOf(0f) }
    var currentStageNumber by remember { mutableStateOf(1) }
    var isSynthesisComplete by remember { mutableStateOf(false) }
    val telemetryLogs = remember { mutableStateListOf<String>() }
    var activeSpeaker by remember { mutableStateOf<CouncilAvatarProfile?>(null) }
    var activeSpeakerQuote by remember { mutableStateOf("") }
    var autoSaved by remember { mutableStateOf(false) }

    var interrogationInput by remember { mutableStateOf("") }
    val interrogationHistory = remember { mutableStateListOf<Pair<String, String>>() }
    var isInterrogating by remember { mutableStateOf(false) }

    val hybridDossier = remember(analysis, dossierA.id, dossierB.id) {
        TheoreticalDossier(
            agent = CouncilAvatarManager.THE_COUNCIL,
            title = analysis.hybridProposalTitle,
            scientificField = "${dossierA.scientificField} × ${dossierB.scientificField}",
            hypothesis = analysis.hybridProposalHypothesis,
            formalMechanism = analysis.hybridProposalMechanism,
            inventionBlueprint = analysis.hybridProposalBlueprint,
            narrativeHook = analysis.hybridProposalHook
        )
    }

    val stages = remember(dossierA, dossierB) {
        listOf(
            SynthesisStageData(
                stageNumber = 1,
                title = "Paradox Analysis & Domain Deconstruction",
                subtitle = "Boundary friction between ${dossierA.scientificField} and ${dossierB.scientificField}",
                speaker = dossierA.agent,
                quote = "Axiom deconstruction: Examining boundary condition divergence between ${dossierA.scientificField} and ${dossierB.scientificField}.",
                log = "[STAGE 1/5] PARADOX DECONSTRUCTION: Interleaving ${dossierA.agent.displayName} axioms with ${dossierB.agent.displayName} spacetime geometry."
            ),
            SynthesisStageData(
                stageNumber = 2,
                title = "Cross-Domain Axiom Reconciliation",
                subtitle = "Mapping non-Hermitian Berry phase to metric stress tensors",
                speaker = dossierB.agent,
                quote = "Constructing coupled tensor field. Non-Hermitian mode-locking eliminates thermal dissipation barrier.",
                log = "[STAGE 2/5] COUPLING HAMILTONIAN: H_hybrid = H_A ⊗ I + I ⊗ H_B + g_int(a^† σ_- + a σ_+). Non-Hermitian mode locks established."
            ),
            SynthesisStageData(
                stageNumber = 3,
                title = "Agent Hot-Seat Iterative Interrogation",
                subtitle = "Dual-avatar dialectic cross-fire debate under 185+ IQ dial-in",
                speaker = CouncilAvatarManager.THE_COUNCIL,
                quote = "Agent Hot-Seat active: Stress-testing thermal decoherence vs metric fluctuation. Co-authors reaching 97.4% theoretical parity.",
                log = "[STAGE 3/5] AGENT HOT-SEAT INTERROGATION: 185+ IQ dial-in confirmed. Dynamic vibrational parity cancels thermal noise at 310K."
            ),
            SynthesisStageData(
                stageNumber = 4,
                title = "Hardware & Mathematical Formalization",
                subtitle = "Heterogeneous substrate & broadband mode-locking envelope",
                speaker = dossierA.agent,
                quote = "Engineering matrix compiled: Dual-frequency mode-locking from 24.6 GHz to 400 THz. Technology Readiness Level upgraded to TRL-4.",
                log = "[STAGE 4/5] BLUEPRINT COMPILATION: Diamondoid-graphene lattice coupled to titanium-aluminide waveguide. Zero cryogenics."
            ),
            SynthesisStageData(
                stageNumber = 5,
                title = "Breakthrough Unified Dossier Emergence",
                subtitle = "Compiling unified theoretical model into Dossier Archive",
                speaker = CouncilAvatarManager.THE_COUNCIL,
                quote = "Breakthrough synthesis verified. Dual-polymath unified theoretical model ready for deployment.",
                log = "[STAGE 5/5] EMERGENCE COMPLETE: Composite theoretical dossier generated. Auto-stored in Dossier Archive."
            )
        )
    }

    fun runSynthesis() {
        if (isSynthesizing) return
        isSynthesizing = true
        isSynthesisComplete = false
        synthesisProgress = 0f
        currentStageNumber = 1
        telemetryLogs.clear()
        activeSpeaker = dossierA.agent
        activeSpeakerQuote = stages[0].quote
        telemetryLogs.add("> [COUNCIL ENGINE] INITIALIZING MULTI-STAGE POLYMATH SYNTHESIS...")
        telemetryLogs.add("> [HOT-SEAT CORE] PAIRING: ${dossierA.agent.displayName} (IQ ${dossierA.agent.iqScore}) × ${dossierB.agent.displayName} (IQ ${dossierB.agent.iqScore})")
        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)

        scope.launch {
            for (i in stages.indices) {
                val stage = stages[i]
                currentStageNumber = stage.stageNumber
                activeSpeaker = stage.speaker
                activeSpeakerQuote = stage.quote
                telemetryLogs.add(stage.log)
                toneGenerator.playChime(SoundEffectType.TAP)

                val startProg = (i * 0.20f)
                val endProg = ((i + 1) * 0.20f)
                val steps = 8
                for (step in 1..steps) {
                    delay(40)
                    synthesisProgress = startProg + (endProg - startProg) * (step.toFloat() / steps)
                }
                delay(120)
            }

            telemetryLogs.add("> [SYSTEM] SYNTHESIS SUCCESS: TRL-4 UNIFIED MODEL FORMALIZED!")
            telemetryLogs.add("> [ARCHIVE SYNC] PROPOSAL COMMITTED TO DOSSIER ARCHIVE REPOSITORY.")
            synthesisProgress = 1.0f
            isSynthesizing = false
            isSynthesisComplete = true
            autoSaved = true
            onSaveHybridToArchive(hybridDossier)
            toneGenerator.playChime(SoundEffectType.CHIME)
        }
    }

    fun fastForwardSynthesis() {
        if (!isSynthesizing) return
        synthesisProgress = 1.0f
        currentStageNumber = 5
        isSynthesizing = false
        isSynthesisComplete = true
        autoSaved = true
        activeSpeaker = CouncilAvatarManager.THE_COUNCIL
        activeSpeakerQuote = stages.last().quote
        telemetryLogs.addAll(stages.map { it.log })
        telemetryLogs.add("> [FAST-FORWARD] MULTI-STAGE SYNTHESIS DIRECTLY COMPLETED.")
        telemetryLogs.add("> [ARCHIVE SYNC] PROPOSAL COMMITTED TO DOSSIER ARCHIVE REPOSITORY.")
        onSaveHybridToArchive(hybridDossier)
        toneGenerator.playChime(SoundEffectType.CHIME)
    }

    fun submitInterrogation(prompt: String) {
        if (prompt.isBlank() || isInterrogating) return
        val query = prompt.trim()
        isInterrogating = true
        toneGenerator.playChime(SoundEffectType.TAP)

        scope.launch {
            delay(350)
            val answer = when {
                query.contains("decoherence", ignoreCase = true) || query.contains("thermal", ignoreCase = true) ->
                    "${dossierA.agent.displayName} & ${dossierB.agent.displayName}: \"Thermal decoherence is eliminated by maintaining continuous dynamic vibrational parity in the non-Hermitian Berry phase manifold. The energy gap ΔE >> k_B T at 310K sustains macroscopic phase coherence without cryogenic refrigeration.\""
                query.contains("scale", ignoreCase = true) || query.contains("macro", ignoreCase = true) ->
                    "${dossierA.agent.displayName} & ${dossierB.agent.displayName}: \"Macro-scale integration is achieved via a 4,096-channel heterogeneous optical crossbar etched directly onto titanium-aluminide. Soliton mode-locking ensures synchronous phase propagation across centimeter dimensions.\""
                query.contains("paradox", ignoreCase = true) || query.contains("energy", ignoreCase = true) ->
                    "${dossierA.agent.displayName} & ${dossierB.agent.displayName}: \"The apparent conservation violation is resolved: extraction draws exclusively from relativistic Casimir boundary fluctuations, while local negative energy density is strictly bounded by Ford-Roman quantum inequalities.\""
                else ->
                    "${dossierA.agent.displayName} & ${dossierB.agent.displayName}: \"Under the coupled Hamiltonian H_hybrid, the metric stress tensor T_μν remains divergence-free. The hybrid apparatus demonstrates continuous non-computable state reductions while producing 1.84 kW/cm³ net usable output.\""
            }
            interrogationHistory.add(0, query to answer)
            interrogationInput = ""
            isInterrogating = false
            toneGenerator.playChime(SoundEffectType.CHIME)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chamber Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
            border = BorderStroke(1.dp, if (isSynthesisComplete) EchoGreen else EchoCyan.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSynthesisComplete) EchoGreen.copy(alpha = 0.2f) else EchoCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSynthesisComplete) Icons.Default.Check else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isSynthesisComplete) EchoGreen else EchoCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SYNTHESIZE HYBRID PROPOSAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSynthesisComplete) EchoGreen else EchoCyan,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Agent Hot-Seat Fusion Engine // Autonomous Polymath Dialectic",
                                fontSize = 8.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    if (isSynthesisComplete) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EchoGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Auto-Saved", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoGreen)
                        }
                    } else if (isSynthesizing) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EchoCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Simulating", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                        }
                    }
                }

                // Dual Polymath Confrontation Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar A Capsule
                    val isSpeakerA = activeSpeaker?.id == dossierA.agent.id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSpeakerA) 1.5.dp else 0.8.dp,
                                color = if (isSpeakerA) dossierA.agent.primaryColor else EchoCardStroke,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                CouncilAvatarManager.HolographicAvatarBadge(profile = dossierA.agent, isActive = isSpeakerA, size = 18.dp)
                                Text(dossierA.agent.displayName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dossierA.agent.primaryColor)
                            }
                            Text(dossierA.title, fontSize = 8.sp, color = EchoText, maxLines = 1)
                        }
                    }

                    // Fusion Reactor Node
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(EchoDarkSurface)
                            .border(1.dp, if (isSynthesizing) EchoCyan else EchoGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isSynthesizing) EchoCyan else EchoGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Avatar B Capsule
                    val isSpeakerB = activeSpeaker?.id == dossierB.agent.id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSpeakerB) 1.5.dp else 0.8.dp,
                                color = if (isSpeakerB) dossierB.agent.primaryColor else EchoCardStroke,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = EchoCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                CouncilAvatarManager.HolographicAvatarBadge(profile = dossierB.agent, isActive = isSpeakerB, size = 18.dp)
                                Text(dossierB.agent.displayName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dossierB.agent.primaryColor)
                            }
                            Text(dossierB.title, fontSize = 8.sp, color = EchoText, maxLines = 1)
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // STATE 1: PRE-SYNTHESIS AUTOMATED PARAMETERS & INITIATION ACTION
        // ---------------------------------------------------------------------
        if (!isSynthesizing && !isSynthesisComplete) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = BorderStroke(0.8.dp, EchoCardStroke),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "AUTONOMOUS FUSION PARAMETERS (DETERMINED BY POLYMATH AVATARS)",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cross-Domain Coupling:", fontSize = 8.sp, color = EchoMuted)
                        Text("${dossierA.scientificField} ⟷ ${dossierB.scientificField}", fontSize = 8.sp, color = EchoText, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Predicted Synergy Index:", fontSize = 8.sp, color = EchoMuted)
                        Text("${analysis.synergyIndex}% (High Convergence)", fontSize = 8.sp, color = EchoGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hot-Seat Dialectic Mode:", fontSize = 8.sp, color = EchoMuted)
                        Text("Autonomous Multi-Stage Cross-Fire", fontSize = 8.sp, color = EchoText)
                    }
                }
            }

            // Launch Button
            Button(
                onClick = { runSynthesis() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_start_hybrid_synthesis"),
                colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Synthesize Hybrid Proposal (Agent Hot-Seat)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // ---------------------------------------------------------------------
        // STATE 2: LIVE MULTI-STAGE SIMULATION & STREAMING TELEMETRY PROGRESS
        // ---------------------------------------------------------------------
        if (isSynthesizing || (isSynthesisComplete && telemetryLogs.isNotEmpty())) {
            // Stage Stepper Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EchoDarkSurface)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEach { stage ->
                    val isPast = isSynthesisComplete || currentStageNumber > stage.stageNumber
                    val isCurrent = isSynthesizing && currentStageNumber == stage.stageNumber
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPast -> EchoGreen
                                        isCurrent -> EchoCyan
                                        else -> EchoCardStroke
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPast) "✓" else "${stage.stageNumber}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPast || isCurrent) Color.Black else EchoMuted
                            )
                        }
                        Text(
                            text = "S${stage.stageNumber}",
                            fontSize = 8.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) EchoCyan else EchoMuted
                        )
                    }
                }
            }

            // Progress Bar & Percentage
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = if (isSynthesisComplete) "SIMULATION COMPLETE" else "STAGE $currentStageNumber/5: ${stages.getOrNull(currentStageNumber - 1)?.title ?: ""}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSynthesisComplete) EchoGreen else EchoCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(synthesisProgress * 100).toInt()}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSynthesisComplete) EchoGreen else EchoCyan
                    )
                }

                // Cybernetic Custom Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EchoDarkSurface)
                        .testTag("synthesis_progress_indicator")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(synthesisProgress.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSynthesisComplete) EchoGreen else EchoCyan)
                    )
                }
            }

            // Live Telemetry Terminal Console
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("telemetry_terminal_log"),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = BorderStroke(0.8.dp, if (isSynthesisComplete) EchoGreen.copy(alpha = 0.5f) else EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "● ● ● TELEMETRY STREAM // AGENT HOT-SEAT CORE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isSynthesizing) {
                            TextButton(
                                onClick = { fastForwardSynthesis() },
                                modifier = Modifier.height(20.dp).testTag("btn_fast_forward_synthesis"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Fast-Forward ⏩", fontSize = 8.sp, color = EchoCyan)
                            }
                        }
                    }

                    // Display streaming lines
                    telemetryLogs.takeLast(4).forEach { logLine ->
                        Text(
                            text = logLine,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (logLine.contains("SUCCESS") || logLine.contains("COMPLETE")) EchoGreen else EchoText,
                            lineHeight = 11.sp
                        )
                    }

                    // Active Speaker Speech Bubble
                    if (isSynthesizing && activeSpeaker != null && activeSpeakerQuote.isNotBlank()) {
                        HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            CouncilAvatarManager.HolographicAvatarBadge(profile = activeSpeaker!!, isActive = true, size = 18.dp)
                            Column {
                                Text(activeSpeaker!!.displayName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = activeSpeaker!!.primaryColor)
                                Text(
                                    text = "\"$activeSpeakerQuote\"",
                                    fontSize = 8.sp,
                                    color = EchoText,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // STATE 3: SYNTHESIZED BREAKTHROUGH DOSSIER REVEAL & ROUTING ACTIONS
        // ---------------------------------------------------------------------
        if (isSynthesisComplete) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("synthesized_hybrid_dossier_card"),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = BorderStroke(1.2.dp, EchoGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Authorship & Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            CouncilAvatarManager.HolographicAvatarBadge(profile = dossierA.agent, isActive = false, size = 18.dp)
                            Text("×", fontSize = 10.sp, color = EchoMuted)
                            CouncilAvatarManager.HolographicAvatarBadge(profile = dossierB.agent, isActive = false, size = 18.dp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${dossierA.agent.displayName} & ${dossierB.agent.displayName}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoGreen
                            )
                        }
                        Text("TRL-4 Validated", fontSize = 8.sp, color = EchoCyan, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = analysis.hybridProposalTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText
                    )

                    HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)

                    Text("UNIFIED BREAKTHROUGH HYPOTHESIS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
                    Text(analysis.hybridProposalHypothesis, fontSize = 10.sp, color = EchoText, lineHeight = 13.sp)

                    HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)

                    Text("COUPLED PHYSICAL MECHANISM & EQUATIONS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoGreen, fontFamily = FontFamily.Monospace)
                    Text(analysis.hybridProposalMechanism, fontSize = 9.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 12.sp)

                    HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)

                    Text("COMPOSITE INVENTION BLUEPRINT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoMuted, fontFamily = FontFamily.Monospace)
                    Text(analysis.hybridProposalBlueprint, fontSize = 9.sp, color = EchoText, fontFamily = FontFamily.Monospace, lineHeight = 12.sp)

                    HorizontalDivider(color = EchoCardStroke, thickness = 0.5.dp)

                    Text("NARRATIVE & METAPHYSICAL HOOK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA726), fontFamily = FontFamily.Monospace)
                    Text(analysis.hybridProposalHook, fontSize = 9.sp, color = EchoText, fontStyle = FontStyle.Italic, lineHeight = 12.sp)
                }
            }

            // Interactive Agent Hot-Seat Interrogation Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = BorderStroke(0.8.dp, EchoCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(14.dp))
                            Text(
                                text = "INTERROGATE MODEL IN AGENT HOT-SEAT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text("185+ IQ Co-Authors", fontSize = 8.sp, color = EchoMuted)
                    }

                    // Prompt Suggestion Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Decoherence threshold", "Macro scalability", "Apparent energy paradox").forEach { chip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EchoBgAlt)
                                    .clickable { submitInterrogation(chip) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(chip, fontSize = 8.sp, color = EchoText)
                            }
                        }
                    }

                    // Challenge Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = interrogationInput,
                            onValueChange = { interrogationInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("input_hybrid_interrogation"),
                            placeholder = { Text("Stress-test this model...", fontSize = 9.sp, color = EchoMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EchoCyan,
                                unfocusedBorderColor = EchoCardStroke,
                                focusedTextColor = EchoText,
                                unfocusedTextColor = EchoText
                            )
                        )

                        Button(
                            onClick = { submitInterrogation(interrogationInput) },
                            enabled = interrogationInput.isNotBlank() && !isInterrogating,
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("btn_interrogate_hybrid_hotseat"),
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            if (isInterrogating) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = EchoOnCyan, strokeWidth = 1.5.dp)
                            } else {
                                Text("Ask", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Interrogation Q&A History
                    interrogationHistory.forEach { (q, a) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Q: $q", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EchoCyan)
                                Text(a, fontSize = 8.sp, color = EchoText, lineHeight = 11.sp)
                            }
                        }
                    }
                }
            }

            // 4 Primary Destination Routing Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Inject to Council Debate
                Button(
                    onClick = {
                        onSynthesizeAndInject(
                            analysis.hybridProposalTitle,
                            analysis.hybridProposalHypothesis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("btn_inject_hybrid_debate"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Inject Debate", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // 2. Save / Update in Archive
                Button(
                    onClick = { onSaveHybridToArchive(hybridDossier) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("btn_save_hybrid_archive"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Save Dossier", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // 3. Export to Workspace Notes
                Button(
                    onClick = { onExportHybridToWorkspace(hybridDossier) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("btn_export_hybrid_notes"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoDarkSurface, contentColor = EchoText),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("To Notes", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // 4. Re-load into Agent Hot-Seat
                Button(
                    onClick = { onReloadHybridToHotSeat(hybridDossier) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("btn_reload_hybrid_hotseat"),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Hot-Seat", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Re-Synthesize Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        isSynthesisComplete = false
                        isSynthesizing = false
                        synthesisProgress = 0f
                        currentStageNumber = 1
                        telemetryLogs.clear()
                        interrogationHistory.clear()
                    },
                    modifier = Modifier.testTag("btn_reset_hybrid_synthesis")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Re-Synthesize Alternate Model", fontSize = 8.sp, color = EchoMuted)
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet for selecting a counterpart dossier to compare with the active dossier.
 * Features categorized domain filtering, search, and instant selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DossierCounterpartPickerSheet(
    primaryDossier: TheoreticalDossier?,
    targetSlot: String,
    dossierArchive: List<TheoreticalDossier>,
    onSelectCounterpart: (TheoreticalDossier) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf("All Domains") }

    val domains = listOf(
        "All Domains",
        "Quantum Mechanics & Bio-Cybernetics",
        "Theoretical AI Architectures & Singularity",
        "Zero-Point Energy & Casimir Physics",
        "Astrophysics & Spacetime Geometry",
        "Silicon Photonics & Neuromorphic Hardware"
    )

    val candidateDossiers = dossierArchive.filter { d ->
        val matchesDomain = selectedDomain == "All Domains" ||
            d.scientificField.contains(selectedDomain, ignoreCase = true) ||
            selectedDomain.contains(d.scientificField, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
            d.title.contains(searchQuery, ignoreCase = true) ||
            d.hypothesis.contains(searchQuery, ignoreCase = true) ||
            d.agent.displayName.contains(searchQuery, ignoreCase = true)
        matchesDomain && matchesQuery
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = EchoBgAlt,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("sheet_dossier_counterpart_picker"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(EchoCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = "SELECT COUNTERPART DOSSIER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (primaryDossier != null) "Comparing against: ${primaryDossier.title.take(28)}..." else "Choose candidate dossier",
                            fontSize = 9.sp,
                            color = EchoCyan
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted, modifier = Modifier.size(18.dp))
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_picker_search"),
                placeholder = { Text("Search by theory, author, or keyword...", fontSize = 11.sp, color = EchoMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = EchoMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EchoCyan,
                    unfocusedBorderColor = EchoCardStroke,
                    focusedTextColor = EchoText,
                    unfocusedTextColor = EchoText,
                    focusedContainerColor = EchoDarkSurface,
                    unfocusedContainerColor = EchoDarkSurface
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Domain Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(domains) { domain ->
                    val isSelected = selectedDomain == domain
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDomain = domain },
                        label = { Text(domain, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EchoCyan,
                            selectedLabelColor = EchoOnCyan,
                            containerColor = EchoDarkSurface,
                            labelColor = EchoText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EchoCyan else EchoCardStroke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            HorizontalDivider(color = EchoCardStroke, thickness = 0.8.dp)

            // Candidate Dossiers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (candidateDossiers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No dossiers match your search filter.", fontSize = 11.sp, color = EchoMuted)
                        }
                    }
                } else {
                    items(candidateDossiers, key = { it.id }) { candidate ->
                        val isPrimary = primaryDossier?.id == candidate.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCounterpart(candidate) }
                                .testTag("card_picker_dossier_${candidate.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPrimary) EchoCyan.copy(alpha = 0.12f) else EchoDarkSurface
                            ),
                            border = BorderStroke(
                                width = if (isPrimary) 1.5.dp else 1.dp,
                                color = if (isPrimary) EchoCyan else candidate.agent.primaryColor.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CouncilAvatarManager.HolographicAvatarBadge(profile = candidate.agent, isActive = false, size = 32.dp)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(candidate.agent.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = candidate.agent.primaryColor)
                                            if (isPrimary) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(EchoCyan.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("Current", fontSize = 8.sp, color = EchoCyan, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(candidate.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                                        Text(candidate.scientificField, fontSize = 8.sp, color = EchoMuted, maxLines = 1)
                                        Text(candidate.hypothesis, fontSize = 9.sp, color = EchoText.copy(alpha = 0.8f), maxLines = 2, lineHeight = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = { onSelectCounterpart(candidate) },
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("btn_picker_select_${candidate.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Select", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

