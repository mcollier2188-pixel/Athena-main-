package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DolaVoiceManager
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoText

enum class FaqCategory(val displayName: String, val icon: ImageVector, val accentColor: Color) {
    ALL("All Topics", Icons.Default.HelpOutline, EchoCyan),
    AI_BATTLE_ARENA("1-on-1 AI Battle", Icons.Default.FlashOn, Color(0xFFFF3366)),
    DOCUMENT_SCANNER("CameraX Scanner", Icons.Default.DocumentScanner, Color(0xFF00E5FF)),
    ENERGY_RESONANCE("42.6 Hz Resonance", Icons.Default.GraphicEq, Color(0xFF00F5D4)),
    ATHENA_COUNCIL("Genius Council", Icons.Default.Groups, Color(0xFFCE93D8)),
    ATHENA_PLAYGROUND("Gemini Playground", Icons.Default.AutoAwesome, Color(0xFF80D8FF)),
    ATHENA_BUILDER("APK Builder", Icons.Default.Build, Color(0xFFFFD54F)),
    ACOUSTICS("Acoustics & Resonance", Icons.Default.GraphicEq, Color(0xFF00E5FF)),
    WORKSPACE("Workspace & Persistence", Icons.Default.Storage, EchoGreen),
    CREATIVE_MEDIA("Media Vault & Cinema", Icons.Default.VideoLibrary, Color(0xFFFF80AB)),
    SYSTEM_DRAWER("System & Display", Icons.Default.Settings, Color(0xFFB388FF)),
    LONG_PRESS("Long-Press Gestures", Icons.Default.TouchApp, Color(0xFFFFD54F)),
    LOST_JOURNAL("Michael's Lost Journal", Icons.Default.MenuBook, Color(0xFFFFD700)),
    GENERAL("General FAQ & Specs", Icons.Default.MenuBook, Color(0xFF80D8FF))
}

data class FaqItem(
    val id: String,
    val category: FaqCategory,
    val title: String,
    val summary: String,
    val detailedAnswer: String,
    val bulletPoints: List<String>,
    val tags: List<String>,
    val isAthenaFlagship: Boolean = false
)

