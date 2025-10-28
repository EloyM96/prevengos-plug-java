package com.prevengos.plug.android.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class PacienteRepositoryTest {
    private PacienteDao pacienteDao;
    private PacienteRepository repository;

    @Before
    public void setUp() {
        pacienteDao = Mockito.mock(PacienteDao.class);
        repository = new PacienteRepository(pacienteDao);
    }

    @Test
    public void createPaciente_guardaEntidadSucede() {
        PacienteEntity entity = repository.createPaciente("12345678A", "Ana", "García", null, "ana@example.com");

        ArgumentCaptor<PacienteEntity> captor = ArgumentCaptor.forClass(PacienteEntity.class);
        verify(pacienteDao).upsert(captor.capture());
        PacienteEntity persisted = captor.getValue();

        assertEquals("12345678A", persisted.getNif());
        assertEquals("Ana", persisted.getNombre());
        assertEquals("García", persisted.getApellidos());
        assertTrue(persisted.isDirty());
        assertNotNull(persisted.getPacienteId());
        assertEquals(entity.getPacienteId(), persisted.getPacienteId());
    }

    @Test
    public void updatePaciente_reutilizaIdentificador() {
        PacienteEntity existente = new PacienteEntity(
                "pac-1",
                "12345678A",
                "Ana",
                "García",
                null,
                null,
                "600111222",
                "ana@example.com",
                null,
                null,
                null,
                null,
                null,
                123L,
                "token-1",
                false);
        when(pacienteDao.findById("pac-1")).thenReturn(existente);

        repository.updatePaciente("pac-1", "87654321B", "Ana", "García Pérez", "600333444", null);

        ArgumentCaptor<PacienteEntity> captor = ArgumentCaptor.forClass(PacienteEntity.class);
        verify(pacienteDao).upsert(captor.capture());
        PacienteEntity actualizado = captor.getValue();
        assertEquals("pac-1", actualizado.getPacienteId());
        assertEquals("87654321B", actualizado.getNif());
        assertEquals("Ana", actualizado.getNombre());
        assertEquals("García Pérez", actualizado.getApellidos());
        assertEquals("600333444", actualizado.getTelefono());
        assertTrue(actualizado.isDirty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updatePaciente_lanzaExcepcionSiNoExiste() {
        when(pacienteDao.findById(any())).thenReturn(null);
        repository.updatePaciente("desconocido", "NIF", "Nombre", "Apellido", null, null);
    }
}
