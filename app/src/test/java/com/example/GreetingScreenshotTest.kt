package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.Song
import com.example.ui.components.MiniPlayer
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun player_screenshot() {
        val testSong = Song(
            id = 1L,
            title = "Acoustic Breeze",
            artist = "Benjamin Tissot",
            album = "Acoustic Dreams",
            duration = 158000L,
            contentUri = "content://media/audio/1"
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                MiniPlayer(
                    song = testSong,
                    isPlaying = true,
                    currentPosition = 45000L,
                    duration = 158000L,
                    onPlayPauseClick = {},
                    onNextClick = {},
                    onClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/player_ui.png")
    }
}
