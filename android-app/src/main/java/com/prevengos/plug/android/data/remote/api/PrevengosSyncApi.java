package com.prevengos.plug.android.data.remote.api;

import com.prevengos.plug.android.data.remote.model.AsyncJobResponse;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;
import com.prevengos.plug.android.data.remote.model.SyncPushRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PrevengosSyncApi {
    @POST("sincronizacion/push")
    Call<AsyncJobResponse> push(@Body SyncPushRequest request);

    @GET("sincronizacion/pull")
    Call<SyncPullResponse> pull(
            @Query("since") String since,
            @Query("entities") String entities,
            @Query("limit") Integer limit);
}
