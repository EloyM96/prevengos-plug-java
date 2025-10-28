package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

public class SyncItem {
    @Json(name = "event_id")
    private final String eventId;
    @Json(name = "change_version")
    private final Long changeVersion;
    private final Object payload;
    private final boolean deleted;
    @Json(name = "observed_at")
    private final String observedAt;

    public SyncItem(String eventId, Long changeVersion, Object payload, boolean deleted, String observedAt) {
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

    public Object getPayload() {
        return payload;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getObservedAt() {
        return observedAt;
    }
}
