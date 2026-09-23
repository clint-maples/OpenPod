package com.openpod.app.ui

/**
 * Approved Now Playing structure: artwork in the top third, then title, scrubber,
 * and transport, with this bottom action order. Speed and sleep open menus.
 */
object NowPlayingChrome {
    const val ARTWORK_HEIGHT_FRACTION = 1f / 3f

    enum class BottomAction {
        Speed,
        Equalizer,
        Playlist,
        Sleep,
        Share
    }

    val bottomActions: List<BottomAction> = listOf(
        BottomAction.Speed,
        BottomAction.Equalizer,
        BottomAction.Playlist,
        BottomAction.Sleep,
        BottomAction.Share
    )

    fun speedButtonLabel(speed: Float): String = "${formatSpeed(speed)}x"
}
