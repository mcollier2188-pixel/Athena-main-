package com.example.council.engine

import com.example.council.model.CouncilDomain
import com.example.council.model.CouncilProfile
import com.example.council.model.CouncilRegistry
import com.example.council.model.ExplainableSelectionScore
import java.util.Locale

/**
 * Selection engine for the ATHENA Genius Council under v3.3 specifications.
 * Supports manual tripartite role validation and explainable automatic selection.
 */
object CouncilSelectorEngine {

    /**
     * Validates that three distinct profiles are assigned across the three mandatory roles.
     */
    fun validateTripartiteRoster(
        domainLeadCode: String,
        comparatorCode: String,
        verificationCode: String
    ): Result<Triple<CouncilProfile, CouncilProfile, CouncilProfile>> {
        val lead = CouncilRegistry.getByCode(domainLeadCode)
            ?: return Result.failure(IllegalArgumentException("Domain Lead [$domainLeadCode] not found in registry."))
        val comp = CouncilRegistry.getByCode(comparatorCode)
            ?: return Result.failure(IllegalArgumentException("Independent Comparator [$comparatorCode] not found in registry."))
        val verif = CouncilRegistry.getByCode(verificationCode)
            ?: return Result.failure(IllegalArgumentException("Verification Specialist [$verificationCode] not found in registry."))

        if (lead.code == comp.code || lead.code == verif.code || comp.code == verif.code) {
            return Result.failure(IllegalArgumentException("Council members must be 3 distinct profiles for tripartite balance."))
        }

        return Result.success(Triple(lead, comp, verif))
    }

    /**
     * Executes explainable automatic selection according to approved v3.3 scoring weights:
     * - Domain Relevance: 40%
     * - Paradigm Diversity / Contrast: 30%
     * - Evidence Rigor / Falsification Weight: 20%
     * - Historical Baseline Calibration: 10%
     */
    fun selectExplainableCouncil(
        userInquiry: String
    ): Triple<CouncilProfile, CouncilProfile, CouncilProfile> {
        val scoredList = scoreAllProfiles(userInquiry)

        // 1. Domain Lead: Highest domain relevance score
        val lead = scoredList.maxByOrNull { it.domainRelevanceScore }?.let { CouncilRegistry.getByCode(it.profileCode) }
            ?: CouncilRegistry.getByCode("G42")!!

        // 2. Independent Comparator: Highest paradigm diversity score among contrasting domains
        val comparatorCandidates = scoredList.filter { it.profileCode != lead.code }
        val comp = comparatorCandidates.maxByOrNull { it.paradigmDiversityScore }?.let { CouncilRegistry.getByCode(it.profileCode) }
            ?: CouncilRegistry.getByCode("G35")!!

        // 3. Verification Specialist: Highest evidence rigor & falsification score
        val verificationCandidates = scoredList.filter { it.profileCode != lead.code && it.profileCode != comp.code }
        val verif = verificationCandidates.maxByOrNull { it.evidenceRigorScore }?.let { CouncilRegistry.getByCode(it.profileCode) }
            ?: CouncilRegistry.getByCode("G04")!!

        return Triple(lead, comp, verif)
    }

