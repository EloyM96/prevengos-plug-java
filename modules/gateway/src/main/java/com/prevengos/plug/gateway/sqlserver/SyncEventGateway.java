package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.sync.dto.SyncEventDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SyncEventGateway {

    long registerEvent(UUID eventId,
                       String eventType,
                       int version,
                       OffsetDateTime occurredAt,
                       String source,
                       UUID correlationId,
                       UUID causationId,
                       String payload,
                       String metadata);

    List<SyncEventDto> fetchAfter(long token, int limit);
}
