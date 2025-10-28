package com.prevengos.plug.android.data.remote.api;

import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PrevengosSyncApi {
    @POST("sincronizacion/push")
    Call<SyncPushResponse> push(@Body SyncPushRequest request);

    @GET("sincronizacion/pull")
    Call<SyncPullResponse> pull(
            @Query("syncToken") Long syncToken,
            @Query("limit") Integer limit);
}
