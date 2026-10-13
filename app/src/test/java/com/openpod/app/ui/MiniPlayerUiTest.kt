package com.openpod.app.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.openpod.app.playback.PlayerUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class MiniPlayerUiTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun blankAreasOpenNowPlayingAndButtonsDoNot() {
        var opened = 0
        var played = 0
        var skippedBack = 0
        var skippedForward = 0
        var upNext = 0
        rule.setContent {
            OpenPodTheme {
                MiniPlayer(
                    player = PlayerUiState(
                        episodeId = 4,
                        title = "Episode title",
                        podcastTitle = "Show name",
                        durationMs = 60_000,
                        positionMs = 1_000
                    ),
                    onOpen = { opened++ },
                    onUpNext = { upNext++ },
                    onTogglePlay = { played++ },
                    onSkipBack = { skippedBack++ },
                    onSkipForward = { skippedForward++ }
                )
            }
        }

        rule.onNodeWithText("Episode title", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Show name", useUnmergedTree = true).assertIsDisplayed()

        val miniPlayer = rule.onNodeWithTag(MiniPlayerVisibility.containerTag)
        miniPlayer.clickText(rule, "Episode title")
        miniPlayer.clickText(rule, "Show name")
        miniPlayer.performTouchInput { click(Offset(4f, 4f)) }
        assertEquals(3, opened)

        rule.onNodeWithContentDescription("Play").performClick()
        rule.onNodeWithContentDescription("Back 15s").performClick()
        rule.onNodeWithContentDescription("Forward 30s").performClick()
        rule.onNodeWithContentDescription("Up Next").performClick()
        assertEquals(3, opened)
        assertEquals(1, played)
        assertEquals(1, skippedBack)
        assertEquals(1, skippedForward)
        assertEquals(1, upNext)
    }
}

private fun SemanticsNodeInteraction.clickText(rule: ComposeContentTestRule, text: String) {
    val parent = fetchSemanticsNode().boundsInRoot
    val child = rule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
    performTouchInput {
        click(Offset(child.center.x - parent.left, child.center.y - parent.top))
    }
}
