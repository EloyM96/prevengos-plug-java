package com.prevengos.plug.android.data.mappers;

import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;
import com.prevengos.plug.android.data.remote.model.CuestionarioPayload;
import com.prevengos.plug.android.data.remote.model.PacientePayload;
import com.prevengos.plug.android.data.remote.model.RespuestaPayload;

import java.util.ArrayList;
import java.util.List;

public final class EntityMappers {
    private EntityMappers() {
    }

    public static PacientePayload toPayload(PacienteEntity entity) {
        return new PacientePayload(
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
                entity.getSyncToken());
    }

    public static PacienteEntity toEntity(PacientePayload payload, boolean isDirty) {
        return new PacienteEntity(
                payload.getPacienteId(),
                payload.getNif(),
                payload.getNombre(),
                payload.getApellidos(),
                payload.getFechaNacimiento(),
                payload.getSexo(),
                payload.getTelefono(),
                payload.getEmail(),
                payload.getEmpresaId(),
                payload.getCentroId(),
                payload.getExternoRef(),
                payload.getCreatedAt(),
                payload.getUpdatedAt(),
                payload.getLastModified(),
                payload.getSyncToken(),
                isDirty);
    }

    public static CuestionarioPayload toPayload(CuestionarioEntity entity) {
        List<RespuestaPayload> respuestas = new ArrayList<>();
        for (RespuestaLocal local : entity.getRespuestas()) {
            respuestas.add(toPayload(local));
        }
        return new CuestionarioPayload(
                entity.getCuestionarioId(),
                entity.getPacienteId(),
                entity.getPlantillaCodigo(),
                entity.getEstado(),
                respuestas,
                entity.getFirmas(),
                entity.getAdjuntos(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastModified(),
                entity.getSyncToken());
    }

    public static CuestionarioEntity toEntity(CuestionarioPayload payload, boolean isDirty) {
        List<RespuestaLocal> respuestas = new ArrayList<>();
        for (RespuestaPayload remote : payload.getRespuestas()) {
            respuestas.add(toLocal(remote));
        }
        return new CuestionarioEntity(
                payload.getCuestionarioId(),
                payload.getPacienteId(),
                payload.getPlantillaCodigo(),
                payload.getEstado(),
                respuestas,
                payload.getFirmas(),
                payload.getAdjuntos(),
                payload.getCreatedAt(),
                payload.getUpdatedAt(),
                payload.getLastModified(),
                payload.getSyncToken(),
                isDirty);
    }

    public static RespuestaPayload toPayload(RespuestaLocal local) {
        return new RespuestaPayload(
                local.getPreguntaCodigo(),
                local.getValor(),
                local.getUnidad(),
                local.getMetadata());
    }

    public static RespuestaLocal toLocal(RespuestaPayload payload) {
        return new RespuestaLocal(
                payload.getPreguntaCodigo(),
                payload.getValor(),
                payload.getUnidad(),
                payload.getMetadata());
    }
}
