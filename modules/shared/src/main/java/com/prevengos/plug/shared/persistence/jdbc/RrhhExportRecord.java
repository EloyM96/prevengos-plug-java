package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RrhhExportRecord(
        UUID exportId,
        UUID traceId,
        String trigger,
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
