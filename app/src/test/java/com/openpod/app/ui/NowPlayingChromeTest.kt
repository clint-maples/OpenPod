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
    fun footerStacksSeekThenTransportThenActions() {
        assertEquals(
            listOf(
                NowPlayingChrome.FooterBand.Progress,
                NowPlayingChrome.FooterBand.Transport,
                NowPlayingChrome.FooterBand.Actions
            ),
            NowPlayingChrome.footerBands
        )
        assertEquals(
            NowPlayingChrome.BottomAction.Favorite,
            NowPlayingChrome.bottomActions.last()
        )
    }

    @Test
    fun transportButtonsStayLargeAndNotesClearTheFooter() {
        assertEquals(70, NowPlayingChrome.TRANSPORT_BUTTON_DP)
        assertEquals(58, NowPlayingChrome.TRANSPORT_PLAY_ICON_DP)
        assertEquals(43, NowPlayingChrome.TRANSPORT_SKIP_ICON_DP)
        assertEquals(32, NowPlayingChrome.PROGRESS_BAND_HEIGHT_DP)
        assertEquals(78, NowPlayingChrome.TRANSPORT_BAND_HEIGHT_DP)
        assertEquals(45, NowPlayingChrome.ACTIONS_BAND_HEIGHT_DP)
        assertEquals(4, NowPlayingChrome.FOOTER_BAND_GAP_DP)
        assertEquals(1, NowPlayingChrome.NAV_ACCENT_LINE_DP)
        assertEquals(12, NowPlayingChrome.PROGRESS_HORIZONTAL_PADDING_DP)
        assertEquals(8, NowPlayingChrome.PROGRESS_INLINE_GAP_DP)
        assertEquals(3, NowPlayingChrome.PROGRESS_TRACK_DP)
        assertEquals(12, NowPlayingChrome.PROGRESS_THUMB_DP)
        assertEquals(0xFF000000L, NowPlayingChrome.PROGRESS_TRANSPORT_COLOR)
        assertTrue(NowPlayingChrome.NOTES_END_PADDING_DP >= 16)
        // Transport band packs 4 dp + 70 dp button + 4 dp; play 58 and skip 43+label clear 70.
        assertEquals(
            NowPlayingChrome.TRANSPORT_BAND_HEIGHT_DP,
            4 + NowPlayingChrome.TRANSPORT_BUTTON_DP + 4
        )
    }

    @Test
    fun speedButtonShowsCurrentRate() {
        assertEquals("1.0x", NowPlayingChrome.speedButtonLabel(1f))
        assertEquals("1.5x", NowPlayingChrome.speedButtonLabel(1.54f))
        assertEquals("0.5x", NowPlayingChrome.speedButtonLabel(0.4f))
        assertEquals("5.0x", NowPlayingChrome.speedButtonLabel(9f))
    }
}
