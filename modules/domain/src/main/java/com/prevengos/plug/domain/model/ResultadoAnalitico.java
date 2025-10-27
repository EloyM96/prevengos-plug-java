package com.prevengos.plug.domain.model;

import java.time.Instant;
import java.util.Objects;

public record ResultadoAnalitico(
        ResultadoId id,
        PacienteId pacienteId,
        CuestionarioId cuestionarioId,
        String tipo,
        String valor,
        Instant registradoEn
) {
    public ResultadoAnalitico {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(pacienteId, "pacienteId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(registradoEn, "registradoEn");
    }
}
