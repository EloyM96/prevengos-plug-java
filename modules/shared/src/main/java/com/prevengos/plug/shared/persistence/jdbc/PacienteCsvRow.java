package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Representa una fila exportable a CSV para RRHH.
 */
public record PacienteCsvRow(
        UUID pacienteId,
        String nif,
        String nombre,
        String apellidos,
        java.time.LocalDate fechaNacimiento,
        String sexo,
        String telefono,
        String email,
        UUID empresaId,
        UUID centroId,
        String externoRef,
        OffsetDateTime createdAt,
        OffsetDateTime lastModified
) {
    public static List<String> headers() {
        return List.of(
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
                "last_modified"
        );
    }

    public List<String> values() {
        return List.of(
                pacienteId.toString(),
                nullSafe(nif),
                nullSafe(nombre),
                nullSafe(apellidos),
                fechaNacimiento == null ? "" : fechaNacimiento.toString(),
                nullSafe(sexo),
                nullSafe(telefono),
                nullSafe(email),
                empresaId == null ? "" : empresaId.toString(),
                centroId == null ? "" : centroId.toString(),
                nullSafe(externoRef),
                createdAt == null ? "" : createdAt.toString(),
                lastModified == null ? "" : lastModified.toString()
        );
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
