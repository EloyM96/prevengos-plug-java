package com.prevengos.plug.shared.rrhh;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa una auditoría básica de exportaciones RRHH.
 */
public record RrhhExportRecord(
        UUID exportId,
        UUID traceId,
        String triggerType,
        String processName,
        String origin,
        String operator,
        String remotePath,
        String archivePath,
        int pacientesCount,
        int cuestionariosCount,
        String status,
        String message,
        OffsetDateTime createdAt
) {
}
