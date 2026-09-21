package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FullPlayerModal
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PermissionScreen
import com.example.ui.components.QueueDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.ArtistsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SongsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current

    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    var usedFallbackMode by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadSongs()
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

    var selectedTab by remember { mutableIntStateOf(0) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showQueueDialog by remember { mutableStateOf(false) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistForSong by remember { mutableStateOf(false) }

    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadSongs()
        }
    }

    if (!hasPermission && !usedFallbackMode) {
        PermissionScreen(
            onRequestPermission = { permissionLauncher.launch(requiredPermission) },
            onUseDemoTracks = {
                usedFallbackMode = true
                viewModel.loadDemoTracks()
            }
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
                    // Mini Player
                    if (playerState.currentSong != null) {
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
                when (selectedTab) {
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
                        onLoadDemoTracks = { viewModel.loadDemoTracks() }
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
                        onAddToPlaylist = { songToAddToPlaylist = it }
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
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onToggleFavorite = { viewModel.toggleFavorite(playerState.currentSong!!) },
                onOpenSleepTimer = { showSleepTimerDialog = true },
                onOpenAddToPlaylist = { songToAddToPlaylist = playerState.currentSong },
                onOpenQueue = { showQueueDialog = true },
                onDismiss = { showFullPlayer = false }
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
    }
}
