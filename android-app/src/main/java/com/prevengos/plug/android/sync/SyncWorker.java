package com.prevengos.plug.android.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Result;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prevengos.plug.android.PrevengosApplication;
import com.prevengos.plug.android.data.repository.SyncRepository;

import java.io.IOException;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        PrevengosApplication app = (PrevengosApplication) getApplicationContext();
        SyncRepository repository = app.getContainer().getSyncRepository();
        try {
            repository.syncAll();
            return Result.success();
        } catch (IOException exception) {
            return Result.retry();
        } catch (Exception exception) {
            return Result.retry();
        }
    }
}
