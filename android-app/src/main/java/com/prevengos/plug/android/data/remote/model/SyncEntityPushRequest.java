package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.Collections;
import java.util.List;

public class SyncEntityPushRequest {
    @Json(name = "client_id")
    private final String clientId;
    @Json(name = "last_sync_token")
    private final String lastSyncToken;
    private final List<SyncItem> items;

    public SyncEntityPushRequest(String clientId, String lastSyncToken, List<SyncItem> items) {
        this.clientId = clientId;
        this.lastSyncToken = lastSyncToken;
        this.items = items == null ? Collections.emptyList() : items;
    }

    public String getClientId() {
        return clientId;
    }

    public String getLastSyncToken() {
        return lastSyncToken;
    }

    public List<SyncItem> getItems() {
        return items;
    }
}
