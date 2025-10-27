package com.prevengos.plug.hubbackend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "sync_events")
public class SyncEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_token")
    private Long syncToken;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Column(name = "causation_id")
    private UUID causationId;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Lob
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SyncEvent() {
        // JPA
    }

    public SyncEvent(UUID eventId, String eventType, int version, OffsetDateTime occurredAt, String source,
                     UUID correlationId, UUID causationId, String payload, String metadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.version = version;
        this.occurredAt = occurredAt;
        this.source = source;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.payload = payload;
        this.metadata = metadata;
    }

    @PrePersist
    void onPersist() {
        if (occurredAt == null) {
            occurredAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getSyncToken() {
        return syncToken;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getVersion() {
        return version;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getSource() {
        return source;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getCausationId() {
        return causationId;
    }

    public String getPayload() {
        return payload;
    }

    public String getMetadata() {
        return metadata;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
