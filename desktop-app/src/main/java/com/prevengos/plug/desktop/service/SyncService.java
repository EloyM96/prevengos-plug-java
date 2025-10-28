package com.prevengos.plug.desktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordina los envíos y pulls con el Hub siguiendo las reglas definidas en los flujos de sincronización.
 */
public class SyncService {

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

        if (patients.isEmpty() && questionnaires.isEmpty()) {
            return SyncSummary.empty();
        }

        SyncPushRequest request = new SyncPushRequest(
                appConfig.sourceSystem(),
                UUID.randomUUID(),
                patients.stream().map(this::toRemotePatient).toList(),
                questionnaires.stream().map(this::toRemoteQuestionnaire).toList()
        );

        SyncPushResponse response = remoteSyncGateway.push(request);
        Instant now = Instant.now();
        if (!patients.isEmpty()) {
            localStorageService.updateLastPatientPush(now);
        }
        if (!questionnaires.isEmpty()) {
            localStorageService.updateLastQuestionnairePush(now);
        }
        long processedPatients = response != null ? response.processedPacientes() : patients.size();
        long processedQuestionnaires = response != null ? response.processedCuestionarios() : questionnaires.size();
        return new SyncSummary((int) processedPatients, (int) processedQuestionnaires,
                (int) (processedPatients + processedQuestionnaires));
    }

    public SyncSummary pullUpdates() {
        Optional<String> maybeToken = localStorageService.readSyncToken();
        Long token = maybeToken.filter(s -> !s.isBlank()).map(this::safeParseLong).orElse(null);

        SyncPullResponse response = remoteSyncGateway.pull(token, appConfig.syncBatchSize());

        int patientsApplied = 0;
        for (PacienteDto pacienteDto : response.pacientes()) {
            Patient patient = toLocalPatient(pacienteDto);
            localStorageService.savePatient(patient);
            patientsApplied++;
        }

        int questionnairesApplied = 0;
        for (CuestionarioDto cuestionarioDto : response.cuestionarios()) {
            Questionnaire questionnaire = toLocalQuestionnaire(cuestionarioDto);
            localStorageService.saveQuestionnaire(questionnaire);
            questionnairesApplied++;
        }

        response.events().forEach(event -> {
            try {
                JsonNode payloadNode = event.payload() != null ? objectMapper.readTree(event.payload()) : objectMapper.nullNode();
                localStorageService.getSyncEventRepository().append(
                        "remote", event.eventId() != null ? event.eventId().toString() : "unknown",
                        event.eventType(), payloadNode,
                        event.source(), event.syncToken() != null ? event.syncToken() : 0L
                );
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo registrar el evento remoto", e);
            }
        });

        localStorageService.updateSyncToken(Long.toString(response.nextSyncToken()));

        return new SyncSummary(patientsApplied, questionnairesApplied,
                patientsApplied + questionnairesApplied + response.events().size());
    }

    private PacienteDto toRemotePatient(Patient patient) {
        OffsetDateTime createdAt = toOffset(patient.getCreatedAt());
        OffsetDateTime updatedAt = toOffset(patient.getUpdatedAt());
        return new PacienteDto(
                patient.getId(),
                patient.getDocumentNumber(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getBirthDate(),
                null,
                null,
                null,
                null,
                null,
                null,
                createdAt,
                updatedAt,
                updatedAt,
                null
        );
    }

    private CuestionarioDto toRemoteQuestionnaire(Questionnaire questionnaire) {
        OffsetDateTime createdAt = toOffset(questionnaire.getCreatedAt());
        OffsetDateTime updatedAt = toOffset(questionnaire.getUpdatedAt());
        return new CuestionarioDto(
                questionnaire.getId(),
                questionnaire.getPatientId(),
                questionnaire.getTitle(),
                null,
                questionnaire.getResponses(),
                null,
                null,
                createdAt,
                updatedAt,
                updatedAt,
                null
        );
    }

    private Patient toLocalPatient(PacienteDto dto) {
        Instant createdAt = toInstant(dto.createdAt());
        Instant updatedAt = toInstant(dto.updatedAt());
        return new Patient(
                dto.pacienteId(),
                dto.nombre() != null ? dto.nombre() : "",
                dto.apellidos() != null ? dto.apellidos() : "",
                dto.nif(),
                dto.fechaNacimiento(),
                createdAt,
                updatedAt
        );
    }

    private Questionnaire toLocalQuestionnaire(CuestionarioDto dto) {
        Instant createdAt = toInstant(dto.createdAt());
        Instant updatedAt = toInstant(dto.updatedAt());
        String title = dto.plantillaCodigo() != null ? dto.plantillaCodigo() : "";
        return new Questionnaire(
                dto.cuestionarioId(),
                dto.pacienteId(),
                title,
                dto.respuestas(),
                createdAt,
                updatedAt
        );
    }

    private OffsetDateTime toOffset(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }

    private Instant toInstant(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant() : Instant.now();
    }

    private Long safeParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
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
