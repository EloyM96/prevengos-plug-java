package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.List;

public class SyncPushRequest<T> {
    private final List<T> items;
    @Json(name = "sync_token")
    private final String syncToken;

    public SyncPushRequest(List<T> items, String syncToken) {
        this.items = items;
        this.syncToken = syncToken;
    }

    public List<T> getItems() {
        return items;
    }

    public String getSyncToken() {
        return syncToken;
    }
}
