package com.prevengos.plug.android.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;

public class CuestionarioRepositoryTest {
    private CuestionarioDao cuestionarioDao;
    private CuestionarioRepository repository;

    @Before
    public void setUp() {
        cuestionarioDao = Mockito.mock(CuestionarioDao.class);
        repository = new CuestionarioRepository(cuestionarioDao);
    }

    @Test
    public void createCuestionario_guardaBorrador() {
        repository.createCuestionario("pac-1", "anamnesis", "borrador", Collections.emptyList());
        ArgumentCaptor<CuestionarioEntity> captor = ArgumentCaptor.forClass(CuestionarioEntity.class);
        verify(cuestionarioDao).upsert(captor.capture());
        CuestionarioEntity entity = captor.getValue();
        assertEquals("pac-1", entity.getPacienteId());
        assertEquals("anamnesis", entity.getPlantillaCodigo());
        assertEquals("borrador", entity.getEstado());
        assertTrue(entity.isDirty());
    }

    @Test
    public void updateCuestionario_actualizaEstado() {
        CuestionarioEntity existente = new CuestionarioEntity(
                "cues-1",
                "pac-1",
                "anamnesis",
                "borrador",
                Collections.singletonList(new RespuestaLocal("nota", "valor", null, null)),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                1L,
                "token",
                false);
        when(cuestionarioDao.findById("cues-1")).thenReturn(existente);

        repository.updateCuestionario("cues-1", "firmado", Collections.emptyList());

        ArgumentCaptor<CuestionarioEntity> captor = ArgumentCaptor.forClass(CuestionarioEntity.class);
        verify(cuestionarioDao).upsert(captor.capture());
        CuestionarioEntity actualizado = captor.getValue();
        assertEquals("cues-1", actualizado.getCuestionarioId());
        assertEquals("firmado", actualizado.getEstado());
        assertTrue(actualizado.isDirty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateCuestionario_errorSiNoExiste() {
        when(cuestionarioDao.findById(anyString())).thenReturn(null);
        repository.updateCuestionario("nope", "estado", Collections.emptyList());
    }
}
