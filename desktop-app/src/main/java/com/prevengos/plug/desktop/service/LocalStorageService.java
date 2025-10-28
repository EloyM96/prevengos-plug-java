package com.prevengos.plug.desktop.service;

import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PatientRepository;
import com.prevengos.plug.desktop.repository.QuestionnaireRepository;
import com.prevengos.plug.desktop.repository.SyncEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fachada sobre los repositorios locales que centraliza operaciones y actualiza los metadatos de sincronización.
 */
public class LocalStorageService {

    public static final String META_PATIENTS_LAST_PUSH = "patients.lastPush";
    public static final String META_QUESTIONNAIRES_LAST_PUSH = "questionnaires.lastPush";
    public static final String META_SYNC_TOKEN = "sync.nextToken";

    private final PatientRepository patientRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final MetadataRepository metadataRepository;
    private final SyncEventRepository syncEventRepository;

    public LocalStorageService(PatientRepository patientRepository,
                               QuestionnaireRepository questionnaireRepository,
                               MetadataRepository metadataRepository,
                               SyncEventRepository syncEventRepository) {
        this.patientRepository = patientRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.metadataRepository = metadataRepository;
        this.syncEventRepository = syncEventRepository;
    }

    public List<Patient> listPatients() {
        return patientRepository.findAll();
    }

    public void savePatient(Patient patient) {
        patientRepository.upsert(patient);
        syncEventRepository.record("patient", patient.getId().toString(), "patient-upserted", "{}", null);
    }

    public void deletePatient(UUID id) {
        patientRepository.delete(id);
        syncEventRepository.record("patient", id.toString(), "patient-deleted", "{}", null);
    }

    public Optional<Patient> findPatient(UUID id) {
        return patientRepository.findById(id);
    }

    public List<Questionnaire> listQuestionnaires() {
        return questionnaireRepository.findAll();
    }

    public void saveQuestionnaire(Questionnaire questionnaire) {
        questionnaireRepository.upsert(questionnaire);
        syncEventRepository.record("questionnaire", questionnaire.getId().toString(), "questionnaire-upserted", "{}", null);
    }

    public void deleteQuestionnaire(UUID id) {
        questionnaireRepository.delete(id);
        syncEventRepository.record("questionnaire", id.toString(), "questionnaire-deleted", "{}", null);
    }

    public Optional<Questionnaire> findQuestionnaire(UUID id) {
        return questionnaireRepository.findById(id);
    }

    public List<Patient> patientsUpdatedSince(Instant since) {
        return patientRepository.findUpdatedSince(since);
    }

    public List<Questionnaire> questionnairesUpdatedSince(Instant since) {
        return questionnaireRepository.findUpdatedSince(since);
    }

    public Instant readLastPatientPush() {
        return metadataRepository.get(META_PATIENTS_LAST_PUSH)
                .map(Instant::parse)
                .orElse(Instant.EPOCH);
    }

    public void updateLastPatientPush(Instant instant) {
        metadataRepository.put(META_PATIENTS_LAST_PUSH, instant.toString());
    }

    public Instant readLastQuestionnairePush() {
        return metadataRepository.get(META_QUESTIONNAIRES_LAST_PUSH)
                .map(Instant::parse)
                .orElse(Instant.EPOCH);
    }

    public void updateLastQuestionnairePush(Instant instant) {
        metadataRepository.put(META_QUESTIONNAIRES_LAST_PUSH, instant.toString());
    }

    public Optional<String> readSyncToken() {
        return metadataRepository.get(META_SYNC_TOKEN);
    }

    public void updateSyncToken(String token) {
        metadataRepository.put(META_SYNC_TOKEN, token);
    }

    public SyncEventRepository getSyncEventRepository() {
        return syncEventRepository;
    }
}
