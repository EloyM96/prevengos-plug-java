package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacienteRepository {

    private final DatabaseManager databaseManager;

    public PacienteRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Paciente> findAll() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM pacientes ORDER BY nombre COLLATE NOCASE")) {
            ResultSet rs = statement.executeQuery();
            List<Paciente> pacientes = new ArrayList<>();
            while (rs.next()) {
                pacientes.add(mapRow(rs));
            }
            return pacientes;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudieron listar los pacientes", e);
        }
    }

    public Paciente findById(UUID pacienteId) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM pacientes WHERE paciente_id = ?")) {
            statement.setString(1, pacienteId.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el paciente" + pacienteId, e);
        }
    }

    public Paciente create(String nif, String nombre, String apellidos, LocalDate fechaNacimiento, String sexo,
                           String telefono, String email, UUID empresaId, UUID centroId, String externoRef) {
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long lastModified = DatabaseManager.nowEpochMillis();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO pacientes (
                         paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email,
                         empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token, dirty
                     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                     """)) {
            statement.setString(1, pacienteId.toString());
            statement.setString(2, nif);
            statement.setString(3, nombre);
            statement.setString(4, apellidos);
            statement.setString(5, fechaNacimiento != null ? fechaNacimiento.toString() : null);
            statement.setString(6, sexo);
            statement.setString(7, telefono);
            statement.setString(8, email);
            statement.setString(9, empresaId != null ? empresaId.toString() : null);
            statement.setString(10, centroId != null ? centroId.toString() : null);
            statement.setString(11, externoRef);
            statement.setString(12, now.toString());
            statement.setString(13, now.toString());
            statement.setLong(14, lastModified);
            statement.setLong(15, 0L);
            statement.executeUpdate();
            return new Paciente(pacienteId, nif, nombre, apellidos, fechaNacimiento, sexo, telefono, email,
                    empresaId, centroId, externoRef, now, now, lastModified, 0L, true);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo crear el paciente", e);
        }
    }

    public Paciente update(UUID pacienteId, String nif, String nombre, String apellidos, LocalDate fechaNacimiento,
                           String sexo, String telefono, String email, UUID empresaId, UUID centroId, String externoRef) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long lastModified = DatabaseManager.nowEpochMillis();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE pacientes SET
                         nif = ?,
                         nombre = ?,
                         apellidos = ?,
                         fecha_nacimiento = ?,
                         sexo = ?,
                         telefono = ?,
                         email = ?,
                         empresa_id = ?,
                         centro_id = ?,
                         externo_ref = ?,
                         updated_at = ?,
                         last_modified = ?,
                         dirty = 1
                     WHERE paciente_id = ?
                     """)) {
            statement.setString(1, nif);
            statement.setString(2, nombre);
            statement.setString(3, apellidos);
            statement.setString(4, fechaNacimiento != null ? fechaNacimiento.toString() : null);
            statement.setString(5, sexo);
            statement.setString(6, telefono);
            statement.setString(7, email);
            statement.setString(8, empresaId != null ? empresaId.toString() : null);
            statement.setString(9, centroId != null ? centroId.toString() : null);
            statement.setString(10, externoRef);
            statement.setString(11, now.toString());
            statement.setLong(12, lastModified);
            statement.setString(13, pacienteId.toString());
            if (statement.executeUpdate() == 0) {
                throw new RepositoryException("Paciente no encontrado: " + pacienteId);
            }
            Paciente existing = findById(pacienteId);
            return new Paciente(pacienteId, nif, nombre, apellidos, fechaNacimiento, sexo, telefono, email,
                    empresaId, centroId, externoRef, existing.createdAt(), now, lastModified, existing.syncToken(), true);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo actualizar el paciente", e);
        }
    }

    public void delete(UUID pacienteId) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM pacientes WHERE paciente_id = ?")) {
            statement.setString(1, pacienteId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo eliminar el paciente", e);
        }
    }

    public List<Paciente> findDirty() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM pacientes WHERE dirty = 1")) {
            ResultSet rs = statement.executeQuery();
            List<Paciente> dirty = new ArrayList<>();
            while (rs.next()) {
                dirty.add(mapRow(rs));
            }
            return dirty;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudieron listar los pacientes pendientes", e);
        }
    }

    public void markAsClean(UUID pacienteId, long syncToken) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE pacientes SET dirty = 0, sync_token = ?, last_modified = ? WHERE paciente_id = ?
                     """)) {
            statement.setLong(1, syncToken);
            statement.setLong(2, DatabaseManager.nowEpochMillis());
            statement.setString(3, pacienteId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo marcar el paciente como sincronizado", e);
        }
    }

    public void upsertFromRemote(Paciente paciente) {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement("""
                    UPDATE pacientes SET
                        nif = ?,
                        nombre = ?,
                        apellidos = ?,
                        fecha_nacimiento = ?,
                        sexo = ?,
                        telefono = ?,
                        email = ?,
                        empresa_id = ?,
                        centro_id = ?,
                        externo_ref = ?,
                        created_at = ?,
                        updated_at = ?,
                        last_modified = ?,
                        sync_token = ?,
                        dirty = 0
                    WHERE paciente_id = ?
                    """)) {
                fillStatement(update, paciente);
                update.setString(15, paciente.pacienteId().toString());
                int updated = update.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement insert = connection.prepareStatement("""
                            INSERT INTO pacientes (
                                paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email,
                                empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token, dirty
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                            """)) {
                        fillStatement(insert, paciente);
                        insert.setString(15, paciente.pacienteId().toString());
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
            throw new RepositoryException("No se pudo registrar el paciente remoto", e);
        }
    }

    private static void fillStatement(PreparedStatement statement, Paciente paciente) throws SQLException {
        statement.setString(1, paciente.nif());
        statement.setString(2, paciente.nombre());
        statement.setString(3, paciente.apellidos());
        statement.setString(4, paciente.fechaNacimiento() != null ? paciente.fechaNacimiento().toString() : null);
        statement.setString(5, paciente.sexo());
        statement.setString(6, paciente.telefono());
        statement.setString(7, paciente.email());
        statement.setString(8, paciente.empresaId() != null ? paciente.empresaId().toString() : null);
        statement.setString(9, paciente.centroId() != null ? paciente.centroId().toString() : null);
        statement.setString(10, paciente.externoRef());
        statement.setString(11, paciente.createdAt() != null ? paciente.createdAt().toString() : null);
        statement.setString(12, paciente.updatedAt() != null ? paciente.updatedAt().toString() : null);
        statement.setLong(13, paciente.lastModified());
        statement.setLong(14, paciente.syncToken());
    }

    private Paciente mapRow(ResultSet rs) throws SQLException {
        return new Paciente(
                UUID.fromString(rs.getString("paciente_id")),
                rs.getString("nif"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                parseLocalDate(rs.getString("fecha_nacimiento")),
                rs.getString("sexo"),
                rs.getString("telefono"),
                rs.getString("email"),
                parseUuid(rs.getString("empresa_id")),
                parseUuid(rs.getString("centro_id")),
                rs.getString("externo_ref"),
                parseOffsetDateTime(rs.getString("created_at")),
                parseOffsetDateTime(rs.getString("updated_at")),
                rs.getLong("last_modified"),
                rs.getLong("sync_token"),
                rs.getInt("dirty") == 1
        );
    }

    private static UUID parseUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static LocalDate parseLocalDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    private static OffsetDateTime parseOffsetDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }
}
