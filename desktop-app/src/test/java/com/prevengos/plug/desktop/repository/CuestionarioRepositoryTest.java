package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CuestionarioRepositoryTest {

    @TempDir
    Path tempDir;

    private CuestionarioRepository cuestionarioRepository;
    private PacienteRepository pacienteRepository;
    private UUID pacienteId;

    @BeforeEach
    void setUp() throws IOException {
        Path dbPath = tempDir.resolve("test.db");
        DatabaseManager manager = new DatabaseManager(dbPath);
        manager.initialize();
        pacienteRepository = new PacienteRepository(manager);
        cuestionarioRepository = new CuestionarioRepository(manager);
        Paciente paciente = pacienteRepository.create("12345678A", "Ana", "López", null, null, null, null, null, null, null);
        pacienteId = paciente.pacienteId();
    }

    @Test
    void createAndListCuestionarios() {
        Cuestionario cuestionario = cuestionarioRepository.create(pacienteId, "PLANTILLA-1", "BORRADOR", "{}", null, null, OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC));
        List<Cuestionario> cuestionarios = cuestionarioRepository.findByPaciente(pacienteId);
        assertEquals(1, cuestionarios.size());
        assertEquals("PLANTILLA-1", cuestionarios.get(0).plantillaCodigo());
        assertTrue(cuestionarios.get(0).dirty());
    }

    @Test
    void markAsCleanClearsDirtyFlag() {
        Cuestionario cuestionario = cuestionarioRepository.create(pacienteId, "PLANTILLA-1", "BORRADOR", null, null, null, OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC));
        cuestionarioRepository.markAsClean(cuestionario.cuestionarioId(), 20L);
        Cuestionario updated = cuestionarioRepository.findById(cuestionario.cuestionarioId());
        assertNotNull(updated);
        assertFalse(updated.dirty());
        assertEquals(20L, updated.syncToken());
    }
}
