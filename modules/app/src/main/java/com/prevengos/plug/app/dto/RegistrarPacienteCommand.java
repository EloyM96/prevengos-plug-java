package com.prevengos.plug.app.dto;

import com.prevengos.plug.domain.model.Paciente;
import com.prevengos.plug.domain.model.PacienteId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record RegistrarPacienteCommand(
        String nombre,
        String apellidos,
        String documentoIdentidad,
        LocalDate fechaNacimiento,
        String empresa,
        String centroTrabajo,
        String fuente
) {
    public RegistrarPacienteCommand {
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(apellidos, "apellidos");
        Objects.requireNonNull(documentoIdentidad, "documentoIdentidad");
        Objects.requireNonNull(fechaNacimiento, "fechaNacimiento");
        Objects.requireNonNull(fuente, "fuente");
    }

    public Paciente toPaciente() {
        return new Paciente(
                new PacienteId(UUID.randomUUID()),
                nombre,
                apellidos,
                documentoIdentidad,
                fechaNacimiento,
                empresa,
                centroTrabajo,
                Instant.now()
        );
    }
}
