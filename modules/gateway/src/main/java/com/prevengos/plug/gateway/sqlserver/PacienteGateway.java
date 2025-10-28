package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Access to patient data stored in the SQL Server operational database.
 */
public interface PacienteGateway {

    void upsertPaciente(PacienteRecord paciente);

    List<PacienteRecord> findUpdatedSince(OffsetDateTime since, int limit);

    List<PacienteRecord> findByIds(List<UUID> identifiers);

    /**
     * Retrieves the patients that changed after the provided timestamp and should be exported.
     *
     * @param since lower bound for the updated timestamp
     * @return list of patients ordered for export
     */
    List<PacienteCsvRow> fetchForRrhhExport(OffsetDateTime since);
}
