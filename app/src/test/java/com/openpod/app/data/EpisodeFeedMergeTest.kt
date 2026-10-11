package com.openpod.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeFeedMergeTest {
    @Test
    fun blankFreshEnclosureKeepsStoredUrl() {
        assertEquals(
            "https://cdn.example/stored.mp3",
            EpisodeFeedMerge.mergedAudioUrl("https://cdn.example/stored.mp3", "  ")
        )
    }

    @Test
    fun rotatedEnclosureReplacesStoredUrl() {
        assertEquals(
            "https://cdn.example/fresh-token.mp3",
            EpisodeFeedMerge.mergedAudioUrl(
                "https://cdn.example/expired.mp3",
                " https://cdn.example/fresh-token.mp3 "
            )
        )
    }
}
