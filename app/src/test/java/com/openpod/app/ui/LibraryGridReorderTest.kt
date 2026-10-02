package com.openpod.app.ui

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.openpod.app.data.db.PodcastEntity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h640dp")
class LibraryGridReorderTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun tapOpensTheShow() {
        var opened = 0L
        rule.setContent {
            OpenPodTheme {
                LibraryPodcastGrid(
                    podcasts = shows,
                    onOpen = { opened = it },
                    onReorder = {},
                    modifier = Modifier.fillMaxSize(),
                    columns = 3
                )
            }
        }
        rule.onNodeWithTag(libraryPodcastCellTag(1)).performClick()
        assertEquals(1L, opened)
        rule.onNodeWithContentDescription("Alpha").assertIsDisplayed()
        assertEquals(0, rule.onAllNodesWithContentDescription("More").fetchSemanticsNodes().size)
    }

    @Test
    fun longPressDragReordersThumbnails() {
        var order = shows.map { it.id }
        rule.setContent {
            OpenPodTheme {
                LibraryPodcastGrid(
                    podcasts = shows,
                    onOpen = {},
                    onReorder = { order = it },
                    modifier = Modifier.fillMaxSize(),
                    columns = 3
                )
            }
        }
        rule.waitForIdle()
        val first = rule.onNodeWithTag(libraryPodcastCellTag(1)).fetchSemanticsNode()
        val second = rule.onNodeWithTag(libraryPodcastCellTag(2)).fetchSemanticsNode()
        val delta = Offset(
            x = (second.positionInRoot.x + second.size.width / 2f) -
                (first.positionInRoot.x + first.size.width / 2f),
            y = (second.positionInRoot.y + second.size.height / 2f) -
                (first.positionInRoot.y + first.size.height / 2f)
        )
        rule.onNodeWithTag(libraryPodcastCellTag(1)).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 200)
            moveBy(delta)
            up()
        }
        rule.waitForIdle()
        assertEquals(listOf(2L, 1L, 3L), order)
    }

    private val shows = listOf(
        PodcastEntity(id = 1, title = "Alpha", feedUrl = "https://a.example/rss"),
        PodcastEntity(id = 2, title = "Beta", feedUrl = "https://b.example/rss"),
        PodcastEntity(id = 3, title = "Gamma", feedUrl = "https://g.example/rss")
    )
}
