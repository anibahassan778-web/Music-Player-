package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.example.R
import com.example.domain.model.PlayerUiState
import com.example.domain.model.RepeatMode
import com.example.domain.model.Song
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
class MusicService : MediaSessionService() {

    companion object {
        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_REWIND_10 = "com.example.action.REWIND_10"
        const val ACTION_FORWARD_10 = "com.example.action.FORWARD_10"
        const val NOTIFICATION_CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 1001
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private val binder = MusicBinder()
    private var mediaSession: MediaSession? = null
    lateinit var player: ExoPlayer
        private set

    private lateinit var rewindButton: CommandButton
    private lateinit var forwardButton: CommandButton

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var artworkJob: Job? = null

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
    var crossfadeSeconds: Int = 0

    private fun updateUiState() {
        val song = _currentSong.value
        val playing = _isPlaying.value
        _playerUiState.value = PlayerUiState(
            currentSong = song,
            isPlaying = playing,
            currentPosition = _currentPosition.value,
            duration = _duration.value,
            isShuffle = _isShuffle.value,
            repeatMode = _repeatMode.value,
            queue = _queue.value,
            currentIndex = _currentIndex.value,
            sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.value
        )
        MusicAppWidgetProvider.updateAllWidgets(applicationContext, song, playing)
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
            .setWakeMode(C.WAKE_MODE_LOCAL)           // Prevents CPU sleeping during background playback
            .setSeekBackIncrementMs(10000L)          // 10s seek back
            .setSeekForwardIncrementMs(10000L)       // 10s seek forward
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

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("MusicService", "Playback exception: ${error.errorCodeName}: ${error.message}", error)
                _isPlaying.value = false
                updateUiState()
            }
        })

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val rewindCommand = SessionCommand(ACTION_REWIND_10, Bundle.EMPTY)
        val forwardCommand = SessionCommand(ACTION_FORWARD_10, Bundle.EMPTY)

        rewindButton = CommandButton.Builder()
            .setDisplayName(getString(R.string.replay_10))
            .setIconResId(R.drawable.ic_replay_10)
            .setSessionCommand(rewindCommand)
            .setEnabled(true)
            .build()

        forwardButton = CommandButton.Builder()
            .setDisplayName(getString(R.string.forward_10))
            .setIconResId(R.drawable.ic_forward_10)
            .setSessionCommand(forwardCommand)
            .setEnabled(true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(MediaSessionCallback())
            .setCustomLayout(listOf(rewindButton, forwardButton))
            .build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setChannelName(R.string.playback_channel_name)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        setMediaNotificationProvider(notificationProvider)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playPause()
            ACTION_PLAY -> if (!player.isPlaying) player.play()
            ACTION_PAUSE -> if (player.isPlaying) player.pause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
            ACTION_REWIND_10 -> seekBy(-10000L)
            ACTION_FORWARD_10 -> seekBy(10000L)
            ACTION_STOP -> {
                player.stop()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onBind(intent: Intent?): IBinder? {
        val superBinder = super.onBind(intent)
        if (superBinder != null) {
            return superBinder
        }
        return binder
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(ACTION_REWIND_10, Bundle.EMPTY))
                .add(SessionCommand(ACTION_FORWARD_10, Bundle.EMPTY))
                .build()

            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_SEEK_FORWARD)
                .add(Player.COMMAND_STOP)
                .add(Player.COMMAND_SET_SHUFFLE_MODE)
                .add(Player.COMMAND_SET_REPEAT_MODE)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .setCustomLayout(listOf(rewindButton, forwardButton))
                .build()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val currentQueue = _queue.value
            val currentIndex = _currentIndex.value.coerceAtLeast(0)
            val currentPos = _currentPosition.value
            if (currentQueue.isNotEmpty() && currentIndex in currentQueue.indices) {
                val mediaItems = currentQueue.map { buildMediaItem(it) }
                return Futures.immediateFuture(
                    MediaSession.MediaItemsWithStartPosition(
                        mediaItems,
                        currentIndex,
                        currentPos
                    )
                )
            }
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_REWIND_10 -> {
                    seekBy(-10000L)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                ACTION_FORWARD_10 -> {
                    seekBy(10000L)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.playback_channel_name)
            val descriptionText = getString(R.string.playback_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // --- Playback Control Methods ---

    private fun buildMediaItem(song: Song, artworkBytes: ByteArray? = null): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)

        if (artworkBytes != null) {
            metadataBuilder.setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        song.albumArtUri?.let {
            val artUri = if (it.startsWith("content://") || it.startsWith("file://") || it.startsWith("http")) {
                Uri.parse(it)
            } else {
                Uri.fromFile(java.io.File(it))
            }
            metadataBuilder.setArtworkUri(artUri)
        }

        val parsedUri = if (song.contentUri.startsWith("content://") || song.contentUri.startsWith("file://") || song.contentUri.startsWith("http")) {
            Uri.parse(song.contentUri)
        } else {
            Uri.fromFile(java.io.File(song.contentUri))
        }

        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(parsedUri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    private fun loadArtworkBytes(song: Song): ByteArray? {
        if (!song.albumArtUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(song.albumArtUri)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        return compressBitmapForNotification(bitmap)
                    }
                }
            } catch (e: Exception) {
                // Ignore and fall through to fallback
            }
        }
        return try {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.img_album_art_placeholder_1790011366041)
            if (bitmap != null) {
                compressBitmapForNotification(bitmap)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun compressBitmapForNotification(bitmap: Bitmap): ByteArray {
        val maxDim = 400
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }
        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }

    private var fadeJob: Job? = null

    private fun rampVolume(from: Float, to: Float, durationMs: Long, onEnd: () -> Unit = {}) {
        fadeJob?.cancel()
        if (durationMs <= 0) {
            player.volume = to
            onEnd()
            return
        }
        fadeJob = serviceScope.launch {
            val steps = 15
            val stepDelay = (durationMs / steps).coerceAtLeast(16L)
            for (i in 0..steps) {
                val fraction = i.toFloat() / steps
                val vol = from + (to - from) * fraction
                player.volume = vol.coerceIn(0f, 1f)
                delay(stepDelay)
            }
            player.volume = to
            onEnd()
        }
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        if (songs.isEmpty()) return
        _queue.value = songs
        _currentIndex.value = startIndex.coerceIn(0, songs.lastIndex)

        val mediaItems = songs.map { song -> buildMediaItem(song) }

        player.setMediaItems(mediaItems, _currentIndex.value, startPositionMs)
        player.prepare()
        if (crossfadeSeconds > 0) {
            player.volume = 0f
            player.play()
            rampVolume(0f, 1f, (crossfadeSeconds * 400L).coerceIn(300L, 2500L))
        } else {
            player.volume = 1f
            player.play()
        }
        updateCurrentSongFromPlayer()
    }

    fun playPause() {
        if (player.isPlaying) {
            if (crossfadeSeconds > 0) {
                rampVolume(1f, 0f, 250L) {
                    player.pause()
                    player.volume = 1f
                }
            } else {
                player.pause()
            }
        } else {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            if (crossfadeSeconds > 0) {
                player.volume = 0f
                player.play()
                rampVolume(0f, 1f, 300L)
            } else {
                player.volume = 1f
                player.play()
            }
        }
    }

    fun playNext() {
        if (crossfadeSeconds > 0 && player.isPlaying) {
            rampVolume(1f, 0.1f, 300L) {
                executePlayNext()
                rampVolume(0.1f, 1f, 400L)
            }
        } else {
            executePlayNext()
        }
    }

    private fun executePlayNext() {
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
        } else if (crossfadeSeconds > 0 && player.isPlaying) {
            rampVolume(1f, 0.1f, 300L) {
                executePlayPrevious()
                rampVolume(0.1f, 1f, 400L)
            }
        } else {
            executePlayPrevious()
        }
    }

    private fun executePlayPrevious() {
        if (player.hasPreviousMediaItem()) {
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

    fun seekBy(offsetMs: Long) {
        val cur = player.currentPosition
        val dur = player.duration
        val target = if (dur > 0) {
            (cur + offsetMs).coerceIn(0L, dur)
        } else {
            (cur + offsetMs).coerceAtLeast(0L)
        }
        seekTo(target)
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
        val song = if (currentMediaItemIndex in currentQueue.indices) {
            currentQueue[currentMediaItemIndex]
        } else {
            null
        }
        _currentSong.value = song
        _duration.value = player.duration.coerceAtLeast(0L)
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
        updateUiState()

        if (song != null) {
            artworkJob?.cancel()
            artworkJob = serviceScope.launch(Dispatchers.IO) {
                val bytes = loadArtworkBytes(song)
                if (bytes != null && isActive && player.currentMediaItemIndex == currentMediaItemIndex) {
                    withContext(Dispatchers.Main) {
                        val currentItem = player.currentMediaItem ?: return@withContext
                        val currentMetadata = currentItem.mediaMetadata
                        if (currentMetadata.artworkData == null) {
                            val updatedMetadata = currentMetadata.buildUpon()
                                .setArtworkData(bytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                .build()
                            val updatedItem = currentItem.buildUpon()
                                .setMediaMetadata(updatedMetadata)
                                .build()
                            if (currentMediaItemIndex in 0 until player.mediaItemCount) {
                                player.replaceMediaItem(currentMediaItemIndex, updatedItem)
                            }
                        }
                    }
                }
            }
        }
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        val sessionPlayer = mediaSession?.player
        if (sessionPlayer == null || !sessionPlayer.playWhenReady || sessionPlayer.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        artworkJob?.cancel()
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
