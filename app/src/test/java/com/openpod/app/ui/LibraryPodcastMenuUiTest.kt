package com.openpod.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.percentOffset
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class LibraryPodcastMenuUiTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun gridMenuOnLightCoverShowsArchiveAndKeepsTheShowClosed() {
        var opened = 0
        var archived = 0
        rule.setContent {
            OpenPodTheme {
                LibraryPodcastGridCell(
                    onOpen = { opened++ },
                    onArchive = { archived++ },
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("cell")
                ) {
                    Box(Modifier.fillMaxSize().background(Color.White))
                }
            }
        }

        rule.onNodeWithTag(LibraryPodcastMenu.overflowTag).assertIsDisplayed()
        // Tap the top-end of the cover, where the overflow sits on top of the artwork.
        rule.onNodeWithTag("cell").performTouchInput { click(percentOffset(0.74f, 0.26f)) }
        rule.onNodeWithText(LibraryPodcastMenu.archiveLabel, substring = false).assertIsDisplayed()
        rule.onNodeWithText("Subscription stays", substring = true).assertIsDisplayed()
        assertEquals(0, opened)

        rule.onNodeWithText(LibraryPodcastMenu.archiveLabel, substring = false).performClick()
        assertEquals(1, archived)
        assertEquals(0, opened)
    }

    @Test
    fun listMenuShowsArchiveWithoutOpeningTheShow() {
        var opened = 0
        var archived = 0
        rule.setContent {
            OpenPodTheme {
                Box(Modifier.width(360.dp)) {
                    LibraryPodcastListRow(
                        title = "The Show",
                        author = "Host",
                        onOpen = { opened++ },
                        onArchive = { archived++ },
                        artwork = { Box(Modifier.size(56.dp).background(Color.DarkGray)) }
                    )
                }
            }
        }

        rule.onNodeWithTag(LibraryPodcastMenu.overflowTag)
            .assertIsDisplayed()
            .performClick()
        rule.onNodeWithText(LibraryPodcastMenu.archiveLabel, substring = false).assertIsDisplayed()
        rule.onNodeWithText("Subscription stays", substring = true).assertIsDisplayed()
        assertEquals(0, opened)

        rule.onNodeWithText(LibraryPodcastMenu.archiveLabel, substring = false).performClick()
        assertEquals(1, archived)
        assertEquals(0, opened)
    }
}
