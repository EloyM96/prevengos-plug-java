package com.prevengos.plug.desktop;

import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.repository.DatabaseManager;
import com.prevengos.plug.desktop.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientRepositoryTest {

    private PatientRepository repository;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        DatabaseManager databaseManager = new DatabaseManager(tempDir.resolve("patients.db"));
        repository = new PatientRepository(databaseManager);
    }

    @Test
    void insertsAndReadsPatients() {
        Patient patient = new Patient(UUID.randomUUID(), "Ana", "Ramírez", "1234", LocalDate.of(1990, 1, 1), Instant.now(), Instant.now());
        repository.upsert(patient);

        List<Patient> patients = repository.findAll();
        assertEquals(1, patients.size());
        assertEquals("Ana", patients.get(0).getFirstName());
    }

    @Test
    void findsByUpdatedTimestamp() {
        Instant olderTime = Instant.parse("2024-01-01T00:00:00Z");
        Instant cutoff = Instant.parse("2024-02-01T00:00:00Z");
        Instant newerTime = Instant.parse("2024-03-01T00:00:00Z");

        Patient older = new Patient(UUID.randomUUID(), "Luis", "García", null, null, olderTime, olderTime);
        repository.upsert(older);

        Patient newer = new Patient(UUID.randomUUID(), "Marta", "Pérez", null, null, newerTime, newerTime);
        repository.upsert(newer);

        List<Patient> updated = repository.findUpdatedSince(cutoff);
        assertEquals(1, updated.size());
        assertEquals(newer.getId(), updated.get(0).getId());
    }

    @Test
    void deletesPatient() {
        Patient patient = new Patient(UUID.randomUUID(), "Ana", "Ramírez", "1234", LocalDate.now(), Instant.now(), Instant.now());
        repository.upsert(patient);

        repository.delete(patient.getId());
        assertTrue(repository.findAll().isEmpty());
    }
}
