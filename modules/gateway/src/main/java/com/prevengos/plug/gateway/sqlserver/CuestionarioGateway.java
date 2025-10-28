package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioRecord;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CuestionarioGateway {

    void upsertCuestionario(CuestionarioRecord cuestionario);

    List<CuestionarioRecord> findUpdatedSince(OffsetDateTime since, int limit);

    List<CuestionarioRecord> findByPacienteId(UUID pacienteId);

    List<CuestionarioCsvRow> fetchForRrhhExport(OffsetDateTime updatedSince);
}
