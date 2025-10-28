package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CuestionarioCsvRow(
        UUID cuestionarioId,
        UUID pacienteId,
        String plantillaCodigo,
        String estado,
        String respuestas,
        String firmas,
        String adjuntos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
