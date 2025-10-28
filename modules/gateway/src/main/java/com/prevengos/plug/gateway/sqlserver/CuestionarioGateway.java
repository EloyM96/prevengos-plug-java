package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioRecord;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Access to questionnaire data stored in the SQL Server operational database.
 */
public interface CuestionarioGateway {

    void upsertCuestionario(CuestionarioRecord cuestionario);

    List<CuestionarioRecord> findUpdatedSince(OffsetDateTime since, int limit);

    List<CuestionarioRecord> findByPacienteId(UUID pacienteId);

    /**
     * Retrieves the questionnaires that changed after the provided timestamp and should be exported.
     *
     * @param since lower bound for the updated timestamp
     * @return list of questionnaires ordered for export
     */
    List<CuestionarioCsvRow> fetchForRrhhExport(OffsetDateTime since);
}
