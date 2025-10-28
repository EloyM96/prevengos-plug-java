package com.prevengos.plug.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permite exportar e importar datos manualmente en formato JSON.
 */
public class ManualTransferService {

    private final LocalStorageService localStorageService;
    private final ObjectMapper objectMapper;

    public ManualTransferService(LocalStorageService localStorageService, ObjectMapper objectMapper) {
        this.localStorageService = localStorageService;
        this.objectMapper = objectMapper;
    }

    public void exportAll(Path output) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("exportedAt", Instant.now().toString());
        payload.put("patients", localStorageService.listPatients());
        payload.put("questionnaires", localStorageService.listQuestionnaires());
        try (OutputStream out = Files.newOutputStream(output)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, payload);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo exportar la información", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void importAll(Path input) {
        try {
            Map<String, Object> payload = objectMapper.readValue(Files.readString(input), Map.class);
            List<Map<String, Object>> patients = (List<Map<String, Object>>) payload.getOrDefault("patients", List.of());
            for (Map<String, Object> patientData : patients) {
                Patient patient = objectMapper.convertValue(patientData, Patient.class);
                localStorageService.savePatient(patient);
            }
            List<Map<String, Object>> questionnaires = (List<Map<String, Object>>) payload.getOrDefault("questionnaires", List.of());
            for (Map<String, Object> questionnaireData : questionnaires) {
                Questionnaire questionnaire = objectMapper.convertValue(questionnaireData, Questionnaire.class);
                localStorageService.saveQuestionnaire(questionnaire);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo importar la información", e);
        }
    }
}
