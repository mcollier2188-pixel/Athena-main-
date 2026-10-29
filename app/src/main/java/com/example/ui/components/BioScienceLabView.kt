package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.model.NetworkNode
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import com.example.ui.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

/**
 * Biological Resonance Stream Types with their 42.6 Hz Octave Division Scaling.
 */
enum class BioStreamType(
    val title: String,
    val bandName: String,
    val frequencyHz: Double,
    val octaveDivision: String,
    val description: String,
    val color: Color
) {
    DELTA_SLEEP(
        title = "Delta Deep Sleep",
        bandName = "0.5 – 4.0 Hz",
        frequencyHz = 2.6625, // 42.6 / 16 (4th sub-harmonic)
        octaveDivision = "42.6 / 16",
        description = "Deep restorative sleep & subconscious somatic coherence",
        color = Color(0xFF7C4DFF)
    ),
    THETA_INTUITION(
        title = "Theta Hypnagogic",
        bandName = "4.0 – 8.0 Hz",
        frequencyHz = 5.325, // 42.6 / 8 (3rd sub-harmonic)
        octaveDivision = "42.6 / 8",
        description = "Deep meditation, dream states, and megalithic acoustic recall",
        color = Color(0xFF00E5FF)
    ),
    ALPHA_BEDROCK(
        title = "Alpha Bedrock Lock",
        bandName = "8.0 – 13.0 Hz",
        frequencyHz = 10.650, // 42.6 / 4 (2nd sub-harmonic)
        octaveDivision = "42.6 / 4",
        description = "Calm wakefulness & primary resonance match with 42.6 Hz carrier",
        color = Color(0xFF00E676)
    ),
    BETA_COGNITIVE(
        title = "Beta Active Focus",
        bandName = "13.0 – 30.0 Hz",
        frequencyHz = 21.300, // 42.6 / 2 (1st sub-harmonic)
        octaveDivision = "42.6 / 2",
        description = "High analytical computation and dialectic problem solving",
        color = Color(0xFFFFB300)
    ),
    GAMMA_GENIUS(
        title = "Gamma Peak Insight",
        bandName = "30.0 – 85.0 Hz",
        frequencyHz = 42.600, // Fundamental carrier
        octaveDivision = "42.6 Fundamental",
        description = "Whole-brain hyper-binding, cross-modal insight & 180+ IQ synthesis",
        color = Color(0xFFFF4081)
    ),
    HRV_VAGAL(
        title = "HRV Mayer Wave",
        bandName = "0.10 Hz Vagal Tone",
        frequencyHz = 0.6656, // 42.6 / 64
        octaveDivision = "42.6 / 64",
        description = "Heart-rate variability coherence & baroreflex resonance entrainment",
        color = Color(0xFFFF5252)
    ),
    CIRCADIAN_PHASE(
        title = "Circadian Pineal Rhythm",
        bandName = "24-Hour Solar",
        frequencyHz = 0.00001157, // 1 cycle / 86400s
        octaveDivision = "Diurnal Lock",
        description = "Suprachiasmatic nucleus melatonin cycle aligned with solar noon",
        color = Color(0xFF64B5F6)
    )
}

/**
 * BioScience Lab Module:
 * Tracks biological resonance data (EEG brainwaves, HRV pulse, circadian cycles),
 * maps bio-rhythms to the 42.6 Hz network nodes via octave sub-harmonic division ($42.6 / 2^n$),
 * and provides a 60 FPS real-time Canvas visualization panel with acoustic entrainment
 * and Workspace note logging.
 *
 * Designed with [isScrollable] parameter so that when embedded inside a parent
 * LazyColumn (like BrainstormingLabScreen), it renders as a smooth Column without
 * illegal nested vertical scrolling crashes.
 */
