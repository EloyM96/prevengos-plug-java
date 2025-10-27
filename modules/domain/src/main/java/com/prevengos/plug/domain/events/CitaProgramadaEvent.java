package com.prevengos.plug.domain.events;

import com.prevengos.plug.domain.model.Cita;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CitaProgramadaEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        String source,
        Cita cita
) implements DomainEvent {
    public CitaProgramadaEvent(String source, Cita cita) {
        this(null, null, 1, source, cita);
    }

    public CitaProgramadaEvent {
        UUID resolvedId = eventId != null ? eventId : UUID.randomUUID();
        Instant resolvedOccurredAt = occurredAt != null ? occurredAt : Instant.now();
        int resolvedVersion = version;
        String resolvedSource = Objects.requireNonNull(source, "source");
        Cita resolvedCita = Objects.requireNonNull(cita, "cita");

        this.eventId = resolvedId;
        this.occurredAt = resolvedOccurredAt;
        this.version = resolvedVersion;
        this.source = resolvedSource;
        this.cita = resolvedCita;
    }
}
