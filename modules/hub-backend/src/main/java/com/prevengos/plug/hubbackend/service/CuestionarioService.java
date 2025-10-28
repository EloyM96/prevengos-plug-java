package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.CuestionarioRecord;
import com.prevengos.plug.gateway.sqlserver.SyncEventRecord;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.CuestionarioDto;
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

    private final CuestionarioGateway cuestionarioGateway;
    private final SyncEventService syncEventService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(CuestionarioService.class);

    public CuestionarioService(CuestionarioGateway cuestionarioGateway,
                               SyncEventService syncEventService,
                               ObjectMapper objectMapper,
                               MeterRegistry meterRegistry) {
        this.cuestionarioGateway = cuestionarioGateway;
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
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime createdAt = dto.createdAt() != null ? dto.createdAt() : now;
            OffsetDateTime updatedAt = dto.updatedAt() != null ? dto.updatedAt() : now;

            SyncEventRecord event = syncEventService.registerEvent("cuestionario-upserted", dto, resolvedSource,
                    null, null, null);
            resolvedSource = event.source();

            CuestionarioRecord record = new CuestionarioRecord(
                    dto.cuestionarioId(),
                    dto.pacienteId(),
                    dto.plantillaCodigo(),
                    dto.estado() != null ? dto.estado() : "borrador",
                    writeJson(dto.respuestas()),
                    writeJson(dto.firmas()),
                    writeJson(dto.adjuntos()),
                    createdAt,
                    updatedAt,
                    now,
                    event.syncToken() != null ? event.syncToken() : 0L
            );
            cuestionarioGateway.upsertCuestionario(record);
            identifiers.add(record.cuestionarioId());

            meterRegistry.counter("hub.sync.cuestionarios.processed",
                    "source", resolvedSource)
                    .increment();
            logger.info("Cuestionario sincronizado",
                    StructuredArguments.kv("cuestionarioId", record.cuestionarioId()),
                    StructuredArguments.kv("source", resolvedSource),
                    StructuredArguments.kv("syncToken", record.syncToken()));
        }
        return new BatchSyncResponse(identifiers.size(), identifiers);
    }

    private String resolveSource(String source) {
        return source != null && !source.isBlank() ? source : SyncEventService.DEFAULT_SOURCE;
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
