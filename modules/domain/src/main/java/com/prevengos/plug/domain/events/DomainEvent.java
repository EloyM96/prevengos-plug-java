package com.prevengos.plug.domain.events;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();

    Instant occurredAt();

    int version();

    String source();
}
