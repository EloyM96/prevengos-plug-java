package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.mapper.PacienteRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcPacienteGateway implements PacienteGateway {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PacienteRowMapper rowMapper = new PacienteRowMapper();

    public JdbcPacienteGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsert(PacienteDto paciente, OffsetDateTime lastModified, long syncToken) {
        OffsetDateTime effectiveLastModified = lastModified != null ? lastModified : OffsetDateTime.now();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paciente_id", paciente.pacienteId())
                .addValue("nif", paciente.nif())
                .addValue("nombre", paciente.nombre())
                .addValue("apellidos", paciente.apellidos())
                .addValue("fecha_nacimiento", paciente.fechaNacimiento())
                .addValue("sexo", paciente.sexo())
                .addValue("telefono", paciente.telefono())
                .addValue("email", paciente.email())
                .addValue("empresa_id", uuidToString(paciente.empresaId()))
                .addValue("centro_id", uuidToString(paciente.centroId()))
                .addValue("externo_ref", paciente.externoRef())
                .addValue("created_at", paciente.createdAt() == null ? OffsetDateTime.now() : paciente.createdAt())
                .addValue("updated_at", paciente.updatedAt() == null ? OffsetDateTime.now() : paciente.updatedAt())
                .addValue("last_modified", effectiveLastModified)
                .addValue("sync_token", syncToken);

        String sql = """
                MERGE INTO dbo.pacientes AS target
                USING (SELECT :paciente_id AS paciente_id) AS source
                ON target.paciente_id = source.paciente_id
                WHEN MATCHED THEN
                    UPDATE SET nif = :nif,
                               nombre = :nombre,
                               apellidos = :apellidos,
                               fecha_nacimiento = :fecha_nacimiento,
                               sexo = :sexo,
                               telefono = :telefono,
                               email = :email,
                               empresa_id = :empresa_id,
                               centro_id = :centro_id,
                               externo_ref = :externo_ref,
                               updated_at = :updated_at,
                               last_modified = :last_modified,
                               sync_token = :sync_token
                WHEN NOT MATCHED THEN
                    INSERT (paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email, empresa_id,
                            centro_id, externo_ref, created_at, updated_at, last_modified, sync_token)
                    VALUES (:paciente_id, :nif, :nombre, :apellidos, :fecha_nacimiento, :sexo, :telefono, :email,
                            :empresa_id, :centro_id, :externo_ref, :created_at, :updated_at, :last_modified, :sync_token);
                """;

        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<PacienteDto> fetchAfterToken(long token, int limit) {
        String sql = """
                SELECT * FROM dbo.pacientes
                WHERE sync_token > :token
                ORDER BY sync_token ASC
                OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
                """;
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource()
                        .addValue("token", token)
                        .addValue("limit", limit),
                rowMapper);
    }

    @Override
    public List<PacienteCsvRow> fetchForRrhhExport(OffsetDateTime since) {
        String sql = """
                SELECT paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email, empresa_id,
                       centro_id, externo_ref, created_at, last_modified
                FROM dbo.pacientes
                WHERE last_modified >= :since
                ORDER BY last_modified ASC
                """;
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("since", since),
                (rs, rowNum) -> new PacienteCsvRow(
                        UUID.fromString(rs.getString("paciente_id")),
                        rs.getString("nif"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getObject("fecha_nacimiento", java.time.LocalDate.class),
                        rs.getString("sexo"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        getUuid(rs.getString("empresa_id")),
                        getUuid(rs.getString("centro_id")),
                        rs.getString("externo_ref"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("last_modified", OffsetDateTime.class)
                ));
    }

    @Override
    public PacienteDto findById(UUID pacienteId) {
        String sql = "SELECT * FROM dbo.pacientes WHERE paciente_id = :paciente_id";
        List<PacienteDto> pacientes = jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("paciente_id", pacienteId),
                rowMapper);
        return pacientes.isEmpty() ? null : pacientes.getFirst();
    }

    private UUID getUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private String uuidToString(UUID value) {
        return value == null ? null : value.toString();
    }
}
