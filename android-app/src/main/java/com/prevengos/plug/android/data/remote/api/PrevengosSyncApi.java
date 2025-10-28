package com.prevengos.plug.android.data.remote.api;

import com.prevengos.plug.android.data.remote.model.AsyncJobResponse;
import com.prevengos.plug.android.data.remote.model.SyncEntityPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PrevengosSyncApi {
    @POST("sincronizacion/pacientes")
    Call<AsyncJobResponse> pushPacientes(@Body SyncEntityPushRequest request);

    @POST("sincronizacion/cuestionarios")
    Call<AsyncJobResponse> pushCuestionarios(@Body SyncEntityPushRequest request);

    @GET("sincronizacion/pull")
    Call<SyncPullResponse> pull(
            @Query("since") String since,
            @Query("entities") String entities,
            @Query("limit") Integer limit);
}
