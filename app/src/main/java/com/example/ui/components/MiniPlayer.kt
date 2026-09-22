package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.Song
import kotlinx.coroutines.isActive

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val targetProgress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "mini_player_progress"
    )

    // Pulsing glowing border and smooth mini vinyl disc rotation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "mini_glow")
    val miniVinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "mini_vinyl_rotation"
    )
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    // Button bouncy scales
    var playButtonPulsing by remember { mutableStateOf(false) }
    val playButtonScale by animateFloatAsState(
        targetValue = if (playButtonPulsing) 0.80f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { playButtonPulsing = false },
        label = "mini_play_scale"
    )

    var nextButtonPulsing by remember { mutableStateOf(false) }
    val nextButtonScale by animateFloatAsState(
        targetValue = if (nextButtonPulsing) 0.80f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { nextButtonPulsing = false },
        label = "mini_next_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("mini_player"),
        shape = RoundedCornerShape(16.dp),
        border = if (isPlaying) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha)) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Smooth progress indicator
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spinning Mini Vinyl Record Album Art
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF141416))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (isPlaying) miniVinylRotation else 0f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center label
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.72f)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = song.albumArtUri ?: R.drawable.img_album_art_placeholder_1790011366041,
                                contentDescription = song.title,
                                placeholder = painterResource(id = R.drawable.img_album_art_placeholder_1790011366041),
                                error = painterResource(id = R.drawable.img_album_art_placeholder_1790011366041),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Spindle hole
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title & Artist with animated transition
                AnimatedContent(
                    targetState = song,
                    transitionSpec = {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(180)))
                    },
                    label = "mini_player_song_transition",
                    modifier = Modifier.weight(1f)
                ) { currentSong ->
                    Column {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Mini Audio Visualizer Indicator
                AnimatedVisibility(
                    visible = isPlaying,
                    enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f),
                    exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f)
                ) {
                    AudioVisualizer(
                        isPlaying = true,
                        barCount = 3,
                        barWidth = 2.5.dp,
                        maxHeight = 14.dp,
                        minHeight = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Play / Pause Button with bouncy scale and morphing icon
                IconButton(
                    onClick = {
                        playButtonPulsing = true
                        onPlayPauseClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .scale(playButtonScale)
                        .testTag("mini_player_play_pause")
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            (fadeIn(tween(150)) + scaleIn(initialScale = 0.75f)) togetherWith
                            (fadeOut(tween(150)) + scaleOut(targetScale = 0.75f))
                        },
                        label = "mini_play_pause_icon"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) stringResource(R.string.pause) else stringResource(R.string.play),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Next Track Button with bouncy scale
                IconButton(
                    onClick = {
                        nextButtonPulsing = true
                        onNextClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .scale(nextButtonScale)
                        .testTag("mini_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.next),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
