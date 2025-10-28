package com.prevengos.plug.android.data.mappers;

import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.room.JsonConverters;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class EntityMappers {

    private static final JsonConverters CONVERTERS = new JsonConverters();

    private EntityMappers() {
    }

    public static PacienteDto toSyncPaciente(PacienteEntity entity) {
        return new PacienteDto(
                UUID(entity.getPacienteId()),
                entity.getNif(),
                entity.getNombre(),
                entity.getApellidos(),
                parseDate(entity.getFechaNacimiento()),
                entity.getSexo(),
                entity.getTelefono(),
                entity.getEmail(),
                UUID(entity.getEmpresaId()),
                UUID(entity.getCentroId()),
                entity.getExternoRef(),
                parseDateTime(entity.getCreatedAt()),
                parseDateTime(entity.getUpdatedAt()),
                toOffsetDateTime(entity.getLastModified()),
                parseLong(entity.getSyncToken())
        );
    }

    public static PacienteEntity toEntity(PacienteDto dto, boolean isDirty) {
        return new PacienteEntity(
                dto.pacienteId() != null ? dto.pacienteId().toString() : null,
                dto.nif(),
                dto.nombre(),
                dto.apellidos(),
                formatDate(dto.fechaNacimiento()),
                dto.sexo(),
                dto.telefono(),
                dto.email(),
                dto.empresaId() != null ? dto.empresaId().toString() : null,
                dto.centroId() != null ? dto.centroId().toString() : null,
                dto.externoRef(),
                formatDateTime(dto.createdAt()),
                formatDateTime(dto.updatedAt()),
                dto.lastModified() != null ? dto.lastModified().toInstant().toEpochMilli() : System.currentTimeMillis(),
                dto.syncToken() != null ? dto.syncToken().toString() : null,
                isDirty
        );
    }

    public static CuestionarioDto toSyncCuestionario(CuestionarioEntity entity) {
        return new CuestionarioDto(
                UUID(entity.getCuestionarioId()),
                UUID(entity.getPacienteId()),
                entity.getPlantillaCodigo(),
                entity.getEstado(),
                CONVERTERS.fromRespuestas(entity.getRespuestas()),
                CONVERTERS.fromStringList(entity.getFirmas()),
                CONVERTERS.fromStringList(entity.getAdjuntos()),
                parseDateTime(entity.getCreatedAt()),
                parseDateTime(entity.getUpdatedAt()),
                toOffsetDateTime(entity.getLastModified()),
                parseLong(entity.getSyncToken())
        );
    }

    public static CuestionarioEntity toEntity(CuestionarioDto dto, boolean isDirty) {
        return new CuestionarioEntity(
                dto.cuestionarioId() != null ? dto.cuestionarioId().toString() : null,
                dto.pacienteId() != null ? dto.pacienteId().toString() : null,
                dto.plantillaCodigo(),
                dto.estado(),
                CONVERTERS.toRespuestas(dto.respuestas()),
                CONVERTERS.toStringList(dto.firmas()),
                CONVERTERS.toStringList(dto.adjuntos()),
                formatDateTime(dto.createdAt()),
                formatDateTime(dto.updatedAt()),
                dto.lastModified() != null ? dto.lastModified().toInstant().toEpochMilli() : System.currentTimeMillis(),
                dto.syncToken() != null ? dto.syncToken().toString() : null,
                isDirty
        );
    }

    private static java.util.UUID UUID(String value) {
        return value == null || value.isBlank() ? null : java.util.UUID.fromString(value);
    }

    private static OffsetDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? null : OffsetDateTime.parse(value);
    }

    private static String formatDateTime(OffsetDateTime value) {
        return value == null ? null : value.toString();
    }

    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private static String formatDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static OffsetDateTime toOffsetDateTime(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
