package com.openpod.app.ui

/**
 * Now Playing: artwork about a third of the area above the pinned footer, then
 * the episode title. Publisher notes, chapters, and the transcript scroll in
 * that area and are not drawn under the footer.
 *
 * The footer is fixed at the bottom of the Now Playing screen, above the app
 * navigation bar. That bar applies the system navigation-bar inset
 * (edge-to-edge), so the footer is not given a second copy of it.
 * Top to bottom the footer is [FooterBand.Progress] (seek bar and times),
 * [FooterBand.Transport] (skip back, play/pause, skip forward), then
 * [FooterBand.Actions]: speed, equalizer, sleep, share, and favorite at the right.
 * Speed and sleep open menus. The equalizer action opens [EQUALIZER_ROUTE].
 * Action icons and labels are [BOTTOM_CONTROL_SCALE] of their previous size.
 * Action tap targets stay at least [MIN_TAP_TARGET_DP] wide. Transport buttons
 * stay at [TRANSPORT_BUTTON_DP]; the play and skip glyphs are 10% smaller than
 * the previous 64 / 48 dp sizes. The progress and transport bands use pure
 * [com.openpod.app.ui.OpenPodColors.progressTransport] (black in dark, accent-wash
 * band in light); the actions bar stays on [com.openpod.app.ui.OpenPodColors.chromeBar]. [FOOTER_BAND_GAP_DP]
 * of page background sits between the bands. Elapsed time sits at the left of the [PROGRESS_BAND_HEIGHT_DP] dp seek row;
 * remaining time sits at the right as a countdown with a leading minus (-42:10). A 1 dp
 * primary line ([NAV_ACCENT_LINE_DP]) is drawn above the app navigation bar on
 * every screen.
 */
object NowPlayingChrome {
    const val ARTWORK_HEIGHT_FRACTION = 1f / 3f
    const val EQUALIZER_ROUTE = "eq"
    const val ELAPSED_TAG = "now-playing-elapsed"
    const val REMAINING_TAG = "now-playing-remaining"
    const val SEEK_TAG = "now-playing-seek"
    const val ACTIONS_BAR_TAG = "now-playing-actions"
    const val EQUALIZER_TAG = "now-playing-eq"
    const val BOTTOM_CONTROL_SCALE = 0.82f
    const val MIN_TAP_TARGET_DP = 48
    const val STANDARD_ICON_DP = 24
    const val EQUALIZER_ICON_DP = 22

    /** Hit target for rewind, play/pause, and fast-forward in the pinned footer. */
    const val TRANSPORT_BUTTON_DP = 70
    /** 10% smaller than the previous 64 dp play glyph. */
    const val TRANSPORT_PLAY_ICON_DP = 58
    /** 10% smaller than the previous 48 dp skip glyph. */
    const val TRANSPORT_SKIP_ICON_DP = 43

    /** Locked band heights from the 1.50 Now Playing footer (round 6 v4). */
    const val PROGRESS_BAND_HEIGHT_DP = 32
    const val TRANSPORT_BAND_HEIGHT_DP = 78
    const val ACTIONS_BAND_HEIGHT_DP = 45

    /** Gap of page background between the Now Playing footer bands (and above the nav line). */
    const val FOOTER_BAND_GAP_DP = 4
    const val NAV_ACCENT_LINE_DP = 1

    /** Horizontal inset of the seek row, and gap between inline times and the track. */
    const val PROGRESS_HORIZONTAL_PADDING_DP = 12
    const val PROGRESS_INLINE_GAP_DP = 8

    /** pb4 seek bar: 3 dp rounded track, 12 dp round dot thumb. */
    const val PROGRESS_TRACK_DP = 3
    const val PROGRESS_THUMB_DP = 12

    /** Dark-mode default for the progress and transport bands. Prefer LocalOpenPodColors. */
    const val PROGRESS_TRANSPORT_COLOR = 0xFF000000L

    /** Space after the last scrolling line so it can clear the pinned footer. */
    const val NOTES_END_PADDING_DP = 24

    enum class FooterBand {
        Progress,
        Transport,
        Actions
    }

    /** Top to bottom inside the pinned footer. */
    val footerBands: List<FooterBand> = listOf(
        FooterBand.Progress,
        FooterBand.Transport,
        FooterBand.Actions
    )

    enum class BottomAction {
        Speed,
        Equalizer,
        Sleep,
        Share,
        Favorite
    }

    val bottomActions: List<BottomAction> = listOf(
        BottomAction.Speed,
        BottomAction.Equalizer,
        BottomAction.Sleep,
        BottomAction.Share,
        BottomAction.Favorite
    )

    fun scaledDp(baseDp: Int): Int = kotlin.math.round(baseDp * BOTTOM_CONTROL_SCALE).toInt()

    fun favoriteContentDescription(isFavorite: Boolean): String =
        if (isFavorite) "Unfavorite" else "Favorite"

    fun speedButtonLabel(speed: Float): String = "${formatSpeed(speed)}x"

    fun equalizerRoute(action: BottomAction): String? =
        if (action == BottomAction.Equalizer) EQUALIZER_ROUTE else null
}

/**
 * Layout math for the Now Playing 12 dp thumb on the 3 dp track.
 * Material3's [androidx.compose.material3.Slider] cannot host this pairing:
 * it wraps the thumb in a box whose minimum height is the 16 dp track token
 * and top-aligns a smaller custom thumb, so the dot sits 2 dp above the track.
 * We draw both ourselves so they share one vertical center.
 */
internal object NowPlayingSeekBarGeometry {
    fun fractionFromX(x: Float, widthPx: Float, thumbPx: Float): Float {
        val usable = (widthPx - thumbPx).coerceAtLeast(0f)
        if (usable <= 0f) return 0f
        return ((x - thumbPx / 2f) / usable).coerceIn(0f, 1f)
    }

    fun thumbCenterX(widthPx: Float, fraction: Float, thumbPx: Float): Float {
        val usable = (widthPx - thumbPx).coerceAtLeast(0f)
        return thumbPx / 2f + usable * fraction.coerceIn(0f, 1f)
    }

    fun centerY(heightPx: Float): Float = heightPx / 2f
}

