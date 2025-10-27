package com.prevengos.plug.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record Cuestionario(
        CuestionarioId id,
        PacienteId pacienteId,
        String plantillaCodigo,
        EstadoCuestionario estado,
        Map<String, Object> respuestas,
        Instant completadoEn
) {
    public Cuestionario {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(plantillaCodigo, "plantillaCodigo");
        Objects.requireNonNull(estado, "estado");
        Objects.requireNonNull(respuestas, "respuestas");
    }
}
