package com.openpod.app.ui

object MiniPlayerVisibility {
    const val containerTag = "mini-player"

    fun shouldShow(route: String?, episodeId: Long): Boolean {
        if (episodeId == 0L) return false
        val path = route.orEmpty().substringBefore("?").substringBefore("/")
        return path != "player"
    }
}
