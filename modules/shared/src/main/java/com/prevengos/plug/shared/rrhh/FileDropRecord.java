package com.prevengos.plug.shared.rrhh;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Auditoría de ficheros entregados mediante drop remoto.
 */
public record FileDropRecord(
        UUID logId,
        UUID traceId,
        String processName,
        String protocol,
        String remotePath,
        String fileName,
        String checksum,
        String status,
        String message,
        OffsetDateTime createdAt
) {
}
