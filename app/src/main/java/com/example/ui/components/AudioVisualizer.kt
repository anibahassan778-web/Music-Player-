package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.VisualizerStyle
import com.example.ui.theme.LocalAppSettings

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barWidth: Dp = 3.5.dp,
    maxHeight: Dp = 22.dp,
    minHeight: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    customStyle: VisualizerStyle? = null
) {
    val appSettings = LocalAppSettings.current
    val visualizerStyle = customStyle ?: appSettings.visualizerStyle
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")

    when (visualizerStyle) {
        VisualizerStyle.ENERGY_PULSE -> {
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(420, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0 until 3).forEach { index ->
                    val curScale = if (isPlaying) (pulseScale + index * 0.1f).coerceIn(0.7f, 1.4f) else 0.8f
                    Box(
                        modifier = Modifier
                            .size(if (index == 1) 12.dp else 8.dp)
                            .scale(curScale)
                            .clip(CircleShape)
                            .background(color.copy(alpha = if (index == 1) 1f else 0.7f))
                    )
                }
            }
        }
        VisualizerStyle.SMOOTH_WAVE -> {
            val wavePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6.28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave_phase"
            )

            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0 until barCount).forEach { index ->
                    val waveFraction = if (isPlaying) {
                        (Math.sin((wavePhase + index * 0.8).toDouble()).toFloat() + 1f) / 2f
                    } else 0.2f

                    val h = minHeight + (maxHeight - minHeight) * waveFraction

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(h)
                            .clip(RoundedCornerShape(barWidth / 2))
                            .background(
                                Brush.verticalGradient(
                                    listOf(color, color.copy(alpha = 0.6f))
                                )
                            )
                    )
                }
            }
        }
        VisualizerStyle.NEON_GLOW -> {
            val neonAnimDurations = listOf(280, 420, 240, 510, 360, 440)
            val animations = (0 until barCount).map { index ->
                val duration = neonAnimDurations[index % neonAnimDurations.size]
                val animatedFraction by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "neon_bar_$index"
                )
                animatedFraction
            }

            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                animations.forEachIndexed { _, fraction ->
                    val targetHeight = if (isPlaying) {
                        minHeight + (maxHeight - minHeight) * fraction
                    } else {
                        minHeight
                    }

                    Box(
                        modifier = Modifier
                            .width(barWidth + 1.dp)
                            .height(targetHeight)
                            .clip(RoundedCornerShape(barWidth / 2))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        color,
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )
                }
            }
        }
        VisualizerStyle.EQUALIZER_BARS -> {
            val animationDurations = listOf(360, 480, 310, 550, 410, 470, 330)
            val animations = (0 until barCount).map { index ->
                val duration = animationDurations[index % animationDurations.size]
                val animatedFraction by infiniteTransition.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$index"
                )
                animatedFraction
            }

            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                animations.forEachIndexed { _, fraction ->
                    val targetHeight = if (isPlaying) {
                        minHeight + (maxHeight - minHeight) * fraction
                    } else {
                        minHeight
                    }

                    val animatedHeight by animateDpAsState(
                        targetValue = targetHeight,
                        animationSpec = spring(stiffness = 600f),
                        label = "bar_height"
                    )

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(animatedHeight)
                            .clip(RoundedCornerShape(barWidth / 2))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        color,
                                        color.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
