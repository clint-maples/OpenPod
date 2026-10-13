package com.openpod.app.ui

/**
 * Now Playing: artwork about a third of the screen, then episode, scrubber, and
 * transport in one vertical scroll so publisher notes sit below. The bottom action
 * bar stays pinned: speed, equalizer, sleep, share, then favorite at the right.
 * Speed and sleep open menus. The equalizer action opens [EQUALIZER_ROUTE].
 * Existing action icons and labels are [BOTTOM_CONTROL_SCALE] of their previous size.
 * Tap targets stay at least [MIN_TAP_TARGET_DP].
 */
object NowPlayingChrome {
    const val ARTWORK_HEIGHT_FRACTION = 1f / 3f
    const val EQUALIZER_ROUTE = "eq"
    const val BOTTOM_CONTROL_SCALE = 0.82f
    const val MIN_TAP_TARGET_DP = 48
    const val STANDARD_ICON_DP = 24
    const val EQUALIZER_ICON_DP = 22

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
