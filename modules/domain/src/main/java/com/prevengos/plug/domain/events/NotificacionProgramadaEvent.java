package com.prevengos.plug.domain.events;

import com.prevengos.plug.domain.model.Notificacion;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record NotificacionProgramadaEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        String source,
        Notificacion notificacion
) implements DomainEvent {
    public NotificacionProgramadaEvent(String source, Notificacion notificacion) {
        this(null, null, 1, source, notificacion);
    }

    public NotificacionProgramadaEvent {
        UUID resolvedId = eventId != null ? eventId : UUID.randomUUID();
        Instant resolvedOccurredAt = occurredAt != null ? occurredAt : Instant.now();
        int resolvedVersion = version;
        String resolvedSource = Objects.requireNonNull(source, "source");
        Notificacion resolvedNotificacion = Objects.requireNonNull(notificacion, "notificacion");

        this.eventId = resolvedId;
        this.occurredAt = resolvedOccurredAt;
        this.version = resolvedVersion;
        this.source = resolvedSource;
        this.notificacion = resolvedNotificacion;
    }
}
