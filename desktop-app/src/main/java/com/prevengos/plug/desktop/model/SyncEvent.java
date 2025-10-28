package com.prevengos.plug.desktop.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Registro de eventos de sincronización locales utilizados para auditoría.
 */
public final class SyncEvent {

    private final long id;
    private final String entityType;
    private final String entityId;
    private final String eventType;
    private final String payload;
    private final Long syncToken;
    private final Instant createdAt;

    public SyncEvent(long id,
                     String entityType,
                     String entityId,
                     String eventType,
                     String payload,
                     Long syncToken,
                     Instant createdAt) {
        this.id = id;
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.entityId = Objects.requireNonNull(entityId, "entityId");
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.payload = Objects.requireNonNull(payload, "payload");
        this.syncToken = syncToken;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public long getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Long getSyncToken() {
        return syncToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