object FaqDataRepository {
    val items: List<FaqItem> = listOf(
        // ================= ATHENA FLAGSHIP 1: GENIUS COUNCIL =================
        FaqItem(
            id = "athena_genius_council",
            category = FaqCategory.ATHENA_COUNCIL,
            title = "Athena Genius Council: 5-Seat Polymath Deliberations",
            summary = "Autonomous dialectic debate panel featuring Athena Core, Gemini 3.1 Pro, Dola AI, the Chief Engineer of ATHENA, and the Directorate of 42.",
            detailedAnswer = "The Genius Council brings together the laboratory's transcendent minds to cross-examine complex inquiries through orchestrated multi-agent reasoning. Anchored by the Chief Engineer of ATHENA as the resident architectural builder, the council members deconstruct axioms, reveal hidden contradictions, and compile executable scientific solutions.",
            bulletPoints = listOf(
                "ATHENA Core (IQ 185): Unified field topologies, bio-cybernetic consciousness & non-linear thermodynamics",
                "Gemini 3.1 Pro (IQ 188): Relativistic space-time tensors, quantum entanglement lattices & artificial super-intelligence",
                "Dola AI (IQ 182): Silicon photonics, neuromorphic substrates & zero-point Casimir energy hardware",
                "Chief Engineer of ATHENA (IQ 194): Autonomous systems compilation, reactive state telemetry, and living laboratory code execution",
                "The Council of 42 (IQ 192): Omni-Science Directorate & universal multi-disciplinary synthesis",
                "Historical Polymath Anchors: Epistemic heuristics inspired by Tesla, Da Vinci, Einstein, Lovelace, Socrates, Hypatia, and Aurelius"
            ),
            tags = listOf("council", "genius", "athena", "gemini", "dola", "chief engineer", "builder", "polymath", "5-seat", "debate"),
            isAthenaFlagship = true
        ),
        FaqItem(
            id = "athena_adversarial_audit",
            category = FaqCategory.ATHENA_COUNCIL,
            title = "Genius Council Adversarial Audit & Synthesis Engine",
            summary = "Real-time logical fallacy detection, cognitive bias auditing, and executive consensus synthesis.",
            detailedAnswer = "During and after Council debates, the Adversarial Audit Engine operates as a neutral epistemological validator. It scans the debate transcript for common reasoning failures (such as strawman arguments, false dichotomies, appeal to authority, or circular reasoning) and generates a structured verdict highlighting agreed consensus, irreconcilable trade-offs, and concrete action steps.",
            bulletPoints = listOf(
                "Fallacy Detection: Audits arguments against formal and informal logical fallacies in real-time",
                "Bias Calibration: Identifies unstated cultural, empirical, or ideological biases in member assertions",
                "Executive Consensus: Formulates actionable synthesis balancing theoretical rigor with practical implementation",
                "Audit Transcript Export: Single-tap export of the debate and audit analysis directly to Workspace notes"
            ),
            tags = listOf("audit", "adversarial", "bias", "fallacy", "synthesis", "logic", "verdict", "consensus"),
            isAthenaFlagship = true
        ),

        // ================= ATHENA FLAGSHIP 2: GEMINI PLAYGROUND =================
        FaqItem(
            id = "athena_gemini_playground",
            category = FaqCategory.ATHENA_PLAYGROUND,
            title = "Gemini AI Playground & Hyperparameter Studio",
            summary = "Direct Gemini inference laboratory with real-time Temperature, Top-P, token sliders, and Dola voice.",
            detailedAnswer = "The Gemini Playground gives researchers and creators direct access to state-of-the-art Gemini models with granular controls. Adjust reasoning temperature from razor-sharp deterministic logic (0.0) to maximum poetic creativity (2.0), configure nucleus sampling thresholds (Top-P), and test multimodal inputs with instant responses.",
            bulletPoints = listOf(
                "Precision Parameter Sliders: Temperature (0.0 - 2.0), Top-P nucleus sampling (0.1 - 1.0), and max output tokens",
                "Real-Time Token Tracking: Inspect token usage, response generation speeds, and stop sequences",
                "Dola Bidirectional Voice: Hands-free voice dictation with natural-cadence speech synthesis",
                "Prompt Template Library: Instant access to structured prompts for scientific papers, narrative design, and code audits",
                "Universal Long-Press Actions: Long-press any message bubble to Remix, Brainstorm 5 variations, Expand, or Copy"
            ),
            tags = listOf("gemini", "ai", "playground", "temperature", "topp", "tokens", "voice", "dola", "prompts"),
            isAthenaFlagship = true
        ),
        FaqItem(
            id = "athena_dola_voice",
            category = FaqCategory.ATHENA_PLAYGROUND,
            title = "Dola Native Voice Dictation & Text-to-Speech Engine",
            summary = "Hardware-tuned speech recognition and British-accented natural Text-to-Speech audio reader.",
            detailedAnswer = "Dola is the voice and ear of ATHENA. Integrated directly with Android's native SpeechRecognizer and TextToSpeech subsystems, Dola provides fluid voice conversations without sending audio data to third-party cloud servers. Tuned to a clear, calm British cadence (en-GB, speechRate = 0.95, pitch = 1.05), Dola can read any generated idea, note, or FAQ card aloud.",
            bulletPoints = listOf(
                "Real-Time RMS Telemetry: Visualizes microphone audio power levels during voice dictation",
                "Phoneme Cadence Optimization: Tuned rate (0.95) and pitch (1.05) designed to prevent listener fatigue",
                "One-Tap Reading: 'Listen' button on any FAQ item or Workspace note activates immediate Dola audio reading",
                "Hands-Free Workflow: Speak prompts aloud in the Gemini Playground and listen to synthesized responses"
            ),
            tags = listOf("dola", "voice", "speech", "dictation", "tts", "audio", "listening", "natural"),
            isAthenaFlagship = true
        ),

        // ================= ATHENA FLAGSHIP 3: APK BUILDER =================
        FaqItem(
            id = "athena_apk_builder",
            category = FaqCategory.ATHENA_BUILDER,
            title = "Athena Standalone Web App & APK Compiler",
            summary = "Synthesizes complete, interactive single-page HTML5/JS applications from natural language prompts.",
            detailedAnswer = "The Athena APK Builder bridges natural language conceptualization and working software. Describe an application, scientific calculator, interactive canvas, or simulation, and the generative engine writes complete, robust HTML5, CSS3, and JavaScript code ready for immediate execution in an on-device sandboxed environment.",
            bulletPoints = listOf(
                "Natural Language to Software: Generates working interactive tools, physics visualizers, and utilities",
                "Sandboxed App Runner: Runs synthesized code in an isolated WebView with JavaScript bridge enabled",
                "Real-Time Console: Live error tracking, DOM inspection, and console log output monitoring",
                "Standalone Export: Save created applications as offline HTML packages or distribution bundles",
                "Prompt Templates: Includes pre-configured blueprints for calculators, sound generators, and canvas visualizers"
            ),
            tags = listOf("apk builder", "compiler", "html", "javascript", "apps", "runner", "sandbox", "code generation"),
            isAthenaFlagship = true
        ),

        // ================= UNIVERSAL LONG-PRESS SUITE =================
        FaqItem(
            id = "long_press_suite",
            category = FaqCategory.LONG_PRESS,
            title = "Universal Long-Press Contextual Action Suite",
            summary = "Press and hold any item across the app to open the categorized Material 3 Bottom Sheet modal.",
            detailedAnswer = "Long-pressing any created item—including Gemini AI messages, Workspace notes, folders, Media Vault images, Creative tasks, playlists, and quotes—opens a categorized action bottom sheet featuring powerful tools tailored to that item.",
            bulletPoints = listOf(
                "Content & Export: Copy to clipboard, system share sheet, export to Workspace note, and export as .txt/.html file",
                "AI Remix & Variations: Gemini AI style remix, brainstorm 5 thematic variations, and expand into an in-depth essay",
                "Voice & Audio: Speak item aloud via Dola Text-to-Speech with instant stop/resume controls",
                "Organization & Lifecycle: Pin item to top of list, rename title dialog, duplicate clone, and move to trash"
            ),
            tags = listOf("long press", "bottom sheet", "remix", "brainstorm", "tts", "pin", "rename", "duplicate", "actions")
        ),

        // ================= ACOUSTICS & RESONANCE STUDIO =================
        FaqItem(
            id = "freq_generator",
            category = FaqCategory.ACOUSTICS,
            title = "Laboratory Harmonic Frequency Generator",
            summary = "Real-time, ultra-low-distortion sinusoidal, square, triangle, and sawtooth tone synthesizer.",
            detailedAnswer = "The Frequency Generator synthesizes laboratory-grade acoustic waveforms in real time using Android's native AudioTrack pipeline with 32-bit floating-point PCM buffers. Frequencies can be set anywhere from 0.1 Hz infrasound up to 24,000.0 Hz ultrasound with 0.01 Hz precision.",
            bulletPoints = listOf(
                "Solfeggio Presets: 174 Hz (Grounding), 285 Hz (Restoration), 396 Hz (Liberation), 417 Hz (Change), 528 Hz (Transformation), 639 Hz (Connection), 741 Hz (Intuition), 852 Hz (Spiritual Order), 963 Hz (Consciousness)",
                "Schumann Resonances: 7.83 Hz fundamental Earth frequency plus 14.3 Hz, 20.8 Hz, 27.3 Hz, 33.8 Hz harmonics",
                "Four Waveform Morphologies: Pure Sine, Rich Square, Warm Triangle, and Harmonic Sawtooth",
                "WAV Exporter: Direct export of custom harmonic chords and frequency sweeps to uncompressed 24-bit 48kHz audio files"
            ),
            tags = listOf("frequency", "tone", "solfeggio", "schumann", "sine", "audio", "wav", "generator", "hertz", "528", "432")
        ),
        FaqItem(
            id = "binaural_beats",
            category = FaqCategory.ACOUSTICS,
            title = "Binaural Harmonics & Brainwave Entrainment",
            summary = "Dual-channel frequency heterodyning for neurological brainwave entrainment (Delta, Theta, Alpha, Beta, Gamma).",
            detailedAnswer = "Binaural beats drive two mathematically distinct frequencies into the left and right audio channels simultaneously. When listening through stereo headphones, the brain's superior olivary nucleus computes the phase difference, generating an internal perceived beat matching target brainwave frequencies.",
            bulletPoints = listOf(
                "Delta (0.5 - 4.0 Hz): Deep restorative sleep, subconscious cellular recovery, and somatic release",
                "Theta (4.0 - 8.0 Hz): Deep meditation, REM dreams, hypnagogic vision, and profound creative insight",
                "Alpha (8.0 - 13.0 Hz): Calm wakefulness, effortless concentration, flow states, and accelerated learning",
                "Beta (13.0 - 30.0 Hz): Active analytical thinking, executive decision-making, and high alertness",
                "Gamma (30.0 - 100.0 Hz): High-level cognitive binding, peak focus, and transcendental flashes of insight"
            ),
            tags = listOf("binaural", "brainwaves", "delta", "theta", "alpha", "beta", "gamma", "entrainment", "meditation")
        ),
        FaqItem(
            id = "bioscience_lab",
            category = FaqCategory.ACOUSTICS,
            title = "BioScience Lab: Biological Resonance & 42.6 Hz Sub-Harmonics",
            summary = "Maps human EEG brainwave bands, HRV autonomic pulse, and circadian rhythms to the 42.6 Hz UK bedrock waveguide network via octave sub-harmonic division.",
            detailedAnswer = "The BioScience Lab module tracks multi-stream biological oscillators mapped mathematically to the 42.600 Hz fundamental standing wave through binary division (f / 2ⁿ). Provides 60 FPS real-time Canvas oscillograms, phase coherence gauges, bio-acoustic entrainment tone playback, and one-tap Workspace note logging.",
            bulletPoints = listOf(
                "Octave Sub-Harmonic Ladder: Gamma (42.600 Hz, 1:1), Beta (21.300 Hz, 1:2), Alpha (10.650 Hz, 1:4), Theta (5.325 Hz, 1:8), Delta (1.331 Hz, 1:32)",
                "Autonomic Mayer Wave & Circadian: 0.100 Hz HRV cardiac sympathetic tone and 24-hour suprachiasmatic cellular diurnal cycle",
                "11-Node Coupling: Connect bodily bio-streams to physical UK monitoring stations (N1 Portsmouth Master, N6 Southsea Common)",
                "Real-time 60 FPS Canvas: Multi-channel simultaneous trace of EEG oscillations, PPG cardiac pulses, and bedrock reference carrier"
            ),
            tags = listOf("bioscience", "eeg", "hrv", "circadian", "brainwave", "sub-harmonic", "alpha", "theta", "bedrock", "entrainment")
        ),
        FaqItem(
            id = "cymatics_simulator",
            category = FaqCategory.ACOUSTICS,
            title = "Chladni Plate Cymatics 2D Simulator",
            summary = "Visualizes acoustic surface wave interference and sand particle aggregation on vibrating resonant plates.",
            detailedAnswer = "The Cymatics Simulator calculates real-time solutions to Ernst Chladni's wave differential equations on a two-dimensional boundary plate. Thousands of simulated particles react to vibrational kinetic energy, naturally migrating away from high-amplitude antinodes toward zero-displacement nodal lines to reveal sacred geometry.",
            bulletPoints = listOf(
                "Equation: ψ(x,y) = a·sin(nπx/L)·sin(mπy/L) - b·cos(mπx/L)·cos(nπy/L)",
                "Variable Modal Integers: Adjust mode integers m and n to uncover complex radial and circular geometric symmetries",
                "Real-time particle dynamics: Thousands of high-density simulated silica grains reacting with friction and surface damping"
            ),
            tags = listOf("cymatics", "chladni", "geometry", "particles", "resonance", "plate", "waves")
        ),
        FaqItem(
            id = "kings_chamber",
            category = FaqCategory.ACOUSTICS,
            title = "King's Chamber Acoustic Waveguide",
            summary = "Mathematical standing wave and resonant cavity acoustic simulation of the Great Pyramid granite chamber.",
            detailedAnswer = "This scientific model simulates the acoustic properties of the Great Pyramid of Giza's King's Chamber, calculated using its exact physical dimensions (10.47m x 5.23m x 5.81m). It calculates dimensional standing wave modes, granite piezoelectric response, and Helmholtz acoustic coupling.",
            bulletPoints = listOf(
                "Mode formula: f = (c/2) · √((nx/L)² + (ny/W)² + (nz/H)²)",
                "Acoustic resonant sweep: Audio frequency sweeps demonstrating room amplification at 117 Hz, 256 Hz, and 432 Hz",
                "Granite acoustic reflection: Models high-Q resonance factors characteristic of red Aswan granite wall slabs"
            ),
            tags = listOf("pyramid", "waveguide", "kings chamber", "acoustics", "standing waves", "helmholtz")
        ),

        // ================= WORKSPACE & PERSISTENCE =================
        FaqItem(
            id = "workspace_notes",
            category = FaqCategory.WORKSPACE,
            title = "Workspace Notes, Folders & Data Persistence",
            summary = "Local Room SQLite database with optional Firebase Firestore cloud synchronization, tagging, and search.",
            detailedAnswer = "The Workspace manages all your personal research, notes, field logs, and created documents. Data is stored on-device in a Room SQLite database for zero-latency offline access, with optional cloud backup and synchronization to Firebase Firestore.",
            bulletPoints = listOf(
                "Hierarchical Folders: Nest notes inside folders with breadcrumb trail navigation",
                "Batch Selection: Multi-select notes to batch pin, color-tag, export, move to folder, or delete",
                "Pro Notepad: Distraction-free Markdown editor with live word count, document reader mode, and export formats",
                "Trash & Recovery: Deleted items enter a dedicated trash bin with full restore and permanent purge capabilities"
            ),
            tags = listOf("workspace", "notes", "folders", "room", "firestore", "database", "offline", "sync")
        ),

        // ================= MEDIA VAULT & CINEMA =================
        FaqItem(
            id = "media_vault_cinema",
            category = FaqCategory.CREATIVE_MEDIA,
            title = "Encrypted Media Vault & Echo Cinema Player",
            summary = "Private local storage for photos and videos, and YouTube-reactive video player with docked miniplayer.",
            detailedAnswer = "The Media Vault provides an isolated repository for photos and videos outside of your public photo gallery. The Echo Cinema engine lets you organize research lectures and audio-visual playlists, featuring variable playback speeds, 90° rotation, and a YouTube-style docked miniplayer that continues playing while you browse other tabs.",
            bulletPoints = listOf(
                "Docked Miniplayer: Minimizes to a compact bar above the bottom navigation with play/pause and swipe-to-dismiss",
                "Variable Speed: 0.25x slow-motion analysis up to 2.0x accelerated playback",
                "Private Storage: Sandboxed internal storage protects research photos and diagrams from public gallery apps"
            ),
            tags = listOf("media vault", "photos", "videos", "cinema", "player", "miniplayer", "youtube")
        ),

        // ================= GENERAL SPECS & WORD EXPORT =================
        FaqItem(
            id = "export_word",
            category = FaqCategory.GENERAL,
            title = "Exporting to Microsoft Word & External Docs",
            summary = "How to copy and paste documentation, notes, and AI outputs into Microsoft Word or Google Docs.",
            detailedAnswer = "You can easily transfer text from this app to Microsoft Word: tap 'Copy Entire Guide' at the top of this FAQ to copy the full multi-page capabilities guide, or use the 'Copy Section' button on any individual topic. For Workspace notes, use the HTML export option, which opens directly in Microsoft Word with complete formatting.",
            bulletPoints = listOf(
                "One-Tap Master Copy: Copies the entire comprehensive capabilities guide formatted cleanly for Word",
                "HTML Export: Notes exported as .html retain headings, bullet lists, and paragraphs in Word and Docs",
                "Single-Topic Copy: Tap the copy icon on any card to copy just that specific section"
            ),
            tags = listOf("word", "google docs", "export", "copy", "clipboard", "documentation", "print")
        ),

        // ================= 1-ON-1 AI BATTLE ARENA =================
        FaqItem(
            id = "athena_1on1_ai_battle",
            category = FaqCategory.AI_BATTLE_ARENA,
            title = "1-on-1 AI Battle Arena: Autonomous Head-to-Head Clash",
            summary = "Toggle the Council off to unleash direct 1-on-1 adversarial debate between rival AI philosophies with real-time dialectic tension scoring.",
            detailedAnswer = "The 1-on-1 AI Battle Arena mode dismisses the collaborative council and pits two rival synthetic intelligences directly against each other in structured adversarial combat. Driven by a single unified Gemini engine with distinct epistemological system prompts, rival combatants (such as Nexus Rationalis and Prometheus Radical) cross-examine premises, expose logical fallacies, and trade devastating mathematical punchlines across multiple rounds.",
            bulletPoints = listOf(
                "Dialectic Engine Mode Switch: One-tap toggle in the Brainstorming Lab header between Council (5-Seat) and 1-on-1 AI Battle",
                "Preset & Customizable Combatants: Nexus Rationalis (Empirical Realist), Prometheus Radical (Quantum Topology Disruptor), Chronos Determinist (4D Block Universe), Gaia Cybernetic (Autopoietic Systems)",
                "Real-Time Momentum Balance Gauge: Dynamic 0-100% tension meter visualizing point shifts as rebuttals land",
                "Structured Round Engine: Opening Salvo, Cross-Examination Rebuttal, and Final Decisive Strike",
                "Supreme Adjudicator Verdict: Post-duel dialectic synthesis crowning the winner, summarizing the decisive insight, and formulating consensus",
                "Unified Gemini Architecture: Powered entirely by a single Gemini 3.5 Flash / 3.1 Pro engine without requiring secondary AI subscriptions"
            ),
            tags = listOf("battle", "arena", "duel", "adversarial", "clash", "1-on-1", "debate", "nexus", "prometheus", "gemini", "verdict"),
            isAthenaFlagship = true
        ),

        // ================= IN-APP CAMERAX SCANNER =================
        FaqItem(
            id = "athena_camerax_scanner",
            category = FaqCategory.DOCUMENT_SCANNER,
            title = "In-App CameraX Document Scanner & Direct Council Injection",
            summary = "Hardware camera scanner with document framing overlay, contrast optimization, and instant AI text/formula extraction.",
            detailedAnswer = "The In-App Document Scanner leverages Android CameraX hardware APIs to capture research notes, handwritten formulas, whiteboard diagrams, and printed articles directly inside the app. Captured images undergo real-time contrast enhancement (Q-Factor 150.0 optimization) before being processed by Gemini's multimodal vision pipeline. Extracted axioms and paradoxes are automatically injected into the live Council Round-Table or AI Battle Arena as the active debate proposition.",
            bulletPoints = listOf(
                "Zero-Permission Gallery Alternative: Uses direct camera hardware capture with runtime permission launcher",
                "Cybernetic Framing Viewfinder: Visual high-contrast guide brackets for aligning paper notes and whiteboard schematics",
                "Contrast Optimization Engine: Algorithmic grayscale stretch and threshold enhancement for maximum optical character readability",
                "Multimodal Gemini Analysis: Extracts handwritten formulas, identifies theoretical paradoxes, and synthesizes unified debate propositions",
                "Direct Council & Arena Routing: Instantly populates the active debate topic in the Brainstorming Lab with one tap",
                "Top-Bar & Drawer Triggers: Quick access via top-bar camera icon or the System Settings Drawer"
            ),
            tags = listOf("camerax", "camera", "scanner", "ocr", "multimodal", "vision", "notes", "whiteboard", "injection", "document"),
            isAthenaFlagship = true
        ),

        // ================= 42.6 HZ ENERGY RESONANCE =================
        FaqItem(
            id = "athena_energy_resonance",
            category = FaqCategory.ENERGY_RESONANCE,
            title = "42.6 Hz Bedrock Energy Resonance & Harmonic Frequency Nodes",
            summary = "Hardware-accelerated 60fps canvas visualizer mapping dialectic energy resonance around the 42.600 Hz geological baseline.",
            detailedAnswer = "Located as a dedicated tab in the Brainstorming Lab, the Energy Resonance visualizer maps the theoretical resonance of active debate topics into interactive frequency nodes. Powered by hardware-accelerated Jetpack Compose Canvas physics at 60fps, users can drag, modulate, and calibrate node frequencies toward the 42.600 Hz Hilsea bedrock resonance baseline discovered in the 1986 acoustic strata calibrations. The engine calculates Q-factor energy transfer coupling in real-time with visual harmonic glow spectrum feedback without audio playback interference.",
            bulletPoints = listOf(
                "42.600 Hz Baseline: Aligns with the fundamental acoustic standing wave discovered across the 11 UK bedrock monitoring nodes",
                "Hardware-Accelerated 60fps Physics: Smooth dragging, harmonic orbital drift, and kinetic node collisions on Compose Canvas",
                "Q-Factor Coupling Calculator: R(f) = 1 / sqrt(1 + Q^2 * (f/f0 - f0/f)^2) calculates real-time resonance lock percentage (up to 100%)",
                "Visual Harmonic Spectrum Glow: Radiant cyan and emerald aura indicates peak resonance lock without unwanted audio synthesizer playback",
                "Direct Debate Linking: Selecting any node injects its hypothesis snippet directly into the Council or AI Battle proposition card"
            ),
            tags = listOf("resonance", "42.6", "frequency", "nodes", "hilsea", "bedrock", "standing wave", "canvas", "physics", "q-factor"),
            isAthenaFlagship = true
        ),

        // ================= AGENT HOT-SEAT & DOSSIERS =================
        FaqItem(
            id = "athena_agent_hot_seat",
            category = FaqCategory.ATHENA_COUNCIL,
            title = "Agent Hot-Seat & Theoretical Dossier Engine",
            summary = "Deep-dive inquiry mode for polymaths generating structured invention blueprints and narrative sci-fi hooks.",
            detailedAnswer = "The Agent Hot-Seat isolates an individual council member (Athena Core, Gemini 3.1 Pro, Dola AI, or The Council of 42) for concentrated theoretical interrogation. When queried, the agent compiles a complete Theoretical Dossier featuring Formal Mechanisms, Invention Blueprints, and Narrative Hooks, automatically cataloged in the Dossier Archive Drawer.",
            bulletPoints = listOf(
                "Targeted Polymath Podium: Switch active interrogation between Athena (Quantum), Gemini (Singularity), Dola (Casimir), and Directorate",
                "Complete Dossier Output: Generates Title, Scientific Field, Hypothesis, Mathematical Mechanism, Blueprint, and Sci-Fi Narrative Hook",
                "Indexed Dossier Archive Drawer: Collapsible quick-peek drawer storing and indexing all compiled dossiers",
                "Side-by-Side Comparison Lab: Compare two dossiers across axioms to synthesize hybrid inventions"
            ),
            tags = listOf("hot seat", "dossier", "blueprint", "inquiry", "athena", "gemini", "dola", "archive", "mechanism")
        ),

        // ================= SYSTEM SETTINGS DRAWER =================
        FaqItem(
            id = "athena_system_drawer",
            category = FaqCategory.SYSTEM_DRAWER,
            title = "System Settings Navigation Drawer & Display Consolidator",
            summary = "Consolidated modal navigation drawer for display modes, orientation locks, and global audio controls.",
            detailedAnswer = "Accessible via the top-app-bar menu button or left edge swipe, the System Settings Drawer unifies display configuration, global tab visibility, device orientation locks, and hardware shortcuts into a clean Material 3 modal sheet.",
            bulletPoints = listOf(
                "Display Modes: Toggle Immersive Fullscreen Mode (System bars auto-hide) and Orientation (Auto-Rotate vs Forced Portrait)",
                "Top & Bottom Bar Toggles: Individually show or hide the TopAppBar and NavigationBar for maximum screen estate",
                "In-App Scanner Shortcut: One-tap launcher for the CameraX document scanner",
                "Global Harmonic Audio Control: Instant mute or chime volume toggle for the 42.6 Hz acoustic synthesizer",
                "Persistent State Preservation: Retains user UI preferences throughout app lifecycle"
            ),
            tags = listOf("drawer", "settings", "fullscreen", "orientation", "display", "bars", "system", "navigation")
        ),

        // ================= CREATIVE MEDIA GENERATION =================
        FaqItem(
            id = "athena_creative_media_generation",
            category = FaqCategory.CREATIVE_MEDIA,
            title = "Generative Media Studio: Nano Banana, Veo 3.1 & Lyria Audio",
            summary = "Full creative generation suite featuring image synthesis, video generation, and harmonic Lyria music.",
            detailedAnswer = "The Creative Media Studio integrates Google's latest multimodal generative models directly into the workstation. Generate high-resolution concept art using gemini-nano-banana-2.1, animate static photos into fluid video using veo-3.1-fast-generate-preview, or compose acoustic soundtracks using lyria-3-clip-preview and lyria-3-pro-preview.",
            bulletPoints = listOf(
                "Image Generation: Generates 1K/2K concept art and diagrams with gemini-nano-banana-2.1",
                "Video Generation & Motion: Renders 1080p 16:9 cinematic clips and animates still images with veo-3.1-fast-generate-preview",
                "Music Synthesis: Generates 30-second Lyria harmonic clips across Ambient, Classical, Cyberpunk, and Solfeggio genres",
                "Integrated Media Vault: All generated images, videos, and audio tracks are saved directly to the local Media Vault"
            ),
            tags = listOf("media", "veo", "video", "lyria", "music", "audio", "nano banana", "image generation", "generative")
        ),

        // ================= UK BEDROCK MONITORING NODES =================
        FaqItem(
            id = "athena_bedrock_nodes",
            category = FaqCategory.ACOUSTICS,
            title = "The 11 UK Bedrock Monitoring Nodes & Planetary Telemetry",
            summary = "Geological acoustic waveguide stations synchronized to the 42.600 Hz Hilsea bedrock resonance baseline.",
            detailedAnswer = "The 11 UK Geological Nodes form a distributed acoustic waveguide array measuring standing wave frequencies in chalk and granite formations. Baseline calibration was established in the 1986 Hilsea laboratory study (measuring 42.598 Hz), confirming Nikola Tesla's 1899 Colorado Springs 43 Hz terrestrial standing wave observations. The array locks synchronously to the 11.74-second Solent tidal modulation cycle.",
            bulletPoints = listOf(
                "N1 Portsmouth (85.200 Hz): 2nd Harmonic Master Clock in chalk bedrock basin (50.8198° N, 1.0879° W)",
                "N2 Farnworth (170.400 Hz): 4th Harmonic Vault in Pennine stone strata",
                "N3 Lizard Point (255.600 Hz): 6th Harmonic Coastal Node in ophiolite metamorphic rock",
                "N4 Cairngorms (340.800 Hz): 8th Harmonic High-Altitude Granite Dome",
                "N5 Solent Deep (42.600 Hz): Fundamental Subterranean Maritime Strata matching tidal surge lock",
                "Acoustic Velocity: 3,420 m/s average speed of sound through dense limestone/chalk waveguides"
            ),
            tags = listOf("bedrock", "nodes", "portsmouth", "solent", "farnworth", "hilsea", "tesla", "telemetry", "geology")
        ),

        // ================= MICHAEL'S LOST JOURNAL & ATHENA HANDBOOK (24 DOCUMENTS) =================
        FaqItem(
            id = "michaels_lost_journal_inventory",
            category = FaqCategory.LOST_JOURNAL,
            title = "Full Archive Inventory: Michael's Lost Journal / ATHENA Handbook (24 Documents)",
            summary = "All 24 canonical documents pulled up, catalogued, and preserved exactly as they are without deletions or revisions.",
            detailedAnswer = "The permanent 24-document archive preserves the complete intellectual property and forensic history of Michael's Lost Journal and the ATHENA Handbook. The archive spans three major categories:\n\n1. Core Manuscript (Docs 1–7): Complete 30-chapter outline, consolidated working edition, Parts V-VII device book, Begun 2026 origin narratives (Drafts A & B), Lost Experiment 01 Screenplay, and The Lost.txt root file.\n\n2. ATHENA Handbook Series (Docs 8–17): The Tridents Code (sealed/reserved section in 2 formats), Master Outline & Origin Chronology, Chapter 16 (Resilience), Constitution (Trident Article), Catch-Up Parts 1 & 2, and Chapter 18 (Flux — The Governed Bridge across 3 revised development drafts).\n\n3. Engineering & PCB Build Documentation (Docs 18–21): 4-layer low-noise PCB design pack (PDF & DOCX) and step-by-step PCB assembly & bring-up test procedure guide (PDF & DOCX).",
            bulletPoints = listOf(
                "Docs 1–7 (Core Manuscript): Complete 30-chapter outline, expanded schematics, Begun 2026 drafts, Screenplay, and The Lost.txt",
                "Docs 8–17 (ATHENA Handbook Series): The Tridents Code (2 formats), Master Origin Chronology, Ch 16 Resilience, Constitution, Ch 18 Flux (3 drafts)",
                "Docs 18–21 (Engineering & PCB): 4-layer low-noise PCB design pack & assembly bring-up QA manuals (PDF + DOCX)",
                "Docs 22–24 (Architecture & Codex): 8 Core Devices Book (ES-01..08), G41/G42 Dual Audit Framework, and the 30-Second Protocol Codex",
                "Immutable Preservation: Zero silent rewriting; all historical versions and parallel branches preserved side-by-side"
            ),
            tags = listOf("lost journal", "archive", "24 documents", "handbook", "pcb", "manuscript", "inventory", "michael", "athena"),
            isAthenaFlagship = true
        ),
        FaqItem(
            id = "michaels_lost_journal_devices",
            category = FaqCategory.LOST_JOURNAL,
            title = "The 8 Core Devices (ES-01 to ES-08) & Schematics Book",
            summary = "Complete operational instrumentation suite built for the Athena acoustic resonance and cognitive laboratory.",
            detailedAnswer = "The Athena hardware suite consists of eight precision devices engineered for ground-truth empirical verification:\n\n• ES-01 Harmonic Field Analyser: Calibrated acoustic wave measurement in airborne and fluid chambers.\n• ES-02 Bedrock Listener: 3-axis seismic vibration transducer with direct mechanical ground coupling.\n• ES-03 Passive EM Mapper: Receive-only electromagnetic ambient field survey (zero transmission radiation).\n• ES-04 Offline Signal Workbench: Reversible DSP engine with immutable bit-for-bit raw PCM data storage.\n• ES-05 Local Knowledge Vault: Offline content-addressed storage with cryptographic provenance hashing.\n• ES-06 Portable Field Node: Ultra-low-power field capture device with automatic peer-to-peer sync.\n• ES-07 Council Deliberation Appliance: Hardware appliance running isolated multi-agent deliberations.\n• ES-08 Adversarial Audit Console: Dedicated cryptographic terminal enforcing G41 challenges and G42 provenance audits.",
            bulletPoints = listOf(
                "ES-01 Harmonic Field Analyser: Precision acoustic resonance calibration",
                "ES-02 Bedrock Listener: 3-axis seismic ground coupling for 42.600 Hz detection",
                "ES-03 Passive EM Mapper: Non-radiating receive-only RF field survey",
                "ES-04 Offline Signal Workbench: Reversible non-destructive DSP pipelines",
                "ES-05 Local Knowledge Vault: Content-addressed local document storage",
                "ES-06 Portable Field Node: Mobile field capture and synchronous station sync",
                "ES-07 Council Deliberation Appliance: Blind multi-agent reasoning appliance",
                "ES-08 Adversarial Audit Console: Hardware enforcement of G41 vs G42 gates"
            ),
            tags = listOf("devices", "es-01", "es-02", "es-03", "es-04", "es-05", "es-06", "es-07", "es-08", "schematics", "hardware"),
            isAthenaFlagship = true
        ),
        FaqItem(
            id = "michaels_lost_journal_g41_g42_audit",
            category = FaqCategory.LOST_JOURNAL,
            title = "G41 vs G42 Dual Audit System, 30-Second Protocol & Canon Codex",
            summary = "The double-blind validation release gate, 30-second stability window, and 6-tier Canon Separation Rule.",
            detailedAnswer = "The core operating governance codified in Michael's Lost Journal:\n\n1. G41 vs G42 Dual Audit System:\n• G41 (The Challenger): Systematically uncovers the strongest counter-argument or flaw. Probes cognitive bias, narrative pressure, and unstated assumptions.\n• G42 (The Auditor): Reconstructs every assertion from primary data, auditing raw math and physical sensor logs.\n• Joint Release Gate: No hypothesis, blueprint, or commercial release can be approved without independent sign-off from BOTH G41 and G42.\n\n2. The 30-Second Protocol: The empirically observed acoustic settling period in bedrock formations. All field measurements require a minimum 30-second continuous uninterrupted observation window.\n\n3. Canon Separation Rule: Categorical isolation across 6 domains that must never be merged: Factual Record ↔ Design Record ↔ Prototype Target ↔ Projected ↔ Fictional Mechanism ↔ Retired.",
            bulletPoints = listOf(
                "G41 (The Challenger): Identifies logical fallacies, bias, and alternative explanations",
                "G42 (The Auditor): Line-by-line verification from primary sensor ground truth",
                "Joint Release Gate: Both G41 and G42 must independently approve for canonization",
                "The 30-Second Protocol: Empirically established bedrock acoustic stability window",
                "6-Tier Canon Separation Rule: Factual, Design, Prototype, Projected, Fictional, Retired"
            ),
            tags = listOf("g41", "g42", "audit", "challenger", "auditor", "30-second protocol", "canon separation", "codex", "gate"),
            isAthenaFlagship = true
        )
    )

