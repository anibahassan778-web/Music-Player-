package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Playlist
import com.example.domain.model.Song

@Composable
fun CreatePlaylistDialog(
    initialName: String = "",
    isRename: Boolean = false,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var playlistName by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isRename) stringResource(R.string.rename) else stringResource(R.string.create_playlist),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text(stringResource(R.string.playlist_name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playlist_name_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onConfirm(playlistName.trim())
                    }
                },
                enabled = playlistName.isNotBlank(),
                modifier = Modifier.testTag("confirm_playlist_button")
            ) {
                Text(if (isRename) stringResource(R.string.save) else stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_playlist_button")
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    song: Song,
    playlists: List<Playlist>,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_to_playlist),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Create new playlist row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onDismiss()
                            onCreateNewPlaylist()
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.create_playlist),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = stringResource(R.string.playlists_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSelectPlaylist(playlist)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
