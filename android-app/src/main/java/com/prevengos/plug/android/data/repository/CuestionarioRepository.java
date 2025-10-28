package com.prevengos.plug.android.data.repository;

import androidx.lifecycle.LiveData;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CuestionarioRepository {
    private final CuestionarioDao cuestionarioDao;

    public CuestionarioRepository(CuestionarioDao cuestionarioDao) {
        this.cuestionarioDao = cuestionarioDao;
    }

    public LiveData<List<CuestionarioEntity>> observeForPaciente(String pacienteId) {
        return cuestionarioDao.observeByPaciente(pacienteId);
    }

    public CuestionarioEntity findById(String cuestionarioId) {
        return cuestionarioDao.findById(cuestionarioId);
    }

    public CuestionarioEntity createCuestionario(String pacienteId,
                                                 String plantillaCodigo,
                                                 String estado,
                                                 List<RespuestaLocal> respuestas) {
        long now = System.currentTimeMillis();
        CuestionarioEntity entity = new CuestionarioEntity(
                UUID.randomUUID().toString(),
                pacienteId,
                plantillaCodigo,
                estado,
                respuestas,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                now,
                null,
                true);
        cuestionarioDao.upsert(entity);
        return entity;
    }

    public CuestionarioEntity updateCuestionario(String cuestionarioId,
                                                 String estado,
                                                 List<RespuestaLocal> respuestas) {
        CuestionarioEntity existing = cuestionarioDao.findById(cuestionarioId);
        if (existing == null) {
            throw new IllegalArgumentException("Cuestionario no encontrado");
        }
        long now = System.currentTimeMillis();
        CuestionarioEntity actualizado = new CuestionarioEntity(
                existing.getCuestionarioId(),
                existing.getPacienteId(),
                existing.getPlantillaCodigo(),
                estado,
                respuestas,
                existing.getFirmas(),
                existing.getAdjuntos(),
                existing.getCreatedAt(),
                existing.getUpdatedAt(),
                now,
                existing.getSyncToken(),
                true);
        cuestionarioDao.upsert(actualizado);
        return actualizado;
    }
}
