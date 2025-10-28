package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JDBC para la entidad {@link Patient}.
 */
public class PatientRepository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DatabaseManager databaseManager;

    public PatientRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void upsert(Patient patient) {
        String sql = """
                INSERT INTO patients(id, first_name, last_name, document_number, birth_date, created_at, updated_at)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    first_name = excluded.first_name,
                    last_name = excluded.last_name,
                    document_number = excluded.document_number,
                    birth_date = excluded.birth_date,
                    updated_at = excluded.updated_at
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patient.getId().toString());
            statement.setString(2, patient.getFirstName());
            statement.setString(3, patient.getLastName());
            statement.setString(4, patient.getDocumentNumber());
            statement.setString(5, patient.getBirthDate() != null ? DATE_FORMATTER.format(patient.getBirthDate()) : null);
            statement.setString(6, patient.getCreatedAt().toString());
            statement.setString(7, patient.getUpdatedAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el paciente", e);
        }
    }

    public List<Patient> findAll() {
        String sql = "SELECT id, first_name, last_name, document_number, birth_date, created_at, updated_at FROM patients";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
            return patients;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo obtener la lista de pacientes", e);
        }
    }

    public List<Patient> findUpdatedSince(Instant since) {
        String sql = """
                SELECT id, first_name, last_name, document_number, birth_date, created_at, updated_at
                FROM patients
                WHERE updated_at > ?
                ORDER BY updated_at ASC
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, since.toString());
            try (ResultSet rs = statement.executeQuery()) {
                List<Patient> patients = new ArrayList<>();
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
                return patients;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo obtener pacientes modificados", e);
        }
    }

    public Optional<Patient> findById(UUID id) {
        String sql = "SELECT id, first_name, last_name, document_number, birth_date, created_at, updated_at FROM patients WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el paciente", e);
        }
    }

    public void delete(UUID id) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM patients WHERE id = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el paciente", e);
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String documentNumber = rs.getString("document_number");
        String birthDateRaw = rs.getString("birth_date");
        LocalDate birthDate = birthDateRaw != null ? LocalDate.parse(birthDateRaw, DATE_FORMATTER) : null;
        Instant createdAt = Instant.parse(rs.getString("created_at"));
        Instant updatedAt = Instant.parse(rs.getString("updated_at"));
        return new Patient(id, firstName, lastName, documentNumber, birthDate, createdAt, updatedAt);
    }
}