    fun getFullCapabilitiesDocumentation(): String {
        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("ATHENA & UNIFIED WORKSTATION: MASTER CAPABILITIES & EXHAUSTIVE SPECIFICATION\n")
        sb.append("================================================================================\n\n")
        sb.append("EXECUTIVE SUMMARY:\n")
        sb.append("This application is an integrated scientific, creative, and cognitive workstation uniting three\n")
        sb.append("disciplines: Multi-Agent Cognitive AI (ATHENA), Acoustic Resonance Physics, and Personal Research Workspace.\n\n")

        val categories = FaqCategory.entries.filter { it != FaqCategory.ALL }
        for (cat in categories) {
            val catItems = items.filter { it.category == cat }
            if (catItems.isNotEmpty()) {
                sb.append("--------------------------------------------------------------------------------\n")
                sb.append("MODULE: ${cat.displayName.uppercase()}\n")
                sb.append("--------------------------------------------------------------------------------\n\n")
                for (item in catItems) {
                    sb.append("TOPIC: ${item.title}\n")
                    sb.append("Summary: ${item.summary}\n\n")
                    sb.append("Comprehensive Overview:\n")
                    sb.append("${item.detailedAnswer}\n\n")
                    sb.append("Key Features & Specifications:\n")
                    for (bp in item.bulletPoints) {
                        sb.append("  • $bp\n")
                    }
                    sb.append("\nKeywords: ${item.tags.joinToString(", ")}\n\n")
                }
            }
        }
        sb.append("================================================================================\n")
        sb.append("END OF MASTER SPECIFICATION GUIDE\n")
        sb.append("================================================================================\n")
        return sb.toString()
    }
}

