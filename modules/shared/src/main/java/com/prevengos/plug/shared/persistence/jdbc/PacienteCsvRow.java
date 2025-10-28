package com.prevengos.plug.shared.persistence.jdbc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Represents the subset of patient data required to generate RRHH exports.
 */
public record PacienteCsvRow(
        UUID pacienteId,
        String nif,
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String sexo,
        String telefono,
        String email,
        UUID empresaId,
        UUID centroId,
        String externoRef,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static final List<String> CSV_HEADERS = List.of(
            "paciente_id",
            "nif",
            "nombre",
            "apellidos",
            "fecha_nacimiento",
            "sexo",
            "telefono",
            "email",
            "empresa_id",
            "centro_id",
            "externo_ref",
            "created_at",
            "updated_at"
    );

    public List<String> toCsvRow() {
        return List.of(
                safeToString(pacienteId),
                safeToString(nif),
                safeToString(nombre),
                safeToString(apellidos),
                safeToDate(fechaNacimiento),
                safeToString(sexo),
                safeToString(telefono),
                safeToString(email),
                safeToString(empresaId),
                safeToString(centroId),
                safeToString(externoRef),
                safeToDateTime(createdAt),
                safeToDateTime(updatedAt)
        );
    }

    private static String safeToString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String safeToDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private static String safeToDateTime(OffsetDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
