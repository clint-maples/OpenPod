package com.openpod.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.openpod.app.data.PodRepository
import com.openpod.app.data.db.SettingsEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PodcastSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            PodRepository(applicationContext).refreshAll()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE = "openpod-sync"

        fun enqueue(
            context: Context,
            frequency: String = SettingsEntity.SYNC_30_MIN,
            hour: Int = 6,
            minute: Int = 0
        ) {
            val wm = WorkManager.getInstance(context)
            val (repeat, unit) = repeatInterval(frequency)
            val delayMs = initialDelayMs(frequency, hour, minute)
            val request = PeriodicWorkRequestBuilder<PodcastSyncWorker>(repeat, unit)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()
            wm.enqueueUniquePeriodicWork(UNIQUE, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        internal fun repeatInterval(frequency: String): Pair<Long, TimeUnit> = when (frequency) {
            SettingsEntity.SYNC_30_MIN -> 30L to TimeUnit.MINUTES
            SettingsEntity.SYNC_HOURLY -> 1L to TimeUnit.HOURS
            SettingsEntity.SYNC_WEEKLY -> 7L to TimeUnit.DAYS
            else -> 1L to TimeUnit.DAYS
        }

        /** Thirty-minute and hourly checks run all day. Daily and weekly still wait for the saved clock time. */
        internal fun initialDelayMs(
            frequency: String,
            hour: Int,
            minute: Int,
            now: Calendar = Calendar.getInstance()
        ): Long {
            if (frequency == SettingsEntity.SYNC_30_MIN || frequency == SettingsEntity.SYNC_HOURLY) return 0L
            return delayUntil(
                hour.coerceIn(0, 23),
                minute.coerceIn(0, 59),
                frequency == SettingsEntity.SYNC_WEEKLY,
                now
            )
        }

        private fun delayUntil(
            hour: Int,
            minute: Int,
            weekly: Boolean,
            now: Calendar = Calendar.getInstance()
        ): Long {
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (!next.after(now)) {
                next.add(Calendar.DAY_OF_YEAR, if (weekly) 7 else 1)
            } else if (weekly) {
                // keep today's time if still upcoming; otherwise already handled
            }
            return next.timeInMillis - now.timeInMillis
        }
    }
}
