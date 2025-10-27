package com.prevengos.plug.app.dto;

import com.prevengos.plug.domain.model.Cuestionario;
import com.prevengos.plug.domain.model.CuestionarioId;
import com.prevengos.plug.domain.model.EstadoCuestionario;
import com.prevengos.plug.domain.model.PacienteId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record GuardarCuestionarioCommand(
        UUID pacienteId,
        String plantillaCodigo,
        EstadoCuestionario estado,
        Map<String, Object> respuestas,
        String fuente
) {
    public GuardarCuestionarioCommand {
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(plantillaCodigo, "plantillaCodigo");
        Objects.requireNonNull(estado, "estado");
        Objects.requireNonNull(respuestas, "respuestas");
        Objects.requireNonNull(fuente, "fuente");
    }

    public Cuestionario toCuestionario() {
        Instant completadoEn = (estado == EstadoCuestionario.COMPLETADO || estado == EstadoCuestionario.VALIDADO)
                ? Instant.now()
                : null;
        return new Cuestionario(
                new CuestionarioId(UUID.randomUUID()),
                new PacienteId(pacienteId),
                plantillaCodigo,
                estado,
                respuestas,
                completadoEn
        );
    }
}
