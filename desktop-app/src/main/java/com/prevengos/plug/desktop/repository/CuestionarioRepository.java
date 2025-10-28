package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.Cuestionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CuestionarioRepository {

    private final DatabaseManager databaseManager;

    public CuestionarioRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Cuestionario> findByPaciente(UUID pacienteId) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cuestionarios WHERE paciente_id = ? ORDER BY updated_at DESC")) {
            statement.setString(1, pacienteId.toString());
            ResultSet rs = statement.executeQuery();
            List<Cuestionario> cuestionarios = new ArrayList<>();
            while (rs.next()) {
                cuestionarios.add(mapRow(rs));
            }
            return cuestionarios;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudieron listar los cuestionarios", e);
        }
    }

    public List<Cuestionario> findDirty() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cuestionarios WHERE dirty = 1")) {
            ResultSet rs = statement.executeQuery();
            List<Cuestionario> dirty = new ArrayList<>();
            while (rs.next()) {
                dirty.add(mapRow(rs));
            }
            return dirty;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudieron listar los cuestionarios pendientes", e);
        }
    }

    public Cuestionario create(UUID pacienteId, String plantillaCodigo, String estado, String respuestas,
                               String firmas, String adjuntos, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        UUID cuestionarioId = UUID.randomUUID();
        OffsetDateTime now = updatedAt != null ? updatedAt : OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime creation = createdAt != null ? createdAt : now;
        long lastModified = DatabaseManager.nowEpochMillis();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO cuestionarios (
                         cuestionario_id, paciente_id, plantilla_codigo, estado, respuestas, firmas, adjuntos,
                         created_at, updated_at, last_modified, sync_token, dirty
                     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                     """)) {
            statement.setString(1, cuestionarioId.toString());
            statement.setString(2, pacienteId.toString());
            statement.setString(3, plantillaCodigo);
            statement.setString(4, estado);
            statement.setString(5, respuestas);
            statement.setString(6, firmas);
            statement.setString(7, adjuntos);
            statement.setString(8, creation != null ? creation.toString() : null);
            statement.setString(9, now != null ? now.toString() : null);
            statement.setLong(10, lastModified);
            statement.setLong(11, 0L);
            statement.executeUpdate();
            return new Cuestionario(cuestionarioId, pacienteId, plantillaCodigo, estado, respuestas, firmas, adjuntos,
                    creation, now, lastModified, 0L, true);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo crear el cuestionario", e);
        }
    }

    public Cuestionario update(UUID cuestionarioId, String estado, String respuestas, String firmas, String adjuntos) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long lastModified = DatabaseManager.nowEpochMillis();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE cuestionarios SET
                         estado = ?,
                         respuestas = ?,
                         firmas = ?,
                         adjuntos = ?,
                         updated_at = ?,
                         last_modified = ?,
                         dirty = 1
                     WHERE cuestionario_id = ?
                     """)) {
            statement.setString(1, estado);
            statement.setString(2, respuestas);
            statement.setString(3, firmas);
            statement.setString(4, adjuntos);
            statement.setString(5, now.toString());
            statement.setLong(6, lastModified);
            statement.setString(7, cuestionarioId.toString());
            if (statement.executeUpdate() == 0) {
                throw new RepositoryException("Cuestionario no encontrado: " + cuestionarioId);
            }
            Cuestionario existing = findById(cuestionarioId);
            return new Cuestionario(cuestionarioId, existing.pacienteId(), existing.plantillaCodigo(), estado, respuestas,
                    firmas, adjuntos, existing.createdAt(), now, lastModified, existing.syncToken(), true);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo actualizar el cuestionario", e);
        }
    }

    public Cuestionario findById(UUID cuestionarioId) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cuestionarios WHERE cuestionario_id = ?")) {
            statement.setString(1, cuestionarioId.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el cuestionario", e);
        }
    }

    public void delete(UUID cuestionarioId) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM cuestionarios WHERE cuestionario_id = ?")) {
            statement.setString(1, cuestionarioId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo eliminar el cuestionario", e);
        }
    }

    public void markAsClean(UUID cuestionarioId, long syncToken) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE cuestionarios SET dirty = 0, sync_token = ?, last_modified = ? WHERE cuestionario_id = ?
                     """)) {
            statement.setLong(1, syncToken);
            statement.setLong(2, DatabaseManager.nowEpochMillis());
            statement.setString(3, cuestionarioId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo marcar el cuestionario como sincronizado", e);
        }
    }

    public void upsertFromRemote(Cuestionario cuestionario) {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement("""
                    UPDATE cuestionarios SET
                        paciente_id = ?,
                        plantilla_codigo = ?,
                        estado = ?,
                        respuestas = ?,
                        firmas = ?,
                        adjuntos = ?,
                        created_at = ?,
                        updated_at = ?,
                        last_modified = ?,
                        sync_token = ?,
                        dirty = 0
                    WHERE cuestionario_id = ?
                    """)) {
                fillStatement(update, cuestionario);
                update.setString(12, cuestionario.cuestionarioId().toString());
                int updated = update.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement insert = connection.prepareStatement("""
                            INSERT INTO cuestionarios (
                                cuestionario_id, paciente_id, plantilla_codigo, estado, respuestas, firmas, adjuntos,
                                created_at, updated_at, last_modified, sync_token, dirty
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                            """)) {
                        fillStatement(insert, cuestionario);
                        insert.setString(12, cuestionario.cuestionarioId().toString());
                        insert.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo registrar el cuestionario remoto", e);
        }
    }

    private static void fillStatement(PreparedStatement statement, Cuestionario cuestionario) throws SQLException {
        statement.setString(1, cuestionario.pacienteId().toString());
        statement.setString(2, cuestionario.plantillaCodigo());
        statement.setString(3, cuestionario.estado());
        statement.setString(4, cuestionario.respuestas());
        statement.setString(5, cuestionario.firmas());
        statement.setString(6, cuestionario.adjuntos());
        statement.setString(7, cuestionario.createdAt() != null ? cuestionario.createdAt().toString() : null);
        statement.setString(8, cuestionario.updatedAt() != null ? cuestionario.updatedAt().toString() : null);
        statement.setLong(9, cuestionario.lastModified());
        statement.setLong(10, cuestionario.syncToken());
        statement.setString(11, cuestionario.cuestionarioId().toString());
    }

    private Cuestionario mapRow(ResultSet rs) throws SQLException {
        return new Cuestionario(
                UUID.fromString(rs.getString("cuestionario_id")),
                UUID.fromString(rs.getString("paciente_id")),
                rs.getString("plantilla_codigo"),
                rs.getString("estado"),
                rs.getString("respuestas"),
                rs.getString("firmas"),
                rs.getString("adjuntos"),
                parseOffsetDateTime(rs.getString("created_at")),
                parseOffsetDateTime(rs.getString("updated_at")),
                rs.getLong("last_modified"),
                rs.getLong("sync_token"),
                rs.getInt("dirty") == 1
        );
    }

    private static OffsetDateTime parseOffsetDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }
}
