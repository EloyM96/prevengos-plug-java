package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.domain.Cuestionario;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.CuestionarioDto;
import com.prevengos.plug.hubbackend.repository.CuestionarioRepository;
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

    public CuestionarioService(CuestionarioRepository cuestionarioRepository,
                               SyncEventService syncEventService,
                               ObjectMapper objectMapper) {
        this.cuestionarioRepository = cuestionarioRepository;
        this.syncEventService = syncEventService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BatchSyncResponse upsertCuestionarios(List<CuestionarioDto> cuestionarios, String source) {
        List<UUID> identifiers = new ArrayList<>();
        for (CuestionarioDto dto : cuestionarios) {
            Cuestionario entity = cuestionarioRepository.findById(dto.cuestionarioId())
                    .orElseGet(() -> new Cuestionario(dto.cuestionarioId()));
            boolean isNew = entity.getCreatedAt() == null;
            mapDtoToEntity(dto, entity);
            if (isNew) {
                entity.setCreatedAt(dto.createdAt() != null ? dto.createdAt() : OffsetDateTime.now(ZoneOffset.UTC));
            }
            entity.setUpdatedAt(dto.updatedAt() != null ? dto.updatedAt() : entity.getUpdatedAt());
            entity.setLastModified(dto.updatedAt() != null ? dto.updatedAt() : OffsetDateTime.now(ZoneOffset.UTC));
            cuestionarioRepository.save(entity);

            long syncToken = syncEventService
                    .registerEvent("cuestionario-upserted", dto, source, null, null, null)
                    .getSyncToken();
            entity.setSyncToken(syncToken);
            entity.setLastModified(OffsetDateTime.now(ZoneOffset.UTC));
            cuestionarioRepository.save(entity);
            identifiers.add(entity.getCuestionarioId());
        }
        return new BatchSyncResponse(identifiers.size(), identifiers);
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
