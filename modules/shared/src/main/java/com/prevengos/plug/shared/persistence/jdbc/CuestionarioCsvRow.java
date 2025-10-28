package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Fila exportable para cuestionarios.
 */
public record CuestionarioCsvRow(
        UUID cuestionarioId,
        UUID pacienteId,
        String plantillaCodigo,
        String estado,
        String respuestas,
        String firmas,
        String adjuntos,
        OffsetDateTime createdAt,
        OffsetDateTime lastModified
) {
    public static List<String> headers() {
        return List.of(
                "cuestionario_id",
                "paciente_id",
                "plantilla_codigo",
                "estado",
                "respuestas",
                "firmas",
                "adjuntos",
                "created_at",
                "last_modified"
        );
    }

    public List<String> values() {
        return List.of(
                cuestionarioId.toString(),
                pacienteId == null ? "" : pacienteId.toString(),
                nullSafe(plantillaCodigo),
                nullSafe(estado),
                nullSafe(respuestas),
                nullSafe(firmas),
                nullSafe(adjuntos),
                createdAt == null ? "" : createdAt.toString(),
                lastModified == null ? "" : lastModified.toString()
        );
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
