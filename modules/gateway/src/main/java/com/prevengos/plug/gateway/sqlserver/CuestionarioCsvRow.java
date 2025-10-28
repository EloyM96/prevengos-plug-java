package com.prevengos.plug.gateway.sqlserver;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CuestionarioCsvRow(
        UUID cuestionarioId,
        UUID pacienteId,
        String plantillaCodigo,
        String estado,
        OffsetDateTime updatedAt
) {
}
