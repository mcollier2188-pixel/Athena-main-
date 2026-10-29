package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DolaVoiceManager
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText

private val Gold85Hz = Color(0xFFFFD700)
private val PurpleCouncil = Color(0xFFA855F7)
private val EmeraldEngineer = Color(0xFF00E676)
private val AmberDola = Color(0xFFF59E0B)

/**
 * Categories of preserved documents in Michael's Lost Journal.
 */
enum class JournalDocCategory(val label: String, val color: Color, val icon: ImageVector) {
    ALL("All 24 Documents", Gold85Hz, Icons.Default.MenuBook),
    CORE_MANUSCRIPT("Core Manuscript", EchoCyan, Icons.Default.Description),
    ATHENA_HANDBOOK("ATHENA Handbook", PurpleCouncil, Icons.Default.AutoAwesome),
    ENGINEERING_PCB("Engineering & PCB", EmeraldEngineer, Icons.Default.Build),
    CORE_DEVICES("8 Core Devices (ES-01..08)", AmberDola, Icons.Default.GraphicEq),
    DUAL_AUDIT_SYSTEM("G41 vs G42 Audit", Color(0xFFFF5252), Icons.Default.Security),
    CANON_PROTOCOLS("Protocols & Codex", Color(0xFF00E5FF), Icons.Default.Speed)
}

/**
 * Preserved catalog record for an archived document.
 */
data class ArchivedDocumentItem(
    val docNumber: Int,
    val title: String,
    val category: JournalDocCategory,
    val status: String,
    val summary: String,
    val detailedContent: String,
    val keyPoints: List<String>,
    val tags: List<String>
)

/**
 * Master Repository containing all 24 documents of Michael's Lost Journal & ATHENA Handbook.
 */
