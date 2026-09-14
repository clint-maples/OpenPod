package com.openpod.app.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow
import kotlin.math.roundToInt

class GainAudioProcessor : BaseAudioProcessor() {
    @Volatile
    var gain: Float = 1f

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return
        val output = replaceOutputBuffer(remaining)
        val g = gain
        if (g > 0.999f && g < 1.001f) {
            output.put(inputBuffer)
        } else {
            when (outputAudioFormat.encoding) {
                C.ENCODING_PCM_16BIT -> {
                    val src = inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    output.order(ByteOrder.LITTLE_ENDIAN)
                    while (src.remaining() >= 2) {
                        val sample = src.short.toInt()
                        val boosted = (sample * g).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        output.putShort(boosted.toShort())
                    }
                }
                C.ENCODING_PCM_FLOAT -> {
                    val src = inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    output.order(ByteOrder.LITTLE_ENDIAN)
                    while (src.remaining() >= 4) {
                        output.putFloat((src.float * g).coerceIn(-1f, 1f))
                    }
                }
                else -> output.put(inputBuffer)
            }
        }
        output.flip()
    }
}

fun volumeBoostToDb(slider: Float): Float {
    val v = slider.coerceIn(-1f, 1f)
    return if (v >= 0f) v * 12f else v * 24f
}

fun volumeBoostToGain(slider: Float): Float {
    return 10f.pow(volumeBoostToDb(slider) / 20f)
}

fun formatVolumeBoost(slider: Float): String {
    val db = volumeBoostToDb(slider)
    return if (kotlin.math.abs(db) < 0.05f) "0.0 dB (default)"
    else String.format(java.util.Locale.US, "%+.1f dB", db)
}
