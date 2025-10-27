package com.prevengos.plug.android.data.remote.api;

import com.prevengos.plug.android.data.remote.model.CuestionarioPayload;
import com.prevengos.plug.android.data.remote.model.PacientePayload;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;
import com.prevengos.plug.android.data.remote.model.SyncPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PrevengosSyncApi {
    @POST("sincronizacion/pacientes")
    Call<SyncResult> pushPacientes(@Body SyncPushRequest<PacientePayload> request);

    @POST("sincronizacion/cuestionarios")
    Call<SyncResult> pushCuestionarios(@Body SyncPushRequest<CuestionarioPayload> request);

    @GET("sincronizacion/pull")
    Call<SyncPullResponse> pull(@Query("since") Long since, @Query("syncToken") String syncToken);
}
