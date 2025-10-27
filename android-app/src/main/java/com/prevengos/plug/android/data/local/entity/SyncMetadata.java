package com.prevengos.plug.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_metadata")
public class SyncMetadata {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "resource_type")
    private final String resourceType;

    @ColumnInfo(name = "last_synced_at")
    private final Long lastSyncedAt;

    @ColumnInfo(name = "sync_token")
    private final String syncToken;

    public SyncMetadata(@NonNull String resourceType, Long lastSyncedAt, String syncToken) {
        this.resourceType = resourceType;
        this.lastSyncedAt = lastSyncedAt;
        this.syncToken = syncToken;
    }

    @NonNull
    public String getResourceType() {
        return resourceType;
    }

    public Long getLastSyncedAt() {
        return lastSyncedAt;
    }

    public String getSyncToken() {
        return syncToken;
    }
}
