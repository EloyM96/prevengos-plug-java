package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.gateway.sqlserver.SyncEventGateway;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncEventDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SynchronizationService {

    private static final Logger log = LoggerFactory.getLogger(SynchronizationService.class);

    private final PacienteGateway pacienteGateway;
    private final CuestionarioGateway cuestionarioGateway;
    private final SyncEventGateway syncEventGateway;
    private final ObjectMapper objectMapper;

    public SynchronizationService(PacienteGateway pacienteGateway,
                                  CuestionarioGateway cuestionarioGateway,
                                  SyncEventGateway syncEventGateway,
                                  ObjectMapper objectMapper) {
        this.pacienteGateway = pacienteGateway;
        this.cuestionarioGateway = cuestionarioGateway;
        this.syncEventGateway = syncEventGateway;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SyncPushResponse push(SyncPushRequest request) {
        long lastToken = 0L;
        List<UUID> processed = new ArrayList<>();
        for (PacienteDto paciente : request.pacientes()) {
            long token = registerEvent("paciente-upserted", paciente, request);
            pacienteGateway.upsert(paciente, paciente.lastModified(), token);
            processed.add(paciente.pacienteId());
            lastToken = Math.max(lastToken, token);
        }
        for (CuestionarioDto cuestionario : request.cuestionarios()) {
            long token = registerEvent("cuestionario-upserted", cuestionario, request);
            cuestionarioGateway.upsert(cuestionario, cuestionario.lastModified(), token);
            processed.add(cuestionario.cuestionarioId());
            lastToken = Math.max(lastToken, token);
        }
        return new SyncPushResponse(request.pacientes().size(), request.cuestionarios().size(), lastToken, processed);
    }

    @Transactional(readOnly = true)
    public SyncPullResponse pull(long afterToken, int limit) {
        List<PacienteDto> pacientes = pacienteGateway.fetchAfterToken(afterToken, limit);
        List<CuestionarioDto> cuestionarios = cuestionarioGateway.fetchAfterToken(afterToken, limit);
        List<SyncEventDto> events = syncEventGateway.fetchAfter(afterToken, limit);
        long nextToken = afterToken;
        for (SyncEventDto event : events) {
            if (event.syncToken() > nextToken) {
                nextToken = event.syncToken();
            }
        }
        for (PacienteDto paciente : pacientes) {
            if (paciente.syncToken() != null && paciente.syncToken() > nextToken) {
                nextToken = paciente.syncToken();
            }
        }
        for (CuestionarioDto cuestionario : cuestionarios) {
            if (cuestionario.syncToken() != null && cuestionario.syncToken() > nextToken) {
                nextToken = cuestionario.syncToken();
            }
        }
        return new SyncPullResponse(pacientes, cuestionarios, events, nextToken);
    }

    private long registerEvent(String type, Object payload, SyncPushRequest request) {
        try {
            UUID eventId = UUID.randomUUID();
            String serializedPayload = objectMapper.writeValueAsString(payload);
            String metadata = objectMapper.writeValueAsString(new SyncMetadata(request.source(), request.correlationId()));
            return syncEventGateway.registerEvent(eventId, type, 1, OffsetDateTime.now(),
                    request.source() == null ? "unknown" : request.source(),
                    request.correlationId(), null, serializedPayload, metadata);
        } catch (JsonProcessingException e) {
            log.error("Error serializando evento de sincronización", e);
            throw new IllegalStateException("No se pudo serializar el payload de sincronización", e);
        }
    }

    private record SyncMetadata(String source, UUID correlationId) {
    }
}
