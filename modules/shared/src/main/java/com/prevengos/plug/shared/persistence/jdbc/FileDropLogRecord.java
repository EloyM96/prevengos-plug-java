package com.prevengos.plug.shared.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileDropLogRecord(
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
