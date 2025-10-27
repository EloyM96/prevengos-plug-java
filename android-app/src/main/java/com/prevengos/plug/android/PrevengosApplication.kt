package com.prevengos.plug.android

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prevengos.plug.android.di.AppContainer
import com.prevengos.plug.android.sync.SyncWorker
import java.util.concurrent.TimeUnit

class PrevengosApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        scheduleBackgroundSync()
    }

    fun triggerOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(this).enqueue(request)
    }

    private fun scheduleBackgroundSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .addTag(SYNC_WORK_NAME)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    companion object {
        private const val SYNC_WORK_NAME = "prevengos-sync"
    }
}
