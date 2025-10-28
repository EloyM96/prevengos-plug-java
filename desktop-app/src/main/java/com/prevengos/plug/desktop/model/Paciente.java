package com.prevengos.plug.desktop.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Paciente(
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
        long lastModified,
        long syncToken,
        boolean dirty
) {
    public String nombreCompleto() {
        return nombre + " " + apellidos;
    }
}
