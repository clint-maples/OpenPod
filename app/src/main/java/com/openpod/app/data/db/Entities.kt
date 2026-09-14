package com.openpod.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "podcasts")
data class PodcastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val feedUrl: String,
    val siteUrl: String = "",
    val author: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val autoDownload: Boolean = false,
    val autoDelete: Boolean = false,
    val folderName: String = ""
)

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["id"],
            childColumns = ["podcastId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("podcastId"), Index(value = ["podcastId", "guid"], unique = true)]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val podcastId: Long,
    val guid: String,
    val title: String,
    val description: String = "",
    val audioUrl: String,
    val mimeType: String = "audio/mpeg",
    val pubDate: Long = 0L,
    val durationMs: Long = 0L,
    val episodeNumber: Int? = null,
    val seasonNumber: Int? = null,
    val imageUrl: String = "",
    val transcriptUrl: String = "",
    val transcriptType: String = "",
    val chaptersUrl: String = "",
    val localUri: String = "",
    val fileName: String = "",
    val downloadStatus: String = STATUS_NONE,
    val positionMs: Long = 0L,
    val completed: Boolean = false,
    val archived: Boolean = false
) {
    companion object {
        const val STATUS_NONE = "none"
        const val STATUS_QUEUED = "queued"
        const val STATUS_DOWNLOADING = "downloading"
        const val STATUS_DONE = "done"
        const val STATUS_ERROR = "error"
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isSmart: Boolean = false,
    val playFilter: String = PLAY_ALL,
    val downloadFilter: String = DOWNLOAD_ALL
) {
    companion object {
        const val PLAY_ALL = "all"
        const val PLAY_UNPLAYED = "unplayed"
        const val PLAY_IN_PROGRESS = "in_progress"
        const val PLAY_PLAYED = "played"
        const val DOWNLOAD_ALL = "all"
        const val DOWNLOAD_DOWNLOADED = "downloaded"
        const val DOWNLOAD_NOT_DOWNLOADED = "not_downloaded"
    }
}

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "episodeId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EpisodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("episodeId")]
)
data class PlaylistItemEntity(
    val playlistId: Long,
    val episodeId: Long,
    val sortOrder: Int
)

@Entity(tableName = "settings", primaryKeys = ["id"])
data class SettingsEntity(
    val id: Int = 1,
    val downloadTreeUri: String = "",
    val namingPattern: String = "{podcast} - {episode}",
    val skipForwardSec: Int = 30,
    val skipBackSec: Int = 15,
    val autoDownloadDefault: Boolean = false,
    val autoDeleteDefault: Boolean = false,
    val eqEnabled: Boolean = false,
    val eqBandsJson: String = "",
    val showArchived: Boolean = false,
    val syncFrequency: String = SYNC_DAILY,
    val syncHour: Int = 6,
    val syncMinute: Int = 0,
    val lastEpisodeId: Long = 0L,
    val volumeBoost: Float = 0f
) {
    companion object {
        const val SYNC_HOURLY = "hourly"
        const val SYNC_DAILY = "daily"
        const val SYNC_WEEKLY = "weekly"
    }
}

data class EpisodeWithPodcast(
    val episode: EpisodeEntity,
    val podcastTitle: String,
    val podcastImage: String
)
