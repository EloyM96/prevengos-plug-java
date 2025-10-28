package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CuestionarioGateway {
    void upsert(CuestionarioDto cuestionario, OffsetDateTime lastModified, long syncToken);

    List<CuestionarioDto> fetchAfterToken(long token, int limit);

    List<CuestionarioCsvRow> fetchForRrhhExport(OffsetDateTime since);

    CuestionarioDto findById(UUID cuestionarioId);
}
