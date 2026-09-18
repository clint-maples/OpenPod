package com.openpod.app.playback

import kotlin.math.abs

object SkipIntervals {
    val PRESETS = listOf(10, 15, 30, 45, 60)
    const val DEFAULT_BACK = 15
    const val DEFAULT_FORWARD = 30

    fun nearest(seconds: Int): Int {
        return PRESETS.minBy { abs(it - seconds) }
    }

    fun compactLabel(seconds: Int, forward: Boolean): String {
        val amount = nearest(seconds)
        return if (forward) "+$amount" else "−$amount"
    }

    fun contentDescription(seconds: Int, forward: Boolean): String {
        val amount = nearest(seconds)
        return if (forward) "Forward ${amount}s" else "Back ${amount}s"
    }
}
