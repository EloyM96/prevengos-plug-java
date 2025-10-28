package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteJdbcMapper;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcPacienteGateway implements PacienteGateway {

    private static final String UPSERT_SQL = """
            MERGE INTO pacientes AS target
            USING (SELECT :paciente_id AS paciente_id) AS source
            ON target.paciente_id = source.paciente_id
            WHEN MATCHED THEN UPDATE SET
                nif = :nif,
                nombre = :nombre,
                apellidos = :apellidos,
                fecha_nacimiento = :fecha_nacimiento,
                sexo = :sexo,
                telefono = :telefono,
                email = :email,
                empresa_id = :empresa_id,
                centro_id = :centro_id,
                externo_ref = :externo_ref,
                created_at = :created_at,
                updated_at = :updated_at,
                last_modified = :last_modified,
                sync_token = :sync_token
            WHEN NOT MATCHED THEN
                INSERT (paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email,
                        empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token)
                VALUES (:paciente_id, :nif, :nombre, :apellidos, :fecha_nacimiento, :sexo, :telefono, :email,
                        :empresa_id, :centro_id, :externo_ref, :created_at, :updated_at, :last_modified, :sync_token);
            """;

    private static final String SELECT_UPDATED_SQL = """
            SELECT paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email,
                   empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token
            FROM pacientes
            WHERE (:since IS NULL OR last_modified >= :since)
            ORDER BY last_modified ASC
            OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY;
            """;

    private static final String SELECT_BY_IDS_SQL = """
            SELECT paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, telefono, email,
                   empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token
            FROM pacientes
            WHERE paciente_id IN (:ids)
            ORDER BY last_modified ASC;
            """;

    private static final String SELECT_RRHH_VIEW_SQL = """
            SELECT paciente_id, nif, nombre, apellidos, sexo, updated_at, telefono, email,
                   empresa_id, centro_id, externo_ref
            FROM dbo.vw_prl_pacientes
            WHERE (:since IS NULL OR updated_at >= :since)
            ORDER BY updated_at ASC;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<PacienteRecord> pacienteRowMapper = new PacienteRecordRowMapper();
    private final RowMapper<PacienteCsvRow> pacienteCsvRowMapper = new PacienteCsvRowMapper();

    public JdbcPacienteGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsertPaciente(PacienteRecord paciente) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paciente_id", paciente.pacienteId())
                .addValue("nif", paciente.nif())
                .addValue("nombre", paciente.nombre())
                .addValue("apellidos", paciente.apellidos())
                .addValue("fecha_nacimiento", paciente.fechaNacimiento())
                .addValue("sexo", paciente.sexo())
                .addValue("telefono", paciente.telefono())
                .addValue("email", paciente.email())
                .addValue("empresa_id", paciente.empresaId())
                .addValue("centro_id", paciente.centroId())
                .addValue("externo_ref", paciente.externoRef())
                .addValue("created_at", paciente.createdAt())
                .addValue("updated_at", paciente.updatedAt())
                .addValue("last_modified", paciente.lastModified())
                .addValue("sync_token", paciente.syncToken());
        jdbcTemplate.update(UPSERT_SQL, params);
    }

    @Override
    public List<PacienteRecord> findUpdatedSince(OffsetDateTime since, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("since", since)
                .addValue("limit", limit);
        return jdbcTemplate.query(SELECT_UPDATED_SQL, params, pacienteRowMapper);
    }

    @Override
    public List<PacienteRecord> findByIds(List<UUID> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return Collections.emptyList();
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", identifiers);
        return jdbcTemplate.query(SELECT_BY_IDS_SQL, params, pacienteRowMapper);
    }

    @Override
    public List<PacienteCsvRow> fetchForRrhhExport(OffsetDateTime updatedSince) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("since", updatedSince);
        return jdbcTemplate.query(SELECT_RRHH_VIEW_SQL, params, pacienteCsvRowMapper);
    }

    private static class PacienteRecordRowMapper implements RowMapper<PacienteRecord> {
        @Override
        public PacienteRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return PacienteJdbcMapper.mapRecord(rs);
        }
    }

    private static class PacienteCsvRowMapper implements RowMapper<PacienteCsvRow> {
        @Override
        public PacienteCsvRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return PacienteJdbcMapper.mapCsvRow(rs);
        }
    }
}
