package com.prevengos.plug.android.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prevengos.plug.android.PrevengosApplication
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as PrevengosApplication
        val repository = app.container.syncRepository
        return try {
            repository.syncAll()
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            Result.retry()
        } catch (exception: Exception) {
            Result.retry()
        }
    }
}
