package com.example.council.engine

import com.example.council.model.AdversarialAuditReport
import com.example.council.model.BlindPassOutput
import com.example.council.model.CouncilSession
import com.example.council.model.CouncilState
import com.example.council.model.EvidenceClass

/**
 * Adversarial scientific review and diagnostic integrity engine.
 * Ensures zero-trust evaluation of claims, evidence class verification,
 * and falsification enforcement.
 */
object AdversarialAuditEngine {

    /**
     * Executes diagnostic audit against blind outputs and disagreement records.
     */
    fun performAdversarialAudit(session: CouncilSession): AdversarialAuditReport {
        val triggers = mutableListOf<String>()
        var evidenceScore = 100.0

        // 1. Audit Falsifiability standard
        var allFalsifiable = true
        for (output in session.blindOutputs) {
            if (output.falsificationCriterion.isBlank() || output.falsificationCriterion.length < 15) {
                triggers.add("FALSIFICATION_ABSENCE: [${output.profileCode}] lacks concrete refutation criteria.")
                allFalsifiable = false
                evidenceScore -= 20.0
            }
        }

        // 2. Audit Evidence Class Calibration
        val hasHighGradeEvidence = session.blindOutputs.any {
            it.evidenceClass == EvidenceClass.CLASS_A || it.evidenceClass == EvidenceClass.CLASS_B
        }
        if (!hasHighGradeEvidence) {
            triggers.add("EVIDENCE_DEFICIT: Session contains only Class C/D evidence without analytical proof or measurement.")
            evidenceScore -= 15.0
        }

        // 3. Audit Disagreement & Homogenization
        if (session.disagreements.isEmpty() && session.blindOutputs.size >= 2) {
            triggers.add("HOMOGENIZATION_WARNING: Zero tension recorded across differing scientific paradigms.")
            evidenceScore -= 10.0
        }

        // 4. Audit Uncertainty Calibration
        val zeroUncertaintyCount = session.blindOutputs.count { it.uncertaintyScore <= 0.0 }
        if (zeroUncertaintyCount == session.blindOutputs.size && session.blindOutputs.isNotEmpty()) {
            triggers.add("UNCERTAINTY_ANOMALY: Dogmatic certainty (0.0) asserted across all independent outputs.")
            evidenceScore -= 15.0
        }

        val passed = triggers.none { it.startsWith("FALSIFICATION_ABSENCE") } && evidenceScore >= 60.0

        val summary = if (passed) {
            "Diagnostic integrity verified. Tripartite falsification standards met with ${"%.1f".format(evidenceScore)}% provenance rating."
        } else {
            "Adversarial review identified integrity flags: ${triggers.joinToString("; ")}"
        }

        return AdversarialAuditReport(
            isIntegrityIntact = passed,
            evidenceAuditScore = evidenceScore.coerceIn(0.0, 100.0),
            falsifiabilityVerified = allFalsifiable,
            diagnosticTriggers = triggers,
            auditSummary = summary
        )
    }

    /**
     * Validates and advances state machine transitions.
     */
    fun advanceState(session: CouncilSession, targetState: CouncilState): Result<CouncilSession> {
        when (targetState) {
            CouncilState.DRAFT -> return Result.success(session.copy(state = CouncilState.DRAFT))

            CouncilState.COUNCIL_REVIEWED -> {
                if (session.blindOutputs.isEmpty()) {
                    return Result.failure(IllegalStateException("Cannot advance to COUNCIL_REVIEWED without blind outputs."))
                }
                return Result.success(session.copy(state = CouncilState.COUNCIL_REVIEWED))
            }

            CouncilState.EVIDENCE_AUDITED -> {
                if (session.state != CouncilState.COUNCIL_REVIEWED) {
                    return Result.failure(IllegalStateException("Session must be COUNCIL_REVIEWED before EVIDENCE_AUDITED."))
                }
                val audit = performAdversarialAudit(session)
                return Result.success(session.copy(auditReport = audit, state = CouncilState.EVIDENCE_AUDITED))
            }

            CouncilState.APPROVED -> {
                if (session.state != CouncilState.EVIDENCE_AUDITED) {
                    return Result.failure(IllegalStateException("Session must be EVIDENCE_AUDITED before APPROVED."))
                }
                val audit = session.auditReport ?: performAdversarialAudit(session)
                if (!audit.isIntegrityIntact) {
                    return Result.failure(IllegalStateException("Cannot approve session failing adversarial integrity."))
                }
                return Result.success(session.copy(auditReport = audit, state = CouncilState.APPROVED))
            }

            CouncilState.REJECTED -> {
                return Result.success(session.copy(state = CouncilState.REJECTED))
            }
        }
    }
}
