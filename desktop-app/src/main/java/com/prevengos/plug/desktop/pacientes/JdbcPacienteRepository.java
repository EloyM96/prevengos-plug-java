package com.prevengos.plug.desktop.pacientes;

import com.prevengos.plug.desktop.config.DatabaseMode;
import com.prevengos.plug.desktop.db.ConnectionProvider;
import com.prevengos.plug.shared.contracts.v1.Paciente;
import com.prevengos.plug.shared.contracts.v1.Paciente.Sexo;
import com.prevengos.plug.shared.time.ContractDateFormats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class JdbcPacienteRepository implements PacienteRepository {

    private final ConnectionProvider connectionProvider;
    private final DatabaseMode mode;
    private final Comparator<Paciente> comparator = Comparator
            .comparing(Paciente::apellidos, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Paciente::nombre, String.CASE_INSENSITIVE_ORDER);

    public JdbcPacienteRepository(ConnectionProvider connectionProvider, DatabaseMode mode) {
        this.connectionProvider = connectionProvider;
        this.mode = mode;
    }

    @Override
    public List<Paciente> findAll() {
        return query(null);
    }

    @Override
    public List<Paciente> search(String filter) {
        String normalized = filter == null ? null : filter.trim();
        if (normalized == null || normalized.isEmpty()) {
            return findAll();
        }
        return query(normalized);
    }

    @Override
    public Optional<Paciente> findById(UUID pacienteId) {
        String sql = "SELECT " + selectColumns() + " FROM " + tableName() + " WHERE paciente_id = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pacienteId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPaciente(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to fetch paciente " + pacienteId, ex);
        }
    }

    @Override
    public Paciente save(Paciente paciente) {
        boolean exists = exists(paciente.pacienteId());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        try (Connection connection = connectionProvider.getConnection()) {
            if (exists) {
                updatePaciente(connection, paciente, now);
            } else {
                insertPaciente(connection, paciente, now);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to persist paciente", ex);
        }

        Paciente.Builder builder = Paciente.builder()
                .pacienteId(paciente.pacienteId())
                .nif(paciente.nif())
                .nombre(paciente.nombre())
                .apellidos(paciente.apellidos())
                .fechaNacimiento(paciente.fechaNacimiento())
                .sexo(paciente.sexo())
                .updatedAt(now);

        paciente.telefono().ifPresent(builder::telefono);
        paciente.email().ifPresent(builder::email);
        paciente.empresaId().ifPresent(builder::empresaId);
        paciente.centroId().ifPresent(builder::centroId);
        paciente.externoRef().ifPresent(builder::externoRef);

        OffsetDateTime createdAt = paciente.createdAt().orElse(null);
        if (createdAt == null && !exists) {
            createdAt = now;
        }
        if (createdAt != null) {
            builder.createdAt(createdAt);
        }

        return builder.build();
    }

    @Override
    public void delete(UUID pacienteId) {
        String sql = "DELETE FROM " + tableName() + " WHERE paciente_id = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pacienteId.toString());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to delete paciente " + pacienteId, ex);
        }
    }

    private List<Paciente> query(String filter) {
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(selectColumns())
                .append(" FROM ")
                .append(tableName());

        List<String> params = new ArrayList<>();
        if (filter != null && !filter.isBlank()) {
            String normalized = "%" + filter.toLowerCase(Locale.ROOT) + "%";
            sql.append(" WHERE LOWER(nif) LIKE ? OR LOWER(nombre) LIKE ? OR LOWER(apellidos) LIKE ?");
            params.add(normalized);
            params.add(normalized);
            params.add(normalized);
        }

        sql.append(" ORDER BY apellidos ASC, nombre ASC");

        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Paciente> pacientes = new ArrayList<>();
                while (rs.next()) {
                    pacientes.add(mapPaciente(rs));
                }
                pacientes.sort(comparator);
                return pacientes;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to query pacientes", ex);
        }
    }

    private boolean exists(UUID pacienteId) {
        String sql = "SELECT 1 FROM " + tableName() + " WHERE paciente_id = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pacienteId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to verify paciente existence", ex);
        }
    }

    private void insertPaciente(Connection connection, Paciente paciente, OffsetDateTime updatedAt) throws SQLException {
        if (mode == DatabaseMode.LOCAL) {
            String sql = "INSERT INTO pacientes (paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email, " +
                    "empresa_id, centro_id, externo_ref, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindCommonParameters(stmt, paciente, updatedAt, false);
                stmt.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO dbo.pacientes (paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email, " +
                    "empresa_id, centro_id, externo_ref, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindCommonParameters(stmt, paciente, updatedAt, true);
                stmt.executeUpdate();
            }
        }
    }

    private void updatePaciente(Connection connection, Paciente paciente, OffsetDateTime updatedAt) throws SQLException {
        if (mode == DatabaseMode.LOCAL) {
            String sql = "UPDATE pacientes SET nif = ?, nombre = ?, apellidos = ?, fecha_nacimiento = ?, sexo = ?, telefono = ?, " +
                    "email = ?, empresa_id = ?, centro_id = ?, externo_ref = ?, updated_at = ? WHERE paciente_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindUpdateParameters(stmt, paciente, updatedAt, false);
                stmt.executeUpdate();
            }
        } else {
            String sql = "UPDATE dbo.pacientes SET nif = ?, nombre = ?, apellidos = ?, fecha_nacimiento = ?, sexo = ?, telefono = ?, " +
                    "email = ?, empresa_id = ?, centro_id = ?, externo_ref = ?, updated_at = ?, last_modified = SYSUTCDATETIME() WHERE paciente_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindUpdateParameters(stmt, paciente, updatedAt, true);
                stmt.executeUpdate();
            }
        }
    }

    private void bindCommonParameters(PreparedStatement stmt, Paciente paciente, OffsetDateTime updatedAt, boolean sqlServer) throws SQLException {
        stmt.setString(1, paciente.pacienteId().toString());
        stmt.setString(2, paciente.nif());
        stmt.setString(3, paciente.nombre());
        stmt.setString(4, paciente.apellidos());

        if (sqlServer) {
            stmt.setDate(5, java.sql.Date.valueOf(paciente.fechaNacimiento()));
        } else {
            stmt.setString(5, ContractDateFormats.formatDate(paciente.fechaNacimiento()));
        }

        stmt.setString(6, paciente.sexo().code());
        stmt.setString(7, paciente.telefono().orElse(null));
        stmt.setString(8, paciente.email().orElse(null));
        stmt.setString(9, paciente.empresaId().map(UUID::toString).orElse(null));
        stmt.setString(10, paciente.centroId().map(UUID::toString).orElse(null));
        stmt.setString(11, paciente.externoRef().orElse(null));

        if (sqlServer) {
            stmt.setObject(12, updatedAt);
        } else {
            stmt.setString(12, ContractDateFormats.formatDateTime(updatedAt));
        }
    }

    private void bindUpdateParameters(PreparedStatement stmt, Paciente paciente, OffsetDateTime updatedAt, boolean sqlServer) throws SQLException {
        stmt.setString(1, paciente.nif());
        stmt.setString(2, paciente.nombre());
        stmt.setString(3, paciente.apellidos());
        if (sqlServer) {
            stmt.setDate(4, java.sql.Date.valueOf(paciente.fechaNacimiento()));
        } else {
            stmt.setString(4, ContractDateFormats.formatDate(paciente.fechaNacimiento()));
        }
        stmt.setString(5, paciente.sexo().code());
        stmt.setString(6, paciente.telefono().orElse(null));
        stmt.setString(7, paciente.email().orElse(null));
        stmt.setString(8, paciente.empresaId().map(UUID::toString).orElse(null));
        stmt.setString(9, paciente.centroId().map(UUID::toString).orElse(null));
        stmt.setString(10, paciente.externoRef().orElse(null));
        if (sqlServer) {
            stmt.setObject(11, updatedAt);
            stmt.setString(12, paciente.pacienteId().toString());
        } else {
            stmt.setString(11, ContractDateFormats.formatDateTime(updatedAt));
            stmt.setString(12, paciente.pacienteId().toString());
        }
    }

    private Paciente mapPaciente(ResultSet rs) throws SQLException {
        UUID pacienteId = UUID.fromString(rs.getString("paciente_id"));
        String nif = rs.getString("nif");
        String nombre = rs.getString("nombre");
        String apellidos = rs.getString("apellidos");

        LocalDate fechaNacimiento;
        if (mode == DatabaseMode.HUB) {
            LocalDate date = rs.getObject("fecha_nacimiento", LocalDate.class);
            if (date == null) {
                throw new SQLException("Missing fecha_nacimiento for paciente " + pacienteId);
            }
            fechaNacimiento = date;
        } else {
            fechaNacimiento = ContractDateFormats.parseDate(rs.getString("fecha_nacimiento"), "fecha_nacimiento");
        }

        Sexo sexo = Sexo.fromCode(rs.getString("sexo"));

        Paciente.Builder builder = Paciente.builder()
                .pacienteId(pacienteId)
                .nif(nif)
                .nombre(nombre)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .sexo(sexo);

        String telefono = rs.getString("telefono");
        if (telefono != null) {
            builder.telefono(telefono);
        }

        String email = rs.getString("email");
        if (email != null) {
            builder.email(email);
        }

        String empresaId = rs.getString("empresa_id");
        if (empresaId != null) {
            builder.empresaId(UUID.fromString(empresaId));
        }

        String centroId = rs.getString("centro_id");
        if (centroId != null) {
            builder.centroId(UUID.fromString(centroId));
        }

        String externoRef = rs.getString("externo_ref");
        if (externoRef != null) {
            builder.externoRef(externoRef);
        }

        if (mode == DatabaseMode.HUB) {
            OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
            if (createdAt != null) {
                builder.createdAt(createdAt);
            }
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            if (updatedAt != null) {
                builder.updatedAt(updatedAt);
            }
        } else {
            String updatedAt = rs.getString("updated_at");
            if (updatedAt != null) {
                builder.updatedAt(ContractDateFormats.parseDateTime(updatedAt, "updated_at"));
            }
        }

        return builder.build();
    }

    private String tableName() {
        return mode == DatabaseMode.LOCAL ? "pacientes" : "dbo.pacientes";
    }

    private String selectColumns() {
        if (mode == DatabaseMode.LOCAL) {
            return "paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email, empresa_id, centro_id, externo_ref, updated_at";
        }
        return "paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email, empresa_id, centro_id, externo_ref, created_at, updated_at";
    }
}
