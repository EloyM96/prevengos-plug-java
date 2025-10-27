package com.prevengos.plug.android.data.repository;

import androidx.lifecycle.LiveData;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;

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

    public CuestionarioEntity createDraft(String pacienteId,
                                          String plantillaCodigo,
                                          List<RespuestaLocal> respuestas) {
        long now = System.currentTimeMillis();
        CuestionarioEntity entity = new CuestionarioEntity(
                UUID.randomUUID().toString(),
                pacienteId,
                plantillaCodigo,
                "borrador",
                respuestas,
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                null,
                null,
                now,
                null,
                true);
        cuestionarioDao.upsert(entity);
        return entity;
    }
}
