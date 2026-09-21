package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("mini_player"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Subtle progress indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.title,
                    placeholder = painterResource(id = R.drawable.ic_default_album_art),
                    error = painterResource(id = R.drawable.ic_default_album_art),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Song Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(40.dp)
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
