package com.openpod.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.openpod.app.data.PodRepository
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
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<PodcastSyncWorker>(6, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "openpod-sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
