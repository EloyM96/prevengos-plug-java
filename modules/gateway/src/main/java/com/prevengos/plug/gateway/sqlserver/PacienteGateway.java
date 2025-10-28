package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PacienteGateway {

    void upsertPaciente(PacienteRecord paciente);

    List<PacienteRecord> findUpdatedSince(OffsetDateTime since, int limit);

    List<PacienteRecord> findByIds(List<UUID> identifiers);

    List<PacienteCsvRow> fetchForRrhhExport(OffsetDateTime updatedSince);
}
