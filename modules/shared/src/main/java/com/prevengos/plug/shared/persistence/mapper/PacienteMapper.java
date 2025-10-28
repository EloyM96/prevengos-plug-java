package com.prevengos.plug.shared.persistence.mapper;

import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;
import com.prevengos.plug.shared.persistence.jpa.PacienteEntity;
import com.prevengos.plug.shared.sync.dto.PacienteDto;

import java.time.OffsetDateTime;

/**
 * Mapper utilities centralizing conversions between DTOs, JDBC records and JPA entities
 * for {@link PacienteEntity}.
 */
public final class PacienteMapper {

    private PacienteMapper() {
    }

    public static PacienteDto toDto(PacienteEntity entity) {
        return new PacienteDto(
                entity.getPacienteId(),
                entity.getNif(),
                entity.getNombre(),
                entity.getApellidos(),
                entity.getFechaNacimiento(),
                entity.getSexo(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getEmpresaId(),
                entity.getCentroId(),
                entity.getExternoRef(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastModified(),
                entity.getSyncToken()
        );
    }

    public static PacienteEntity toEntity(PacienteDto dto,
                                          OffsetDateTime createdAt,
                                          OffsetDateTime updatedAt,
                                          OffsetDateTime lastModified,
                                          long syncToken) {
        OffsetDateTime effectiveCreatedAt = createdAt != null ? createdAt : dto.createdAt();
        OffsetDateTime effectiveUpdatedAt = updatedAt != null ? updatedAt : dto.updatedAt();
        OffsetDateTime effectiveLastModified = lastModified != null ? lastModified : dto.lastModified();
        long effectiveSyncToken = dto.syncToken() != null ? dto.syncToken() : syncToken;
        return new PacienteEntity(
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
                effectiveCreatedAt,
                effectiveUpdatedAt,
                effectiveLastModified != null ? effectiveLastModified : OffsetDateTime.now(),
                effectiveSyncToken
        );
    }

    public static void copyToEntity(PacienteDto dto,
                                    PacienteEntity entity,
                                    OffsetDateTime createdAt,
                                    OffsetDateTime updatedAt,
                                    OffsetDateTime lastModified,
                                    long syncToken) {
        entity.setPacienteId(dto.pacienteId());
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
        entity.setCreatedAt(createdAt != null ? createdAt : dto.createdAt());
        entity.setUpdatedAt(updatedAt != null ? updatedAt : dto.updatedAt());
        entity.setLastModified(lastModified != null ? lastModified : dto.lastModified());
        entity.setSyncToken(dto.syncToken() != null ? dto.syncToken() : syncToken);
    }

    public static PacienteRecord toRecord(PacienteDto dto,
                                          OffsetDateTime createdAt,
                                          OffsetDateTime updatedAt,
                                          OffsetDateTime lastModified,
                                          long syncToken) {
        OffsetDateTime effectiveCreatedAt = createdAt != null ? createdAt : dto.createdAt();
        OffsetDateTime effectiveUpdatedAt = updatedAt != null ? updatedAt : dto.updatedAt();
        OffsetDateTime effectiveLastModified = lastModified != null ? lastModified : dto.lastModified();
        long effectiveSyncToken = dto.syncToken() != null ? dto.syncToken() : syncToken;
        return new PacienteRecord(
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
                effectiveCreatedAt,
                effectiveUpdatedAt,
                effectiveLastModified,
                effectiveSyncToken
        );
    }

    public static PacienteRecord toRecord(PacienteEntity entity) {
        return new PacienteRecord(
                entity.getPacienteId(),
                entity.getNif(),
                entity.getNombre(),
                entity.getApellidos(),
                entity.getFechaNacimiento(),
                entity.getSexo(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getEmpresaId(),
                entity.getCentroId(),
                entity.getExternoRef(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastModified(),
                entity.getSyncToken()
        );
    }
}
