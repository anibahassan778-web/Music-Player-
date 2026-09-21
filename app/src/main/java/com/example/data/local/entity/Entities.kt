package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Song

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val contentUri: String,
    val albumArtUri: String?,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song = Song(
        id = songId,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        contentUri = contentUri,
        albumArtUri = albumArtUri
    )
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val contentUri: String,
    val albumArtUri: String?,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song = Song(
        id = songId,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        contentUri = contentUri,
        albumArtUri = albumArtUri
    )
}
