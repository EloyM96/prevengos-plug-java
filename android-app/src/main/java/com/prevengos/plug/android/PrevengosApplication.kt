package com.prevengos.plug.android

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prevengos.plug.android.sync.SyncWorker
import java.util.concurrent.TimeUnit

class PrevengosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundSync()
    }

    private fun scheduleBackgroundSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .addTag(SYNC_WORK_NAME)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    companion object {
        private const val SYNC_WORK_NAME = "prevengos-sync"
    }
}
