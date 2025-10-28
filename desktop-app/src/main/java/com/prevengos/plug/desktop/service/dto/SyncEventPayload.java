package com.prevengos.plug.desktop.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento devuelto por el Hub durante un pull.
 */
public final class SyncEventPayload {

    private final long syncToken;
    private final String entityType;
    private final String eventType;
    private final String payload;

    @JsonCreator
    public SyncEventPayload(@JsonProperty("syncToken") long syncToken,
                            @JsonProperty("entityType") String entityType,
                            @JsonProperty("eventType") String eventType,
                            @JsonProperty("payload") String payload) {
        this.syncToken = syncToken;
        this.entityType = entityType;
        this.eventType = eventType;
        this.payload = payload;
    }

    public long getSyncToken() {
        return syncToken;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }
}
