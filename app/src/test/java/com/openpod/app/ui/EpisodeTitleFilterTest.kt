package com.openpod.app.ui

import com.openpod.app.data.db.EpisodeEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeTitleFilterTest {
    private val episodes = listOf(
        episode(1, "E123: Markets and AI"),
        episode(2, "E122: Energy"),
        episode(3, "Interview: Dina Powell")
    )

    @Test
    fun blankQueryKeepsAll() {
        assertEquals(episodes, EpisodeTitleFilter.filter(episodes, "  "))
    }

    @Test
    fun matchesTitleCaseInsensitive() {
        val found = EpisodeTitleFilter.filter(episodes, "e123")
        assertEquals(listOf(1L), found.map { it.id })
    }

    @Test
    fun matchesPartialWordInLongFeed() {
        val found = EpisodeTitleFilter.filter(episodes, "energy")
        assertEquals(listOf(2L), found.map { it.id })
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertEquals(emptyList<EpisodeEntity>(), EpisodeTitleFilter.filter(episodes, "xyz"))
    }

    private fun episode(id: Long, title: String) = EpisodeEntity(
        id = id,
        podcastId = 1,
        guid = "guid-$id",
        title = title,
        audioUrl = "https://cdn.example.com/$id.mp3"
    )
}
