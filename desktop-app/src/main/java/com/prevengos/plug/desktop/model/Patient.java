package com.prevengos.plug.desktop.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa a un paciente en el almacenamiento local y en el proceso de sincronización.
 */
public final class Patient {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String documentNumber;
    private final LocalDate birthDate;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Patient(UUID id,
                   String firstName,
                   String lastName,
                   String documentNumber,
                   LocalDate birthDate,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        this.documentNumber = documentNumber;
        this.birthDate = birthDate;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Patient withUpdatedAt(Instant newUpdatedAt) {
        return new Patient(id, firstName, lastName, documentNumber, birthDate, createdAt, newUpdatedAt);
    }

    public Patient withNames(String newFirstName, String newLastName) {
        return new Patient(id, newFirstName, newLastName, documentNumber, birthDate, createdAt, Instant.now());
    }

    public Patient withDocument(String newDocument) {
        return new Patient(id, firstName, lastName, newDocument, birthDate, createdAt, Instant.now());
    }

    public Patient withBirthDate(LocalDate newBirthDate) {
        return new Patient(id, firstName, lastName, documentNumber, newBirthDate, createdAt, Instant.now());
    }
}
