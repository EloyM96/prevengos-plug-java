package com.prevengos.plug.hubbackend.service;

import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.shared.dto.PacienteDto;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import io.micrometer.core.instrument.MeterRegistry;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PacienteService {

    private final PacienteGateway pacienteGateway;
    private final SyncEventService syncEventService;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);

    public PacienteService(PacienteGateway pacienteGateway,
                           SyncEventService syncEventService,
                           MeterRegistry meterRegistry) {
        this.pacienteGateway = pacienteGateway;
        this.syncEventService = syncEventService;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public BatchSyncResponse upsertPacientes(List<PacienteDto> pacientes, String source) {
        List<UUID> identifiers = new ArrayList<>();
        String resolvedSource = resolveSource(source);
        meterRegistry.summary("hub.sync.pacientes.batch.size",
                "source", resolvedSource).record(pacientes.size());
        logger.info("Procesando lote de pacientes",
                StructuredArguments.kv("batchSize", pacientes.size()),
                StructuredArguments.kv("source", resolvedSource));

        for (PacienteDto dto : pacientes) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime createdAt = dto.createdAt() != null ? dto.createdAt() : now;
            OffsetDateTime updatedAt = dto.updatedAt() != null ? dto.updatedAt() : now;

            SyncEventRecord event = syncEventService.registerEvent("paciente-upserted", dto, resolvedSource,
                    null, null, null);
            resolvedSource = event.source();

            PacienteRecord record = new PacienteRecord(
                    dto.pacienteId(),
                    dto.nif(),
                    dto.nombre(),
                    dto.apellidos(),
                    dto.fechaNacimiento(),
                    dto.sexo(),
                    dto.telefono(),
                    dto.email(),
                    dto.empresaId(),
                    dto.centroId(),
                    dto.externoRef(),
                    createdAt,
                    updatedAt,
                    now,
                    event.syncToken() != null ? event.syncToken() : 0L
            );
            pacienteGateway.upsertPaciente(record);
            identifiers.add(record.pacienteId());

            meterRegistry.counter("hub.sync.pacientes.processed",
                    "source", resolvedSource)
                    .increment();
            logger.info("Paciente sincronizado",
                    StructuredArguments.kv("pacienteId", record.pacienteId()),
                    StructuredArguments.kv("source", resolvedSource),
                    StructuredArguments.kv("syncToken", record.syncToken()));
        }
        return new BatchSyncResponse(identifiers.size(), identifiers);
    }

    private String resolveSource(String source) {
        return source != null && !source.isBlank() ? source : SyncEventService.DEFAULT_SOURCE;
    }
}
