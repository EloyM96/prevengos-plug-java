package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.domain.Cuestionario;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.CuestionarioDto;
import com.prevengos.plug.hubbackend.repository.CuestionarioRepository;
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
public class CuestionarioService {

    private final CuestionarioRepository cuestionarioRepository;
    private final SyncEventService syncEventService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(CuestionarioService.class);

    public CuestionarioService(CuestionarioRepository cuestionarioRepository,
                               SyncEventService syncEventService,
                               ObjectMapper objectMapper,
                               MeterRegistry meterRegistry) {
        this.cuestionarioRepository = cuestionarioRepository;
        this.syncEventService = syncEventService;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public BatchSyncResponse upsertCuestionarios(List<CuestionarioDto> cuestionarios, String source) {
        List<UUID> identifiers = new ArrayList<>();
        String resolvedSource = resolveSource(source);
        meterRegistry.summary("hub.sync.cuestionarios.batch.size",
                "source", resolvedSource).record(cuestionarios.size());
        logger.info("Procesando lote de cuestionarios",
                StructuredArguments.kv("batchSize", cuestionarios.size()),
                StructuredArguments.kv("source", resolvedSource));
        for (CuestionarioDto dto : cuestionarios) {
            Cuestionario entity = cuestionarioRepository.findById(dto.cuestionarioId())
                    .orElseGet(() -> new Cuestionario(dto.cuestionarioId()));
            boolean isNew = entity.getCreatedAt() == null;
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime resolvedCreatedAt = dto.createdAt() != null ? dto.createdAt() : now;
            OffsetDateTime resolvedUpdatedAt = dto.updatedAt() != null ? dto.updatedAt() : now;
            mapDtoToEntity(dto, entity);
            if (isNew) {
                entity.setCreatedAt(resolvedCreatedAt);
            }
            entity.setUpdatedAt(resolvedUpdatedAt);
            entity.setLastModified(resolvedUpdatedAt);
            cuestionarioRepository.save(entity);

            var event = syncEventService.registerEvent("cuestionario-upserted", dto, source, null, null, null);
            resolvedSource = event.getSource();
            long syncToken = event.getSyncToken();
            entity.setSyncToken(syncToken);
            entity.setLastModified(OffsetDateTime.now(ZoneOffset.UTC));
            cuestionarioRepository.save(entity);
            identifiers.add(entity.getCuestionarioId());
            meterRegistry.counter("hub.sync.cuestionarios.processed",
                    "source", resolvedSource)
                    .increment();
            logger.info("Cuestionario sincronizado",
                    StructuredArguments.kv("cuestionarioId", entity.getCuestionarioId()),
                    StructuredArguments.kv("source", resolvedSource),
                    StructuredArguments.kv("syncToken", syncToken),
                    StructuredArguments.kv("isNew", isNew));
        }
        return new BatchSyncResponse(identifiers.size(), identifiers);
    }

    private String resolveSource(String source) {
        return source != null && !source.isBlank() ? source : SyncEventService.DEFAULT_SOURCE;
    }

    private void mapDtoToEntity(CuestionarioDto dto, Cuestionario entity) {
        entity.setPacienteId(dto.pacienteId());
        entity.setPlantillaCodigo(dto.plantillaCodigo());
        entity.setEstado(dto.estado() != null ? dto.estado() : "borrador");
        entity.setRespuestas(writeJson(dto.respuestas()));
        entity.setFirmas(writeJson(dto.firmas()));
        entity.setAdjuntos(writeJson(dto.adjuntos()));
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize cuestionario field", e);
        }
    }
}
