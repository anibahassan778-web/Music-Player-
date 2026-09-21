package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // --- Favorites ---
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavoriteDirect(songId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)

    // --- Playlists ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteAllSongsFromPlaylist(playlistId: Long)

    // --- Playlist Songs ---
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY addedAt ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(song: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
}
