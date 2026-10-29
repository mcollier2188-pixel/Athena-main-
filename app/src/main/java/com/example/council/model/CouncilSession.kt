package com.example.council.model

import java.security.MessageDigest

/**
 * Formal scientific Evidence Classes for Council claims.
 */
enum class EvidenceClass(val tag: String, val levelName: String, val description: String) {
    CLASS_A("Class A", "Empirical Measurement", "Instrumental measurement, calibrated seismic telemetry, sensor logs."),
    CLASS_B("Class B", "Mathematical Proof", "Rigorous analytical derivation, conservation laws, wave equations."),
    CLASS_C("Class C", "Historical Canon", "Direct historical observation, field journal records, archival reports."),
    CLASS_D("Class D", "Theoretical Conjecture", "Hypothesis, unverified extrapolation, exploratory heuristic.")
}

/**
 * Strict 5-stage lifecycle state machine for Council sessions.
 */
enum class CouncilState(val displayName: String) {
    DRAFT("Draft"),
    COUNCIL_REVIEWED("Council Reviewed"),
    EVIDENCE_AUDITED("Evidence Audited"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

/**
 * Independent, blind first-pass output produced by a single Council member
 * without premature cross-pollination from other members.
 */
data class BlindPassOutput(
    val profileCode: String,
    val profileName: String,
    val role: CouncilRole,
    val hypothesis: String,
    val theoreticalRationale: String,
    val evidenceClass: EvidenceClass,
    val uncertaintyScore: Double, // 0.0 (certain) to 1.0 (highly uncertain)
    val falsificationCriterion: String // Condition that would prove this wrong
)

/**
 * Preservation of scientific tension and disagreement.
 */
data class DisagreementRecord(
    val topic: String,
    val perspectiveA: String,
    val perspectiveB: String,
    val tensionClass: String // "PARADIGM_DIVERGENCE", "EVIDENCE_DISPUTE", "EQUATION_BOUNDARY"
)

/**
 * Diagnostic integrity and adversarial review report.
 */
data class AdversarialAuditReport(
    val isIntegrityIntact: Boolean,
    val evidenceAuditScore: Double, // 0.0 - 100.0
    val falsifiabilityVerified: Boolean,
    val diagnosticTriggers: List<String>,
    val auditSummary: String
)

/**
 * Explainable v3.3 selection score breakdown.
 */
data class ExplainableSelectionScore(
    val profileCode: String,
    val profileName: String,
    val domainRelevanceScore: Double, // 40%
    val paradigmDiversityScore: Double, // 30%
    val evidenceRigorScore: Double, // 20%
    val baselineCalibrationScore: Double, // 10%
    val totalWeightedScore: Double,
    val selectionRationale: String
)

/**
 * Advanced Council Deliberation Protocols.
 */
enum class CouncilProtocol(val displayName: String, val badge: String, val summary: String) {
    SOCRATIC_CROSS_EXAM(
        displayName = "Socratic Cross-Exam",
        badge = "DIALECTIC",
        summary = "Structured cross-examination where personas directly question each other's assumptions."
    ),
    FORMAL_TOURNAMENT(
        displayName = "Debate Tournament",
        badge = "TOURNAMENT",
        summary = "Affirmative vs Negative rounds with rigorous criteria-based argument scoring."
    ),
    DISSERTATION_DEFENSE(
        displayName = "Dissertation Defense",
        badge = "PEER-REVIEW",
        summary = "Academic thesis defense with hostile committee questioning and jury grade."
    ),
    CONSENSUS_PANEL(
        displayName = "Tripartite Workbench",
        badge = "BLIND PASS",
        summary = "Independent blind first-pass outputs, preserved disagreements, and traceable synthesis."
    )
}

/**
 * Visual or physical exhibit submitted to the Council for examination.
 */
data class CouncilExhibit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val imageUri: String? = null,
    val technicalDomain: String = "Acoustic Resonance & Geology"
)

/**
 * Direct conversational exchange between council personas in dialectic modes.
 */
data class CrossExamExchange(
    val roundNumber: Int,
    val speakerCode: String,
    val speakerName: String,
    val targetCode: String,
    val targetName: String,
    val challengeOrQuestion: String,
    val responseOrDefense: String,
    val scoreDelta: Int = 0
)

/**
 * Epistemic consensus vote by a Council member.
 */
data class ConsensusVote(
    val profileCode: String,
    val profileName: String,
    val agreementPercent: Int, // 0 - 100%
    val epistemicConfidence: Int, // 0 - 100%
    val ideologicalAlignment: String,
    val verdict: String // "ACCEPT", "REVISE", "REJECT", "VALIDATE"
)

/**
 * Immutable Council Session capturing the full v3.3 deliberation cycle.
 */
data class CouncilSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val packageVersion: String = "ATHENA-GC-UPDATE-3.3-PROPOSED",
    val timestamp: Long = System.currentTimeMillis(),
    val userInquiry: String,
    val domainLeadCode: String,
    val comparatorCode: String,
    val verificationCode: String,
    val protocol: CouncilProtocol = CouncilProtocol.CONSENSUS_PANEL,
    val exhibit: CouncilExhibit? = null,
    val crossExamRounds: List<CrossExamExchange> = emptyList(),
    val consensusMatrix: List<ConsensusVote> = emptyList(),
    val blindOutputs: List<BlindPassOutput> = emptyList(),
    val disagreements: List<DisagreementRecord> = emptyList(),
    val athenaSynthesis: String = "",
    val auditReport: AdversarialAuditReport? = null,
    val state: CouncilState = CouncilState.DRAFT,
    val sessionSha256Hash: String = ""
) {
    companion object {
        fun computeSha256(data: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
                hash.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                "0000000000000000000000000000000000000000000000000000000000000000"
            }
        }
    }
}