@Composable
fun WorkspaceFaqTab(
    toneGenerator: HarmonicToneGenerator,
    onSaveToWorkspaceNote: (title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FaqCategory.ALL) }
    var expandedItemIds by remember { mutableStateOf(setOf("athena_genius_council", "athena_gemini_playground", "athena_apk_builder")) }
    var currentlySpeakingId by remember { mutableStateOf<String?>(null) }
    val voiceManager = remember { DolaVoiceManager.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.stopSpeaking()
        }
    }

    val filteredItems = remember(searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        FaqDataRepository.items.filter { item ->
            val matchesCategory = selectedCategory == FaqCategory.ALL || item.category == selectedCategory
            val matchesQuery = query.isEmpty() ||
                item.title.lowercase().contains(query) ||
                item.summary.lowercase().contains(query) ||
                item.detailedAnswer.lowercase().contains(query) ||
                item.bulletPoints.any { it.lowercase().contains(query) } ||
                item.tags.any { it.lowercase().contains(query) }
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoBg)
    ) {
        // Master Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search ATHENA capabilities, formulas, features, or questions...", color = EchoMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = EchoCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = EchoMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EchoBgAlt,
                unfocusedContainerColor = EchoBgAlt,
                focusedBorderColor = EchoCyan,
                unfocusedBorderColor = EchoCardStroke,
                focusedTextColor = EchoText,
                unfocusedTextColor = EchoText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("faq_search_input")
        )

        // ATHENA Flagship Spotlight Hero Banner (Genius Council, AI Playground, APK Builder)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
            border = BorderStroke(1.dp, Color(0xFFB388FF).copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFB388FF), modifier = Modifier.size(16.dp))
                    Text(
                        text = "ATHENA COGNITIVE ENGINES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB388FF),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Genius Council Spotlight Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategory == FaqCategory.ATHENA_COUNCIL) Color(0xFFCE93D8).copy(alpha = 0.25f) else EchoBg,
                        border = BorderStroke(1.dp, if (selectedCategory == FaqCategory.ATHENA_COUNCIL) Color(0xFFCE93D8) else EchoCardStroke),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCategory = if (selectedCategory == FaqCategory.ATHENA_COUNCIL) FaqCategory.ALL else FaqCategory.ATHENA_COUNCIL
                                expandedItemIds = expandedItemIds + "athena_genius_council"
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFFCE93D8), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(2.dp))
                            Text("Genius Council", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                            Text("7 Polymaths", fontSize = 8.sp, color = EchoMuted)
                        }
                    }

                    // AI Playground Spotlight Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategory == FaqCategory.ATHENA_PLAYGROUND) Color(0xFF80D8FF).copy(alpha = 0.25f) else EchoBg,
                        border = BorderStroke(1.dp, if (selectedCategory == FaqCategory.ATHENA_PLAYGROUND) Color(0xFF80D8FF) else EchoCardStroke),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCategory = if (selectedCategory == FaqCategory.ATHENA_PLAYGROUND) FaqCategory.ALL else FaqCategory.ATHENA_PLAYGROUND
                                expandedItemIds = expandedItemIds + "athena_gemini_playground"
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF80D8FF), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(2.dp))
                            Text("Playground", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                            Text("Dola Voice", fontSize = 8.sp, color = EchoMuted)
                        }
                    }

                    // APK Builder Spotlight Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategory == FaqCategory.ATHENA_BUILDER) Color(0xFFFFD54F).copy(alpha = 0.25f) else EchoBg,
                        border = BorderStroke(1.dp, if (selectedCategory == FaqCategory.ATHENA_BUILDER) Color(0xFFFFD54F) else EchoCardStroke),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                selectedCategory = if (selectedCategory == FaqCategory.ATHENA_BUILDER) FaqCategory.ALL else FaqCategory.ATHENA_BUILDER
                                expandedItemIds = expandedItemIds + "athena_apk_builder"
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(2.dp))
                            Text("APK Builder", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EchoText, maxLines = 1)
                            Text("App Synthesis", fontSize = 8.sp, color = EchoMuted)
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FaqCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                val count = if (cat == FaqCategory.ALL) FaqDataRepository.items.size else FaqDataRepository.items.count { it.category == cat }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        selectedCategory = cat
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = null,
                            tint = if (isSelected) EchoOnCyan else cat.accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "${cat.displayName} ($count)",
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
                    modifier = Modifier.testTag("faq_chip_${cat.name.lowercase()}")
                )
            }
        }

        // Master Actions Bar (Copy All to Word or Save to Note)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Complete ATHENA Capabilities Guide", FaqDataRepository.getFullCapabilitiesDocumentation())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full Capabilities Guide copied to clipboard! Ready to paste into Word.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("faq_copy_all_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Entire Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        onSaveToWorkspaceNote(
                            "ATHENA Master Capabilities & Reference Manual",
                            FaqDataRepository.getFullCapabilitiesDocumentation()
                        )
                        Toast.makeText(context, "Created 'ATHENA Master Capabilities' note!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("faq_save_note_all_btn")
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save as Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // FAQ Items List (LazyColumn with Expandable Accordion Cards)
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = EchoMuted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No matching capabilities found", color = EchoMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = { searchQuery = ""; selectedCategory = FaqCategory.ALL }) {
                        Text("Clear Filters", color = EchoCyan, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val isExpanded = expandedItemIds.contains(item.id)
                    val isSpeaking = currentlySpeakingId == item.id

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                        border = if (item.isAthenaFlagship) BorderStroke(1.dp, Color(0xFFB388FF).copy(alpha = 0.3f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(animationSpec = tween(250))
                            .testTag("faq_card_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Header Row (Clickable to Expand/Collapse)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        expandedItemIds = if (isExpanded) {
                                            expandedItemIds - item.id
                                        } else {
                                            expandedItemIds + item.id
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Badge
                                Surface(
                                    shape = CircleShape,
                                    color = item.category.accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.category.icon,
                                            contentDescription = null,
                                            tint = item.category.accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EchoText,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (item.isAthenaFlagship) {
                                            Surface(
                                                color = Color(0xFFB388FF).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(start = 6.dp)
                                            ) {
                                                Text(
                                                    text = "ATHENA",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB388FF),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.summary,
                                        fontSize = 12.sp,
                                        color = EchoMuted,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        toneGenerator.playChime(SoundEffectType.TAP)
                                        expandedItemIds = if (isExpanded) {
                                            expandedItemIds - item.id
                                        } else {
                                            expandedItemIds + item.id
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = EchoCyan
                                    )
                                }
                            }

                            // Expanded Detailed Section
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    // Divider Line
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(EchoCardStroke)
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    // Deep Answer
                                    Text(
                                        text = item.detailedAnswer,
                                        fontSize = 13.sp,
                                        color = EchoText,
                                        lineHeight = 19.sp
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    // Bullet Points
                                    item.bulletPoints.forEach { point ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "•",
                                                color = item.category.accentColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = point,
                                                fontSize = 12.sp,
                                                color = EchoText.copy(alpha = 0.9f),
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Action Buttons for this FAQ card
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Copy Section
                                        OutlinedButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val textToCopy = buildString {
                                                    append(item.title).append("\n\n")
                                                    append(item.summary).append("\n\n")
                                                    append(item.detailedAnswer).append("\n\n")
                                                    item.bulletPoints.forEach { append("• ").append(it).append("\n") }
                                                }
                                                clipboard.setPrimaryClip(ClipData.newPlainText(item.title, textToCopy))
                                                Toast.makeText(context, "Copied '${item.title}' to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp), tint = EchoCyan)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Copy", fontSize = 11.sp, color = EchoCyan)
                                        }

                                        // Save to Workspace Note
                                        OutlinedButton(
                                            onClick = {
                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                val noteContent = buildString {
                                                    append("# ").append(item.title).append("\n\n")
                                                    append("**Category:** ").append(item.category.displayName).append("\n\n")
                                                    append(item.detailedAnswer).append("\n\n")
                                                    append("### Key Capabilities:\n")
                                                    item.bulletPoints.forEach { append("- ").append(it).append("\n") }
                                                }
                                                onSaveToWorkspaceNote(item.title, noteContent)
                                                Toast.makeText(context, "Saved to Workspace Note!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(13.dp), tint = EchoGreen)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Save Note", fontSize = 11.sp, color = EchoGreen)
                                        }

                                        // Speak Aloud with Dola TTS
                                        OutlinedButton(
                                            onClick = {
                                                if (isSpeaking) {
                                                    voiceManager.stopSpeaking()
                                                    currentlySpeakingId = null
                                                } else {
                                                    toneGenerator.playChime(SoundEffectType.TAP)
                                                    currentlySpeakingId = item.id
                                                    val speechText = "${item.title}. ${item.summary}. ${item.detailedAnswer}"
                                                    voiceManager.speak(
                                                        text = speechText,
                                                        onFinished = { currentlySpeakingId = null }
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = if (isSpeaking) Color(0xFFFF80AB) else EchoCyan
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (isSpeaking) "Stop" else "Listen",
                                                fontSize = 11.sp,
                                                color = if (isSpeaking) Color(0xFFFF80AB) else EchoCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
