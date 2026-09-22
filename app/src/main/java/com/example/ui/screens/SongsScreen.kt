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
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun SongsScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    favoriteIds: Set<Long>,
    onSongClick: (Song) -> Unit,
    onShuffleAllClick: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onRescanSongs: () -> Unit,
    onImportAudioFiles: () -> Unit,
    modifier: Modifier = Modifier,
    onEditSongInfo: ((Song) -> Unit)? = null,
    onEditLyrics: ((Song) -> Unit)? = null
) {
    if (songs.isEmpty()) {
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
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.empty_songs_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.empty_songs_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRescanSongs,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("songs_screen_rescan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.rescan_library))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onImportAudioFiles,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("songs_screen_import_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.import_audio))
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("songs_list"),
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
                        text = stringResource(R.string.songs_count, songs.size),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = onShuffleAllClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("shuffle_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(stringResource(R.string.shuffle))
                    }
                }
            }

            items(songs, key = { it.id }) { song ->
                SongItem(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrent = currentSong?.id == song.id,
                    isFavorite = favoriteIds.contains(song.id),
                    onSongClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onEditSongInfo = if (onEditSongInfo != null) { { onEditSongInfo(song) } } else null,
                    onEditLyrics = if (onEditLyrics != null) { { onEditLyrics(song) } } else null,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
