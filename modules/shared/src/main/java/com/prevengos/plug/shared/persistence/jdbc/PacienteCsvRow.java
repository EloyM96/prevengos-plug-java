package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PacienteCsvRow(
        UUID pacienteId,
        String nif,
        String nombre,
        String apellidos,
        String sexo,
        OffsetDateTime updatedAt,
        String telefono,
        String email,
        UUID empresaId,
        UUID centroId,
        String externoRef
) {
}
