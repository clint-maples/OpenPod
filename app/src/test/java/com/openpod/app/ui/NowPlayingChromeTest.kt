package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class NowPlayingChromeTest {
    @Test
    fun bottomActionsMatchApprovedOrder() {
        assertEquals(
            listOf(
                NowPlayingChrome.BottomAction.Speed,
                NowPlayingChrome.BottomAction.Equalizer,
                NowPlayingChrome.BottomAction.Sleep,
                NowPlayingChrome.BottomAction.Share
            ),
            NowPlayingChrome.bottomActions
        )
    }

    @Test
    fun playlistIsNotOnTheBottomBar() {
        assertEquals(4, NowPlayingChrome.bottomActions.size)
        assertFalse(NowPlayingChrome.bottomActions.any { it.name == "Playlist" })
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
