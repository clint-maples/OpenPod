package com.openpod.app.ui

/**
 * Now Playing: artwork about a third of the screen, then episode, scrubber, and
 * transport in one vertical scroll so publisher notes sit below. The bottom action
 * bar stays pinned: speed, equalizer, sleep, share. Speed and sleep open menus.
 * The equalizer action opens [EQUALIZER_ROUTE].
 */
object NowPlayingChrome {
    const val ARTWORK_HEIGHT_FRACTION = 1f / 3f
    const val EQUALIZER_ROUTE = "eq"

    enum class BottomAction {
        Speed,
        Equalizer,
        Sleep,
        Share
    }

    val bottomActions: List<BottomAction> = listOf(
        BottomAction.Speed,
        BottomAction.Equalizer,
        BottomAction.Sleep,
        BottomAction.Share
    )

    fun speedButtonLabel(speed: Float): String = "${formatSpeed(speed)}x"

    fun equalizerRoute(action: BottomAction): String? =
        if (action == BottomAction.Equalizer) EQUALIZER_ROUTE else null
}
