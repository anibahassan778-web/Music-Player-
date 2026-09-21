package com.example.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val contentUri: String,
    val albumArtUri: String? = null,
    val data: String = ""
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Album(
    val name: String,
    val artist: String,
    val albumArtUri: String?,
    val songCount: Int,
    val songs: List<Song>
)

data class Artist(
    val name: String,
    val songCount: Int,
    val songs: List<Song>
)

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val sleepTimerRemainingSeconds: Long? = null
)
