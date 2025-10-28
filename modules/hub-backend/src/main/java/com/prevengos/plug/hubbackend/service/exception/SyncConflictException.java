package com.prevengos.plug.hubbackend.service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Excepción lanzada cuando se detecta un conflicto de sincronización basado en timestamps.
 */
public class SyncConflictException extends RuntimeException {

    private final SyncConflictDetails details;

    private SyncConflictException(SyncConflictDetails details) {
        super("Conflicto de sincronización");
        this.details = details;
    }

    public static SyncConflictException paciente(UUID pacienteId,
                                                  OffsetDateTime incomingUpdatedAt,
                                                  OffsetDateTime storedUpdatedAt) {
        return new SyncConflictException(new SyncConflictDetails(
                "pacientes",
                pacienteId,
                null,
                incomingUpdatedAt,
                storedUpdatedAt
        ));
    }

    public static SyncConflictException cuestionario(UUID cuestionarioId,
                                                      OffsetDateTime incomingUpdatedAt,
                                                      OffsetDateTime storedUpdatedAt) {
        return new SyncConflictException(new SyncConflictDetails(
                "cuestionarios",
                null,
                cuestionarioId,
                incomingUpdatedAt,
                storedUpdatedAt
        ));
    }

    public SyncConflictDetails getDetails() {
        return details;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SyncConflictDetails(String entity,
                                      UUID pacienteId,
                                      UUID cuestionarioId,
                                      OffsetDateTime incomingUpdatedAt,
                                      OffsetDateTime storedUpdatedAt) {
    }
}
