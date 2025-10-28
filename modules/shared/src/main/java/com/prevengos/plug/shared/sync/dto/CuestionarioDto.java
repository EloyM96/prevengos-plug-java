package com.prevengos.plug.shared.sync.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO que representa un cuestionario sincronizado entre dispositivos y el hub.
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
}
