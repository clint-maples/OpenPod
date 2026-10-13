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
 * Action tap targets stay at least [MIN_TAP_TARGET_DP]. Transport buttons stay
 * at [TRANSPORT_BUTTON_DP].
 */
object NowPlayingChrome {
    const val ARTWORK_HEIGHT_FRACTION = 1f / 3f
    const val EQUALIZER_ROUTE = "eq"
    const val BOTTOM_CONTROL_SCALE = 0.82f
    const val MIN_TAP_TARGET_DP = 48
    const val STANDARD_ICON_DP = 24
    const val EQUALIZER_ICON_DP = 22

    /** Hit target for rewind, play/pause, and fast-forward in the pinned footer. */
    const val TRANSPORT_BUTTON_DP = 96
    const val TRANSPORT_PLAY_ICON_DP = 64
    const val TRANSPORT_SKIP_ICON_DP = 48

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
