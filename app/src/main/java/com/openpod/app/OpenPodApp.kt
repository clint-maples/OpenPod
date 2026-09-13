package com.openpod.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.openpod.app.work.PodcastSyncWorker

class OpenPodApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    "playback",
                    "Playback",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        PodcastSyncWorker.enqueue(this)
    }
}
