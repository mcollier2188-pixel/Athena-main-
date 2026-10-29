package com.example.council.model

/**
 * Canon verification status enforcing zero-assumption provenance.
 * Fields marked CANON_REQUIRED remain unresolved until source-backed ledger mappings are verified.
 */
enum class CanonStatus {
    VERIFIED,
    CANON_REQUIRED,
    UNRESOLVED
}

/**
 * Scientific domain classification for the 42 Geniuses Council (G01 - G42).
 */
enum class CouncilDomain(val displayName: String, val focusArea: String) {
    MEGALITHIC_ACOUSTICS("Megalithic Acoustics", "Stonehenge, resonance cavities, prehistoric sound conduits"),
    CLASSICAL_HARMONICS("Classical Harmonics", "Pythagorean ratios, monochord tuning, interval physics"),
    GEOPHYSICS_SEISMOLOGY("Geophysics & Seismology", "Bedrock wave velocity, tectonic fault resonance, Solent strata"),
    ELECTROMAGNETISM("Electromagnetism & Waves", "Tesla Colorado Springs telemetry, terrestrial standing waves"),
    WAVE_MECHANICS("Mathematical Wave Mechanics", "Lord Rayleigh equations, Q-factor matrices, standing waves"),
    ACOUSTIC_TELEMETRY("Acoustic Telemetry", "1986 Hilsea laboratory calibration, 42.600 Hz harmonic detection"),
    INFORMATION_THEORY("Information Physics", "Signal-to-noise ratio, entropy, wave packet preservation"),
    EXPERIMENTAL_METROLOGY("Experimental Metrology", "Interferometry, calibration standards, dimensional analysis")
}

/**
 * Tripartite Council assignment roles required for valid v3.3 deliberation.
 */
enum class CouncilRole(val title: String, val responsibility: String) {
    DOMAIN_LEAD(
        "Domain Lead",
        "Primary theoretical framework, foundational hypothesis, and core disciplinary models."
    ),
    INDEPENDENT_COMPARATOR(
        "Independent Comparator",
        "Contrasting paradigm, cross-domain stress testing, and alternate hypothesis formulation."
    ),
    VERIFICATION_SPECIALIST(
        "Verification / Translation Specialist",
        "Falsification protocol, empirical evidence grading, and dimensional consistency checks."
    )
}

/**
 * Council Profile data model for G01–G42 scientific profiles.
 * Preserves strict isolation and marks identity mappings as CANON_REQUIRED.
 */
data class CouncilProfile(
    val code: String, // e.g. "G01", "G02", ... "G42"
    val name: String,
    val historicalEra: String,
    val primaryDomain: CouncilDomain,
    val coreParadigm: String,
    val primaryEquations: List<String>,
    val epistemicBias: String,
    val falsificationStandard: String,
    val evidencePreference: String,
    val originalMappingStatus: CanonStatus = CanonStatus.CANON_REQUIRED,
    val cloneLineageStatus: CanonStatus = CanonStatus.CANON_REQUIRED,
    val archiveChapterStatus: CanonStatus = CanonStatus.CANON_REQUIRED,
    val canonNotes: String = "CANON_REQUIRED: Cryptographic identity and chapter ledger mapping pending source confirmation."
) {
    val displayTitle: String
        get() = "[$code] $name"
}
