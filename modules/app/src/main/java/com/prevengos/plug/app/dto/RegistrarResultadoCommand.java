package com.prevengos.plug.app.dto;

import com.prevengos.plug.domain.model.CuestionarioId;
import com.prevengos.plug.domain.model.PacienteId;
import com.prevengos.plug.domain.model.ResultadoAnalitico;
import com.prevengos.plug.domain.model.ResultadoId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RegistrarResultadoCommand(
        UUID pacienteId,
        UUID cuestionarioId,
        String tipo,
        String valor,
        String fuente
) {
    public RegistrarResultadoCommand {
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(fuente, "fuente");
    }

    public ResultadoAnalitico toResultado() {
        CuestionarioId mappedCuestionario = cuestionarioId != null ? new CuestionarioId(cuestionarioId) : null;
        return new ResultadoAnalitico(
                new ResultadoId(UUID.randomUUID()),
                new PacienteId(pacienteId),
                mappedCuestionario,
                tipo,
                valor,
                Instant.now()
        );
    }
}
