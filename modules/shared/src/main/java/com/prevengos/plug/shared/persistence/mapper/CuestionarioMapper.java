package com.prevengos.plug.shared.persistence.mapper;

import com.prevengos.plug.shared.dto.CuestionarioDto;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioRecord;
import com.prevengos.plug.shared.persistence.jpa.CuestionarioEntity;
import com.prevengos.plug.shared.persistence.jpa.PacienteEntity;

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
                MapUtils.readJson(entity.getRespuestas()),
                MapUtils.readList(entity.getFirmas()),
                MapUtils.readList(entity.getAdjuntos()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CuestionarioEntity toEntity(CuestionarioDto dto,
                                              PacienteEntity paciente,
                                              OffsetDateTime createdAt,
                                              OffsetDateTime updatedAt,
                                              OffsetDateTime lastModified,
                                              long syncToken) {
        return new CuestionarioEntity(
                dto.cuestionarioId(),
                paciente,
                dto.plantillaCodigo(),
                dto.estado(),
                MapUtils.writeJson(dto.respuestas()),
                MapUtils.writeList(dto.firmas()),
                MapUtils.writeList(dto.adjuntos()),
                createdAt,
                updatedAt,
                lastModified,
                syncToken
        );
    }

    public static CuestionarioRecord toRecord(CuestionarioDto dto,
                                              OffsetDateTime createdAt,
                                              OffsetDateTime updatedAt,
                                              OffsetDateTime lastModified,
                                              long syncToken) {
        return new CuestionarioRecord(
                dto.cuestionarioId(),
                dto.pacienteId(),
                dto.plantillaCodigo(),
                dto.estado(),
                MapUtils.writeJson(dto.respuestas()),
                MapUtils.writeList(dto.firmas()),
                MapUtils.writeList(dto.adjuntos()),
                createdAt,
                updatedAt,
                lastModified,
                syncToken
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
