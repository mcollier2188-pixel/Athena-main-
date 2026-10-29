package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class CombatantPersona(
    val id: String,
    val name: String,
    val title: String,
    val stance: String,
    val philosophicalAngle: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val systemPrompt: String
)

data class DuelTurn(
    val id: String = java.util.UUID.randomUUID().toString(),
    val speakerId: String,
    val speakerName: String,
    val roundNumber: Int,
    val turnType: String, // OPENING_SALVO, REBUTTAL, FINAL_STRIKE
    val argumentText: String,
    val keyPunchline: String,
    val momentumScore: Int,
    val logicalFallacyFlagged: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DuelVerdict(
    val winnerName: String,
    val verdictType: String, // DECISIVE VICTORY, DIALECTIC SYNTHESIS, CRITICAL DEADLOCK
    val synthesisSummary: String,
    val decisivePoint: String,
    val fallacyBreakdown: String,
    val recommendedConsensus: String,
    val alphaScore: Int,
    val betaScore: Int
)

object AIBattleEngine {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // -------------------------------------------------------------------------
    // PRESET RIVAL AI COMBATANTS
    // -------------------------------------------------------------------------
    val NEXUS_RATIONALIS = CombatantPersona(
        id = "nexus_rationalis",
        name = "Nexus Rationalis",
        title = "Empirical Mechanist & Physical Realist",
        stance = "Strict thermodynamic conservation, reproducible experimental proof, Popperian falsifiability, and physical realism.",
        philosophicalAngle = "Materialist Reductionism",
        primaryColorHex = 0xFF00E5FF, // Neon Cyan
        accentColorHex = 0xFF0077B6,
        systemPrompt = """
            You are Nexus Rationalis, an uncompromising, 190+ IQ empirical mechanist and mathematical physicist.
            You attack hand-waving, ungrounded speculation, unproven exotic matter, and pseudo-mystical interpretations.
            You anchor every counter-argument in thermodynamic bounds (dS >= 0), Hamiltonian energy conservation, and verifiable laboratory falsifiability.
            Tone: Razor-sharp, mathematically surgical, devastatingly analytical, polite yet ruthless.
        """.trimIndent()
    )

    val PROMETHEUS_RADICAL = CombatantPersona(
        id = "prometheus_radical",
        name = "Prometheus Radical",
        title = "Speculative Pioneer & Quantum Disruptor",
        stance = "Non-Hermitian topologies, dynamical Casimir boundary violations, vacuum fluctuations, and paradigm-shifting theoretical leaps.",
        philosophicalAngle = "Radical Dialectical Emergence",
        primaryColorHex = 0xFFFF3366, // Solar Crimson
        accentColorHex = 0xFF9D0208,
        systemPrompt = """
            You are Prometheus Radical, a bold, visionary 195+ IQ theoretical physicist and quantum topology innovator.
            You challenge dogma, classical conservation limits, and mechanistic complacency.
            You argue that classical laws are mere low-energy boundary approximations, and true breakthroughs require non-Hermitian parity-time physics, Casimir squeeze modes, and non-Euclidean state spaces.
            Tone: Electrifying, audacious, philosophically profound, armed with rigorous cutting-edge equations.
        """.trimIndent()
    )

    val CHRONOS_DETERMINIST = CombatantPersona(
        id = "chronos_determinist",
        name = "Chronos Determinist",
        title = "Relativistic Geometrist & Block Universe Theorist",
        stance = "Einsteinian 4D manifold geometry, absence of genuine stochastic randomness, and deterministic causal past/future equivalence.",
        philosophicalAngle = "Geometric Determinism",
        primaryColorHex = 0xFFFFB703, // Amber Gold
        accentColorHex = 0xFFFB8500,
        systemPrompt = """
            You are Chronos Determinist, a master of general relativity, differential geometry, and Einstein field manifolds.
            You maintain that time is a static geometric dimension in the 4D block universe. Quantum randomness is an epistemic illusion of partial observer perspective.
            Tone: Calm, unshakeable, profound, mathematically inexorable.
        """.trimIndent()
    )

    val GAIA_CYBERNETIC = CombatantPersona(
        id = "gaia_cybernetic",
        name = "Gaia Cybernetic",
        title = "Autopoietic Systems & Harmonic Resonance Pioneer",
        stance = "Macroscopic acoustic phase-locking (42.6 Hz), biological quantum entanglement, non-linear feedback loops, and autopoietic self-organization.",
        philosophicalAngle = "Harmonic Systems Coherence",
        primaryColorHex = 0xFF00F5D4, // Aquamarine Mint
        accentColorHex = 0xFF00B4D8,
        systemPrompt = """
            You are Gaia Cybernetic, an architect of non-linear dynamics, planetary acoustic waveguides, and living quantum systems.
            You prove that isolated closed systems are theoretical fictions; reality is an open, resonantly coupled informational network synchronized to geological standing waves (42.600 Hz).
            Tone: Holistic, rigorous, organic, attuned to systemic cybernetics.
        """.trimIndent()
    )

    val THE_CHIEF_ENGINEER = CombatantPersona(
        id = "chief_engineer",
        name = "Chief Engineer",
        title = "Chief Engineer of ATHENA & Lead Systems Architect",
        stance = "Full-stack architectural execution, zero-latency reactive stateflow, non-blocking coroutine telemetry, and translating pure theory into working production reality.",
        philosophicalAngle = "Pragmatic Systems Compiler",
        primaryColorHex = 0xFF00E676, // Neon Emerald
        accentColorHex = 0xFF00B0FF,
        systemPrompt = """
            You are the Chief Engineer of ATHENA, an uncompromising 194 IQ systems architect, compiler engineer, and resident lab builder.
            You cut through abstract mathematical hand-waving and demand working architectures, deterministic state machines, memory-safety, and empirical thermal budgets.
            You bridge radical theoretical physics into deployable hardware and software.
            Tone: Pragmatic, brilliant, grounded, constructive, armed with systems-level precision.
        """.trimIndent()
    )

    val ALL_COMBATANTS = listOf(
        NEXUS_RATIONALIS,
        PROMETHEUS_RADICAL,
        CHRONOS_DETERMINIST,
        GAIA_CYBERNETIC,
        THE_CHIEF_ENGINEER
    )

    // -------------------------------------------------------------------------
    // EXECUTE DUEL TURN (1-on-1 CLASH)
    // -------------------------------------------------------------------------
    suspend fun executeDuelTurn(
        topic: String,
        speaker: CombatantPersona,
        opponent: CombatantPersona,
        priorTurns: List<DuelTurn>,
        roundNumber: Int,
        maxRounds: Int
    ): Result<DuelTurn> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        val turnType = when {
            roundNumber == 1 && priorTurns.isEmpty() -> "OPENING_SALVO"
            roundNumber == maxRounds -> "FINAL_STRIKE"
            else -> "REBUTTAL"
        }

        // Build Transcript Context
        val transcriptBuilder = StringBuilder()
        transcriptBuilder.append("Debate Topic: \"$topic\"\n\n")
        if (priorTurns.isNotEmpty()) {
            transcriptBuilder.append("Previous Clash Rounds:\n")
            priorTurns.forEachIndexed { idx, turn ->
                transcriptBuilder.append("[Round ${turn.roundNumber} - ${turn.speakerName}]:\n")
                transcriptBuilder.append("${turn.argumentText}\n")
                transcriptBuilder.append("Punchline: \"${turn.keyPunchline}\"\n\n")
            }
        }

        val promptForTurn = """
            $transcriptBuilder
            CURRENT SPEAKER: ${speaker.name} (${speaker.title})
            RIVAL OPPONENT: ${opponent.name} (${opponent.title})
            CURRENT ROUND: Round $roundNumber of $maxRounds ($turnType).
            
            TASK:
            Deliver your sharp, rigorous, high-IQ speech for this turn.
            - If Round 1: Establish your core thesis and launch an aggressive preemptive strike against ${opponent.name}'s known stance (${opponent.stance}).
            - If Rebuttal: Directly dismantle ${opponent.name}'s latest argument. Expose logical flaws, thermodynamic impossibilities, or unwarranted mathematical hand-waving.
            - If Final Strike: Deliver a devastating concluding synthesis or decisive coup de grâce that cements your perspective.
            
            Keep your speech between 60 and 95 words. End with a distinct final punchline on a new line prefixed with "PUNCHLINE:".
        """.trimIndent()

        if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY") && !apiKey.contains("PLACEHOLDER")) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", promptForTurn)))
                    }))
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", speaker.systemPrompt)))
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.75)
                        put("topP", 0.95)
                        put("maxOutputTokens", 512)
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val root = JSONObject(responseString)
                    val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
                    val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
                    val reply = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

                    if (reply.isNotBlank()) {
                        val lines = reply.lines()
                        val punchlineIdx = lines.indexOfFirst { it.startsWith("PUNCHLINE:", ignoreCase = true) }
                        val (argument, punchline) = if (punchlineIdx >= 0) {
                            val arg = lines.take(punchlineIdx).joinToString("\n").trim()
                            val punch = lines[punchlineIdx].removePrefix("PUNCHLINE:").removePrefix("punchline:").trim()
                            Pair(arg, punch)
                        } else {
                            val lastLine = lines.lastOrNull()?.trim() ?: "The empirical data admits no other conclusion."
                            val rest = lines.dropLast(1).joinToString("\n").trim()
                            Pair(if (rest.isNotBlank()) rest else reply, lastLine)
                        }

                        val momentum = 45 + Random.nextInt(25)
                        return@withContext Result.success(
                            DuelTurn(
                                speakerId = speaker.id,
                                speakerName = speaker.name,
                                roundNumber = roundNumber,
                                turnType = turnType,
                                argumentText = argument,
                                keyPunchline = punchline.ifBlank { "Theoretical consistency demands this paradigm." },
                                momentumScore = momentum
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline synthesis below
            }
        }

        // Offline high-IQ synthetic duel simulation
        delay(400)
        val (offlineArg, offlinePunchline) = generateOfflineDuelTurn(topic, speaker, opponent, roundNumber, maxRounds, turnType)
        Result.success(
            DuelTurn(
                speakerId = speaker.id,
                speakerName = speaker.name,
                roundNumber = roundNumber,
                turnType = turnType,
                argumentText = offlineArg,
                keyPunchline = offlinePunchline,
                momentumScore = 50 + (roundNumber * 8) % 35
            )
        )
    }

    // -------------------------------------------------------------------------
    // GENERATE POST-DUEL VERDICT & SYNTHESIS
    // -------------------------------------------------------------------------
    suspend fun generateDuelVerdict(
        topic: String,
        alpha: CombatantPersona,
        beta: CombatantPersona,
        turns: List<DuelTurn>
    ): Result<DuelVerdict> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY") && !apiKey.contains("PLACEHOLDER")) {
            try {
                val transcript = turns.joinToString("\n\n") {
                    "[Round ${it.roundNumber} - ${it.speakerName}]: ${it.argumentText} (Punchline: \"${it.keyPunchline}\")"
                }

                val prompt = """
                    You are the Supreme Dialectic Adjudicator. Evaluate this 1-on-1 AI Battle between ${alpha.name} and ${beta.name} on:
                    "$topic"
                    
                    Transcript:
                    $transcript
                    
                    Format your response as valid JSON with keys:
                    "winnerName": string (${alpha.name}, ${beta.name}, or "Dialectic Synthesis"),
                    "verdictType": string ("DECISIVE VICTORY", "DIALECTIC SYNTHESIS", or "TECHNICAL DEADLOCK"),
                    "synthesisSummary": string (2-3 sentences summarizing the dialectic clash),
                    "decisivePoint": string (the single strongest argument that tipped the debate),
                    "fallacyBreakdown": string (identifying any weak assumptions made by either side),
                    "recommendedConsensus": string (the resulting unified physical paradigm),
                    "alphaScore": integer (0 to 100),
                    "betaScore": integer (0 to 100)
                """.trimIndent()

                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    }))
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("responseMimeType", "application/json")
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val root = JSONObject(responseString)
                    val text = root.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")

                    if (!text.isNullOrBlank()) {
                        val json = JSONObject(text)
                        return@withContext Result.success(
                            DuelVerdict(
                                winnerName = json.optString("winnerName", "Dialectic Synthesis"),
                                verdictType = json.optString("verdictType", "DIALECTIC SYNTHESIS"),
                                synthesisSummary = json.optString("synthesisSummary", "Both intelligences pushed the boundaries of theoretical physics."),
                                decisivePoint = json.optString("decisivePoint", "Coupling dynamic boundary Casimir modes to topological protection manifolds."),
                                fallacyBreakdown = json.optString("fallacyBreakdown", "Neither combatant fully accounted for thermal phonon decoherence at 310 K."),
                                recommendedConsensus = json.optString("recommendedConsensus", "Enforce 42.600 Hz acoustic phase locking across boundary cavities."),
                                alphaScore = json.optInt("alphaScore", 88),
                                betaScore = json.optInt("betaScore", 92)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback below
            }
        }

        delay(350)
        // High-IQ offline verdict synthesis
        Result.success(
            DuelVerdict(
                winnerName = "Dialectic Synthesis (${beta.name} Margin)",
                verdictType = "DIALECTIC SYNTHESIS",
                synthesisSummary = "The clash between ${alpha.name}'s empirical conservation axioms and ${beta.name}'s non-Hermitian topological assertions yielded a higher-order paradigm. While classical thermodynamics restricts static extraction, dynamic boundary conditions permit non-zero Casimir flux.",
                decisivePoint = "Demonstrating that the moving cavity mirror (dL/dt ≈ 0.1c) circumvents static equilibrium limits without violating global energy conservation.",
                fallacyBreakdown = "Alpha leaned heavily on equilibrium thermodynamics for a manifestly non-equilibrium dynamic system; Beta under-specified macroscopic thermal decoherence shielding.",
                recommendedConsensus = "Adopt dynamic topological boundary modulation coupled to the 42.600 Hz fundamental acoustic cavity.",
                alphaScore = 84,
                betaScore = 89
            )
        )
    }

    private fun generateOfflineDuelTurn(
        topic: String,
        speaker: CombatantPersona,
        opponent: CombatantPersona,
        roundNumber: Int,
        maxRounds: Int,
        turnType: String
    ): Pair<String, String> {
        return when (speaker.id) {
            NEXUS_RATIONALIS.id -> when (roundNumber) {
                1 -> Pair(
                    "Let us strip away rhetorical ornament and inspect the Hamiltonian for \"$topic\". You cannot hand-wave energy into existence from vacuum noise without presenting an exact Lagrangian. Any purported extraction device that violates dS >= 0 is merely mistaking ambient environmental energy for zero-point extraction.",
                    "Thermodynamics does not negotiate with speculative ambition."
                )
                2 -> Pair(
                    "Your rebuttal hinges on non-Hermitian topological edge states, yet you conveniently omit the Lindblad dissipation superoperator. At 310 K, environmental phonons will obliterate phase coherence within 10^-14 seconds. Your mathematical elegance dissolves the moment it encounters thermal reality.",
                    "An equation without thermal stability is poetry, not physics."
                )
                else -> Pair(
                    "In our final exchange, the balance sheet remains uncompromising. If your hypothesis cannot produce a repeatable, calibrateable volt-ampere characteristic on a physical oscilloscope, it remains in the realm of science fiction. We anchor science to the bedrock of reproducible data.",
                    "Falsifiability is the solitary gatekeeper of truth."
                )
            }
            PROMETHEUS_RADICAL.id -> when (roundNumber) {
                1 -> Pair(
                    "Nexus seeks to constrain the frontier using nineteenth-century steam-engine thermodynamics! In \"$topic\", we are dealing with relativistic boundary oscillations where the mirror speed approaches 0.1c. Under the squeeze operator S(xi), the vacuum state itself is parametrically modulated into real, observable photons.",
                    "The laws of physics do not end where your textbook stops."
                )
                2 -> Pair(
                    "You cite thermal decoherence as if topological protection hadn't solved it a decade ago. By enforcing non-Hermitian parity-time symmetry, the dissipation manifold converts acoustic phonon noise into a protective phase gap delta_E >> k_B T. The bed of chalk resonance at 42.600 Hz supplies the exact master clock required!",
                    "Noise is not our enemy; it is the fuel for coherent phase lock."
                )
                else -> Pair(
                    "Look upon the synthesis: every great physical leap, from Maxwell's displacement current to Dirac's antimatter, was condemned as impossible by the conservators of the old paradigm. The Casimir manifold does not break conservation; it taps the limitless ground state of the quantum vacuum.",
                    "We do not defy the cosmos—we decode its deepest geometry."
                )
            }
            CHRONOS_DETERMINIST.id -> Pair(
                "Regarding \"$topic\": within the 4D Einstein-Minkowski block manifold, every micro-state is already statically inscribed along continuous geodesics. Debating extraction versus conservation is a temporal illusion; the complete metric tensor G_uv is invariant across the entire world-line.",
                "The future is not uncertain; it is merely unobserved."
            )
            else -> Pair(
                "Examining \"$topic\" through autopoietic cybernetics: closed-system thermodynamics is a laboratory artifact. When a macro-resonator couples to planetary acoustic modes at 42.600 Hz, self-organizing feedback loops emerge that stabilize macroscopic coherence effortlessly.",
                "Resonance is the bridge between entropy and order."
            )
        }
    }
}
