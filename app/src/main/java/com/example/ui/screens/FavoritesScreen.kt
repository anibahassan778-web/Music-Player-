package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Song
import com.example.ui.components.SongItem

@Composable
fun FavoritesScreen(
    favorites: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    favoriteIds: Set<Long>,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAllClick: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.favorites_empty),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("favorites_list"),
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.songs_count, favorites.size),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = onPlayAllClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("play_all_favorites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(stringResource(R.string.play))
                    }
                }
            }

            items(favorites, key = { it.id }) { song ->
                SongItem(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrent = currentSong?.id == song.id,
                    isFavorite = true,
                    onSongClick = { onSongClick(song, favorites) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) }
                )
            }
        }
    }
}
