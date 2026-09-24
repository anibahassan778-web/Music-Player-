package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.EditLyricsDialog
import com.example.ui.components.EditSongInfoDialog
import com.example.ui.components.FullPlayerModal
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PermissionScreen
import com.example.ui.components.QueueDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.ArtistsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongsScreen

@Composable
fun MainScreen(
    viewModel: MusicViewModel = viewModel()
) {
    val currentRegistryOwner = LocalActivityResultRegistryOwner.current
    if (currentRegistryOwner == null) {
        val dummyOwner = remember {
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(
                        requestCode: Int,
                        contract: ActivityResultContract<I, O>,
                        input: I,
                        options: ActivityOptionsCompat?
                    ) {}
                }
            }
        }
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides dummyOwner) {
            MainScreenContent(viewModel = viewModel)
        }
    } else {
        MainScreenContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    viewModel: MusicViewModel
) {
    val context = LocalContext.current

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var hasAudioPermission by remember {
        val primaryPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, primaryPerm) == PackageManager.PERMISSION_GRANTED
        )
    }

    var usedFallbackMode by remember { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val primaryPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val isAudioGranted = results[primaryPerm] == true
        hasAudioPermission = isAudioGranted
        if (isAudioGranted) {
            viewModel.loadSongs()
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.importAudioUris(uris)
            usedFallbackMode = true
        }
    }

    // States from ViewModel
    val songs by viewModel.filteredSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playerState by viewModel.playerUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showQueueDialog by remember { mutableStateOf(false) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistForSong by remember { mutableStateOf(false) }
    var songToEditInfo by remember { mutableStateOf<Song?>(null) }
    var songToEditLyrics by remember { mutableStateOf<Song?>(null) }

    val currentSongLyrics by produceState<String?>(initialValue = null, key1 = playerState.currentSong?.id) {
        val songId = playerState.currentSong?.id
        if (songId != null) {
            viewModel.getLyricsForSong(songId).collect { value = it?.lyricsText }
        } else {
            value = null
        }
    }

    var isSearchActive by remember { mutableStateOf(false) }

    BackHandler(enabled = showSettingsScreen || showFullPlayer || isSearchActive) {
        when {
            showFullPlayer -> showFullPlayer = false
            showSettingsScreen -> showSettingsScreen = false
            isSearchActive -> isSearchActive = false
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            viewModel.loadSongs()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val primaryPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                val granted = ContextCompat.checkSelfPermission(context, primaryPerm) == PackageManager.PERMISSION_GRANTED
                if (granted != hasAudioPermission) {
                    hasAudioPermission = granted
                    if (granted) {
                        viewModel.loadSongs()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!hasAudioPermission && !usedFallbackMode && songs.isEmpty()) {
        PermissionScreen(
            onRequestPermission = { permissionsLauncher.launch(requiredPermissions.toTypedArray()) },
            onOpenAppSettings = {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onImportAudioFiles = {
                audioPickerLauncher.launch(arrayOf("audio/*"))
            },
            onContinueToApp = {
                usedFallbackMode = true
                viewModel.loadSongs()
            }
        )
    } else if (showSettingsScreen) {
        SettingsScreen(
            appSettings = appSettings,
            onNavigateBack = { showSettingsScreen = false },
            onUpdateFontPreset = { viewModel.updateFontPreset(it) },
            onUpdateLanguageCode = { viewModel.updateLanguageCode(it) },
            onUpdateFontScale = { viewModel.updateFontScale(it) },
            onImportCustomFont = { uri, name, cb -> viewModel.importCustomTtfFont(uri, name, cb) },
            onRemoveCustomFont = { viewModel.removeCustomTtfFont() },
            onUpdateCrossfadeSeconds = { viewModel.updateCrossfadeSeconds(it) }
        )
    } else {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (isSearchActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (searchQuery.isNotEmpty()) {
                                            viewModel.onSearchQueryChanged("")
                                        } else {
                                            isSearchActive = false
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_input_field"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    } else {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { isSearchActive = true },
                                    modifier = Modifier.testTag("search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                }
                                IconButton(
                                    onClick = { showSettingsScreen = true },
                                    modifier = Modifier.testTag("settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.settings)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini Player with smooth enter/exit animation
                    AnimatedVisibility(
                        visible = playerState.currentSong != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(250)),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(200))
                    ) {
                        MiniPlayer(
                            song = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            currentPosition = playerState.currentPosition,
                            duration = playerState.duration,
                            onPlayPauseClick = { viewModel.playPause() },
                            onNextClick = { viewModel.playNext() },
                            onClick = { showFullPlayer = true }
                        )
                    }

                    // Navigation Bar (5 tabs)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // Tab 0: Songs
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.MusicNote else Icons.Outlined.MusicNote,
                                    contentDescription = stringResource(R.string.tab_songs)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_songs)) },
                            modifier = Modifier.testTag("nav_tab_songs")
                        )

                        // Tab 1: Albums
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Default.Album else Icons.Outlined.Album,
                                    contentDescription = stringResource(R.string.tab_albums)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_albums)) },
                            modifier = Modifier.testTag("nav_tab_albums")
                        )

                        // Tab 2: Artists
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Default.Person else Icons.Outlined.Person,
                                    contentDescription = stringResource(R.string.tab_artists)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_artists)) },
                            modifier = Modifier.testTag("nav_tab_artists")
                        )

                        // Tab 3: Favorites
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 3) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(R.string.tab_favorites)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_favorites)) },
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )

                        // Tab 4: Playlists
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 4) Icons.Default.PlaylistPlay else Icons.Outlined.PlaylistPlay,
                                    contentDescription = stringResource(R.string.tab_playlists)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_playlists)) },
                            modifier = Modifier.testTag("nav_tab_playlists")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
                            ) + fadeIn(tween(240))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(200),
                                targetOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() }
                            ) + fadeOut(tween(180)))
                        } else {
                            (slideInHorizontally(
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                initialOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() }
                            ) + fadeIn(tween(240))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(200),
                                targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
                            ) + fadeOut(tween(180)))
                        }
                    },
                    label = "tab_screens_transition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> SongsScreen(
                            songs = songs,
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            favoriteIds = favoriteIds,
                            onSongClick = { song -> viewModel.playSong(song, songs) },
                            onShuffleAllClick = {
                                if (songs.isNotEmpty()) {
                                    val shuffled = songs.shuffled()
                                    viewModel.playSong(shuffled.first(), shuffled)
                                }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { songToAddToPlaylist = it },
                            onRescanSongs = { viewModel.loadSongs() },
                            onImportAudioFiles = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                            onEditSongInfo = { songToEditInfo = it },
                            onEditLyrics = { songToEditLyrics = it }
                        )
                        1 -> AlbumsScreen(
                            albums = albums,
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            favoriteIds = favoriteIds,
                            onSongClick = { song, albumSongs -> viewModel.playSong(song, albumSongs) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { songToAddToPlaylist = it }
                        )
                        2 -> ArtistsScreen(
                            artists = artists,
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            favoriteIds = favoriteIds,
                            onSongClick = { song, artistSongs -> viewModel.playSong(song, artistSongs) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { songToAddToPlaylist = it }
                        )
                        3 -> FavoritesScreen(
                            favorites = favorites,
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            favoriteIds = favoriteIds,
                            onSongClick = { song, favSongs -> viewModel.playSong(song, favSongs) },
                            onPlayAllClick = {
                                if (favorites.isNotEmpty()) {
                                    viewModel.playSong(favorites.first(), favorites)
                                }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { songToAddToPlaylist = it },
                            onEditSongInfo = { songToEditInfo = it },
                            onEditLyrics = { songToEditLyrics = it }
                        )
                        4 -> PlaylistsScreen(
                            playlists = playlists,
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            favoriteIds = favoriteIds,
                            onCreatePlaylist = { viewModel.createPlaylist(it) },
                            onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onSongClick = { song, pSongs -> viewModel.playSong(song, pSongs) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onRemoveSongFromPlaylist = { pId, sId -> viewModel.removeSongFromPlaylist(pId, sId) },
                            getPlaylistSongs = { viewModel.getSongsForPlaylist(it) }
                        )
                    }
                }
            }
        }

        // Full Player Modal
        if (showFullPlayer && playerState.currentSong != null) {
            val isFav = favoriteIds.contains(playerState.currentSong!!.id)
            FullPlayerModal(
                playerState = playerState,
                isFavorite = isFav,
                onPlayPause = { viewModel.playPause() },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onSeekTo = { viewModel.seekTo(it) },
                onSeekBy = { viewModel.seekBy(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onToggleFavorite = { viewModel.toggleFavorite(playerState.currentSong!!) },
                onOpenSleepTimer = { showSleepTimerDialog = true },
                onOpenAddToPlaylist = { songToAddToPlaylist = playerState.currentSong },
                onOpenQueue = { showQueueDialog = true },
                onDismiss = { showFullPlayer = false },
                lyricsText = currentSongLyrics,
                onOpenLyricsEditor = { songToEditLyrics = playerState.currentSong },
                onOpenSongInfoEditor = { songToEditInfo = playerState.currentSong }
            )
        }

        // Sleep Timer Dialog
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                remainingSeconds = playerState.sleepTimerRemainingSeconds,
                onSetTimer = { viewModel.setSleepTimer(it) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                onDismiss = { showSleepTimerDialog = false }
            )
        }

        // Queue Dialog
        if (showQueueDialog) {
            QueueDialog(
                queue = playerState.queue,
                currentIndex = playerState.currentIndex,
                onPlayIndex = { viewModel.playQueueIndex(it) },
                onRemoveIndex = { viewModel.removeQueueItem(it) },
                onClearQueue = { viewModel.clearQueue() },
                onDismiss = { showQueueDialog = false }
            )
        }

        // Add to Playlist Dialog
        songToAddToPlaylist?.let { song ->
            AddToPlaylistDialog(
                song = song,
                playlists = playlists,
                onSelectPlaylist = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, song)
                    songToAddToPlaylist = null
                },
                onCreateNewPlaylist = {
                    showCreatePlaylistForSong = true
                },
                onDismiss = { songToAddToPlaylist = null }
            )
        }

        // Create playlist dialog triggered from Add to Playlist
        if (showCreatePlaylistForSong) {
            CreatePlaylistDialog(
                onConfirm = { name ->
                    viewModel.createPlaylist(name)
                    showCreatePlaylistForSong = false
                },
                onDismiss = { showCreatePlaylistForSong = false }
            )
        }

        // Edit Song Metadata / ID3 Info Dialog
        songToEditInfo?.let { song ->
            EditSongInfoDialog(
                song = song,
                onSave = { title, artist, album, artworkUri ->
                    viewModel.updateSongMetadata(song.id, title, artist, album, artworkUri)
                    songToEditInfo = null
                },
                onReset = {
                    viewModel.deleteSongCustomMetadata(song.id)
                    songToEditInfo = null
                },
                onDismiss = { songToEditInfo = null }
            )
        }

        // Edit Lyrics Dialog
        songToEditLyrics?.let { song ->
            val editingLyrics by produceState(initialValue = "", key1 = song.id) {
                viewModel.getLyricsForSong(song.id).collect { value = it?.lyricsText ?: "" }
            }
            EditLyricsDialog(
                song = song,
                initialLyrics = editingLyrics,
                onSave = { lyrics ->
                    viewModel.saveLyricsForSong(song.id, lyrics)
                    songToEditLyrics = null
                },
                onDelete = {
                    viewModel.deleteLyricsForSong(song.id)
                    songToEditLyrics = null
                },
                onDismiss = { songToEditLyrics = null }
            )
        }
    }
}
