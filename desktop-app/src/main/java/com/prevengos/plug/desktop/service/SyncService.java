package com.prevengos.plug.desktop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;
import com.prevengos.plug.desktop.service.dto.PullResponse;
import com.prevengos.plug.desktop.service.dto.SyncBatch;
import com.prevengos.plug.desktop.service.dto.SyncEventPayload;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordina los envíos y pulls con el Hub siguiendo las reglas definidas en los flujos de sincronización.
 */
public class SyncService {

    private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private final LocalStorageService localStorageService;
    private final RemoteSyncGateway remoteSyncGateway;
    private final ObjectMapper objectMapper;
    private final AppConfig appConfig;

    public SyncService(LocalStorageService localStorageService,
                       RemoteSyncGateway remoteSyncGateway,
                       AppConfig appConfig) {
        this.localStorageService = localStorageService;
        this.remoteSyncGateway = remoteSyncGateway;
        this.appConfig = appConfig;
        this.objectMapper = appConfig.objectMapper();
    }

    public SyncSummary pushChanges() {
        Instant lastPatientPush = localStorageService.readLastPatientPush();
        Instant lastQuestionnairePush = localStorageService.readLastQuestionnairePush();

        List<Patient> patients = localStorageService.patientsUpdatedSince(lastPatientPush);
        List<Questionnaire> questionnaires = localStorageService.questionnairesUpdatedSince(lastQuestionnairePush);

        SyncBatch batch = new SyncBatch(patients, questionnaires, appConfig.sourceSystem(), Instant.now());
        if (batch.isEmpty()) {
            return SyncSummary.empty();
        }

        SyncBatch response = remoteSyncGateway.pushBatch(batch);
        Instant now = Instant.now();
        if (!patients.isEmpty()) {
            localStorageService.updateLastPatientPush(now);
        }
        if (!questionnaires.isEmpty()) {
            localStorageService.updateLastQuestionnairePush(now);
        }
        return SyncSummary.from(batch, response);
    }

    public SyncSummary pullUpdates() {
        Optional<String> maybeToken = localStorageService.readSyncToken();
        String token = maybeToken.orElse(null);
        String since = null;
        if (token == null) {
            Instant lastPatientPush = localStorageService.readLastPatientPush();
            Instant lastQuestionnairePush = localStorageService.readLastQuestionnairePush();
            Instant sinceInstant = lastPatientPush.isAfter(lastQuestionnairePush) ? lastPatientPush : lastQuestionnairePush;
            since = ISO8601.format(sinceInstant);
        }

        PullResponse response = remoteSyncGateway.pull(token, since, appConfig.syncBatchSize());
        AtomicInteger patientsApplied = new AtomicInteger();
        AtomicInteger questionnairesApplied = new AtomicInteger();

        for (SyncEventPayload event : response.getEvents()) {
            String entityId = applyEvent(event, patientsApplied, questionnairesApplied);
            localStorageService.getSyncEventRepository().record(
                    event.getEntityType(),
                    entityId,
                    event.getEventType(),
                    event.getPayload(),
                    event.getSyncToken()
            );
        }

        if (response.getNextToken() != null) {
            localStorageService.updateSyncToken(response.getNextToken());
        }

        return new SyncSummary(patientsApplied.get(), questionnairesApplied.get(), response.getEvents().size());
    }

    private String applyEvent(SyncEventPayload event,
                              AtomicInteger patientsApplied,
                              AtomicInteger questionnairesApplied) {
        try {
            switch (event.getEntityType()) {
                case "patient" -> {
                    Patient patient = objectMapper.readValue(event.getPayload(), Patient.class);
                    localStorageService.savePatient(patient);
                    patientsApplied.incrementAndGet();
                    return patient.getId().toString();
                }
                case "questionnaire" -> {
                    Questionnaire questionnaire = objectMapper.readValue(event.getPayload(), Questionnaire.class);
                    localStorageService.saveQuestionnaire(questionnaire);
                    questionnairesApplied.incrementAndGet();
                    return questionnaire.getId().toString();
                }
                default -> {
                    // Ignorar eventos desconocidos manteniendo la idempotencia.
                }
            }
            return "unknown";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo aplicar el evento remoto", e);
        }
    }

    /**
     * Resultado resumido de una operación de sincronización.
     */
    public static class SyncSummary {
        private final int patientsProcessed;
        private final int questionnairesProcessed;
        private final int eventsProcessed;

        public SyncSummary(int patientsProcessed, int questionnairesProcessed, int eventsProcessed) {
            this.patientsProcessed = patientsProcessed;
            this.questionnairesProcessed = questionnairesProcessed;
            this.eventsProcessed = eventsProcessed;
        }

        public static SyncSummary empty() {
            return new SyncSummary(0, 0, 0);
        }

        public static SyncSummary from(SyncBatch request, SyncBatch response) {
            int patients = response.getPatients().isEmpty() ? request.getPatients().size() : response.getPatients().size();
            int questionnaires = response.getQuestionnaires().isEmpty() ? request.getQuestionnaires().size() : response.getQuestionnaires().size();
            return new SyncSummary(patients, questionnaires, patients + questionnaires);
        }

        public int getPatientsProcessed() {
            return patientsProcessed;
        }

        public int getQuestionnairesProcessed() {
            return questionnairesProcessed;
        }

        public int getEventsProcessed() {
            return eventsProcessed;
        }
    }
}
