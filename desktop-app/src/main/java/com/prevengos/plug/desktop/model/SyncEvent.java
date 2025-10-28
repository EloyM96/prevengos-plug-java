package com.prevengos.plug.desktop.model;

import java.time.OffsetDateTime;

public record SyncEvent(
        long eventId,
        String entityType,
        String entityId,
        String eventType,
        String payload,
        String source,
        OffsetDateTime createdAt,
        long syncToken
) {
}
