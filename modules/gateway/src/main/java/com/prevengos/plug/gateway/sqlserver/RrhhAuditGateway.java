package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.FileDropLogRecord;
import com.prevengos.plug.shared.persistence.jdbc.RrhhExportRecord;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Records the outcome of RRHH integrations for traceability.
 */
public interface RrhhAuditGateway {

    void recordExport(RrhhExportRecord record);

    void recordFileDrop(FileDropLogRecord record);

    default void recordSuccessfulExport(
            String exportLabel,
            OffsetDateTime since,
            OffsetDateTime exportedAt,
            int pacientesCount,
            int cuestionariosCount,
            Path archiveLocation,
            String remoteDestination,
            String protocol,
            String processName
    ) {
        RrhhExportRecord record = new RrhhExportRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                exportLabel,
                processName != null ? processName : "rrhh-csv-export",
                protocol,
                null,
                remoteDestination,
                archiveLocation != null ? archiveLocation.toString() : null,
                pacientesCount,
                cuestionariosCount,
                "SUCCESS",
                since != null ? "Exported changes since " + since : null,
                exportedAt != null ? exportedAt : OffsetDateTime.now()
        );
        recordExport(record);
    }
}
