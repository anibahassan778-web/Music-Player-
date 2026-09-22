package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.PlayerUiState
import com.example.domain.model.RepeatMode
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FullPlayerModal(
    playerState: PlayerUiState,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBy: (Long) -> Unit = {},
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAddToPlaylist: () -> Unit,
    onOpenQueue: () -> Unit,
    onDismiss: () -> Unit,
    lyricsText: String? = null,
    onOpenLyricsEditor: () -> Unit = {},
    onOpenSongInfoEditor: () -> Unit = {}
) {
    val currentSong = playerState.currentSong ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showLyricsView by remember { mutableStateOf(false) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }
    var showRemainingTime by remember { mutableStateOf(false) }

    val currentPos = if (isDraggingSlider) {
        (sliderDragPosition * playerState.duration).toLong()
    } else {
        playerState.currentPosition
    }

    val sliderValue = if (playerState.duration > 0) {
        if (isDraggingSlider) sliderDragPosition
        else (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Smooth spring-based album art scale
    val albumArtScale by animateFloatAsState(
        targetValue = if (playerState.isPlaying) 1.0f else 0.91f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "album_art_scale"
    )

    // Ambient pulsing glow and smooth vinyl rotation when audio is actively playing
    val infiniteGlow = rememberInfiniteTransition(label = "ambient_glow")
    val vinylRotationAngle by infiniteGlow.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )
    val glowAlpha by infiniteGlow.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val glowScale1 by infiniteGlow.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "glow_scale_1"
    )
    val glowScale2 by infiniteGlow.animateFloat(
        initialValue = 1.02f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "glow_scale_2"
    )

    // Bouncy spring animation for Favorite heart button
    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.28f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fav_scale"
    )

    // Bouncy spring feedback for transport control buttons
    var playButtonPulsing by remember { mutableStateOf(false) }
    val playButtonScale by animateFloatAsState(
        targetValue = if (playButtonPulsing) 0.82f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { playButtonPulsing = false },
        label = "play_btn_scale"
    )

    var prevButtonPulsing by remember { mutableStateOf(false) }
    val prevButtonScale by animateFloatAsState(
        targetValue = if (prevButtonPulsing) 0.78f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { prevButtonPulsing = false },
        label = "prev_btn_scale"
    )

    var nextButtonPulsing by remember { mutableStateOf(false) }
    val nextButtonScale by animateFloatAsState(
        targetValue = if (nextButtonPulsing) 0.78f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { nextButtonPulsing = false },
        label = "next_btn_scale"
    )

    var rewindButtonPulsing by remember { mutableStateOf(false) }
    val rewindButtonScale by animateFloatAsState(
        targetValue = if (rewindButtonPulsing) 0.75f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { rewindButtonPulsing = false },
        label = "rewind_btn_scale"
    )

    var forwardButtonPulsing by remember { mutableStateOf(false) }
    val forwardButtonScale by animateFloatAsState(
        targetValue = if (forwardButtonPulsing) 0.75f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { forwardButtonPulsing = false },
        label = "forward_btn_scale"
    )

    var shuffleRotateAngle by remember { mutableFloatStateOf(0f) }
    val shuffleAnimatedAngle by animateFloatAsState(
        targetValue = shuffleRotateAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "shuffle_angle"
    )

    var repeatBounce by remember { mutableStateOf(false) }
    val repeatButtonScale by animateFloatAsState(
        targetValue = if (repeatBounce) 1.35f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { repeatBounce = false },
        label = "repeat_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("full_player_modal")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: Collapse button, Header Title, and Queue button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("collapse_full_player")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Close Player",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.now_playing).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        if (currentSong.album.isNotBlank()) {
                            Text(
                                text = currentSong.album,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenQueue,
                        modifier = Modifier.testTag("open_queue_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = stringResource(R.string.queue),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ambient Glowing Aura & Vinyl Disc Artwork or Lyrics with dynamic scale
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing glow aura background when playing with dual shockwave rings
                    if (playerState.isPlaying) {
                        // Outer expansive wave
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(glowScale2)
                                .blur(28.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.7f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = glowAlpha * 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        // Inner focused wave
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(glowScale1)
                                .blur(16.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    AnimatedContent(
                        targetState = showLyricsView,
                        transitionSpec = {
                            fadeIn(tween(300)) + scaleIn(initialScale = 0.92f) togetherWith
                                    fadeOut(tween(250)) + scaleOut(targetScale = 0.92f)
                        },
                        label = "player_visual_switcher"
                    ) { isLyrics ->
                        if (isLyrics) {
                            // Glassmorphic Lyrics View Card
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                    .testTag("full_player_lyrics_view"),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                tonalElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.lyrics_title),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Row {
                                            IconButton(
                                                onClick = onOpenLyricsEditor,
                                                modifier = Modifier.size(32.dp).testTag("btn_edit_lyrics_in_player")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = stringResource(R.string.edit_lyrics),
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { showLyricsView = false },
                                                modifier = Modifier.size(32.dp).testTag("btn_close_lyrics_view")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = stringResource(R.string.show_cover),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (!lyricsText.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .verticalScroll(rememberScrollState()),
                                            contentAlignment = Alignment.TopCenter
                                        ) {
                                            Text(
                                                text = lyricsText,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    lineHeight = 28.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FormatQuote,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = stringResource(R.string.lyrics_empty),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedButton(
                                                onClick = onOpenLyricsEditor,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.testTag("btn_add_lyrics_prompt")
                                            ) {
                                                Text(stringResource(R.string.add_lyrics))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Rotating Vinyl Turntable Disc with Grooves & Album Label
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(albumArtScale)
                                    .shadow(32.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141416))
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    )
                                    .clickable { showLyricsView = true },
                                contentAlignment = Alignment.Center
                            ) {
                                // Continuously spinning vinyl turntable disc
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .rotate(if (playerState.isPlaying) vinylRotationAngle else 0f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Concentric Vinyl Sound Grooves
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.92f)
                                            .border(1.dp, Color.White.copy(alpha = 0.09f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.82f)
                                            .border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.72f)
                                            .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape)
                                    )

                                    // Center Album Artwork Label
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.62f)
                                            .clip(CircleShape)
                                            .shadow(10.dp, CircleShape)
                                            .border(
                                                width = 2.5.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = currentSong.albumArtUri ?: R.drawable.img_album_art_placeholder_1790011366041,
                                            contentDescription = currentSong.title,
                                            placeholder = painterResource(id = R.drawable.img_album_art_placeholder_1790011366041),
                                            error = painterResource(id = R.drawable.img_album_art_placeholder_1790011366041),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Center Turntable Spindle Hole
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Animated Metadata Switcher (Title, Artist, HD Tag, Visualizer, Favorite Heart)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(
                            targetState = currentSong,
                            transitionSpec = {
                                (slideInHorizontally { width -> width / 4 } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { width -> -width / 4 } + fadeOut(tween(200)))
                            },
                            label = "song_metadata_transition",
                            modifier = Modifier.weight(1f)
                        ) { song ->
                            Column {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(favScale)
                                .testTag("full_player_favorite_toggle")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Quality Badge + Animated Audio Visualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.high_fidelity_audio),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        AudioVisualizer(
                            isPlaying = playerState.isPlaying,
                            barCount = 5,
                            barWidth = 3.5.dp,
                            maxHeight = 18.dp,
                            minHeight = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Slider, Seek Bar & Timestamps
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderDragPosition = it
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            val targetMs = (sliderDragPosition * playerState.duration).toLong()
                            onSeekTo(targetMs)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_progress_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(currentPos),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Tap right timestamp to toggle remaining vs total duration
                        val remainingMs = (playerState.duration - currentPos).coerceAtLeast(0L)
                        val rightTimeString = if (showRemainingTime) "-${formatDuration(remainingMs)}" else formatDuration(playerState.duration)

                        Text(
                            text = rightTimeString,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showRemainingTime = !showRemainingTime
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Main Playback Transport Controls (Shuffle, Prev, -10s, Play/Pause, +10s, Next, Repeat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Toggle with rotational spin on toggle
                    IconButton(
                        onClick = {
                            shuffleRotateAngle += 360f
                            onToggleShuffle()
                        },
                        modifier = Modifier
                            .rotate(shuffleAnimatedAngle)
                            .testTag("shuffle_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = stringResource(R.string.shuffle),
                            tint = if (playerState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous Track with spring bounce feedback
                    IconButton(
                        onClick = {
                            prevButtonPulsing = true
                            onPrevious()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(prevButtonScale)
                            .testTag("full_player_previous")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = stringResource(R.string.previous),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Rewind 10 Seconds with spring scale
                    IconButton(
                        onClick = {
                            rewindButtonPulsing = true
                            onSeekBy(-10000L)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .scale(rewindButtonScale)
                            .testTag("full_player_rewind_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = stringResource(R.string.replay_10),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Primary Play / Pause Button with animated pulse aura and scale bounce
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(84.dp)
                    ) {
                        // Pulsing outer halo behind play button when playing
                        if (playerState.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .scale(glowScale1)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.4f),
                                        shape = CircleShape
                                    )
                            )
                        }

                        IconButton(
                            onClick = {
                                playButtonPulsing = true
                                onPlayPause()
                            },
                            modifier = Modifier
                                .testTag("full_player_play_pause")
                                .size(72.dp)
                                .scale(playButtonScale)
                                .shadow(8.dp, shape = CircleShape)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clip(CircleShape)
                        ) {
                            AnimatedContent(
                                targetState = playerState.isPlaying,
                                transitionSpec = {
                                    (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f)) togetherWith
                                    (fadeOut(tween(160)) + scaleOut(targetScale = 0.6f))
                                },
                                label = "play_pause_icon_morph"
                            ) { isPlaying ->
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    // Fast-Forward 10 Seconds with spring scale
                    IconButton(
                        onClick = {
                            forwardButtonPulsing = true
                            onSeekBy(10000L)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .scale(forwardButtonScale)
                            .testTag("full_player_forward_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = stringResource(R.string.forward_10),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Next Track with spring scale
                    IconButton(
                        onClick = {
                            nextButtonPulsing = true
                            onNext()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(nextButtonScale)
                            .testTag("full_player_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.next),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Repeat Mode Cycle with bouncy spring feedback
                    IconButton(
                        onClick = {
                            repeatBounce = true
                            onCycleRepeat()
                        },
                        modifier = Modifier
                            .scale(repeatButtonScale)
                            .testTag("repeat_mode_button")
                    ) {
                        val (icon, tint) = when (playerState.repeatMode) {
                            RepeatMode.OFF -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.onSurfaceVariant)
                            RepeatMode.ALL -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.primary)
                            RepeatMode.ONE -> Pair(Icons.Default.RepeatOne, MaterialTheme.colorScheme.primary)
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(R.string.repeat_all),
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Bottom Actions: Add to Playlist, Lyrics Toggle, Edit Info, and Sleep Timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add to Playlist
                    IconButton(
                        onClick = onOpenAddToPlaylist,
                        modifier = Modifier.testTag("full_player_add_to_playlist")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistAdd,
                            contentDescription = stringResource(R.string.add_to_playlist),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Lyrics Toggle Button
                    IconButton(
                        onClick = { showLyricsView = !showLyricsView },
                        modifier = Modifier.testTag("full_player_lyrics_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = stringResource(R.string.lyrics),
                            tint = if (showLyricsView || !lyricsText.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Edit Song Info / ID3 Tags
                    IconButton(
                        onClick = onOpenSongInfoEditor,
                        modifier = Modifier.testTag("full_player_edit_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_song_info),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Sleep Timer
                    IconButton(
                        onClick = onOpenSleepTimer,
                        modifier = Modifier.testTag("full_player_sleep_timer")
                    ) {
                        val timerActive = playerState.sleepTimerRemainingSeconds != null && playerState.sleepTimerRemainingSeconds > 0
                        if (timerActive) {
                            BadgedBox(
                                badge = {
                                    val minutesLeft = (playerState.sleepTimerRemainingSeconds!! / 60) + 1
                                    Badge { Text("$minutesLeft") }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = stringResource(R.string.sleep_timer),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = stringResource(R.string.sleep_timer),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
