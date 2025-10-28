package com.prevengos.plug.desktop.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Cuestionario(
        UUID cuestionarioId,
        UUID pacienteId,
        String plantillaCodigo,
        String estado,
        String respuestas,
        String firmas,
        String adjuntos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long lastModified,
        long syncToken,
        boolean dirty
) {
}
