package com.openpod.app.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.openpod.app.R
import com.openpod.app.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * One download per episode, independent of the sync worker. Manual downloads
 * and auto-downloads both enqueue this work. A network constraint, exponential
 * backoff, and a data-sync foreground notification keep a long file from being
 * dropped when the screen turns off or the feed refresh returns.
 */
typealias EpisodeDownloadWork = EpisodeDownloadWorker

class EpisodeDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun getForegroundInfo(): ForegroundInfo = foregroundInfo(applicationContext, idHint())

    override suspend fun doWork(): Result {
        val episodeId = inputData.getLong(KEY_EPISODE_ID, 0L)
        if (episodeId == 0L) return Result.failure()
        setForeground(foregroundInfo(applicationContext, episodeId))
        val result = EpisodeDownloader(applicationContext).download(episodeId)
        val error = result.exceptionOrNull()
        return when {
            result.isSuccess -> Result.success()
            error != null && isRetryable(error) -> Result.retry()
            else -> Result.failure()
        }
    }

    private fun idHint(): Long = inputData.getLong(KEY_EPISODE_ID, 0L)

    companion object {
        const val KEY_EPISODE_ID = "episodeId"
        const val DOWNLOAD_TAG = "openpod-episode-download"
        private const val CHANNEL_ID = "downloads"
        private const val CHANNEL_NAME = "Downloads"

        fun uniqueName(episodeId: Long) = "openpod-download-$episodeId"

        fun episodeTag(episodeId: Long) = "openpod-episode-$episodeId"

        fun enqueue(context: Context, episodeId: Long, replace: Boolean = false) {
            if (episodeId == 0L) return
            val request = OneTimeWorkRequestBuilder<EpisodeDownloadWorker>()
                .setInputData(workDataOf(KEY_EPISODE_ID to episodeId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(DOWNLOAD_TAG)
                .addTag(episodeTag(episodeId))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueName(episodeId),
                if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
                request
            )
        }

        suspend fun sweep(context: Context) = withContext(Dispatchers.IO) {
            val db = AppDatabase.get(context)
            val active = activeEpisodeIds(context)
            val now = android.os.SystemClock.elapsedRealtime()
            db.podcasts().all().forEach { podcast ->
                db.episodes().forPodcast(podcast.id).forEach { episode ->
                    val fresh = DownloadTracker.isProgressFresh(episode.id, now)
                    val running = episode.id in active
                    if (!AutoDownloadPlan.shouldEnqueue(episode, podcast.autoDownload, running, fresh)) {
                        return@forEach
                    }
                    enqueue(
                        context,
                        episode.id,
                        replace = AutoDownloadPlan.replaceExisting(running, fresh)
                    )
                }
            }
        }

        fun isRetryable(error: Throwable): Boolean {
            if (error is IOException) return true
            val message = error.message.orEmpty()
            return message.startsWith("Download failed (HTTP 5") ||
                message.startsWith("Download failed (HTTP 429")
        }

        internal fun activeEpisodeIds(context: Context): Set<Long> {
            val infos = WorkManager.getInstance(context)
                .getWorkInfosByTag(DOWNLOAD_TAG)
                .get()
            return infos
                .filter { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.BLOCKED }
                .flatMap { info -> info.tags.mapNotNull { tag -> tag.removePrefix("openpod-episode-").toLongOrNull() } }
                .toSet()
        }

        private fun foregroundInfo(context: Context, episodeId: Long): ForegroundInfo {
            ensureChannel(context)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Downloading episode")
                .setContentText("OpenPod is saving an episode")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
            val notificationId = 10_000 + (episodeId % 20_000).toInt()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(
                    notificationId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                ForegroundInfo(notificationId, notification)
            }
        }

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < 26) return
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
    }
}
