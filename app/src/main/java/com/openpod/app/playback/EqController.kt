package com.openpod.app.playback

import android.media.audiofx.Equalizer
import org.json.JSONArray
import org.json.JSONObject

class EqController {
    private var equalizer: Equalizer? = null

    data class Band(val index: Short, val freqHz: Int, val levelMilliBel: Short)

    fun attach(audioSessionId: Int, enabled: Boolean, saved: String) {
        release()
        if (audioSessionId == 0) return
        val eq = Equalizer(0, audioSessionId)
        eq.enabled = enabled
        if (saved.isNotBlank()) {
            try {
                val arr = JSONObject(saved).optJSONArray("bands") ?: JSONArray()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val index = obj.optInt("index")
                    val level = obj.optInt("level").toShort()
                    if (index in 0 until eq.numberOfBands) {
                        eq.setBandLevel(index.toShort(), level)
                    }
                }
            } catch (_: Exception) {
            }
        }
        equalizer = eq
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
    }

    fun setBand(index: Short, level: Short) {
        equalizer?.setBandLevel(index, level)
    }

    fun bands(): List<Band> {
        val eq = equalizer ?: return emptyList()
        return (0 until eq.numberOfBands).map { i ->
            val idx = i.toShort()
            Band(idx, eq.getCenterFreq(idx) / 1000, eq.getBandLevel(idx))
        }
    }

    fun range(): Pair<Short, Short> {
        val eq = equalizer ?: return 0.toShort() to 0.toShort()
        val r = eq.bandLevelRange
        return r[0] to r[1]
    }

    fun snapshotJson(): String {
        val arr = JSONArray()
        bands().forEach { band ->
            arr.put(JSONObject().put("index", band.index.toInt()).put("level", band.levelMilliBel.toInt()))
        }
        return JSONObject().put("bands", arr).toString()
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {
        }
        equalizer = null
    }
}