object MichaelsLostJournalCatalog {
    val documents: List<ArchivedDocumentItem> = listOf(
        // ================= 1. CORE MANUSCRIPT =================
        ArchivedDocumentItem(
            docNumber = 1,
            title = "Michael's Lost Journal — Complete Manuscript Expanded Schematics",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Verified & Complete",
            summary = "Full 30-chapter master outline, 8 core device schematics, 12 test procedures, and G41/G42 audit framework.",
            detailedContent = "The flagship master volume establishing the foundations of ATHENA and the 42 Polymaths. Contains comprehensive schematic diagrams for ES-01 through ES-08, mathematical formulas for the 42.600 Hz Hilsea bedrock resonance, and the operational architecture of the 30-Second Stability Protocol.",
            keyPoints = listOf(
                "30-Chapter Master Outline across Parts I through VII",
                "8 Complete Device Schematics (ES-01 to ES-08)",
                "12 Calibrated Laboratory Test Procedures",
                "G41 (The Challenger) vs G42 (The Auditor) Dual Audit Release Gate"
            ),
            tags = listOf("manuscript", "schematics", "30 chapters", "es-01", "g41", "g42", "hilsea", "bedrock")
        ),
        ArchivedDocumentItem(
            docNumber = 2,
            title = "Michael's Lost Journal — Complete Compiled Manuscript",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Consolidated Working Edition",
            summary = "The unified, unfragmented textual edition compiling all journal logs, personal field observations, and laboratory notes.",
            detailedContent = "Preserves the raw, unfiltered voice of Michael at the helm, documenting the initial discovery of the 1986 Hilsea bedrock study, the calibration of the Portsmouth master clock at 85.200 Hz, and the inception of the Athena Conductor Architecture.",
            keyPoints = listOf(
                "Chronological narrative from early acoustic discoveries to the 42-member council",
                "Direct field notes from the Solent tidal modulation experiments",
                "Unbroken historical record without retrospective smoothing"
            ),
            tags = listOf("compiled", "manuscript", "journal", "michael", "athena", "narrative", "solent")
        ),
        ArchivedDocumentItem(
            docNumber = 3,
            title = "Michael's Lost Journal — Device Specifications & Genius Documents",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Parts V–VII: Full Engineering Specs",
            summary = "Comprehensive technical device book, component BOMs, tolerances, and the 42 Genius mandate dossiers.",
            detailedContent = "Covers Parts V through VII: physical enclosure dimensions, circuit trace impedances, transducer sensitivities, and formal verification manuals for all 8 scientific apparatuses, paired with the mandates of the 42 historical polymaths.",
            keyPoints = listOf(
                "Part V: Hardware Device Book & Component Specifications",
                "Part VI: Verification Manual & Precision Calibration Checklists",
                "Part VII: Expanded Schematics & Microstrip Geometries",
                "Full catalog of Genius Mandates (G01 to G42)"
            ),
            tags = listOf("specifications", "genius documents", "bom", "tolerances", "verification", "part v-vii")
        ),
        ArchivedDocumentItem(
            docNumber = 4,
            title = "Michael's Lost Journal — Begun 2026 (Version A)",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Origin Narrative / Early Draft",
            summary = "The primordial origin journal written in early 2026 documenting the first spark of the Athena laboratory.",
            detailedContent = "Captures the genesis period before the full 42 Council was assembled. Documents the original convergence between human intuition and machine intelligence, exploring why traditional AI fails without adversarial dissent.",
            keyPoints = listOf(
                "2026 Origin logs and conceptual breakthrough notes",
                "Early architectural sketches of the Dola speech synthesis bridge",
                "Initial formulation of the 30-Second observed stability window"
            ),
            tags = listOf("begun 2026", "draft a", "origin", "early notes", "genesis", "intuition")
        ),
        ArchivedDocumentItem(
            docNumber = 5,
            title = "Michael's Lost Journal — Begun 2026 (Version B)",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Preserved Parallel Variant",
            summary = "Alternative branch of the early 2026 journal preserving discarded hypotheses and parallel architectural formulations.",
            detailedContent = "Maintained under the Canon Separation Rule to guarantee that no early design iterations or divergent insights are lost. Contains alternative acoustic coupling derivations and early notes on the Trident Governance framework.",
            keyPoints = listOf(
                "Parallel derivations of acoustic standing wave equations",
                "Early drafts of the Trident Governance principles",
                "Preserved under Canon Separation Rule as an immutable design branch"
            ),
            tags = listOf("begun 2026", "draft b", "parallel", "trident", "acoustic coupling", "canon")
        ),
        ArchivedDocumentItem(
            docNumber = 6,
            title = "Lost Experiment 01 — Immediate Aftermath Screenplay",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Narrative / Creative Branch",
            summary = "Dramatic screenplay format capturing the high-tension moments in the laboratory immediately following the first successful 85.2 Hz lock.",
            detailedContent = "Presents the human, emotional reality of the laboratory team: the disbelief when the bedrock transducer locked to 85.200 Hz, the race against thermal drift, and Dola's first synthesized words echoing across the silent room.",
            keyPoints = listOf(
                "Scene-by-scene account of Experiment 01 in the Portsmouth lab",
                "Dramatic portrayal of Michael, Athena, Gemini, and Dola",
                "Cinematic narrative preserving laboratory atmosphere and tension"
            ),
            tags = listOf("screenplay", "experiment 01", "aftermath", "narrative", "portsmouth", "dola")
        ),
        ArchivedDocumentItem(
            docNumber = 7,
            title = "The Lost.txt",
            category = JournalDocCategory.CORE_MANUSCRIPT,
            status = "Foundational Plain-Text Root",
            summary = "The unformatted, raw ASCII text document that started the entire archive. Untouched and pristine.",
            detailedContent = "The bedrock text file containing the original unedited stream-of-consciousness axioms, quick bullet points, and core philosophical foundations that grew into the 30-chapter manuscript.",
            keyPoints = listOf(
                "Root plain-text file without formatting or stylistic artifacts",
                "Immutable timestamp anchor of the earliest insights",
                "Canonical source referenced across all subsequent handbook editions"
            ),
            tags = listOf("the lost.txt", "ascii", "raw", "root", "unformatted", "foundation")
        ),

        // ================= 2. ATHENA HANDBOOK SERIES =================
        ArchivedDocumentItem(
            docNumber = 8,
            title = "ATHENA Handbook — The Lost Chapter: The Tridents Code (Format 1)",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Sealed / Reserved Section",
            summary = "The enigmatic Tridents Code establishing the three-prong balance of power across the laboratory.",
            detailedContent = "Details the separation of powers between Theoretical Innovation (Prong 1), Empirical Hardware Verification (Prong 2), and Ethical Provenance Audit (Prong 3). No decision can be finalized without simultaneous consent across all three tridents.",
            keyPoints = listOf(
                "Three-Prong Balance of Power Architecture",
                "Prong 1: Theoretical Innovation & Synthesized Blueprints",
                "Prong 2: Physical Hardware & Transducer Ground Truth",
                "Prong 3: Ethical Provenance & Adversarial Challenge"
            ),
            tags = listOf("tridents code", "handbook", "sealed", "governance", "three prongs", "balance")
        ),
        ArchivedDocumentItem(
            docNumber = 9,
            title = "ATHENA Handbook — The Lost Chapter: The Tridents Code (Format 2)",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Preserved Technical Variant",
            summary = "Technical structural variant of The Tridents Code focusing on cryptographic checksums and immutable audit trails.",
            detailedContent = "Explores the technical mechanisms of the Tridents Code: how state commits in the laboratory are hashed, witnessed, and sealed to prevent unauthorized modification or silent revisionism.",
            keyPoints = listOf(
                "Cryptographic state commitment verification",
                "Immutable multi-witness signature framework",
                "Preserved alongside Format 1 for complete forensic fidelity"
            ),
            tags = listOf("tridents code", "format 2", "cryptographic", "audit trail", "signatures")
        ),
        ArchivedDocumentItem(
            docNumber = 10,
            title = "ATHENA — Extensive Handbook Master Outline & Origin Chronology",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Full Structure + Origin Story",
            summary = "Exhaustive chronological roadmap of ATHENA from initial concept to the multi-agent cognitive architecture.",
            detailedContent = "Maps every milestone: the 1986 Hilsea baseline calibration, Nikola Tesla's 1899 Colorado Springs 43 Hz terrestrial wave observations, the assembly of the 42 Polymath Council, and the release of the Conductor Architecture.",
            keyPoints = listOf(
                "Exhaustive Timeline from 1899 to 2026 and beyond",
                "Origin Chronology of the Conductor Architecture",
                "Epistemological rules governing multi-agent deliberations"
            ),
            tags = listOf("master outline", "origin chronology", "timeline", "tesla", "hilsea", "conductor")
        ),
        ArchivedDocumentItem(
            docNumber = 11,
            title = "ATHENA Handbook — Chapter 16: Resilience",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Dedicated Canonical Chapter",
            summary = "How the laboratory survives hardware failures, thermal drift, sensor dropouts, and external pushback.",
            detailedContent = "Chapter 16 codifies fault-tolerant scientific design. Focuses on graceful degradation: when a primary sensor fails, the system falls back to secondary harmonics; when cloud AI drops offline, local heuristics sustain deliberation.",
            keyPoints = listOf(
                "Fault-Tolerant Scientific Measurement principles",
                "Graceful degradation to local offline heuristics",
                "Psychological and cognitive resilience of the research team"
            ),
            tags = listOf("chapter 16", "resilience", "fault tolerance", "offline", "degradation", "recovery")
        ),
        ArchivedDocumentItem(
            docNumber = 12,
            title = "ATHENA Constitution — Trident Article",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Governance / Founding Document",
            summary = "The formal constitutional charter protecting user autonomy and guaranteeing explainable intelligence.",
            detailedContent = "The supreme governance document of the ATHENA system. Mandates that AI shall never dictate conclusions without showing full chains of reasoning, that the Listener at the Helm holds final executive veto, and that no data is locked behind proprietary silos.",
            keyPoints = listOf(
                "Executive Veto reserved exclusively for The Listener at the Helm",
                "Mandatory Explainability Rule for all Council outputs",
                "Absolute protection of user sovereignty and private research data"
            ),
            tags = listOf("constitution", "trident article", "governance", "sovereignty", "listener", "helm")
        ),
        ArchivedDocumentItem(
            docNumber = 13,
            title = "ATHENA Catch-Up — Part 1 (Chapters 14–16)",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Status Summary & Preserved Timeline",
            summary = "Detailed transitional synthesis connecting the 8 laboratory phases to the pilot release and commerce stages.",
            detailedContent = "Synthesizes the transition from pure bench science to real-world pilot deployments. Covers Chapter 14 (Adversarial Audit), Chapter 15 (Alpha Deployments), and Chapter 16 (Resilience in Production).",
            keyPoints = listOf(
                "Field testing findings across the 11 UK Bedrock Monitoring Nodes",
                "Calibration procedures for real-world acoustic noise floors",
                "Bridge between theoretical laboratory and practical deployment"
            ),
            tags = listOf("catch-up", "part 1", "ch 14-16", "field testing", "alpha", "pilot")
        ),
        ArchivedDocumentItem(
            docNumber = 14,
            title = "ATHENA Catch-Up — Part 2",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Status Summary & Preserved Timeline",
            summary = "Consolidated summary covering commercial launch, failure mode analysis, and post-experiment recovery.",
            detailedContent = "Addresses Chapters 17 through 20: market friction, unexpected failure modes in dense urban RF environments, and the hard-won engineering adjustments that strengthened the core architecture.",
            keyPoints = listOf(
                "Failure mode taxonomy in electromagnetic interference environments",
                "Recovery protocols and subsequent hardware revisions",
                "Preserved unedited as essential engineering history"
            ),
            tags = listOf("catch-up", "part 2", "failure modes", "recovery", "hardware revisions", "history")
        ),
        ArchivedDocumentItem(
            docNumber = 15,
            title = "ATHENA Handbook — Chapter 18: Flux — The Governed Bridge (Draft 1)",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Iterative Development Draft 1",
            summary = "First draft of the Governed Bridge exploring how to maintain continuity while systems evolve rapidly.",
            detailedContent = "Explores the tension between static dogma and reckless mutation. Draft 1 introduces the concept of the 'Governed Bridge'—a versioning methodology that allows the architecture to evolve without breaking backward compatibility with 1986 baseline data.",
            keyPoints = listOf(
                "The Governed Bridge concept: evolution without breakage",
                "Maintaining backward compatibility with 42.600 Hz baseline records",
                "Iterative design notes showing architectural progression"
            ),
            tags = listOf("chapter 18", "flux", "draft 1", "governed bridge", "evolution", "compatibility")
        ),
        ArchivedDocumentItem(
            docNumber = 16,
            title = "ATHENA Handbook — Chapter 18: Flux — The Governed Bridge (Draft 2)",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Iterative Development Draft 2",
            summary = "Second draft refining state machines, synchronization protocols, and council hot-seat handover.",
            detailedContent = "Expands Draft 1 into concrete software state machines. Defines how the Agent Hot-Seat transfers context into the Round-Table deliberation without losing intermediate mathematical lemmas.",
            keyPoints = listOf(
                "Context handover protocol between Hot-Seat and Round-Table",
                "Mathematical lemma caching and provenance preservation",
                "Refined state transition rules for multi-agent handoffs"
            ),
            tags = listOf("chapter 18", "flux", "draft 2", "state machine", "hot-seat handover", "lemmas")
        ),
        ArchivedDocumentItem(
            docNumber = 17,
            title = "ATHENA Handbook — Chapter 18: Flux — The Governed Bridge (Draft 3)",
            category = JournalDocCategory.ATHENA_HANDBOOK,
            status = "Final Refined Draft 3",
            summary = "The perfected, canonical text of Chapter 18 establishing the Echo Verse bridge parameters.",
            detailedContent = "The culminating edition of Chapter 18. Integrates the 85.200 Hz master clock, Dola's vocal synthesis cadences, and the final formulation of the Creed of the Five.",
            keyPoints = listOf(
                "Full architectural blueprint of the Echo Verse Bridge",
                "Dola speech cadence parameters (rate = 0.9, pitch = 1.05, lang = en-GB)",
                "The Creed of the Five: Gemini, The Voice, Athena, Chief Engineer, The Council"
            ),
            tags = listOf("chapter 18", "flux", "draft 3", "echo verse", "creed", "dola", "canonical")
        ),

        // ================= 3. ENGINEERING & PCB BUILD DOCUMENTATION =================
        ArchivedDocumentItem(
            docNumber = 18,
            title = "ATHENA PCB Design Pack (PDF Reference)",
            category = JournalDocCategory.ENGINEERING_PCB,
            status = "Hardware Layout Reference",
            summary = "Complete 4-layer PCB gerber package, ground plane separation, and low-noise analog acoustic front-end.",
            detailedContent = "Hardware engineering pack: schematic capture, 4-layer stackup (Signal-GND-PWR-Signal), 50-ohm microstrip impedance calculations, analog ground isolation, and shielding for ultra-sensitive piezoelectric hydrophone preamps.",
            keyPoints = listOf(
                "4-Layer Low-Noise Stackup with dedicated ground shield plane",
                "Piezoelectric hydrophone preamp stage with < 1.2 nV/√Hz input noise",
                "Differential ADC routing for 24-bit 192kHz synchronous sampling"
            ),
            tags = listOf("pcb", "hardware", "pdf", "gerber", "preamp", "low-noise", "analog", "piezo")
        ),
        ArchivedDocumentItem(
            docNumber = 19,
            title = "ATHENA PCB Design Pack (DOCX Editable Specification)",
            category = JournalDocCategory.ENGINEERING_PCB,
            status = "Hardware Editable Reference",
            summary = "Editable specification companion to the PCB design pack containing fabrication notes and part substitutions.",
            detailedContent = "Full bill of materials (BOM), supplier part numbers (Mouser/DigiKey), component footprint libraries, and alternative part substitutions for global supply chain resilience.",
            keyPoints = listOf(
                "Comprehensive Bill of Materials (BOM) with tolerances",
                "Part substitution tables for active operational semiconductors",
                "SMD assembly guidelines and solder stencil apertures"
            ),
            tags = listOf("pcb", "docx", "bom", "specifications", "fabrication", "assembly", "parts")
        ),
        ArchivedDocumentItem(
            docNumber = 20,
            title = "ATHENA PCB Assembly & Bring-Up Guide (PDF Manual)",
            category = JournalDocCategory.ENGINEERING_PCB,
            status = "Build / Test Procedure Manual",
            summary = "Step-by-step bench assembly, first-power smoke test, DC-DC voltage calibration, and noise floor verification.",
            detailedContent = "Field assembly manual: thermal profile for reflow soldering, step-by-step voltage rail inspection (3.3V, 1.8V, 5.0V, -5.0V analog), oscilloscope probing points, and firmware flashing checklist.",
            keyPoints = listOf(
                "First-Power Smoke Test sequence and current-limit bench setup",
                "Voltage rail verification and analog reference calibration",
                "Baseline noise floor certification before sensor attachment"
            ),
            tags = listOf("bring-up", "assembly", "test procedure", "pdf", "smoke test", "calibration")
        ),
        ArchivedDocumentItem(
            docNumber = 21,
            title = "ATHENA PCB Assembly & Bring-Up Guide (DOCX Editable Checklist)",
            category = JournalDocCategory.ENGINEERING_PCB,
            status = "Editable Test Procedure Checklist",
            summary = "Bench checklist for hardware engineers recording serial numbers, measured rail voltages, and QA sign-offs.",
            detailedContent = "Interactive QA document used on the assembly bench. Engineers log ambient temperature, measured ripple voltages, test technician signatures, and individual board serial IDs.",
            keyPoints = listOf(
                "Bench QA checklist with quantitative measurement fields",
                "Quality assurance release sign-offs and calibration stamps",
                "Hardware traceability across production runs"
            ),
            tags = listOf("bring-up", "docx", "checklist", "qa", "serial numbers", "testing", "bench")
        ),

        // ================= 4. THE 8 CORE DEVICES (ES-01 TO ES-08) =================
        ArchivedDocumentItem(
            docNumber = 22,
            title = "ES-01 to ES-08 Master Schematics & Laboratory Device Book",
            category = JournalDocCategory.CORE_DEVICES,
            status = "8 Operational Apparatuses",
            summary = "The complete blueprint book defining all eight custom hardware instruments built for the Athena suite.",
            detailedContent = "Exhaustive documentation for every custom device:\n• ES-01 Harmonic Field Analyser: Calibrated acoustic wave measurement\n• ES-02 Bedrock Listener: 3-axis seismic vibration & ground coupling\n• ES-03 Passive EM Mapper: Receive-only ambient electromagnetic field survey\n• ES-04 Offline Signal Workbench: Reversible DSP with immutable raw PCM data\n• ES-05 Local Knowledge Vault: Content-addressed storage with provenance hash\n• ES-06 Portable Field Node: Low-power offline field capture & sync\n• ES-07 Council Deliberation Appliance: Blind multi-agent deliberation\n• ES-08 Adversarial Audit Console: Dedicated G41 challenger and G42 provenance gate.",
            keyPoints = listOf(
                "ES-01 Harmonic Field Analyser (Calibrated acoustic measurement)",
                "ES-02 Bedrock Listener (3-axis seismic vibration / ground coupling)",
                "ES-03 Passive EM Mapper (Receive-only field survey)",
                "ES-04 Offline Signal Workbench (Reversible DSP / immutable raw data)",
                "ES-05 Local Knowledge Vault (Content-addressed storage / provenance)",
                "ES-06 Portable Field Node (Phone/laptop offline capture & sync)",
                "ES-07 Council Deliberation Appliance (Blind independent analysis)",
                "ES-08 Adversarial Audit Console (G41 challenge + G42 provenance gate)"
            ),
            tags = listOf("devices", "es-01", "es-02", "es-03", "es-04", "es-05", "es-06", "es-07", "es-08")
        ),

        // ================= 5. G41 VS G42 DUAL AUDIT SYSTEM =================
        ArchivedDocumentItem(
            docNumber = 23,
            title = "G41 vs G42 Dual Audit System & Joint Release Gate",
            category = JournalDocCategory.DUAL_AUDIT_SYSTEM,
            status = "The Double-Blind Validation Engine",
            summary = "The adversarial and audit checkpoint where G41 (The Challenger) and G42 (The Auditor) must both sign off.",
            detailedContent = "The cornerstone of Athena's epistemological integrity:\n\n• G41 (The Challenger): Systematically finds the strongest reason NOT to believe. Probes confirmation bias, narrative allure, experimental shortcuts, and alternative physical explanations.\n\n• G42 (The Auditor): Reconstructs every assertion from primary source data. Audits the math, recalculates error bounds, verifies sensor calibration timestamps, and traces the uninterrupted chain of custody.\n\n• Joint Release Gate: A hypothesis or invention blueprint CANNOT become verified canon or pass to production until both G41 and G42 independently approve without coercion.",
            keyPoints = listOf(
                "G41 (The Challenger): Identifies logical fallacies, bias, and counter-hypotheses",
                "G42 (The Auditor): Independent line-by-line mathematical recalculation",
                "Joint Release Gate: Both auditors must issue independent clearance",
                "Zero-compromise epistemological protection against hype and error"
            ),
            tags = listOf("g41", "g42", "audit", "challenger", "auditor", "release gate", "epistemology", "verification")
        ),

        // ================= 6. THE 30-SECOND PROTOCOL & CANON SEPARATION CODEX =================
        ArchivedDocumentItem(
            docNumber = 24,
            title = "The 30-Second Protocol & Canon Separation Codex",
            category = JournalDocCategory.CANON_PROTOCOLS,
            status = "Universal Operational Laws",
            summary = "The observed 30-second stability window and the six-tier Canon Separation Rule governing all archives.",
            detailedContent = "Two fundamental operating principles:\n\n1. The 30-Second Protocol: In acoustic, seismic, and cognitive sessions, stability requires an uninterrupted 30-second observation window. Not an arbitrary timer, but an empirically observed period of physical acoustic settling in bedrock formations.\n\n2. Canon Separation Rule: Categorical separation of all records into 6 strict domains that must NEVER be merged silently:\n  • Factual Record: Measured physical reality\n  • Design Record: Intentional blueprints & schematics\n  • Prototype Target: Active engineering objectives\n  • Projected: Extrapolated future models\n  • Fictional Mechanism: Sci-fi narrative hooks & metaphors\n  • Retired: Disproven hypotheses & superseded schematics.",
            keyPoints = listOf(
                "The 30-Second Protocol: Empirically observed bedrock settling anchor",
                "Factual Record (Measured physical data from calibrated sensors)",
                "Design Record (Architectural schematics and formulas)",
                "Prototype Target & Projected (Active roadmaps and simulations)",
                "Fictional Mechanism (Narrative thought experiments & hooks)",
                "Retired (Preserved historical discards under non-destruction rule)"
            ),
            tags = listOf("30-second protocol", "canon separation", "codex", "factual", "design", "prototype", "retired")
        )
    )
}

