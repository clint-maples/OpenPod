package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PodcastEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadMatcherTest {
    private val podcast = PodcastEntity(
        id = 1,
        title = "All-In",
        feedUrl = "https://feeds.example.com/all-in.xml",
        folderName = "All-In"
    )
    private val pattern = "{podcast} - {episode}"

    @Test
    fun matchesExactOpenPodFileName() {
        val episode = episode(1, "E162: Markets and AI", "guid-e162-unique")
        val expected = FileNamer.fileName(pattern, podcast, episode)
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(episode),
            listOf(DownloadMatcher.LocalFile(expected, "content://file/1")),
            pattern
        )
        assertEquals(listOf(DownloadMatcher.Link(1, "content://file/1", expected)), links)
    }

    @Test
    fun matchesSameBaseWithAnotherAudioExtension() {
        val episode = episode(2, "E163: Title Here", "guid-e163-unique", episodeNumber = 163)
        val expected = FileNamer.fileName(pattern, podcast, episode)
        val alt = expected.substringBeforeLast('.') + ".m4a"
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(episode),
            listOf(DownloadMatcher.LocalFile(alt, "content://file/2")),
            pattern
        )
        assertEquals(1, links.size)
        assertEquals(2L, links[0].episodeId)
        assertEquals(alt, links[0].fileName)
    }

    @Test
    fun matchesUniqueGuidToken() {
        val episode = episode(3, "Some Episode", "abc123xyz789-rest-of-guid")
        val token = FileNamer.sanitize(episode.guid.take(12))
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(episode),
            listOf(DownloadMatcher.LocalFile("All-In - $token.mp3", "content://file/3")),
            "{title}"
        )
        assertEquals(1, links.size)
        assertEquals(3L, links[0].episodeId)
    }

    @Test
    fun matchesUniqueTitleSlug() {
        val episode = episode(4, "Deep Dive on Memory Safety", "https://cdn.example.com/ep/4")
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(episode),
            listOf(DownloadMatcher.LocalFile("Deep Dive on Memory Safety.mp3", "content://file/4")),
            "{guid}"
        )
        assertEquals(1, links.size)
        assertEquals(4L, links[0].episodeId)
    }

    @Test
    fun ignoresRandomUnrelatedFiles() {
        val episode = episode(5, "Markets this week", "guid-markets-5")
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(episode),
            listOf(
                DownloadMatcher.LocalFile("vacation-photos.mp3", "content://file/noise"),
                DownloadMatcher.LocalFile("readme.txt", "content://file/txt")
            ),
            pattern
        )
        assertTrue(links.isEmpty())
    }

    @Test
    fun doesNotAssignOneFileToTwoEpisodes() {
        val first = episode(6, "Shared Title", "guid-one-aaaaaaa")
        val second = episode(7, "Shared Title", "guid-two-bbbbbbb")
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(first, second),
            listOf(DownloadMatcher.LocalFile("Shared Title.mp3", "content://file/shared")),
            "{guid}"
        )
        assertEquals(0, links.size)
    }

    @Test
    fun skipsHttpGuidPrefixThatIsNotDistinctive() {
        val first = episode(8, "One", "https://cdn.example.com/ep/one")
        val second = episode(9, "Two", "https://cdn.example.com/ep/two")
        val links = DownloadMatcher.findLinks(
            podcast,
            listOf(first, second),
            listOf(DownloadMatcher.LocalFile("https---cdn.mp3", "content://file/http")),
            "{title}"
        )
        assertTrue(links.isEmpty())
    }

    @Test
    fun fileNameUsesPodcastFolderAndPattern() {
        assertEquals("All-In", FileNamer.podcastFolder(podcast))
        val episode = episode(10, "Hello World", "guid-hello-world", episodeNumber = 10)
        assertEquals("All-In - 10.mp3", FileNamer.fileName(pattern, podcast, episode))
    }

    private fun episode(
        id: Long,
        title: String,
        guid: String,
        episodeNumber: Int? = null
    ) = EpisodeEntity(
        id = id,
        podcastId = 1,
        guid = guid,
        title = title,
        audioUrl = "https://cdn.example.com/$id.mp3",
        episodeNumber = episodeNumber
    )
}
