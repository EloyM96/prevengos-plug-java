package com.prevengos.plug.android.data.remote.api

import com.prevengos.plug.android.data.remote.model.CuestionarioPayload
import com.prevengos.plug.android.data.remote.model.PacientePayload
import com.prevengos.plug.android.data.remote.model.SyncPullResponse
import com.prevengos.plug.android.data.remote.model.SyncPushRequest
import com.prevengos.plug.android.data.remote.model.SyncResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PrevengosSyncApi {
    @POST("sincronizacion/pacientes")
    suspend fun pushPacientes(@Body request: SyncPushRequest<PacientePayload>): SyncResult

    @POST("sincronizacion/cuestionarios")
    suspend fun pushCuestionarios(@Body request: SyncPushRequest<CuestionarioPayload>): SyncResult

    @GET("sincronizacion/pull")
    suspend fun pull(@Query("since") since: Long?, @Query("syncToken") syncToken: String?): SyncPullResponse
}
