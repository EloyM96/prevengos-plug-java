package com.prevengos.plug.domain.events;

import com.prevengos.plug.domain.model.Paciente;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PacienteRegistradoEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        String source,
        Paciente paciente
) implements DomainEvent {
    public PacienteRegistradoEvent(String source, Paciente paciente) {
        this(null, null, 1, source, paciente);
    }

    public PacienteRegistradoEvent {
        UUID resolvedId = eventId != null ? eventId : UUID.randomUUID();
        Instant resolvedOccurredAt = occurredAt != null ? occurredAt : Instant.now();
        int resolvedVersion = version;
        String resolvedSource = Objects.requireNonNull(source, "source");
        Paciente resolvedPaciente = Objects.requireNonNull(paciente, "paciente");

        this.eventId = resolvedId;
        this.occurredAt = resolvedOccurredAt;
        this.version = resolvedVersion;
        this.source = resolvedSource;
        this.paciente = resolvedPaciente;
    }
}
