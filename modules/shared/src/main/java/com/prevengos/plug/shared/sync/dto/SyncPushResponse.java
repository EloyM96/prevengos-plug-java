package com.prevengos.plug.shared.sync.dto;

import java.util.List;
import java.util.UUID;

/**
 * Respuesta después de procesar un lote de sincronización entrante.
 */
public record SyncPushResponse(
        long processedPacientes,
        long processedCuestionarios,
        long lastSyncToken,
        List<UUID> createdOrUpdatedIds
) {
}
