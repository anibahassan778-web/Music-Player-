package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R
import com.example.domain.model.PlayerUiState
import com.example.domain.model.RepeatMode
import com.example.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicService : MediaSessionService() {

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private val binder = MusicBinder()
    private var mediaSession: MediaSession? = null
    lateinit var player: ExoPlayer
        private set

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    // Internal state flows
    private val _currentSong = MutableStateFlow<Song?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _isShuffle = MutableStateFlow(false)
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    private val _currentIndex = MutableStateFlow(-1)
    private val _sleepTimerRemainingSeconds = MutableStateFlow<Long?>(null)

    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState = _playerUiState.asStateFlow()

    private fun updateUiState() {
        _playerUiState.value = PlayerUiState(
            currentSong = _currentSong.value,
            isPlaying = _isPlaying.value,
            currentPosition = _currentPosition.value,
            duration = _duration.value,
            isShuffle = _isShuffle.value,
            repeatMode = _repeatMode.value,
            queue = _queue.value,
            currentIndex = _currentIndex.value,
            sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.value
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Audio Focus automatically handled
            .setHandleAudioBecomingNoisy(true)        // Pauses when earphones are unplugged
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                updateUiState()
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSongFromPlayer()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0L)
                    _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                    updateUiState()
                } else if (playbackState == Player.STATE_ENDED) {
                    handlePlaybackEnded()
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffle.value = shuffleModeEnabled
                updateUiState()
            }

            override fun onRepeatModeChanged(playerRepeatMode: Int) {
                _repeatMode.value = when (playerRepeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                updateUiState()
            }
        })

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onBind(intent: Intent?): IBinder {
        val action = intent?.action
        if (action == "androidx.media3.session.MediaSessionService") {
            return super.onBind(intent) ?: binder
        }
        return binder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.playback_channel_name)
            val descriptionText = getString(R.string.playback_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("music_player_channel", name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // --- Playback Control Methods ---

    fun playSongList(songs: List<Song>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        if (songs.isEmpty()) return
        _queue.value = songs
        _currentIndex.value = startIndex.coerceIn(0, songs.lastIndex)

        val mediaItems = songs.map { song ->
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)

            song.albumArtUri?.let {
                metadataBuilder.setArtworkUri(Uri.parse(it))
            }

            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }

        player.setMediaItems(mediaItems, _currentIndex.value, startPositionMs)
        player.prepare()
        player.play()
        updateCurrentSongFromPlayer()
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.play()
        }
    }

    fun playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (_repeatMode.value == RepeatMode.ALL && _queue.value.isNotEmpty()) {
            player.seekTo(0, 0L)
            player.play()
        }
    }

    fun playPrevious() {
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else if (_queue.value.isNotEmpty()) {
            player.seekTo(0, 0L)
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPosition.value = positionMs
        updateUiState()
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffle.value
        player.shuffleModeEnabled = newShuffle
        _isShuffle.value = newShuffle
        updateUiState()
    }

    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        val exoRepeat = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        player.repeatMode = exoRepeat
        _repeatMode.value = nextMode
        updateUiState()
    }

    fun playQueueIndex(index: Int) {
        if (index in 0 until player.mediaItemCount) {
            player.seekToDefaultPosition(index)
            player.play()
        }
    }

    fun removeQueueItem(index: Int) {
        if (index in 0 until player.mediaItemCount) {
            player.removeMediaItem(index)
            val updatedQueue = _queue.value.toMutableList()
            if (index in updatedQueue.indices) {
                updatedQueue.removeAt(index)
                _queue.value = updatedQueue
                updateUiState()
            }
        }
    }

    fun clearQueue() {
        player.clearMediaItems()
        _queue.value = emptyList()
        _currentSong.value = null
        _currentIndex.value = -1
        _currentPosition.value = 0L
        _duration.value = 0L
        updateUiState()
    }

    // --- Sleep Timer ---

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingSeconds.value = null
            updateUiState()
            return
        }

        val totalSeconds = minutes * 60L
        _sleepTimerRemainingSeconds.value = totalSeconds
        updateUiState()

        sleepTimerJob = serviceScope.launch {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
                updateUiState()
            }
            if (remaining <= 0) {
                player.pause()
                _sleepTimerRemainingSeconds.value = null
                updateUiState()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSeconds.value = null
        updateUiState()
    }

    private fun handlePlaybackEnded() {
        if (_repeatMode.value == RepeatMode.ALL && _queue.value.isNotEmpty()) {
            player.seekTo(0, 0L)
            player.play()
        }
    }

    private fun updateCurrentSongFromPlayer() {
        val currentMediaItemIndex = player.currentMediaItemIndex
        _currentIndex.value = currentMediaItemIndex
        val currentQueue = _queue.value
        if (currentMediaItemIndex in currentQueue.indices) {
            _currentSong.value = currentQueue[currentMediaItemIndex]
        } else {
            _currentSong.value = null
        }
        _duration.value = player.duration.coerceAtLeast(0L)
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
        updateUiState()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                    _duration.value = player.duration.coerceAtLeast(0L)
                    updateUiState()
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
        updateUiState()
    }

    override fun onDestroy() {
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
