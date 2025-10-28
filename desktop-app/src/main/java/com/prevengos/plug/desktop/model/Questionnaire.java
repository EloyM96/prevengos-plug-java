package com.prevengos.plug.desktop.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un cuestionario local que puede sincronizarse con el Hub.
 */
public final class Questionnaire {

    private final UUID id;
    private final UUID patientId;
    private final String title;
    private final String responses;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Questionnaire(UUID id,
                         UUID patientId,
                         String title,
                         String responses,
                         Instant createdAt,
                         Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.patientId = Objects.requireNonNull(patientId, "patientId");
        this.title = Objects.requireNonNull(title, "title");
        this.responses = responses;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getTitle() {
        return title;
    }

    public String getResponses() {
        return responses;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Questionnaire withUpdatedAt(Instant newUpdatedAt) {
        return new Questionnaire(id, patientId, title, responses, createdAt, newUpdatedAt);
    }

    public Questionnaire withTitleAndResponses(String newTitle, String newResponses) {
        return new Questionnaire(id, patientId, newTitle, newResponses, createdAt, Instant.now());
    }
}
