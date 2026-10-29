package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoText
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * YouTube-style Docked Miniplayer Bar.
 * Sits persistently above the bottom navigation bar, providing continuous playback,
 * tap-to-expand, play/pause controls, progress indication, and swipe-to-dismiss.
 */
@Composable
fun DockedMiniPlayerBar(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onExpandFullPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeSource by GlobalVideoPlaybackManager.activeVideoSource.collectAsState()
    val isPlaying by GlobalVideoPlaybackManager.isPlaying.collectAsState()
    val isVisible by GlobalVideoPlaybackManager.isMiniplayerVisible.collectAsState()
    val currentPositionMs by GlobalVideoPlaybackManager.currentPositionMs.collectAsState()
    val durationMs by GlobalVideoPlaybackManager.durationMs.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }

    AnimatedVisibility(
        visible = isVisible && activeSource != null,
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
    ) {
        val source = activeSource ?: return@AnimatedVisibility
        val progress = if (durationMs > 0L) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Surface(
            color = EchoBgAlt,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (kotlin.math.abs(dragOffsetX.value) > 180f) {
                                coroutineScope.launch {
                                    val target = if (dragOffsetX.value > 0) 600f else -600f
                                    dragOffsetX.animateTo(target, tween(150))
                                    GlobalVideoPlaybackManager.dismissMiniplayer()
                                    dragOffsetX.snapTo(0f)
                                }
                            } else {
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(0f, tween(150))
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                dragOffsetX.snapTo(dragOffsetX.value + dragAmount)
                            }
                        }
                    )
                }
                .clickable {
                    toneGenerator.playChime(SoundEffectType.TAP)
                    GlobalVideoPlaybackManager.expandFullPlayer()
                    onExpandFullPlayer()
                }
                .testTag("docked_miniplayer_bar")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top border stroke
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(EchoCardStroke)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left 16:9 Thumbnail preview box
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(45.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = EchoCyan.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Title & Channel/Subtitle
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = source.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = source.subtitle,
                            fontSize = 11.sp,
                            color = EchoMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Play / Pause Button
                    IconButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            GlobalVideoPlaybackManager.togglePlayPause()
                        },
                        modifier = Modifier.size(40.dp).testTag("miniplayer_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = EchoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Close / Dismiss Button
                    IconButton(
                        onClick = {
                            toneGenerator.playChime(SoundEffectType.TAP)
                            GlobalVideoPlaybackManager.dismissMiniplayer()
                        },
                        modifier = Modifier.size(40.dp).testTag("miniplayer_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Miniplayer",
                            tint = EchoMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Micro Progress Scrubber Bar at Bottom
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = EchoCyan,
                    trackColor = EchoCardStroke
                )
            }
        }
    }
}
