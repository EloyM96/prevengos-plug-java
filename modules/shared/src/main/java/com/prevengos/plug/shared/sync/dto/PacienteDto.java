package com.prevengos.plug.shared.sync.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data transfer object representing the Paciente contract shared between hub and clients.
 */
public record PacienteDto(
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
        Long syncToken
) {
    public PacienteDto {
        if (sexo != null && sexo.length() > 1) {
            throw new IllegalArgumentException("Sexo debe ser una sigla de un carácter");
        }
    }
}
