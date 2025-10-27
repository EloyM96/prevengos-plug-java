package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.Collections;
import java.util.List;

public class SyncPullResponse {
    private final List<PacientePayload> pacientes;
    private final List<CuestionarioPayload> cuestionarios;
    @Json(name = "sync_token")
    private final String syncToken;
    @Json(name = "last_synced_at")
    private final Long lastSyncedAt;

    public SyncPullResponse(List<PacientePayload> pacientes,
                            List<CuestionarioPayload> cuestionarios,
                            String syncToken,
                            Long lastSyncedAt) {
        this.pacientes = pacientes == null ? Collections.emptyList() : pacientes;
        this.cuestionarios = cuestionarios == null ? Collections.emptyList() : cuestionarios;
        this.syncToken = syncToken;
        this.lastSyncedAt = lastSyncedAt;
    }

    public List<PacientePayload> getPacientes() {
        return pacientes;
    }

    public List<CuestionarioPayload> getCuestionarios() {
        return cuestionarios;
    }

    public String getSyncToken() {
        return syncToken;
    }

    public Long getLastSyncedAt() {
        return lastSyncedAt;
    }
}
