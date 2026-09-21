package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongRepository(private val context: Context) {

    // Default sample tracks (high quality royalty-free public domain / creative commons audio)
    // Used if storage is empty or user requests demo tracks
    val demoSongs: List<Song> = listOf(
        Song(
            id = -101L,
            title = "Acoustic Breeze",
            artist = "Benjamin Tissot",
            album = "Acoustic Dreams",
            duration = 158000L,
            contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            albumArtUri = null
        ),
        Song(
            id = -102L,
            title = "Sunny Horizon",
            artist = "SoundHelix Project",
            album = "Electronic Journeys",
            duration = 185000L,
            contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            albumArtUri = null
        ),
        Song(
            id = -103L,
            title = "Midnight Melody",
            artist = "Creative Sounds",
            album = "Nocturne Vibes",
            duration = 172000L,
            contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            albumArtUri = null
        ),
        Song(
            id = -104L,
            title = "Desert Caravan",
            artist = "Orient Ensemble",
            album = "Eastern Melodies",
            duration = 210000L,
            contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            albumArtUri = null
        ),
        Song(
            id = -105L,
            title = "Chillwave Groove",
            artist = "SoundHelix Project",
            album = "Electronic Journeys",
            duration = 198000L,
            contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            albumArtUri = null
        )
    )

    suspend fun getSongsFromDevice(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val data = if (dataColumn >= 0) cursor.getString(dataColumn) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            data = data
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songList
    }

    fun groupSongsByAlbum(songs: List<Song>): List<Album> {
        return songs.groupBy { it.album }
            .map { (albumName, albumSongs) ->
                Album(
                    name = albumName,
                    artist = albumSongs.firstOrNull()?.artist ?: "Unknown Artist",
                    albumArtUri = albumSongs.firstOrNull()?.albumArtUri,
                    songCount = albumSongs.size,
                    songs = albumSongs
                )
            }
            .sortedBy { it.name }
    }

    fun groupSongsByArtist(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                Artist(
                    name = artistName,
                    songCount = artistSongs.size,
                    songs = artistSongs
                )
            }
            .sortedBy { it.name }
    }
}