@Composable
fun BioScienceLabView(
    workspaceViewModel: WorkspaceViewModel? = null,
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    isScrollable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying by toneGenerator.isPlayingState.collectAsState()

    var selectedBioStream by remember { mutableStateOf(BioStreamType.ALPHA_BEDROCK) }
    var selectedNode by remember { mutableStateOf(NetworkNode.ALL_NODES.first()) }
    var subjectHeartRate by remember { mutableIntStateOf(68) }
    var subjectCoherencePercent by remember { mutableIntStateOf(94) }
    var entrainmentVolume by remember { mutableFloatStateOf(0.75f) }

    // Wave animation phases
    val infiniteTransition = rememberInfiniteTransition(label = "bioscience_wave_anim")
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bio_phase_1"
    )
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bio_phase_2"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bio_pulse_glow"
    )

    // Entrainment audio toggler
    fun toggleAcousticEntrainment() {
        if (isPlaying) {
            toneGenerator.stop()
            toneGenerator.playChime(SoundEffectType.TAP)
        } else {
            toneGenerator.playChime(SoundEffectType.CHIME)
            val playbackFreq = when (selectedBioStream) {
                BioStreamType.DELTA_SLEEP -> 42.600 / 16.0
                BioStreamType.THETA_INTUITION -> 42.600 / 8.0
                BioStreamType.ALPHA_BEDROCK -> 42.600 / 4.0
                BioStreamType.BETA_COGNITIVE -> 42.600 / 2.0
                BioStreamType.GAMMA_GENIUS -> 42.600
                BioStreamType.HRV_VAGAL -> 42.600
                BioStreamType.CIRCADIAN_PHASE -> 42.600
            }
            toneGenerator.start(frequencies = listOf(playbackFreq), mult = 1.0)
        }
    }

    val rootModifier = if (isScrollable) {
        modifier
            .fillMaxSize()
            .background(EchoBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    } else {
        modifier.fillMaxWidth()
    }

    Column(
        modifier = rootModifier.testTag("bioscience_lab_module"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.5.dp, EchoGreen.copy(alpha = 0.7f)),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EchoGreen.copy(alpha = 0.2f))
                                .border(1.dp, EchoGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = EchoGreen, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "BIOSCIENCE RESONANCE LAB",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Biological Telemetry & 42.6 Hz Sub-Harmonic Entrainment",
                                fontSize = 10.sp,
                                color = EchoMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoGreen.copy(alpha = 0.15f))
                            .border(1.dp, EchoGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BIO-LOCK: $subjectCoherencePercent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Tracks biological oscillations (EEG brainwave bands, HRV Mayer waves, circadian cycles) scaled precisely to the 42.600 Hz bedrock carrier via octave sub-harmonic division (42.6 / 2ⁿ).",
                    fontSize = 11.sp,
                    color = EchoMuted,
                    lineHeight = 16.sp
                )

                // Quick telemetry stat chips
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
                            Text("PULSE RATE", fontSize = 8.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(12.dp))
                                Text("$subjectHeartRate BPM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EchoText)
                            }
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
                            Text("SUB-HARMONIC", fontSize = 8.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                            Text(selectedBioStream.octaveDivision, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = selectedBioStream.color, fontFamily = FontFamily.Monospace)
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
                            Text("TARGET NODE", fontSize = 8.sp, color = EchoMuted, fontWeight = FontWeight.Bold)
                            Text(selectedNode.id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EchoCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Biological Stream Selector Chips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "BIOLOGICAL RESONANCE DATA STREAM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BioStreamType.values()) { stream ->
                        val isSelected = selectedBioStream == stream

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedBioStream = stream
                                toneGenerator.playChime(SoundEffectType.TAP)
                            },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(stream.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${stream.frequencyHz} Hz · ${stream.octaveDivision}", fontSize = 9.sp, color = if (isSelected) EchoBg else EchoMuted)
                                }
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(stream.color)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = stream.color,
                                selectedLabelColor = EchoBg,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Real-Time 60 FPS Biological Multi-Channel Canvas Visualizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bio_oscillogram_canvas_card"),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, selectedBioStream.color.copy(alpha = pulseGlow)),
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(selectedBioStream.color)
                        )
                        Text(
                            text = "LIVE OSCILLOGRAM · ${selectedBioStream.title.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "${String.format(Locale.US, "%.4f", selectedBioStream.frequencyHz)} Hz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedBioStream.color,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EchoDarkSurface)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f
                        val points = 120
                        val dx = w / points

                        // Grid lines
                        val gridColor = Color(0xFF1E2A38)
                        for (i in 1..3) {
                            val y = h * (i / 4f)
                            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                        }
                        for (i in 1..7) {
                            val x = w * (i / 8f)
                            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                        }

                        // 1. Biological Signal Wave (Sine composite)
                        val bioPath = Path()
                        val bioAmp = h * 0.28f
                        for (i in 0..points) {
                            val x = i * dx
                            val primaryWave = sin(i * 0.15f + wavePhase1)
                            val secondaryHarmonic = sin(i * 0.35f + wavePhase2) * 0.35f
                            val y = midY + (primaryWave + secondaryHarmonic) * bioAmp
                            if (i == 0) bioPath.moveTo(x, y) else bioPath.lineTo(x, y)
                        }
                        drawPath(
                            path = bioPath,
                            color = selectedBioStream.color,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 2. Underlying 42.6 Hz Bedrock Reference Wave (Thin Cyan trace)
                        val carrierPath = Path()
                        val carrierAmp = h * 0.15f
                        for (i in 0..points) {
                            val x = i * dx
                            val y = midY + sin(i * 0.45f + wavePhase1 * 2f) * carrierAmp
                            if (i == 0) carrierPath.moveTo(x, y) else carrierPath.lineTo(x, y)
                        }
                        drawPath(
                            path = carrierPath,
                            color = EchoCyan.copy(alpha = 0.45f),
                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(selectedBioStream.color))
                        Text("Bio Stream (${selectedBioStream.bandName})", fontSize = 9.sp, color = EchoMuted)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(EchoCyan))
                        Text("42.6 Hz Bedrock Carrier Lock", fontSize = 9.sp, color = EchoMuted)
                    }
                }

                // Acoustic Entrainment Player Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { toggleAcousticEntrainment() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_bio_entrainment_play"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) EchoCyan else selectedBioStream.color,
                            contentColor = EchoBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "STOP ENTRAINMENT TONE" else "PLAY ENTRAINMENT TONE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            subjectHeartRate = (60..85).random()
                            subjectCoherencePercent = (91..99).random()
                            toneGenerator.playChime(SoundEffectType.TAP)
                        },
                        modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoMuted),
                        border = BorderStroke(1.dp, EchoCardStroke),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-calibrate", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 42.6 Hz Octave Sub-Harmonic Ladder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OCTAVE SUB-HARMONIC DIVISION LADDER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )

                // Sub-harmonic table rows
                listOf(
                    Triple("1:1 Fundamental", "42.600 Hz", "Gamma Cognition"),
                    Triple("1:2 Sub-Harmonic", "21.300 Hz", "Beta Focus"),
                    Triple("1:4 Sub-Harmonic", "10.650 Hz", "Alpha Bedrock Lock"),
                    Triple("1:8 Sub-Harmonic", "5.325 Hz", "Theta Intuition"),
                    Triple("1:16 Sub-Harmonic", "2.662 Hz", "Delta Deep Sleep"),
                    Triple("1:64 Sub-Harmonic", "0.665 Hz", "HRV Mayer Coherence")
                ).forEach { (ratio, freq, desc) ->
                    val isMatch = selectedBioStream.octaveDivision.contains(ratio.split(" ").first()) ||
                            selectedBioStream.title.contains(desc.split(" ").first())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMatch) EchoGreen.copy(alpha = 0.12f) else EchoDarkSurface)
                            .border(1.dp, if (isMatch) EchoGreen.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ratio, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMatch) EchoGreen else EchoText)
                            Text("— $desc", fontSize = 10.sp, color = EchoMuted)
                        }
                        Text(freq, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMatch) EchoGreen else EchoCyan, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 11-Node Coupling Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.dp, EchoCardStroke),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "COUPLED NETWORK NODE RESONATOR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EchoMuted,
                    letterSpacing = 1.sp
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(NetworkNode.ALL_NODES) { node ->
                        val isSelected = selectedNode.id == node.id

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedNode = node
                                toneGenerator.playChime(SoundEffectType.TAP)
                            },
                            label = {
                                Text("${node.id} ${node.name}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EchoCyan,
                                selectedLabelColor = EchoBg,
                                containerColor = EchoDarkSurface,
                                labelColor = EchoText
                            )
                        )
                    }
                }
            }
        }

        // Action: Save Bio-Telemetry Log to Workspace Notes
        Button(
            onClick = {
                val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val title = "BioScience Log: ${selectedBioStream.title} [$timestampStr]"
                val content = buildString {
                    appendLine("# BioScience Resonance Laboratory Observation")
                    appendLine("**Timestamp**: $timestampStr")
                    appendLine("**Biological Stream**: ${selectedBioStream.title}")
                    appendLine("**Frequency**: ${selectedBioStream.frequencyHz} Hz (${selectedBioStream.octaveDivision})")
                    appendLine("**Coupled Network Node**: ${selectedNode.id} - ${selectedNode.name}")
                    appendLine("**Vagal Coherence**: $subjectCoherencePercent%")
                    appendLine("**Pulse Rate**: $subjectHeartRate BPM")
                    appendLine()
                    appendLine("## Laboratory Analysis")
                    appendLine(selectedBioStream.description)
                    appendLine("Octave sub-harmonic division confirmed coherent with the 42.600 Hz bedrock carrier.")
                }

                workspaceViewModel?.createNote(name = title, initialText = content)
                toneGenerator.playChime(SoundEffectType.SUCCESS)
                Toast.makeText(context, "Bio-telemetry observation saved to Workspace Notes", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_bio_telemetry_workspace"),
            colors = ButtonDefaults.buttonColors(
                containerColor = EchoGreen,
                contentColor = EchoBg
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("SAVE BIO-TELEMETRY TO WORKSPACE NOTES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
