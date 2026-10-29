package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.model.NetworkNode
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import java.util.Locale
import kotlin.math.sin

private val Gold85Hz = Color(0xFFFFD700)

data class HarmonicPreset(
    val name: String,
    val activeNodeIds: List<String>,
    val octaveLift: Int
)

/**
 * HarmonicNodeMixerView Composable
 *
 * Feature 6: Harmonic Mixer — Node Layering
 * Toggles N1 through N11 as on/off buttons to combine frequencies into a blended tone,
 * displays composite superposition waveform, supports octave lift for mobile speakers,
 * and saves custom mixes as presets.
 */
@Composable
fun HarmonicNodeMixerView(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nodes = NetworkNode.ALL_NODES
    val activeNodeIds = remember { mutableStateListOf("N1", "N2") }
    var octaveLift by remember { mutableIntStateOf(1) } // 0 = 1x, 1 = 2x (+1 octave), 2 = 4x (+2 octaves)
    val isPlaying by toneGenerator.isPlayingState.collectAsState()

    // Saved presets
    val presets = remember {
        mutableStateListOf(
            HarmonicPreset("Master Baseline", listOf("N1", "N2"), 1),
            HarmonicPreset("Solent Harmonic Chords", listOf("N1", "N5", "N8"), 2),
            HarmonicPreset("Deep Geological Triad", listOf("N1", "N3", "N4"), 1),
            HarmonicPreset("Full Bedrock Superposition", nodes.map { it.id }, 1)
        )
    }

    // Calculate blended frequency (effective composite acoustic frequency)
    val activeNodes = nodes.filter { activeNodeIds.contains(it.id) }
    val baseCompositeFreq = if (activeNodes.isEmpty()) 42.6 else activeNodes.map { it.frequency }.average()
    val multiplier = when (octaveLift) {
        1 -> 2.0 // +1 octave
        2 -> 4.0 // +2 octaves
        else -> 1.0 // raw
    }
    val effectiveFrequency = baseCompositeFreq * multiplier

    fun togglePlayback() {
        if (isPlaying) {
            toneGenerator.stop()
        } else {
            toneGenerator.start(frequencies = listOf(effectiveFrequency), mult = 1.0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("harmonic_node_mixer_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EchoCard),
            border = BorderStroke(1.5.dp, EchoCyan.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
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
                                .background(EchoCyan.copy(alpha = 0.2f))
                                .border(1.dp, EchoCyan, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "HARMONIC MIXER — NODE LAYERING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EchoText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${activeNodeIds.size} of 11 Nodes Blended · Composite Waveform",
                                fontSize = 9.sp,
                                color = EchoCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Octave Lift Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (octaveLift > 0) "+${octaveLift} Oct" else "Raw",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold85Hz,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = {
                                octaveLift = (octaveLift + 1) % 3
                                if (isPlaying) {
                                    val newMult = when (octaveLift) {
                                        1 -> 2.0
                                        2 -> 4.0
                                        else -> 1.0
                                    }
                                    toneGenerator.start(frequencies = listOf(baseCompositeFreq * newMult), mult = 1.0)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Octave Lift", tint = Gold85Hz, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Superposition Oscilloscope Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EchoDarkSurface)
                        .border(1.dp, EchoCardStroke, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2f

                        // Draw baseline
                        drawLine(
                            color = EchoCyan.copy(alpha = 0.15f),
                            start = Offset(0f, midY),
                            end = Offset(width, midY),
                            strokeWidth = 1f
                        )

                        if (activeNodes.isNotEmpty()) {
                            val path = Path()
                            val numSamples = width.toInt()
                            for (x in 0..numSamples step 3) {
                                val progress = x.toFloat() / width
                                var compositeY = 0.0

                                activeNodes.forEachIndexed { idx, node ->
                                    val f = (node.frequency * multiplier) / 40.0
                                    compositeY += sin(progress * f * 2 * Math.PI + idx * 0.5)
                                }
                                val normalizedY = (compositeY / activeNodes.size).coerceIn(-1.0, 1.0)
                                val y = midY + (normalizedY.toFloat() * (height * 0.38f))

                                if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
                            }

                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(listOf(EchoCyan, Gold85Hz, EchoGreen)),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Composite frequency HUD
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EchoDarkSurface.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "BLEND: %.2f Hz", effectiveFrequency),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold85Hz,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Node Selection Grid: N1 through N11 as Toggle Buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TOGGLE NODE OSCILLATORS (N1 – N11):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoMuted,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // 11 Buttons in 2 Rows
                    val row1 = nodes.take(6)
                    val row2 = nodes.drop(6)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row1.forEach { node ->
                            val isSelected = activeNodeIds.contains(node.id)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EchoCyan else EchoDarkSurface)
                                    .border(1.dp, if (isSelected) EchoCyan else EchoCardStroke, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected) {
                                            if (activeNodeIds.size > 1) activeNodeIds.remove(node.id)
                                        } else {
                                            activeNodeIds.add(node.id)
                                        }
                                        if (isPlaying) toneGenerator.start(frequencies = listOf(effectiveFrequency), mult = 1.0)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = node.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EchoDarkSurface else EchoText
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row2.forEach { node ->
                            val isSelected = activeNodeIds.contains(node.id)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EchoCyan else EchoDarkSurface)
                                    .border(1.dp, if (isSelected) EchoCyan else EchoCardStroke, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected) {
                                            if (activeNodeIds.size > 1) activeNodeIds.remove(node.id)
                                        } else {
                                            activeNodeIds.add(node.id)
                                        }
                                        if (isPlaying) toneGenerator.start(frequencies = listOf(effectiveFrequency), mult = 1.0)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = node.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EchoDarkSurface else EchoText
                                )
                            }
                        }
                    }
                }

                // Presets Bar
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                activeNodeIds.clear()
                                activeNodeIds.addAll(preset.activeNodeIds)
                                octaveLift = preset.octaveLift
                                if (isPlaying) toneGenerator.start(frequencies = listOf(effectiveFrequency), mult = 1.0)
                                Toast.makeText(context, "Loaded preset: ${preset.name}", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(preset.name, fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = EchoDarkSurface,
                                labelColor = EchoText
                            ),
                            border = BorderStroke(1.dp, EchoCardStroke),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Play / Stop Master Audio Button
                Button(
                    onClick = { togglePlayback() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("harmonic_mixer_play_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Gold85Hz else EchoCyan,
                        contentColor = EchoDarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "PAUSE HARMONIC MIX (${String.format(Locale.US, "%.1f", effectiveFrequency)} Hz)" else "PLAY BLENDED HARMONIC MIX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
