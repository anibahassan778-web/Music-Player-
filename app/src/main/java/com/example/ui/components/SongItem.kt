package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    isCurrent: Boolean,
    isFavorite: Boolean,
    onSongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .clickable { onSongClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = song.title,
                placeholder = painterResource(id = R.drawable.ic_default_album_art),
                error = painterResource(id = R.drawable.ic_default_album_art),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp)
            )
            if (isCurrent && isPlaying) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Song Title & Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${formatDuration(song.duration)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Quick favorite button
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) stringResource(R.string.removed_from_favorites) else stringResource(R.string.added_to_favorites),
                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // More options menu
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(if (isFavorite) stringResource(R.string.removed_from_favorites) else stringResource(R.string.added_to_favorites))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleFavorite()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_to_playlist)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistAdd,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylist()
                    }
                )
            }
        }
    }
}
