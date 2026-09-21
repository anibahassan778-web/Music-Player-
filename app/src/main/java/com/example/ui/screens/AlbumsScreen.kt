package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.domain.model.Album
import com.example.domain.model.Song
import com.example.ui.components.SongItem

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    currentSong: Song?,
    isPlaying: Boolean,
    favoriteIds: Set<Long>,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }

    if (albums.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_results_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = modifier
                .fillMaxSize()
                .testTag("albums_grid"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(albums) { album ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedAlbum = album }
                        .testTag("album_item_${album.name.hashCode()}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        AsyncImage(
                            model = album.albumArtUri,
                            contentDescription = album.name,
                            placeholder = painterResource(id = R.drawable.ic_default_album_art),
                            error = painterResource(id = R.drawable.ic_default_album_art),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = album.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = album.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.songs_count, album.songCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Album Detail Dialog
    selectedAlbum?.let { album ->
        AlertDialog(
            onDismissRequest = { selectedAlbum = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { selectedAlbum = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = {
                            if (album.songs.isNotEmpty()) {
                                onSongClick(album.songs.first(), album.songs)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(stringResource(R.string.play))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        items(album.songs) { song ->
                            SongItem(
                                song = song,
                                isPlaying = isPlaying,
                                isCurrent = currentSong?.id == song.id,
                                isFavorite = favoriteIds.contains(song.id),
                                onSongClick = { onSongClick(song, album.songs) },
                                onToggleFavorite = { onToggleFavorite(song) },
                                onAddToPlaylist = { onAddToPlaylist(song) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAlbum = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
