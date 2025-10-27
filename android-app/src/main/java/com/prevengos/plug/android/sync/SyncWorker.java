package com.prevengos.plug.android.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Result;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prevengos.plug.android.data.local.PrevengosDatabase;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;

import java.util.List;

public class SyncWorker extends Worker {
    private static final String TAG = "PrevengosSyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PrevengosDatabase database = PrevengosDatabase.instance(getApplicationContext());
            List<CuestionarioEntity> pendientes = database.cuestionarioDao().pendientes();
            Log.d(TAG, "Encontrados " + pendientes.size() + " cuestionarios pendientes de sincronizar");
            return Result.success();
        } catch (Exception ex) {
            Log.e(TAG, "Error ejecutando sincronización", ex);
            return Result.retry();
        }
    }
}
