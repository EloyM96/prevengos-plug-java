package com.prevengos.plug.shared.sync.dto;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Contrato compartido para los cuestionarios intercambiados durante la sincronización.
 * Garantiza un formato coherente independientemente del cliente que produzca el evento.
 */
public record CuestionarioDto(
        UUID cuestionarioId,
        UUID pacienteId,
        String plantillaCodigo,
        String estado,
        String respuestas,
        String firmas,
        String adjuntos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime lastModified,
        Long syncToken
) {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("borrador", "completado", "validado");

    public CuestionarioDto {
        Objects.requireNonNull(cuestionarioId, "cuestionarioId no puede ser nulo");
        Objects.requireNonNull(pacienteId, "pacienteId no puede ser nulo");
        Objects.requireNonNull(plantillaCodigo, "plantillaCodigo no puede ser nulo");

        if (plantillaCodigo.isBlank()) {
            throw new IllegalArgumentException("El código de plantilla no puede estar vacío");
        }
        if (estado != null && !estado.isBlank() && !ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("El estado del cuestionario no es válido");
        }
        if (respuestas != null && respuestas.isBlank()) {
            throw new IllegalArgumentException("Las respuestas no pueden ser una cadena vacía");
        }
        if (firmas != null && firmas.isBlank()) {
            throw new IllegalArgumentException("Las firmas no pueden ser una cadena vacía");
        }
        if (adjuntos != null && adjuntos.isBlank()) {
            throw new IllegalArgumentException("Los adjuntos no pueden ser una cadena vacía");
        }
    }
}
