package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.sync.dto.PacienteDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PacienteGateway {

    void upsert(PacienteDto paciente, OffsetDateTime lastModified, long syncToken);

    List<PacienteDto> fetchAfterToken(long token, int limit);

    List<PacienteCsvRow> fetchForRrhhExport(OffsetDateTime since);

    PacienteDto findById(UUID pacienteId);
}
