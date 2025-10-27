package com.prevengos.plug.hubbackend.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SyncEventResponse(
        long syncToken,
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
