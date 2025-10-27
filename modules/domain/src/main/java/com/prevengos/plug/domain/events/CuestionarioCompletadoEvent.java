package com.prevengos.plug.domain.events;

import com.prevengos.plug.domain.model.Cuestionario;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CuestionarioCompletadoEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        String source,
        Cuestionario cuestionario
) implements DomainEvent {
    public CuestionarioCompletadoEvent(String source, Cuestionario cuestionario) {
        this(null, null, 1, source, cuestionario);
    }

    public CuestionarioCompletadoEvent {
        UUID resolvedId = eventId != null ? eventId : UUID.randomUUID();
        Instant resolvedOccurredAt = occurredAt != null ? occurredAt : Instant.now();
        int resolvedVersion = version;
        String resolvedSource = Objects.requireNonNull(source, "source");
        Cuestionario resolvedCuestionario = Objects.requireNonNull(cuestionario, "cuestionario");

        this.eventId = resolvedId;
        this.occurredAt = resolvedOccurredAt;
        this.version = resolvedVersion;
        this.source = resolvedSource;
        this.cuestionario = resolvedCuestionario;
    }
}
