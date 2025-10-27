package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

public class SyncVersion {
    private final String id;

    @Json(name = "last_modified")
    private final long lastModified;

    @Json(name = "sync_token")
    private final String syncToken;

    public SyncVersion(String id, long lastModified, String syncToken) {
        this.id = id;
        this.lastModified = lastModified;
        this.syncToken = syncToken;
    }

    public String getId() {
        return id;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getSyncToken() {
        return syncToken;
    }
}
