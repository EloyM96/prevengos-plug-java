package com.prevengos.plug.hubbackend.service;

import com.prevengos.plug.hubbackend.domain.Paciente;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.PacienteDto;
import com.prevengos.plug.hubbackend.repository.PacienteRepository;
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

    private final PacienteRepository pacienteRepository;
    private final SyncEventService syncEventService;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);

    public PacienteService(PacienteRepository pacienteRepository,
                           SyncEventService syncEventService,
                           MeterRegistry meterRegistry) {
        this.pacienteRepository = pacienteRepository;
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
            Paciente entity = pacienteRepository.findById(dto.pacienteId())
                    .orElseGet(() -> new Paciente(dto.pacienteId()));
            boolean isNew = entity.getCreatedAt() == null;
            mapDtoToEntity(dto, entity);
            if (isNew) {
                entity.setCreatedAt(dto.createdAt() != null ? dto.createdAt() : OffsetDateTime.now(ZoneOffset.UTC));
            }
            entity.setUpdatedAt(dto.updatedAt() != null ? dto.updatedAt() : entity.getUpdatedAt());
            entity.setLastModified(dto.updatedAt() != null ? dto.updatedAt() : OffsetDateTime.now(ZoneOffset.UTC));
            pacienteRepository.save(entity);

            var event = syncEventService.registerEvent("paciente-upserted", dto, source, null, null, null);
            resolvedSource = event.getSource();
            long syncToken = event.getSyncToken();
            entity.setSyncToken(syncToken);
            entity.setLastModified(OffsetDateTime.now(ZoneOffset.UTC));
            pacienteRepository.save(entity);
            identifiers.add(entity.getPacienteId());
            meterRegistry.counter("hub.sync.pacientes.processed",
                    "source", resolvedSource)
                    .increment();
            logger.info("Paciente sincronizado",
                    StructuredArguments.kv("pacienteId", entity.getPacienteId()),
                    StructuredArguments.kv("source", resolvedSource),
                    StructuredArguments.kv("syncToken", syncToken),
                    StructuredArguments.kv("isNew", isNew));
        }
        return new BatchSyncResponse(identifiers.size(), identifiers);
    }

    private String resolveSource(String source) {
        return source != null && !source.isBlank() ? source : SyncEventService.DEFAULT_SOURCE;
    }

    private void mapDtoToEntity(PacienteDto dto, Paciente entity) {
        entity.setNif(dto.nif());
        entity.setNombre(dto.nombre());
        entity.setApellidos(dto.apellidos());
        entity.setFechaNacimiento(dto.fechaNacimiento());
        entity.setSexo(dto.sexo());
        entity.setTelefono(dto.telefono());
        entity.setEmail(dto.email());
        entity.setEmpresaId(dto.empresaId());
        entity.setCentroId(dto.centroId());
        entity.setExternoRef(dto.externoRef());
    }
}
