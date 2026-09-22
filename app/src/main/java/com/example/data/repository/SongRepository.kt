package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Song
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongRepository(private val context: Context) {

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

        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%') AND ${MediaStore.Audio.Media.DURATION} >= 1000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                parseSongsFromCursor(cursor, songList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: If no songs found with the filter, query all audio files
        if (songList.isEmpty()) {
            try {
                context.contentResolver.query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->
                    parseSongsFromCursor(cursor, songList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Also load internally imported songs
        val importedDir = File(context.filesDir, "imported_audio")
        if (importedDir.exists()) {
            importedDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".mp3") || it.name.endsWith(".wav") || it.name.endsWith(".m4a") || it.name.endsWith(".ogg") || it.name.endsWith(".flac") || it.name.endsWith(".aac")) }?.forEach { file ->
                val song = extractSongFromFile(file)
                if (songList.none { it.data == file.absolutePath || it.id == song.id }) {
                    songList.add(song)
                }
            }
        }

        songList
    }

    private fun extractSongFromFile(file: File): Song {
        var title = file.nameWithoutExtension.ifBlank { "Imported Track" }
        var artist = "Local Audio"
        var album = "Imported"
        var duration = 0L
        var albumArtUri: String? = null

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val rTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val rArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val rAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val rDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!rTitle.isNullOrBlank()) title = rTitle
            if (!rArtist.isNullOrBlank()) artist = rArtist
            if (!rAlbum.isNullOrBlank()) album = rAlbum
            duration = rDuration?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val id = (file.absolutePath.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL)
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            contentUri = Uri.fromFile(file).toString(),
            albumArtUri = albumArtUri,
            data = file.absolutePath
        )
    }

    private fun parseSongsFromCursor(cursor: Cursor, outList: MutableList<Song>) {
        val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
        val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

        while (cursor.moveToNext()) {
            val id = if (idColumn >= 0) cursor.getLong(idColumn) else System.currentTimeMillis()
            val rawTitle = if (titleColumn >= 0) cursor.getString(titleColumn) else null
            val rawArtist = if (artistColumn >= 0) cursor.getString(artistColumn) else null
            val rawAlbum = if (albumColumn >= 0) cursor.getString(albumColumn) else null
            val duration = if (durationColumn >= 0) cursor.getLong(durationColumn) else 0L
            val albumId = if (albumIdColumn >= 0) cursor.getLong(albumIdColumn) else -1L
            val data = if (dataColumn >= 0) cursor.getString(dataColumn) ?: "" else ""

            val title = if (!rawTitle.isNullOrBlank()) rawTitle else {
                data.substringAfterLast('/').substringBeforeLast('.').ifBlank { "Audio Track" }
            }
            val artist = if (!rawArtist.isNullOrBlank() && rawArtist != "<unknown>") rawArtist else "Unknown Artist"
            val album = if (!rawAlbum.isNullOrBlank() && rawAlbum != "<unknown>") rawAlbum else "Unknown Album"

            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id
            ).toString()

            val albumArtUri = if (albumId >= 0) {
                ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ).toString()
            } else null

            outList.add(
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

    suspend fun getSongsFromUris(uris: List<Uri>): List<Song> = withContext(Dispatchers.IO) {
        val importedSongs = mutableListOf<Song>()
        val importedDir = File(context.filesDir, "imported_audio").apply { mkdirs() }

        for (uri in uris) {
            try {
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // Ignore if takePersistableUriPermission is not supported for this uri
                }

                // Copy file locally so playback is 100% reliable and permanent
                val fileName = (uri.lastPathSegment ?: "track_${System.currentTimeMillis()}").substringAfterLast('/')
                val cleanFileName = if (fileName.contains('.')) fileName else "$fileName.mp3"
                val destFile = File(importedDir, "${System.currentTimeMillis()}_$cleanFileName")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    val song = extractSongFromFile(destFile)
                    importedSongs.add(song)
                } else {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    val rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    val rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val duration = durationStr?.toLongOrNull() ?: 0L
                    retriever.release()

                    val fallbackName = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                    val title = if (!rawTitle.isNullOrBlank()) rawTitle else fallbackName ?: "Imported Track"
                    val artist = if (!rawArtist.isNullOrBlank()) rawArtist else "Local Audio"
                    val album = if (!rawAlbum.isNullOrBlank()) rawAlbum else "Imported"
                    val id = (uri.toString().hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL)

                    importedSongs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            contentUri = uri.toString(),
                            albumArtUri = null,
                            data = uri.toString()
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        importedSongs
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
