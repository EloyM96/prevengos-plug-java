package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.rrhh.FileDropRecord;
import com.prevengos.plug.shared.rrhh.RrhhExportRecord;

public interface RrhhAuditGateway {

    void recordExport(RrhhExportRecord record);

    void recordFileDrop(FileDropRecord record);
}
