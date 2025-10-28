package com.prevengos.plug.desktop.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa un lote de sincronización enviado al Hub.
 */
public final class SyncBatch {

    private final List<Patient> patients;
    private final List<Questionnaire> questionnaires;
    private final String sourceSystem;
    private final Instant generatedAt;

    @JsonCreator
    public SyncBatch(@JsonProperty("patients") List<Patient> patients,
                     @JsonProperty("questionnaires") List<Questionnaire> questionnaires,
                     @JsonProperty("sourceSystem") String sourceSystem,
                     @JsonProperty("generatedAt") Instant generatedAt) {
        this.patients = patients != null ? List.copyOf(patients) : Collections.emptyList();
        this.questionnaires = questionnaires != null ? List.copyOf(questionnaires) : Collections.emptyList();
        this.sourceSystem = Objects.requireNonNull(sourceSystem, "sourceSystem");
        this.generatedAt = Objects.requireNonNullElseGet(generatedAt, Instant::now);
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public List<Questionnaire> getQuestionnaires() {
        return questionnaires;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public boolean isEmpty() {
        return patients.isEmpty() && questionnaires.isEmpty();
    }
}
