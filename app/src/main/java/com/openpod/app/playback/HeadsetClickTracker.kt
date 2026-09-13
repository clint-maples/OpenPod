package com.openpod.app.playback

import android.os.Handler
import android.os.Looper

class HeadsetClickTracker(
    private val onSingle: () -> Unit,
    private val onDouble: () -> Unit,
    private val onTriple: () -> Unit,
    private val windowMs: Long = 450L
) {
    private val handler = Handler(Looper.getMainLooper())
    private var count = 0
    private val fire = Runnable {
        val n = count
        count = 0
        when (n) {
            1 -> onSingle()
            2 -> onDouble()
            else -> if (n >= 3) onTriple()
        }
    }

    fun onClick() {
        count += 1
        handler.removeCallbacks(fire)
        if (count >= 3) {
            handler.post(fire)
        } else {
            handler.postDelayed(fire, windowMs)
        }
    }
}
