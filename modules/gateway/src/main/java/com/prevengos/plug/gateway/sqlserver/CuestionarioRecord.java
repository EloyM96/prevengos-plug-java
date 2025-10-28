package com.prevengos.plug.gateway.sqlserver;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CuestionarioRecord(
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
        long syncToken
) {
}
