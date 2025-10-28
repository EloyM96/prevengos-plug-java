package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.Map;

public class SyncChangeItem {
    @Json(name = "event_id")
    private final String eventId;
    @Json(name = "change_version")
    private final Long changeVersion;
    private final Map<String, Object> payload;
    private final boolean deleted;
    @Json(name = "observed_at")
    private final String observedAt;

    public SyncChangeItem(String eventId,
                          Long changeVersion,
                          Map<String, Object> payload,
                          boolean deleted,
                          String observedAt) {
        this.eventId = eventId;
        this.changeVersion = changeVersion;
        this.payload = payload;
        this.deleted = deleted;
        this.observedAt = observedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getChangeVersion() {
        return changeVersion;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getObservedAt() {
        return observedAt;
    }
}