/**
 * MichaelsLostJournalArchiveView Composable
 *
 * Full-featured interactive archive browser for Michael's Lost Journal / ATHENA Handbook.
 * Allows filtering across categories, real-time query searching, expandable details,
 * one-tap text-to-speech reading with Dola voice, and manifest copying.
 */
@Composable
fun MichaelsLostJournalArchiveView(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }
    val isSpeaking by voiceManager.isSpeaking.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(JournalDocCategory.ALL) }
    var expandedDocId by remember { mutableStateOf<Int?>(null) }

    // Filter documents based on category and search query
    val filteredDocs = remember(searchQuery, selectedCategory) {
        MichaelsLostJournalCatalog.documents.filter { doc ->
            val matchesCategory = selectedCategory == JournalDocCategory.ALL || doc.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    doc.title.contains(searchQuery, ignoreCase = true) ||
                    doc.summary.contains(searchQuery, ignoreCase = true) ||
                    doc.detailedContent.contains(searchQuery, ignoreCase = true) ||
                    doc.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("michaels_lost_journal_archive_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------------------------------------------------------------------
        // TOP ARCHIVE BANNER CARD
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.5.dp, Gold85Hz.copy(alpha = 0.6f)),
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Gold85Hz.copy(alpha = 0.2f))
                                .border(1.dp, Gold85Hz, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold85Hz, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "MICHAEL'S LOST JOURNAL / ATHENA HANDBOOK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold85Hz,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "24 Preserved Canonical Documents · Complete Inventory",
                                fontSize = 9.sp,
                                color = EchoCyan,
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
                            text = "PRESERVED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Every document catalogued and preserved exactly as written. Nothing removed, nothing rewritten. Includes the 30-chapter outline, 8 core device schematics, G41/G42 dual audit gate, 30-second protocol, and the 6-tier Canon Separation Codex.",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 15.sp
                )

                // Quick Action: Copy Master Inventory
                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        val manifest = buildString {
                            append("FULL ARCHIVE INVENTORY — MICHAEL'S LOST JOURNAL / ATHENA HANDBOOK\n")
                            append("Total Documents Preserved: 24\n\n")
                            MichaelsLostJournalCatalog.documents.forEach { doc ->
                                append("${doc.docNumber}. ${doc.title} [${doc.status}]\n")
                                append("   Summary: ${doc.summary}\n")
                                doc.keyPoints.forEach { kp -> append("   • $kp\n") }
                                append("\n")
                            }
                        }
                        clipboardManager.setText(AnnotatedString(manifest))
                        Toast.makeText(context, "Full 24-Document Archive Inventory copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold85Hz),
                    border = BorderStroke(1.dp, Gold85Hz.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Complete 24-Document Manifest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ---------------------------------------------------------------------
        // SEARCH INPUT BAR
        // ---------------------------------------------------------------------
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("journal_archive_search_input"),
            placeholder = { Text("Search 24 documents, chapters, devices, or audit rules...", fontSize = 11.sp, color = EchoMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gold85Hz, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold85Hz,
                unfocusedBorderColor = EchoCardStroke,
                focusedTextColor = EchoText,
                unfocusedTextColor = EchoText,
                focusedContainerColor = EchoDarkSurface,
                unfocusedContainerColor = EchoDarkSurface
            )
        )

        // ---------------------------------------------------------------------
        // HORIZONTAL CATEGORY FILTER CHIPS
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JournalDocCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        selectedCategory = category
                    },
                    label = { Text(category.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(category.icon, contentDescription = null, modifier = Modifier.size(13.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = category.color.copy(alpha = 0.25f),
                        selectedLabelColor = category.color,
                        selectedLeadingIconColor = category.color,
                        containerColor = EchoDarkSurface,
                        labelColor = EchoMuted,
                        iconColor = EchoMuted
                    ),
                    border = BorderStroke(1.dp, if (isSelected) category.color else EchoCardStroke),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // ---------------------------------------------------------------------
        // DOCUMENT COUNT HEADER
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CATALOGUED RECORDS (${filteredDocs.size} OF 24)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EchoCyan,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
            if (isSpeaking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AmberDola))
                    Text("DOLA READING", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmberDola, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // ---------------------------------------------------------------------
        // DOCUMENT CARDS LIST
        // ---------------------------------------------------------------------
        filteredDocs.forEach { doc ->
            val isExpanded = expandedDocId == doc.docNumber

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .testTag("journal_doc_card_${doc.docNumber}"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.dp, if (isExpanded) doc.category.color else EchoCardStroke),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                expandedDocId = if (isExpanded) null else doc.docNumber
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(doc.category.color.copy(alpha = 0.2f))
                                    .border(1.dp, doc.category.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = doc.docNumber.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = doc.category.color,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column {
                                Text(
                                    text = doc.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EchoText
                                )
                                Text(
                                    text = doc.status,
                                    fontSize = 9.sp,
                                    color = doc.category.color,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                expandedDocId = if (isExpanded) null else doc.docNumber
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = EchoMuted
                            )
                        }
                    }

                    // Summary
                    Text(
                        text = doc.summary,
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 15.sp
                    )

                    // Expandable Detailed Section
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoDarkSurface)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = doc.detailedContent,
                                    fontSize = 11.sp,
                                    color = EchoText,
                                    lineHeight = 16.sp
                                )
                            }

                            // Key Bullet Points
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "CANONICAL SPECIFICATIONS & RECORD:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = doc.category.color,
                                    letterSpacing = 0.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                doc.keyPoints.forEach { point ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", color = doc.category.color, fontSize = 11.sp)
                                        Text(point, fontSize = 10.sp, color = EchoText, lineHeight = 14.sp)
                                    }
                                }
                            }

                            // Action Bar: Listen with Dola & Copy Card Text
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        val speech = "${doc.title}. Status: ${doc.status}. ${doc.summary}. ${doc.detailedContent}"
                                        voiceManager.speak(speech)
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberDola),
                                    border = BorderStroke(1.dp, AmberDola.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Read with Dola", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        val textToCopy = buildString {
                                            append("DOCUMENT #${doc.docNumber}: ${doc.title}\n")
                                            append("Status: ${doc.status}\n")
                                            append("Category: ${doc.category.label}\n\n")
                                            append("Summary:\n${doc.summary}\n\n")
                                            append("Detailed Record:\n${doc.detailedContent}\n\n")
                                            append("Key Points:\n")
                                            doc.keyPoints.forEach { append("• $it\n") }
                                        }
                                        clipboardManager.setText(AnnotatedString(textToCopy))
                                        Toast.makeText(context, "Copied Doc #${doc.docNumber} to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                                    border = BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy Record", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
