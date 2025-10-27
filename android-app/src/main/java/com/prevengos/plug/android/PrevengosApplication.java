package com.prevengos.plug.android;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.prevengos.plug.android.sync.SyncWorker;

import java.util.concurrent.TimeUnit;

public class PrevengosApplication extends Application {
    private static final String SYNC_WORK_NAME = "prevengos-sync";

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleBackgroundSync();
    }

    private void scheduleBackgroundSync() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SyncWorker.class, 6, TimeUnit.HOURS)
                .addTag(SYNC_WORK_NAME)
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}
