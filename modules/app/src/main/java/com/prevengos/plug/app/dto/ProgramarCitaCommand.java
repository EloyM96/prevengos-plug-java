package com.prevengos.plug.app.dto;

import com.prevengos.plug.domain.model.Cita;
import com.prevengos.plug.domain.model.CitaId;
import com.prevengos.plug.domain.model.PacienteId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ProgramarCitaCommand(
        UUID pacienteId,
        Instant fechaHora,
        String motivo,
        String localizacion,
        String fuente
) {
    public ProgramarCitaCommand {
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(fechaHora, "fechaHora");
        Objects.requireNonNull(motivo, "motivo");
        Objects.requireNonNull(localizacion, "localizacion");
        Objects.requireNonNull(fuente, "fuente");
    }

    public Cita toCita() {
        return new Cita(
                new CitaId(UUID.randomUUID()),
                new PacienteId(pacienteId),
                fechaHora,
                motivo,
                localizacion
        );
    }
}
