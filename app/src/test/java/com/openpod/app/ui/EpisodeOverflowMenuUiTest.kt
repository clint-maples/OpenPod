package com.openpod.app.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class EpisodeOverflowMenuUiTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun overflowShowsArchiveEpisodeBesideQueueAndShare() {
        var playedNext = 0
        var queued = 0
        var shared = 0
        var archived = 0
        var favorited = 0
        rule.setContent {
            OpenPodTheme {
                EpisodeOverflowMenu(
                    archived = false,
                    favorite = false,
                    onPlayNext = { playedNext++ },
                    onAddToQueue = { queued++ },
                    onShare = { shared++ },
                    onArchive = { archived++ },
                    onToggleFavorite = { favorited++ }
                )
            }
        }

        rule.onNodeWithTag(EpisodeOverflowMenu.overflowTag).performClick()
        rule.onNodeWithText(EpisodeOverflowMenu.playNextLabel).assertIsDisplayed()
        rule.onNodeWithText(EpisodeOverflowMenu.upNextLabel).assertIsDisplayed()
        rule.onNodeWithText(EpisodeOverflowMenu.shareLabel).assertIsDisplayed()
        rule.onNodeWithText(EpisodeOverflowMenu.addFavoriteLabel).assertIsDisplayed()
        rule.onNodeWithText(EpisodeOverflowMenu.archiveLabel).assertIsDisplayed()

        rule.onNodeWithText(EpisodeOverflowMenu.archiveLabel).performClick()
        assertEquals(1, archived)
        assertEquals(0, playedNext)
        assertEquals(0, queued)
        assertEquals(0, shared)
        assertEquals(0, favorited)
    }

    @Test
    fun favoriteRowOffersRemoveFromFavorites() {
        var favorited = 0
        rule.setContent {
            OpenPodTheme {
                EpisodeOverflowMenu(
                    archived = false,
                    favorite = true,
                    onPlayNext = {},
                    onAddToQueue = {},
                    onShare = {},
                    onArchive = {},
                    onToggleFavorite = { favorited++ }
                )
            }
        }

        rule.onNodeWithTag(EpisodeOverflowMenu.overflowTag).performClick()
        rule.onNodeWithText(EpisodeOverflowMenu.removeFavoriteLabel).assertIsDisplayed().performClick()
        assertEquals(1, favorited)
    }

    @Test
    fun archivedRowOffersUnarchiveEpisode() {
        var archived = 0
        rule.setContent {
            OpenPodTheme {
                EpisodeOverflowMenu(
                    archived = true,
                    favorite = false,
                    onPlayNext = {},
                    onAddToQueue = {},
                    onShare = {},
                    onArchive = { archived++ },
                    onToggleFavorite = {}
                )
            }
        }

        rule.onNodeWithTag(EpisodeOverflowMenu.overflowTag).performClick()
        rule.onNodeWithText(EpisodeOverflowMenu.unarchiveLabel).assertIsDisplayed().performClick()
        assertEquals(1, archived)
    }
}
