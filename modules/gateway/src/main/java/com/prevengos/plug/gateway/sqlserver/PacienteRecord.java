package com.prevengos.plug.gateway.sqlserver;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PacienteRecord(
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
        OffsetDateTime updatedAt,
        OffsetDateTime lastModified,
        long syncToken
) {
}