    /**
     * Scores all 42 profiles with transparent breakdown for auditability.
     */
    fun scoreAllProfiles(userInquiry: String): List<ExplainableSelectionScore> {
        val lowerInquiry = userInquiry.lowercase(Locale.ROOT)

        return CouncilRegistry.profiles.map { profile ->
            val domainRelevance = computeDomainRelevance(profile, lowerInquiry)
            val paradigmDiversity = computeParadigmDiversity(profile, lowerInquiry)
            val evidenceRigor = computeEvidenceRigor(profile)
            val baselineCalibration = computeBaselineCalibration(profile)

            val weightedTotal = (domainRelevance * 0.40) +
                    (paradigmDiversity * 0.30) +
                    (evidenceRigor * 0.20) +
                    (baselineCalibration * 0.10)

            val explanation = buildString {
                append("Domain Match: ${"%.1f".format(domainRelevance)} | ")
                append("Paradigm Contrast: ${"%.1f".format(paradigmDiversity)} | ")
                append("Evidence Weight: ${"%.1f".format(evidenceRigor)} | ")
                append("Baseline Calibration: ${"%.1f".format(baselineCalibration)}")
            }

            ExplainableSelectionScore(
                profileCode = profile.code,
                profileName = profile.name,
                domainRelevanceScore = domainRelevance,
                paradigmDiversityScore = paradigmDiversity,
                evidenceRigorScore = evidenceRigor,
                baselineCalibrationScore = baselineCalibration,
                totalWeightedScore = weightedTotal,
                selectionRationale = explanation
            )
        }.sortedByDescending { it.totalWeightedScore }
    }

    private fun computeDomainRelevance(profile: CouncilProfile, query: String): Double {
        var score = 30.0
        val domainTerms = profile.primaryDomain.displayName.lowercase() + " " + profile.coreParadigm.lowercase()

        // Acoustic / Bedrock keywords
        if (query.contains("42.6") || query.contains("resonance") || query.contains("bedrock") || query.contains("hilsea")) {
            if (profile.code == "G42" || profile.code == "G37" || profile.code == "G35" || profile.code == "G01") score += 50.0
        }
        if (query.contains("wave") || query.contains("standing wave") || query.contains("frequenc")) {
            if (profile.primaryDomain == CouncilDomain.WAVE_MECHANICS || profile.primaryDomain == CouncilDomain.ACOUSTIC_TELEMETRY) score += 40.0
        }
        if (query.contains("tesla") || query.contains("electric") || query.contains("transmitt")) {
            if (profile.primaryDomain == CouncilDomain.ELECTROMAGNETISM) score += 45.0
        }
        if (query.contains("stone") || query.contains("megalith") || query.contains("chamber")) {
            if (profile.primaryDomain == CouncilDomain.MEGALITHIC_ACOUSTICS || profile.primaryDomain == CouncilDomain.GEOPHYSICS_SEISMOLOGY) score += 45.0
        }

        // Direct keyword hits
        query.split(" ").filter { it.length > 3 }.forEach { word ->
            if (domainTerms.contains(word)) score += 8.0
        }

        return score.coerceIn(10.0, 100.0)
    }

    private fun computeParadigmDiversity(profile: CouncilProfile, query: String): Double {
        var score = 40.0
        // Diversity favors analytical derivations and differing viewpoints
        if (profile.primaryDomain == CouncilDomain.WAVE_MECHANICS) score += 30.0
        if (profile.primaryDomain == CouncilDomain.CLASSICAL_HARMONICS) score += 25.0
        if (profile.primaryDomain == CouncilDomain.INFORMATION_THEORY) score += 20.0
        if (profile.primaryEquations.isNotEmpty()) score += 15.0
        return score.coerceIn(10.0, 100.0)
    }

    private fun computeEvidenceRigor(profile: CouncilProfile): Double {
        return when {
            profile.evidencePreference.contains("Class A") -> 95.0
            profile.evidencePreference.contains("Class B") -> 85.0
            profile.evidencePreference.contains("Class C") -> 70.0
            else -> 50.0
        }
    }

    private fun computeBaselineCalibration(profile: CouncilProfile): Double {
        // Hilsea 1986, Tesla 1899, Rayleigh 1877, Pythagoras are gold-standard baselines
        return when (profile.code) {
            "G42" -> 100.0 // Hilsea baseline
            "G37" -> 95.0  // Tesla 43 Hz
            "G35" -> 90.0  // Rayleigh sound theory
            "G04" -> 85.0  // Pythagorean harmonics
            "G01" -> 80.0  // Stonehenge megalithic acoustics
            else -> 65.0
        }
    }
}
