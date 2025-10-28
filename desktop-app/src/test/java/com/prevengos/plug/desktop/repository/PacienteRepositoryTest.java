package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacienteRepositoryTest {

    @TempDir
    Path tempDir;

    private PacienteRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        Path dbPath = tempDir.resolve("test.db");
        DatabaseManager manager = new DatabaseManager(dbPath);
        manager.initialize();
        repository = new PacienteRepository(manager);
    }

    @Test
    void createAndRetrievePaciente() {
        Paciente paciente = repository.create("12345678A", "Ana", "López", LocalDate.of(1990, 1, 1), "F", "600000000", "ana@example.com", null, null, null);
        assertNotNull(paciente);
        List<Paciente> pacientes = repository.findAll();
        assertEquals(1, pacientes.size());
        assertEquals("Ana López", pacientes.get(0).nombreCompleto());
        assertTrue(pacientes.get(0).dirty());
    }

    @Test
    void updatePacienteMarksDirty() {
        Paciente paciente = repository.create("12345678A", "Ana", "López", null, null, null, null, null, null, null);
        Paciente actualizado = repository.update(paciente.pacienteId(), "12345678A", "Ana", "García", null, null, null, null, null, null, null);
        assertEquals("Ana García", actualizado.nombreCompleto());
        assertTrue(actualizado.dirty());
        Paciente loaded = repository.findById(paciente.pacienteId());
        assertEquals("Ana García", loaded.nombreCompleto());
    }

    @Test
    void markAsCleanUpdatesDirtyFlag() {
        Paciente paciente = repository.create("12345678A", "Ana", "López", null, null, null, null, null, null, null);
        repository.markAsClean(paciente.pacienteId(), 10L);
        Paciente loaded = repository.findById(paciente.pacienteId());
        assertFalse(loaded.dirty());
        assertEquals(10L, loaded.syncToken());
    }
}
