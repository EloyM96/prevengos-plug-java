package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.model.Questionnaire;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JDBC para cuestionarios.
 */
public class QuestionnaireRepository {

    private final DatabaseManager databaseManager;

    public QuestionnaireRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void upsert(Questionnaire questionnaire) {
        String sql = """
                INSERT INTO questionnaires(id, patient_id, title, responses, created_at, updated_at)
                VALUES(?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    title = excluded.title,
                    responses = excluded.responses,
                    updated_at = excluded.updated_at
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, questionnaire.getId().toString());
            statement.setString(2, questionnaire.getPatientId().toString());
            statement.setString(3, questionnaire.getTitle());
            statement.setString(4, questionnaire.getResponses());
            statement.setString(5, questionnaire.getCreatedAt().toString());
            statement.setString(6, questionnaire.getUpdatedAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el cuestionario", e);
        }
    }

    public List<Questionnaire> findAll() {
        String sql = "SELECT id, patient_id, title, responses, created_at, updated_at FROM questionnaires";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Questionnaire> questionnaires = new ArrayList<>();
            while (rs.next()) {
                questionnaires.add(mapRow(rs));
            }
            return questionnaires;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo obtener la lista de cuestionarios", e);
        }
    }

    public List<Questionnaire> findUpdatedSince(Instant since) {
        String sql = """
                SELECT id, patient_id, title, responses, created_at, updated_at
                FROM questionnaires
                WHERE updated_at > ?
                ORDER BY updated_at ASC
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, since.toString());
            try (ResultSet rs = statement.executeQuery()) {
                List<Questionnaire> questionnaires = new ArrayList<>();
                while (rs.next()) {
                    questionnaires.add(mapRow(rs));
                }
                return questionnaires;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo obtener cuestionarios modificados", e);
        }
    }

    public Optional<Questionnaire> findById(UUID id) {
        String sql = "SELECT id, patient_id, title, responses, created_at, updated_at FROM questionnaires WHERE id = ?";
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
            throw new IllegalStateException("No se pudo buscar el cuestionario", e);
        }
    }

    public void delete(UUID id) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM questionnaires WHERE id = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el cuestionario", e);
        }
    }

    private Questionnaire mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID patientId = UUID.fromString(rs.getString("patient_id"));
        String title = rs.getString("title");
        String responses = rs.getString("responses");
        Instant createdAt = Instant.parse(rs.getString("created_at"));
        Instant updatedAt = Instant.parse(rs.getString("updated_at"));
        return new Questionnaire(id, patientId, title, responses, createdAt, updatedAt);
    }
}
