package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.PlayerUiState
import com.example.domain.model.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerModal(
    playerState: PlayerUiState,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAddToPlaylist: () -> Unit,
    onOpenQueue: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentSong = playerState.currentSong ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    val currentPos = if (isDraggingSlider) {
        (sliderDragPosition * playerState.duration).toLong()
    } else {
        playerState.currentPosition
    }

    val sliderValue = if (playerState.duration > 0) {
        if (isDraggingSlider) sliderDragPosition
        else (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("full_player_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Collapse button and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("collapse_full_player")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close Player",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.now_playing),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.testTag("open_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = stringResource(R.string.queue),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .shadow(16.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentSong.albumArtUri,
                    contentDescription = currentSong.title,
                    placeholder = painterResource(id = R.drawable.ic_default_album_art),
                    error = painterResource(id = R.drawable.ic_default_album_art),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Artist + Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentSong.artist} • ${currentSong.album}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_favorite_toggle")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Progress Slider and Times
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderDragPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        val targetMs = (sliderDragPosition * playerState.duration).toLong()
                        onSeekTo(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("player_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPos),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(playerState.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Playback Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("shuffle_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(R.string.shuffle),
                        tint = if (playerState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(48.dp).testTag("full_player_previous")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.previous),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause FAB
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("full_player_play_pause"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(48.dp).testTag("full_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.next),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier.testTag("repeat_mode_button")
                ) {
                    val (icon, tint) = when (playerState.repeatMode) {
                        RepeatMode.OFF -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.onSurfaceVariant)
                        RepeatMode.ALL -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.primary)
                        RepeatMode.ONE -> Pair(Icons.Default.RepeatOne, MaterialTheme.colorScheme.primary)
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(R.string.repeat_all),
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Bottom Actions (Add to Playlist, Sleep Timer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add to Playlist
                IconButton(
                    onClick = onOpenAddToPlaylist,
                    modifier = Modifier.testTag("full_player_add_to_playlist")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlaylistAdd,
                        contentDescription = stringResource(R.string.add_to_playlist),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Sleep Timer with badge if active
                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.testTag("full_player_sleep_timer")
                ) {
                    val timerActive = playerState.sleepTimerRemainingSeconds != null && playerState.sleepTimerRemainingSeconds > 0
                    if (timerActive) {
                        BadgedBox(
                            badge = {
                                val minutesLeft = (playerState.sleepTimerRemainingSeconds!! / 60) + 1
                                Badge { Text("$minutesLeft") }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = stringResource(R.string.sleep_timer),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = stringResource(R.string.sleep_timer),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
