package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.FileDropLogRecord;
import com.prevengos.plug.shared.persistence.jdbc.RrhhExportRecord;

public interface RrhhAuditGateway {

    void recordExport(RrhhExportRecord record);

    void recordFileDrop(FileDropLogRecord record);
}
