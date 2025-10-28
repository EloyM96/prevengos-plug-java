package com.prevengos.plug.desktop.sync.dto;

public record SyncBatchResponse(
        int pacientesProcesados,
        int cuestionariosProcesados,
        long lastSyncToken
) {
}
