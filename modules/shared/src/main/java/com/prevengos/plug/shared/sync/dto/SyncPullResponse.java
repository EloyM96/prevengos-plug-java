package com.prevengos.plug.shared.sync.dto;

import java.util.Collections;
import java.util.List;

/**
 * Respuesta a una petición de pull incremental.
 */
public record SyncPullResponse(
        List<PacienteDto> pacientes,
        List<CuestionarioDto> cuestionarios,
        List<SyncEventDto> events,
        long nextSyncToken
) {
    public SyncPullResponse {
        pacientes = pacientes == null ? Collections.emptyList() : List.copyOf(pacientes);
        cuestionarios = cuestionarios == null ? Collections.emptyList() : List.copyOf(cuestionarios);
        events = events == null ? Collections.emptyList() : List.copyOf(events);
    }
}
