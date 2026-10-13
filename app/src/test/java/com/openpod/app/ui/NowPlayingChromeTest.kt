package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingChromeTest {
    @Test
    fun bottomActionsMatchApprovedOrder() {
        assertEquals(
            listOf(
                NowPlayingChrome.BottomAction.Speed,
                NowPlayingChrome.BottomAction.Equalizer,
                NowPlayingChrome.BottomAction.Sleep,
                NowPlayingChrome.BottomAction.Share,
                NowPlayingChrome.BottomAction.Favorite
            ),
            NowPlayingChrome.bottomActions
        )
        assertEquals(
            NowPlayingChrome.BottomAction.Favorite,
            NowPlayingChrome.bottomActions.last()
        )
    }

    @Test
    fun playlistIsNotOnTheBottomBar() {
        assertEquals(5, NowPlayingChrome.bottomActions.size)
        assertFalse(NowPlayingChrome.bottomActions.any { it.name == "Playlist" })
    }

    @Test
    fun existingControlsShrinkWithoutDroppingTheTapTarget() {
        assertTrue(NowPlayingChrome.BOTTOM_CONTROL_SCALE in 0.80f..0.85f)
        assertEquals(20, NowPlayingChrome.scaledDp(NowPlayingChrome.STANDARD_ICON_DP))
        assertEquals(18, NowPlayingChrome.scaledDp(NowPlayingChrome.EQUALIZER_ICON_DP))
        assertTrue(NowPlayingChrome.MIN_TAP_TARGET_DP >= 48)
    }

    @Test
    fun favoriteContentDescriptionTogglesWithState() {
        assertEquals("Favorite", NowPlayingChrome.favoriteContentDescription(false))
        assertEquals("Unfavorite", NowPlayingChrome.favoriteContentDescription(true))
    }

    @Test
    fun equalizerActionOpensEqualizerRoute() {
        assertEquals("eq", NowPlayingChrome.EQUALIZER_ROUTE)
        assertEquals(
            NowPlayingChrome.EQUALIZER_ROUTE,
            NowPlayingChrome.equalizerRoute(NowPlayingChrome.BottomAction.Equalizer)
        )
        assertNull(NowPlayingChrome.equalizerRoute(NowPlayingChrome.BottomAction.Speed))
        assertNull(NowPlayingChrome.equalizerRoute(NowPlayingChrome.BottomAction.Sleep))
        assertNull(NowPlayingChrome.equalizerRoute(NowPlayingChrome.BottomAction.Share))
        assertNull(NowPlayingChrome.equalizerRoute(NowPlayingChrome.BottomAction.Favorite))
    }

    @Test
    fun artworkTargetsTheTopThird() {
        assertEquals(1f / 3f, NowPlayingChrome.ARTWORK_HEIGHT_FRACTION, 0.0001f)
    }

    @Test
    fun speedButtonShowsCurrentRate() {
        assertEquals("1.0x", NowPlayingChrome.speedButtonLabel(1f))
        assertEquals("1.5x", NowPlayingChrome.speedButtonLabel(1.54f))
        assertEquals("0.5x", NowPlayingChrome.speedButtonLabel(0.4f))
        assertEquals("5.0x", NowPlayingChrome.speedButtonLabel(9f))
    }
}
