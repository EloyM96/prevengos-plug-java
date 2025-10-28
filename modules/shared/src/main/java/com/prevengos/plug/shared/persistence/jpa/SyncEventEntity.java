package com.prevengos.plug.shared.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sync_events")
public class SyncEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_token")
    private Long syncToken;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "source", nullable = false, length = 128)
    private String source;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Column(name = "causation_id")
    private UUID causationId;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Lob
    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SyncEventEntity() {
        // JPA only
    }

    public SyncEventEntity(Long syncToken,
                           UUID eventId,
                           String eventType,
                           int version,
                           OffsetDateTime occurredAt,
                           String source,
                           UUID correlationId,
                           UUID causationId,
                           String payload,
                           String metadata,
                           OffsetDateTime createdAt) {
        this.syncToken = syncToken;
        this.eventId = eventId;
        this.eventType = eventType;
        this.version = version;
        this.occurredAt = occurredAt;
        this.source = source;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.payload = payload;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public Long getSyncToken() {
        return syncToken;
    }

    public void setSyncToken(Long syncToken) {
        this.syncToken = syncToken;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public UUID getCausationId() {
        return causationId;
    }

    public void setCausationId(UUID causationId) {
        this.causationId = causationId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SyncEventEntity that)) {
            return false;
        }
        return Objects.equals(syncToken, that.syncToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(syncToken);
    }

    @Override
    public String toString() {
        return "SyncEventEntity{" +
                "syncToken=" + syncToken +
                ", eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", version=" + version +
                ", occurredAt=" + occurredAt +
                ", source='" + source + '\'' +
                ", correlationId=" + correlationId +
                ", causationId=" + causationId +
                ", createdAt=" + createdAt +
                '}';
    }
}
