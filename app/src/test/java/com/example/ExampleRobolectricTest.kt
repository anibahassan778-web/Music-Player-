package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.PlayerUiState
import com.example.domain.model.Song
import com.example.ui.components.FullPlayerModal
import com.example.ui.components.MiniPlayer
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSong = Song(
        id = 1L,
        title = "Starlight Serenade",
        artist = "Aurora Waves",
        album = "Cosmic Echoes",
        duration = 180000L,
        contentUri = "content://media/audio/1"
    )

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Music Player", appName)
    }

    @Test
    fun `mini player displays metadata and triggers play pause`() {
        var playPauseClicked = false
        var nextClicked = false
        var cardClicked = false

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MyApplicationTheme {
                MiniPlayer(
                    song = sampleSong,
                    isPlaying = false,
                    currentPosition = 30000L,
                    duration = 180000L,
                    onPlayPauseClick = { playPauseClicked = true },
                    onNextClick = { nextClicked = true },
                    onClick = { cardClicked = true }
                )
            }
        }
        composeTestRule.mainClock.advanceTimeBy(300)

        composeTestRule.onNodeWithText("Starlight Serenade").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aurora Waves").assertIsDisplayed()

        composeTestRule.onNodeWithTag("mini_player_play_pause").performClick()
        assertTrue(playPauseClicked)

        composeTestRule.onNodeWithTag("mini_player_next").performClick()
        assertTrue(nextClicked)

        composeTestRule.onNodeWithTag("mini_player").performClick()
        assertTrue(cardClicked)
    }

    @Test
    fun `full player displays track metadata and controls`() {
        var playPauseClicked = false
        var seekTriggered = false

        val state = PlayerUiState(
            currentSong = sampleSong,
            isPlaying = true,
            currentPosition = 45000L,
            duration = 180000L
        )

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MyApplicationTheme {
                FullPlayerModal(
                    playerState = state,
                    isFavorite = true,
                    onPlayPause = { playPauseClicked = true },
                    onNext = {},
                    onPrevious = {},
                    onSeekTo = { seekTriggered = true },
                    onSeekBy = {},
                    onToggleShuffle = {},
                    onCycleRepeat = {},
                    onToggleFavorite = {},
                    onOpenSleepTimer = {},
                    onOpenAddToPlaylist = {},
                    onOpenQueue = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.mainClock.advanceTimeBy(300)

        composeTestRule.onNodeWithText("Starlight Serenade").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aurora Waves").assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_progress_slider").assertIsDisplayed()

        composeTestRule.onNodeWithTag("full_player_play_pause").performClick()
        assertTrue(playPauseClicked)
    }

    @Test
    fun `main activity launches without crash`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
        controller.setup()
        val activity = controller.get()
        org.junit.Assert.assertNotNull(activity)
    }
}
