package com.example.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MusicDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongEntity
import com.example.data.repository.SongRepository
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.PlayerUiState
import com.example.domain.model.Playlist
import com.example.domain.model.RepeatMode
import com.example.domain.model.Song
import com.example.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = MusicDatabase.getDatabase(context)
    private val musicDao = database.musicDao()
    private val songRepository = SongRepository(context)
    private val preferencesManager = PreferencesManager(context)

    private var musicService: MusicService? = null
    private var isBound = false

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoadingSongs = MutableStateFlow(false)
    val isLoadingSongs = _isLoadingSongs.asStateFlow()

    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    // Filtered songs according to search query
    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery) { songList, query ->
        if (query.isBlank()) {
            songList
        } else {
            songList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Albums
    val albums: StateFlow<List<Album>> = _songs.combine(_searchQuery) { songList, query ->
        val grouped = songRepository.groupSongsByAlbum(songList)
        if (query.isBlank()) grouped else {
            grouped.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Artists
    val artists: StateFlow<List<Artist>> = _songs.combine(_searchQuery) { songList, query ->
        val grouped = songRepository.groupSongsByArtist(songList)
        if (query.isBlank()) grouped else {
            grouped.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites from Room
    val favorites: StateFlow<List<Song>> = musicDao.getAllFavorites().combine(_searchQuery) { favEntities, query ->
        val favSongs = favEntities.map { it.toSong() }
        if (query.isBlank()) favSongs else {
            favSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorite IDs set for fast check in lists
    val favoriteIds: StateFlow<Set<Long>> = musicDao.getAllFavorites().combine(_searchQuery) { favs, _ ->
        favs.map { it.songId }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Playlists from Room
    val playlists: StateFlow<List<Playlist>> = musicDao.getAllPlaylists().combine(musicDao.getAllFavorites()) { plistEntities, _ ->
        plistEntities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                songCount = 0,
                createdAt = entity.createdAt
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicBinder
            musicService = binder?.getService()
            isBound = true
            observeServiceState()
            restoreLastPlayedState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    init {
        bindMusicService()
        loadSongs()
    }

    private fun bindMusicService() {
        val intent = Intent(context, MusicService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceState() {
        val service = musicService ?: return
        viewModelScope.launch {
            service.playerUiState.collect { state ->
                _playerUiState.value = state
                // Save last played periodically when playing or paused
                state.currentSong?.let { song ->
                    viewModelScope.launch {
                        preferencesManager.saveLastPlayed(song.id, state.currentPosition)
                    }
                }
            }
        }
    }

    private fun restoreLastPlayedState() {
        viewModelScope.launch {
            val lastSongId = preferencesManager.lastPlayedSongId.first()
            val lastPos = preferencesManager.lastPlayedPosition.first()
            if (lastSongId != null && _playerUiState.value.currentSong == null) {
                val foundSong = _songs.value.find { it.id == lastSongId }
                if (foundSong != null) {
                    _playerUiState.value = _playerUiState.value.copy(
                        currentSong = foundSong,
                        currentPosition = lastPos,
                        duration = foundSong.duration
                    )
                }
            }
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            _isLoadingSongs.value = true
            val deviceSongs = songRepository.getSongsFromDevice()
            if (deviceSongs.isNotEmpty()) {
                _songs.value = deviceSongs
            } else {
                // If device has no songs, provide sample tracks so user can test the player right away
                _songs.value = songRepository.demoSongs
            }
            _isLoadingSongs.value = false
            restoreLastPlayedState()
        }
    }

    fun loadDemoTracks() {
        _songs.value = songRepository.demoSongs
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // --- Playback Controls ---

    fun playSong(song: Song, playlist: List<Song> = _songs.value) {
        val index = playlist.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        musicService?.playSongList(playlist, index)
    }

    fun playPause() {
        val currentSong = _playerUiState.value.currentSong
        if (currentSong == null && _songs.value.isNotEmpty()) {
            playSong(_songs.value.first())
        } else {
            musicService?.playPause()
        }
    }

    fun playNext() {
        musicService?.playNext()
    }

    fun playPrevious() {
        musicService?.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        musicService?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        musicService?.toggleShuffle()
    }

    fun cycleRepeatMode() {
        musicService?.cycleRepeatMode()
    }

    fun playQueueIndex(index: Int) {
        musicService?.playQueueIndex(index)
    }

    fun removeQueueItem(index: Int) {
        musicService?.removeQueueItem(index)
    }

    fun clearQueue() {
        musicService?.clearQueue()
    }

    // --- Sleep Timer ---

    fun setSleepTimer(minutes: Int) {
        musicService?.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        musicService?.cancelSleepTimer()
    }

    // --- Favorites ---

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFav = musicDao.isFavoriteDirect(song.id)
            if (isFav) {
                musicDao.deleteFavorite(song.id)
            } else {
                musicDao.insertFavorite(
                    FavoriteEntity(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        duration = song.duration,
                        contentUri = song.contentUri,
                        albumArtUri = song.albumArtUri
                    )
                )
            }
        }
    }

    // --- Playlists ---

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            musicDao.insertPlaylist(PlaylistEntity(name = name.trim()))
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            musicDao.updatePlaylist(PlaylistEntity(id = playlistId, name = newName.trim()))
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicDao.deleteAllSongsFromPlaylist(playlistId)
            musicDao.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            musicDao.insertSongToPlaylist(
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    duration = song.duration,
                    contentUri = song.contentUri,
                    albumArtUri = song.albumArtUri
                )
            )
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getSongsForPlaylist(playlistId: Long): StateFlow<List<Song>> {
        return musicDao.getSongsForPlaylist(playlistId)
            .combine(_searchQuery) { entities, _ ->
                entities.map { it.toSong() }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    override fun onCleared() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        super.onCleared()
    }
}
