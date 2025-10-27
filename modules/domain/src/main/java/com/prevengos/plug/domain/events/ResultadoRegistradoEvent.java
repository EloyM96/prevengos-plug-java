package com.prevengos.plug.domain.events;

import com.prevengos.plug.domain.model.ResultadoAnalitico;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ResultadoRegistradoEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        String source,
        ResultadoAnalitico resultado
) implements DomainEvent {
    public ResultadoRegistradoEvent(String source, ResultadoAnalitico resultado) {
        this(null, null, 1, source, resultado);
    }

    public ResultadoRegistradoEvent {
        UUID resolvedId = eventId != null ? eventId : UUID.randomUUID();
        Instant resolvedOccurredAt = occurredAt != null ? occurredAt : Instant.now();
        int resolvedVersion = version;
        String resolvedSource = Objects.requireNonNull(source, "source");
        ResultadoAnalitico resolvedResultado = Objects.requireNonNull(resultado, "resultado");

        this.eventId = resolvedId;
        this.occurredAt = resolvedOccurredAt;
        this.version = resolvedVersion;
        this.source = resolvedSource;
        this.resultado = resolvedResultado;
    }
}
