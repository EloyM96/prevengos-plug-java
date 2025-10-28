package com.prevengos.plug.shared.sync.dto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Solicitud de push enviada por clientes desconectados.
 */
public record SyncPushRequest(
        String source,
        UUID correlationId,
        List<PacienteDto> pacientes,
        List<CuestionarioDto> cuestionarios
) {
    public SyncPushRequest {
        pacientes = pacientes == null ? Collections.emptyList() : List.copyOf(pacientes);
        cuestionarios = cuestionarios == null ? Collections.emptyList() : List.copyOf(cuestionarios);
    }
}
