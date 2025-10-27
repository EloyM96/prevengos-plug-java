package com.prevengos.plug.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prevengos.plug.android.data.local.PrevengosDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val database = PrevengosDatabase.instance(applicationContext)
            val pendientes = database.cuestionarioDao().pendientes()
            Log.d(TAG, "Encontrados ${pendientes.size} cuestionarios pendientes de sincronizar")
            Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Error ejecutando sincronización", ex)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "PrevengosSyncWorker"
    }
}
