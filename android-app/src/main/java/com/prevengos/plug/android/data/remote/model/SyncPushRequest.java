package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.Collections;
import java.util.List;

public class SyncPushRequest {
    @Json(name = "client_id")
    private final String clientId;
    @Json(name = "last_sync")
    private final String lastSync;
    private final List<SyncBatch> batches;

    public SyncPushRequest(String clientId, String lastSync, List<SyncBatch> batches) {
        this.clientId = clientId;
        this.lastSync = lastSync;
        this.batches = batches == null ? Collections.emptyList() : batches;
    }

    public String getClientId() {
        return clientId;
    }

    public String getLastSync() {
        return lastSync;
    }

    public List<SyncBatch> getBatches() {
        return batches;
    }
}
