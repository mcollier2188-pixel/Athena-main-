package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AIBattleEngine
import com.example.ai.CombatantPersona
import com.example.ai.DuelTurn
import com.example.ai.DuelVerdict
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AIBattleArenaView(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    workspaceViewModel: WorkspaceViewModel? = null,
    initialDebateTopic: String? = null,
    onOpenDocumentScanner: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Combatant selection
    var combatantAlpha by remember { mutableStateOf(AIBattleEngine.NEXUS_RATIONALIS) }
    var combatantBeta by remember { mutableStateOf(AIBattleEngine.PROMETHEUS_RADICAL) }

    // Debate Topic
    var duelTopic by remember(initialDebateTopic) {
        mutableStateOf(
            initialDebateTopic?.takeIf { it.isNotBlank() }
                ?: "Dynamical Casimir Cavity Zero-Point Extraction vs Thermodynamic Conservation"
        )
    }

    var totalRounds by remember { mutableIntStateOf(3) }
    var isDuelRunning by remember { mutableStateOf(false) }
    var activeSpeakerId by remember { mutableStateOf<String?>(null) }
    var currentRoundExecuting by remember { mutableIntStateOf(0) }
    var streamingDuelTurnText by remember { mutableStateOf("") }
    var activeStreamingCombatant by remember { mutableStateOf<CombatantPersona?>(null) }

    // Duel Transcript & Verdict
    val duelTranscript = remember { mutableStateListOf<DuelTurn>() }
    var duelVerdict by remember { mutableStateOf<DuelVerdict?>(null) }

    // Dialectic Tension Balance (0.0 = 100% Alpha, 1.0 = 100% Beta, 0.5 = Dead heat)
    var alphaTensionScore by remember { mutableIntStateOf(50) }
    var betaTensionScore by remember { mutableIntStateOf(50) }

    val animatedBalance by animateFloatAsState(
        targetValue = if (alphaTensionScore + betaTensionScore > 0) {
            betaTensionScore.toFloat() / (alphaTensionScore + betaTensionScore).toFloat()
        } else 0.5f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "tension_balance"
    )

    val alphaColor = Color(combatantAlpha.primaryColorHex)
    val betaColor = Color(combatantBeta.primaryColorHex)

    val quickTopics = listOf(
        "Dynamical Casimir Cavity Zero-Point Extraction vs Thermodynamic Conservation",
        "Macro-Scale Quantum Superposition in 310K Tubulin vs Thermal Phonon Decoherence",
        "Deterministic 4D Einstein Block Universe vs Indeterministic Many-Worlds Quantum Branching",
        "Planetary 42.600 Hz Bedrock Standing Waves vs Geological Waveguide Attenuation"
    )

    fun runAutonomousDuel() {
        if (duelTopic.isBlank() || isDuelRunning) return
        isDuelRunning = true
        duelVerdict = null
        duelTranscript.clear()
        alphaTensionScore = 50
        betaTensionScore = 50
        toneGenerator.playChime(SoundEffectType.HARMONIC_PING)

        scope.launch {
            for (round in 1..totalRounds) {
                currentRoundExecuting = round

                // Turn A: Combatant Alpha
                activeSpeakerId = combatantAlpha.id
                activeStreamingCombatant = combatantAlpha
                streamingDuelTurnText = ""
                toneGenerator.playChime(SoundEffectType.TAP)

                val alphaResult = AIBattleEngine.executeDuelTurn(
                    topic = duelTopic,
                    speaker = combatantAlpha,
                    opponent = combatantBeta,
                    priorTurns = duelTranscript.toList(),
                    roundNumber = round,
                    maxRounds = totalRounds
                )

                if (alphaResult.isSuccess) {
                    val turn = alphaResult.getOrThrow()
                    val fullWords = turn.argumentText.split(" ")
                    for (word in fullWords) {
                        streamingDuelTurnText = if (streamingDuelTurnText.isEmpty()) word else "$streamingDuelTurnText $word"
                        delay(25)
                    }
                    delay(150)
                    duelTranscript.add(turn)
                    alphaTensionScore += turn.momentumScore / 2
                }
                activeStreamingCombatant = null
                streamingDuelTurnText = ""
                delay(300)

                // Turn B: Combatant Beta
                activeSpeakerId = combatantBeta.id
                activeStreamingCombatant = combatantBeta
                streamingDuelTurnText = ""
                toneGenerator.playChime(SoundEffectType.TAP)

                val betaResult = AIBattleEngine.executeDuelTurn(
                    topic = duelTopic,
                    speaker = combatantBeta,
                    opponent = combatantAlpha,
                    priorTurns = duelTranscript.toList(),
                    roundNumber = round,
                    maxRounds = totalRounds
                )

                if (betaResult.isSuccess) {
                    val turn = betaResult.getOrThrow()
                    val fullWords = turn.argumentText.split(" ")
                    for (word in fullWords) {
                        streamingDuelTurnText = if (streamingDuelTurnText.isEmpty()) word else "$streamingDuelTurnText $word"
                        delay(25)
                    }
                    delay(150)
                    duelTranscript.add(turn)
                    betaTensionScore += turn.momentumScore / 2
                }
                activeStreamingCombatant = null
                streamingDuelTurnText = ""
                delay(350)
            }

            // Post-Duel Verdict
            activeSpeakerId = null
            toneGenerator.playChime(SoundEffectType.HARMONIC_PING)
            val verdictResult = AIBattleEngine.generateDuelVerdict(
                topic = duelTopic,
                alpha = combatantAlpha,
                beta = combatantBeta,
                turns = duelTranscript.toList()
            )
            if (verdictResult.isSuccess) {
                duelVerdict = verdictResult.getOrThrow()
            }
            isDuelRunning = false
            toneGenerator.playChime(SoundEffectType.CHIME)
            Toast.makeText(context, "AI Battle Concluded • Dialectic Verdict Rendered!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_battle_arena_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------------------------------------------------------------------
        // 1. COMBATANT SELECTION & ARENA CORNERS
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("arena_corners_card"),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
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
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "RIVAL COMBATANTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                    }

                    // Swap Combatants Action
                    IconButton(
                        onClick = {
                            if (!isDuelRunning) {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                val temp = combatantAlpha
                                combatantAlpha = combatantBeta
                                combatantBeta = temp
                            }
                        },
                        enabled = !isDuelRunning,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("swap_combatants_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Corners", tint = EchoCyan)
                    }
                }

                // Dual Corner Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Alpha Corner (Left)
                    CombatantCornerCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("combatant_alpha_card"),
                        cornerTag = "ALPHA (THESIS)",
                        combatant = combatantAlpha,
                        isActiveSpeaker = activeSpeakerId == combatantAlpha.id,
                        accentColor = alphaColor,
                        onCycleCombatant = {
                            if (!isDuelRunning) {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                val pool = AIBattleEngine.ALL_COMBATANTS.filter { it.id != combatantBeta.id }
                                val nextIdx = (pool.indexOf(combatantAlpha) + 1) % pool.size
                                combatantAlpha = pool[nextIdx]
                            }
                        }
                    )

                    // VS Divider Emblem
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clip(CircleShape)
                            .background(EchoDarkSurface)
                            .border(1.dp, EchoCardStroke, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Beta Corner (Right)
                    CombatantCornerCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("combatant_beta_card"),
                        cornerTag = "BETA (ANTITHESIS)",
                        combatant = combatantBeta,
                        isActiveSpeaker = activeSpeakerId == combatantBeta.id,
                        accentColor = betaColor,
                        onCycleCombatant = {
                            if (!isDuelRunning) {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                val pool = AIBattleEngine.ALL_COMBATANTS.filter { it.id != combatantAlpha.id }
                                val nextIdx = (pool.indexOf(combatantBeta) + 1) % pool.size
                                combatantBeta = pool[nextIdx]
                            }
                        }
                    )
                }

                // Dialectic Momentum & Tension Gauge
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${combatantAlpha.name.take(12).uppercase()}: $alphaTensionScore PTS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = alphaColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "MOMENTUM BALANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${combatantBeta.name.take(12).uppercase()}: $betaTensionScore PTS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = betaColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EchoDarkSurface)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f - animatedBalance.coerceIn(0.1f, 0.9f))
                                    .height(8.dp)
                                    .background(alphaColor)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(animatedBalance.coerceIn(0.1f, 0.9f))
                                    .height(8.dp)
                                    .background(betaColor)
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 2. DUEL TOPIC & CONTROLS
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(16.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = "BATTLE TOPIC & PROPOSITION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Camera Document Scan Import Shortcut
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoCyan.copy(alpha = 0.15f))
                            .clickable {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onOpenDocumentScanner()
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📷 IMPORT NOTES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                OutlinedTextField(
                    value = duelTopic,
                    onValueChange = { duelTopic = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battle_topic_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = EchoText),
                    placeholder = { Text("Enter proposition or scientific paradox for duel...", fontSize = 11.sp, color = EchoMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        unfocusedBorderColor = EchoCardStroke,
                        focusedContainerColor = EchoDarkSurface,
                        unfocusedContainerColor = EchoDarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4
                )

                // Quick Topic Suggestions Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickTopics) { topic ->
                        val isSelected = duelTopic == topic
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EchoCyan.copy(alpha = 0.2f) else EchoDarkSurface)
                                .border(1.dp, if (isSelected) EchoCyan else EchoCardStroke, RoundedCornerShape(8.dp))
                                .clickable {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    duelTopic = topic
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = topic.take(28) + "...",
                                fontSize = 10.sp,
                                color = if (isSelected) EchoCyan else EchoMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Round Depth Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLASH ROUNDS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoMuted,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("round_selector_row")
                    ) {
                        listOf(1, 2, 3, 4).forEach { count ->
                            val isSelected = totalRounds == count
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) EchoCyan else EchoDarkSurface)
                                    .border(1.dp, if (isSelected) EchoCyan else EchoCardStroke, RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (!isDuelRunning) {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            totalRounds = count
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$count ROUND${if (count > 1) "S" else ""}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EchoBg else EchoText,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Ignite Duel Primary Action
                Button(
                    onClick = { runAutonomousDuel() },
                    enabled = !isDuelRunning && duelTopic.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ignite_duel_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EchoCyan,
                        contentColor = EchoBg,
                        disabledContainerColor = EchoDarkSurface,
                        disabledContentColor = EchoMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isDuelRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = EchoCyan, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "DUEL IN PROGRESS (ROUND $currentRoundExecuting/$totalRounds)...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "IGNITE 1-ON-1 AI BATTLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 3. LIVE STREAMING SPEECH BUBBLE (WHEN ACTOR IS GENERATING)
        // ---------------------------------------------------------------------
        AnimatedVisibility(
            visible = isDuelRunning && activeStreamingCombatant != null && streamingDuelTurnText.isNotBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val actor = activeStreamingCombatant ?: combatantAlpha
            val actorColor = Color(actor.primaryColorHex)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_streaming_turn_card"),
                colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
                border = BorderStroke(1.5.dp, actorColor),
                shape = RoundedCornerShape(14.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(actorColor)
                            )
                            Text(
                                text = "${actor.name.uppercase()} SPEAKING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = actorColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "ROUND $currentRoundExecuting/$totalRounds",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = streamingDuelTurnText,
                        fontSize = 12.sp,
                        color = EchoText,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // ---------------------------------------------------------------------
        // 4. DUEL TRANSCRIPT TURNS
        // ---------------------------------------------------------------------
        if (duelTranscript.isNotEmpty()) {
            Text(
                text = "DIALECTIC CLASH TRANSCRIPT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EchoCyan,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            duelTranscript.forEach { turn ->
                val isAlpha = turn.speakerId == combatantAlpha.id
                val speakerColor = if (isAlpha) alphaColor else betaColor

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("duel_turn_${turn.id}"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = BorderStroke(1.dp, speakerColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(speakerColor)
                                )
                                Text(
                                    text = turn.speakerName.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = speakerColor,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(speakerColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "R${turn.roundNumber} • ${turn.turnType}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = speakerColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = turn.argumentText,
                            fontSize = 12.sp,
                            color = EchoText,
                            lineHeight = 17.sp
                        )

                        // Highlight Punchline Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoDarkSurface)
                                .border(1.dp, speakerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = speakerColor, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "\"${turn.keyPunchline}\"",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = speakerColor,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 5. POST-DUEL VERDICT CARD
        // ---------------------------------------------------------------------
        duelVerdict?.let { verdict ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verdict_card"),
                colors = CardDefaults.cardColors(containerColor = EchoCard),
                border = BorderStroke(1.5.dp, EchoGreen),
                shape = RoundedCornerShape(16.dp)
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
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(20.dp))
                            Text(
                                text = "DIALECTIC VERDICT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoGreen,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EchoGreen.copy(alpha = 0.2f))
                                .border(1.dp, EchoGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = verdict.verdictType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Winner Display
                    Text(
                        text = "VICTOR / SYNTHESIS: ${verdict.winnerName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EchoText
                    )

                    Text(
                        text = verdict.synthesisSummary,
                        fontSize = 11.sp,
                        color = EchoMuted,
                        lineHeight = 16.sp
                    )

                    // Decisive Point
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoDarkSurface)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "DECISIVE POINT:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = verdict.decisivePoint,
                            fontSize = 11.sp,
                            color = EchoText
                        )
                    }

                    // Recommended Consensus
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EchoDarkSurface)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SYNTHESIZED SCIENTIFIC CONSENSUS:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = verdict.recommendedConsensus,
                            fontSize = 11.sp,
                            color = EchoText
                        )
                    }

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                val copyText = """
                                    [1-on-1 AI BATTLE VERDICT]
                                    Topic: $duelTopic
                                    Winner: ${verdict.winnerName}
                                    Verdict: ${verdict.verdictType}
                                    Synthesis: ${verdict.synthesisSummary}
                                    Decisive Point: ${verdict.decisivePoint}
                                    Consensus: ${verdict.recommendedConsensus}
                                """.trimIndent()
                                clipboardManager.setText(AnnotatedString(copyText))
                                Toast.makeText(context, "Battle Verdict Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("copy_verdict_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoDarkSurface,
                                contentColor = EchoCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy Verdict", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                workspaceViewModel?.createNote(
                                    name = "AI Battle: ${duelTopic.take(30)}",
                                    initialText = "DIALECTIC VERDICT (${verdict.winnerName}):\n${verdict.synthesisSummary}\n\nDECISIVE POINT:\n${verdict.decisivePoint}\n\nCONSENSUS:\n${verdict.recommendedConsensus}",
                                    tags = "THEORY,BATTLE"
                                )
                                Toast.makeText(context, "Saved to Workspace Notes!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("save_verdict_to_workspace_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan,
                                contentColor = EchoBg
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatantCornerCard(
    modifier: Modifier = Modifier,
    cornerTag: String,
    combatant: CombatantPersona,
    isActiveSpeaker: Boolean,
    accentColor: Color,
    onCycleCombatant: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onCycleCombatant() },
        colors = CardDefaults.cardColors(containerColor = EchoDarkSurface),
        border = BorderStroke(if (isActiveSpeaker) 2.dp else 1.dp, if (isActiveSpeaker) accentColor else EchoCardStroke),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cornerTag,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )

                if (isActiveSpeaker) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            Text(
                text = combatant.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = EchoText,
                maxLines = 1
            )

            Text(
                text = combatant.title,
                fontSize = 9.sp,
                color = EchoMuted,
                maxLines = 2,
                lineHeight = 12.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "TAP TO CHANGE ↻",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
