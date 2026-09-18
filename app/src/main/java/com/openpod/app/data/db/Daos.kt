package com.openpod.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcasts ORDER BY title COLLATE NOCASE")
    fun observeAll(): Flow<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts ORDER BY title COLLATE NOCASE")
    suspend fun all(): List<PodcastEntity>

    @Query("SELECT * FROM podcasts WHERE id = :id")
    suspend fun byId(id: Long): PodcastEntity?

    @Query("SELECT * FROM podcasts WHERE feedUrl = :url LIMIT 1")
    suspend fun byFeed(url: String): PodcastEntity?

    @Insert
    suspend fun insert(podcast: PodcastEntity): Long

    @Update
    suspend fun update(podcast: PodcastEntity)

    @Delete
    suspend fun delete(podcast: PodcastEntity)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate DESC")
    fun observeForPodcast(podcastId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId AND (:includeArchived = 1 OR archived = 0) ORDER BY pubDate DESC")
    fun observeForPodcastFiltered(podcastId: Long, includeArchived: Boolean): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate DESC")
    suspend fun forPodcast(podcastId: Long): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE archived = 0 ORDER BY pubDate DESC")
    suspend fun allActive(): List<EpisodeEntity>

    @Query("UPDATE episodes SET archived = :archived WHERE id IN (:ids)")
    suspend fun setArchived(ids: List<Long>, archived: Boolean)

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun byId(id: Long): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE id = :id")
    fun observeById(id: Long): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE downloadStatus = 'done' ORDER BY pubDate DESC")
    fun observeDownloaded(): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(episode: EpisodeEntity): Long

    @Update
    suspend fun update(episode: EpisodeEntity)

    @Query("UPDATE episodes SET downloadStatus = :status WHERE id = :id")
    suspend fun setDownloadStatus(id: Long, status: String)

    @Query("UPDATE episodes SET localUri = :localUri, fileName = :fileName, downloadStatus = :status WHERE id = :id")
    suspend fun setDownloadResult(id: Long, localUri: String, fileName: String, status: String)

    @Query("UPDATE episodes SET positionMs = :position, completed = :completed WHERE id = :id")
    suspend fun updateProgress(id: Long, position: Long, completed: Boolean)

    @Query("SELECT * FROM episodes WHERE completed = 1 AND downloadStatus = 'done' AND localUri != ''")
    suspend fun completedDownloads(): List<EpisodeEntity>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun byId(id: Long): PlaylistEntity?

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY sortOrder")
    fun observeItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY sortOrder")
    suspend fun items(playlistId: Long): List<PlaylistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND episodeId = :episodeId")
    suspend fun removeItem(playlistId: Long, episodeId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearItems(playlistId: Long)
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_items ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM queue_items ORDER BY sortOrder ASC")
    suspend fun all(): List<QueueItemEntity>

    @Query("SELECT episodeId FROM queue_items")
    fun observeEpisodeIds(): Flow<List<Long>>

    @Query(
        """
        SELECT episodes.*, podcasts.title AS podcastTitle, podcasts.imageUrl AS podcastImage
        FROM queue_items
        INNER JOIN episodes ON episodes.id = queue_items.episodeId
        INNER JOIN podcasts ON podcasts.id = episodes.podcastId
        ORDER BY queue_items.sortOrder ASC
        """
    )
    fun observeUpcoming(): Flow<List<EpisodeWithPodcast>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items WHERE episodeId = :episodeId")
    suspend fun remove(episodeId: Long)

    @Query("DELETE FROM queue_items")
    suspend fun clear()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)
}
