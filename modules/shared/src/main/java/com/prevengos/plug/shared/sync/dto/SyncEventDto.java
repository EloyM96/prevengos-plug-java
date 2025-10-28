package com.prevengos.plug.shared.sync.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa un evento de sincronización persistido para pull incremental.
 */
public record SyncEventDto(
        Long syncToken,
        UUID eventId,
        String eventType,
        int version,
        OffsetDateTime occurredAt,
        String source,
        UUID correlationId,
        UUID causationId,
        String payload,
        String metadata
) {
}
