package com.example

import com.example.ui.screens.TheoreticalDossier
import com.example.ui.service.CouncilAvatarManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Polymath Theoretical Dossier modeling and
 * Hybrid Proposal Synthesis generation.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify polymath avatar council profiles`() {
    val profiles = CouncilAvatarManager.getAllProfiles()
    assertEquals(5, profiles.size)
    val athena = profiles.find { it.id.equals("ATHENA", ignoreCase = true) }
    assertNotNull(athena)
    assertEquals(185, athena!!.iqScore)

    val gemini = profiles.find { it.id.equals("GEMINI", ignoreCase = true) }
    assertNotNull(gemini)
    assertEquals(188, gemini!!.iqScore)
  }

  @Test
  fun `verify theoretical dossier structure and coupling`() {
    val dossierA = TheoreticalDossier(
        agent = CouncilAvatarManager.ATHENA,
        title = "Bio-Entangled Topological Quantum Lattice",
        scientificField = "Quantum Mechanics & Bio-Cybernetics",
        hypothesis = "Macro-scale quantum entanglement can be sustained in warm cellular media.",
        formalMechanism = "The system Hamiltonian H = H_0 + g_k Berry phase stabilization.",
        inventionBlueprint = "Blueprint: NEURO-DIRAC TOPOLOGICAL CHIP",
        narrativeHook = "Revealing human cognition as an entangled quantum node."
    )

    val dossierB = TheoreticalDossier(
        agent = CouncilAvatarManager.GEMINI,
        title = "Singularity Kernel & Relativistic Spacetime Resonator",
        scientificField = "Theoretical AI Architectures & Singularity",
        hypothesis = "Consciousness singularity occurs when a self-referential neural model executes non-computable state reductions.",
        formalMechanism = "Stress-energy tensor T_μν couples directly to recursive algorithmic feedback.",
        inventionBlueprint = "Blueprint: CHRONO-SYNAPSE TENSOR CORE",
        narrativeHook = "The super-intelligence creates a micro-gravitational lens."
    )

    assertTrue(dossierA.title.isNotBlank())
    assertTrue(dossierB.title.isNotBlank())
    assertEquals(185, dossierA.agent.iqScore)
    assertEquals(188, dossierB.agent.iqScore)
  }

  @Test
  fun `verify document topic extraction heuristic`() {
    val sampleAnalysis = """
      ### Document Classification & Domain
      **Field:** Bio-Cybernetic Quantum Architecture

      ### Extracted Content & Key Axioms
      * High-frequency dipole coupling suppresses decoherence.

      ### Council Debate Inoculation (Synthesis Topic)
      **Unified Topic:** *Non-Thermal Zero-Point Energy Extraction via Dynamical Casimir Cavity Modulation*
    """.trimIndent()

    val topicLine = sampleAnalysis.lines().firstOrNull { it.contains("Unified Topic:") }
    assertNotNull(topicLine)
    val extracted = topicLine!!.substringAfter(":").replace("*", "").trim()
    assertEquals("Non-Thermal Zero-Point Energy Extraction via Dynamical Casimir Cavity Modulation", extracted)
  }

  @Test
  fun `verify energy resonance 42_6 Hz harmonic node coupling`() {
    val nodeLocked = com.example.ui.components.ResonanceTopicNode(
        id = "test_node_locked",
        title = "Standing Wave Resonator",
        field = "Planetary Acoustics",
        speaker = CouncilAvatarManager.ATHENA,
        frequencyHz = 42.600f,
        targetFrequencyHz = 42.600f,
        relX = 0.5f,
        relY = 0.5f,
        hypothesisSnippet = "100% Harmonic resonance lock achieved"
    )

    val nodeDetuned = com.example.ui.components.ResonanceTopicNode(
        id = "test_node_detuned",
        title = "Dispersed Node",
        field = "Planetary Acoustics",
        speaker = CouncilAvatarManager.GEMINI,
        frequencyHz = 75.000f,
        targetFrequencyHz = 42.600f,
        relX = 0.8f,
        relY = 0.8f,
        hypothesisSnippet = "Out of phase"
    )

    // Node locked at 42.600 Hz must achieve maximum coupling coherence ~1.0
    assertTrue("Locked node coherence should be near 1.0", nodeLocked.couplingCoherence >= 0.99f)
    // Detuned node must have significantly lower coupling coherence
    assertTrue("Detuned node coherence should drop below 0.3", nodeDetuned.couplingCoherence < 0.3f)
  }
}



