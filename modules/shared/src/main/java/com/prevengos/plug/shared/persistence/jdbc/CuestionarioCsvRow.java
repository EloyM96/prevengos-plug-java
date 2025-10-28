package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Represents the questionnaire data exported to RRHH consumers.
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
        OffsetDateTime updatedAt
) {

    public static final List<String> CSV_HEADERS = List.of(
            "cuestionario_id",
            "paciente_id",
            "plantilla_codigo",
            "estado",
            "respuestas",
            "firmas",
            "adjuntos",
            "created_at",
            "updated_at"
    );

    public List<String> toCsvRow() {
        return List.of(
                safeToString(cuestionarioId),
                safeToString(pacienteId),
                safeToString(plantillaCodigo),
                safeToString(estado),
                safeToString(respuestas),
                safeToString(firmas),
                safeToString(adjuntos),
                safeToDateTime(createdAt),
                safeToDateTime(updatedAt)
        );
    }

    private static String safeToString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String safeToDateTime(OffsetDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
