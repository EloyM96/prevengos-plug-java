package com.prevengos.plug.shared.persistence.mapper;

import com.prevengos.plug.shared.persistence.jdbc.CuestionarioRecord;
import com.prevengos.plug.shared.persistence.jpa.CuestionarioEntity;
import com.prevengos.plug.shared.persistence.jpa.PacienteEntity;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;

import java.time.OffsetDateTime;

public final class CuestionarioMapper {

    private CuestionarioMapper() {
    }

    public static CuestionarioDto toDto(CuestionarioEntity entity) {
        return new CuestionarioDto(
                entity.getCuestionarioId(),
                entity.getPaciente() != null ? entity.getPaciente().getPacienteId() : null,
                entity.getPlantillaCodigo(),
                entity.getEstado(),
                entity.getRespuestas(),
                entity.getFirmas(),
                entity.getAdjuntos(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastModified(),
                entity.getSyncToken()
        );
    }

    public static CuestionarioEntity toEntity(CuestionarioDto dto,
                                              PacienteEntity paciente,
                                              OffsetDateTime createdAt,
                                              OffsetDateTime updatedAt,
                                              OffsetDateTime lastModified,
                                              long syncToken) {
        OffsetDateTime effectiveCreatedAt = createdAt != null ? createdAt : dto.createdAt();
        OffsetDateTime effectiveUpdatedAt = updatedAt != null ? updatedAt : dto.updatedAt();
        OffsetDateTime effectiveLastModified = lastModified != null ? lastModified : dto.lastModified();
        long effectiveSyncToken = dto.syncToken() != null ? dto.syncToken() : syncToken;
        return new CuestionarioEntity(
                dto.cuestionarioId(),
                paciente,
                dto.plantillaCodigo(),
                dto.estado(),
                dto.respuestas(),
                dto.firmas(),
                dto.adjuntos(),
                effectiveCreatedAt,
                effectiveUpdatedAt,
                effectiveLastModified != null ? effectiveLastModified : OffsetDateTime.now(),
                effectiveSyncToken
        );
    }

    public static CuestionarioRecord toRecord(CuestionarioDto dto,
                                              OffsetDateTime createdAt,
                                              OffsetDateTime updatedAt,
                                              OffsetDateTime lastModified,
                                              long syncToken) {
        OffsetDateTime effectiveCreatedAt = createdAt != null ? createdAt : dto.createdAt();
        OffsetDateTime effectiveUpdatedAt = updatedAt != null ? updatedAt : dto.updatedAt();
        OffsetDateTime effectiveLastModified = lastModified != null ? lastModified : dto.lastModified();
        long effectiveSyncToken = dto.syncToken() != null ? dto.syncToken() : syncToken;
        return new CuestionarioRecord(
                dto.cuestionarioId(),
                dto.pacienteId(),
                dto.plantillaCodigo(),
                dto.estado(),
                dto.respuestas(),
                dto.firmas(),
                dto.adjuntos(),
                effectiveCreatedAt,
                effectiveUpdatedAt,
                effectiveLastModified,
                effectiveSyncToken
        );
    }

    public static CuestionarioRecord toRecord(CuestionarioEntity entity) {
        return new CuestionarioRecord(
                entity.getCuestionarioId(),
                entity.getPaciente() != null ? entity.getPaciente().getPacienteId() : null,
                entity.getPlantillaCodigo(),
                entity.getEstado(),
                entity.getRespuestas(),
                entity.getFirmas(),
                entity.getAdjuntos(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastModified(),
                entity.getSyncToken()
        );
    }
}
