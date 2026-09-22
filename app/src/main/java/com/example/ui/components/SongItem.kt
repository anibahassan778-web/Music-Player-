package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
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
import androidx.compose.ui.draw.scale
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
    modifier: Modifier = Modifier,
    menuActionText: String? = null,
    menuActionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onEditSongInfo: (() -> Unit)? = null,
    onEditLyrics: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "item_press_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "song_item_bg"
    )

    val artScale by animateFloatAsState(
        targetValue = if (isCurrent && isPlaying) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_art_scale"
    )

    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.30f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "item_fav_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .then(
                if (isCurrent) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp)
                ) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSongClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art thumbnail with responsive scale
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(artScale)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.albumArtUri ?: R.drawable.img_album_art_placeholder_1790011366041,
                contentDescription = song.title,
                placeholder = painterResource(id = R.drawable.img_album_art_placeholder_1790011366041),
                error = painterResource(id = R.drawable.img_album_art_placeholder_1790011366041),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp)
            )
            if (isCurrent && isPlaying) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    AudioVisualizer(
                        isPlaying = true,
                        barCount = 3,
                        barWidth = 3.dp,
                        maxHeight = 18.dp,
                        minHeight = 4.dp,
                        color = MaterialTheme.colorScheme.primary
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

        // Quick favorite button with bouncy animation
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(36.dp)
                .scale(favScale)
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
                    text = { Text(menuActionText ?: stringResource(R.string.add_to_playlist)) },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylist()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = menuActionIcon ?: Icons.Outlined.PlaylistAdd,
                            contentDescription = null
                        )
                    }
                )

                if (onEditSongInfo != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_song_info)) },
                        onClick = {
                            menuExpanded = false
                            onEditSongInfo()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                }

                if (onEditLyrics != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.lyrics)) },
                        onClick = {
                            menuExpanded = false
                            onEditLyrics()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}
