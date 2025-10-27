package com.prevengos.plug.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record Paciente(
        PacienteId id,
        String nombre,
        String apellidos,
        String documentoIdentidad,
        LocalDate fechaNacimiento,
        String empresa,
        String centroTrabajo,
        Instant actualizadoEn
) {
    public Paciente {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(apellidos, "apellidos");
        Objects.requireNonNull(documentoIdentidad, "documentoIdentidad");
        Objects.requireNonNull(fechaNacimiento, "fechaNacimiento");
        Objects.requireNonNull(actualizadoEn, "actualizadoEn");
    }
}
