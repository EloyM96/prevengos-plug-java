package com.prevengos.plug.gateway.sqlserver;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SyncEventRecord(
        Long syncToken,
        UUID eventId,
        String eventType,
        int version,
        OffsetDateTime occurredAt,
        String source,
        UUID correlationId,
        UUID causationId,
        JsonNode payload,
        JsonNode metadata
) {
}
