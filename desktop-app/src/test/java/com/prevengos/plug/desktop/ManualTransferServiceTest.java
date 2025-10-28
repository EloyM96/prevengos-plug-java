package com.prevengos.plug.desktop;

import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;
import com.prevengos.plug.desktop.repository.DatabaseManager;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PatientRepository;
import com.prevengos.plug.desktop.repository.QuestionnaireRepository;
import com.prevengos.plug.desktop.repository.SyncEventRepository;
import com.prevengos.plug.desktop.service.LocalStorageService;
import com.prevengos.plug.desktop.service.ManualTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ManualTransferServiceTest {

    @TempDir
    Path tempDir;

    private ManualTransferService manualTransferService;
    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        DatabaseManager databaseManager = new DatabaseManager(tempDir.resolve("transfer.db"));
        localStorageService = new LocalStorageService(
                new PatientRepository(databaseManager),
                new QuestionnaireRepository(databaseManager),
                new MetadataRepository(databaseManager),
                new SyncEventRepository(databaseManager)
        );
        manualTransferService = new ManualTransferService(localStorageService, AppConfig.load().objectMapper());

        localStorageService.savePatient(new Patient(UUID.randomUUID(), "Ana", "Ruiz", null, null, Instant.now(), Instant.now()));
        localStorageService.saveQuestionnaire(new Questionnaire(UUID.randomUUID(),
                localStorageService.listPatients().get(0).getId(), "Test", "{}", Instant.now(), Instant.now()));
    }

    @Test
    void exportsAndImportsData() throws IOException {
        Path exportPath = tempDir.resolve("data.json");
        manualTransferService.exportAll(exportPath);
        assertTrue(Files.exists(exportPath));

        // Clean DB and import again
        localStorageService.listPatients().forEach(patient -> localStorageService.deletePatient(patient.getId()));
        localStorageService.listQuestionnaires().forEach(q -> localStorageService.deleteQuestionnaire(q.getId()));

        manualTransferService.importAll(exportPath);
        assertTrue(localStorageService.listPatients().size() >= 1);
        assertTrue(localStorageService.listQuestionnaires().size() >= 1);
    }
}
