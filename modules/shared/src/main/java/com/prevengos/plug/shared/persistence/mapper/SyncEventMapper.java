package com.prevengos.plug.shared.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import com.prevengos.plug.shared.persistence.jpa.SyncEventEntity;

import java.time.OffsetDateTime;

public final class SyncEventMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SyncEventMapper() {
    }

    public static SyncEventRecord toRecord(SyncEventEntity entity) {
        return new SyncEventRecord(
                entity.getSyncToken(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getVersion(),
                entity.getOccurredAt(),
                entity.getSource(),
                entity.getCorrelationId(),
                entity.getCausationId(),
                readJson(entity.getPayload()),
                readJson(entity.getMetadata())
        );
    }

    public static SyncEventEntity toEntity(SyncEventRecord record) {
        return new SyncEventEntity(
                record.syncToken(),
                record.eventId(),
                record.eventType(),
                record.version(),
                record.occurredAt(),
                record.source(),
                record.correlationId(),
                record.causationId(),
                writeJson(record.payload()),
                writeJson(record.metadata()),
                OffsetDateTime.now()
        );
    }

    private static JsonNode readJson(String value) {
        if (value == null || value.isBlank()) {
            return OBJECT_MAPPER.nullNode();
        }
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JSON payload", e);
        }
    }

    private static String writeJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize JSON payload", e);
        }
    }
}
