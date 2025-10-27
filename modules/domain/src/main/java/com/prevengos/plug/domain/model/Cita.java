package com.prevengos.plug.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Cita(
        CitaId id,
        PacienteId pacienteId,
        Instant fechaHora,
        String motivo,
        String localizacion
) {
    public Cita {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(fechaHora, "fechaHora");
        Objects.requireNonNull(motivo, "motivo");
        Objects.requireNonNull(localizacion, "localizacion");
    }
}
