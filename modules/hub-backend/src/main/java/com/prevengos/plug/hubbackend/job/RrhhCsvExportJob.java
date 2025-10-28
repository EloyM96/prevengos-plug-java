package com.prevengos.plug.hubbackend.job;

import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class RrhhCsvExportJob {

    private static final Logger logger = LoggerFactory.getLogger(RrhhCsvExportJob.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PacienteGateway pacienteGateway;
    private final CuestionarioGateway cuestionarioGateway;
    private final CsvFileWriter csvFileWriter;
    private final RrhhExportProperties properties;

    public RrhhCsvExportJob(PacienteGateway pacienteGateway,
                            CuestionarioGateway cuestionarioGateway,
                            CsvFileWriter csvFileWriter,
                            RrhhExportProperties properties) {
        this.pacienteGateway = pacienteGateway;
        this.cuestionarioGateway = cuestionarioGateway;
        this.csvFileWriter = csvFileWriter;
        this.properties = properties;
    }

    @Scheduled(cron = "${hub.jobs.rrhh-export.cron:0 0 3 * * *}")
    public void scheduledRun() {
        RrhhExportResult result = runExport("scheduled");
        logger.info("Exportación RRHH programada completada (pacientes={}, cuestionarios={}, destino={})",
                result.pacientesCount(), result.cuestionariosCount(), result.targetDir());
    }

    public RrhhExportResult runExport(String trigger) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime since = now.minusHours(properties.getLookbackHours());
        logger.info("Iniciando exportación RRHH (trigger={}, since={})", trigger, since);
        Path targetDir = properties.getBaseDir()
                .resolve(DATE_FORMATTER.format(now))
                .resolve(properties.getProcessName());

        int pacientesCount = exportPacientes(targetDir.resolve("pacientes.csv"), since);
        int cuestionariosCount = exportCuestionarios(targetDir.resolve("cuestionarios.csv"), since);
        logger.info("Exportación RRHH completada (trigger={}, targetDir={}, pacientes={}, cuestionarios={})",
                trigger, targetDir, pacientesCount, cuestionariosCount);
        return new RrhhExportResult(targetDir, pacientesCount, cuestionariosCount);
    }

    private int exportPacientes(Path file, OffsetDateTime since) {
        List<PacienteCsvRow> rows = pacienteGateway.fetchForRrhhExport(since);
        List<String> headers = List.of("paciente_id", "nif", "nombre", "apellidos", "sexo",
                "updated_at", "telefono", "email", "empresa_id", "centro_id", "externo_ref");
        List<List<String>> values = new ArrayList<>();
        for (PacienteCsvRow row : rows) {
            values.add(List.of(
                    toString(row.pacienteId()),
                    row.nif(),
                    row.nombre(),
                    row.apellidos(),
                    row.sexo(),
                    toString(row.updatedAt()),
                    row.telefono(),
                    row.email(),
                    toString(row.empresaId()),
                    toString(row.centroId()),
                    row.externoRef()
            ));
        }
        csvFileWriter.writeCsv(file, headers, values);
        csvFileWriter.writeChecksum(file);
        return rows.size();
    }

    private int exportCuestionarios(Path file, OffsetDateTime since) {
        List<CuestionarioCsvRow> rows = cuestionarioGateway.fetchForRrhhExport(since);
        List<String> headers = List.of("cuestionario_id", "paciente_id", "plantilla_codigo", "estado", "updated_at");
        List<List<String>> values = new ArrayList<>();
        for (CuestionarioCsvRow row : rows) {
            values.add(List.of(
                    toString(row.cuestionarioId()),
                    toString(row.pacienteId()),
                    row.plantillaCodigo(),
                    row.estado(),
                    toString(row.updatedAt())
            ));
        }
        csvFileWriter.writeCsv(file, headers, values);
        csvFileWriter.writeChecksum(file);
        return rows.size();
    }

    private String toString(Object value) {
        return value != null ? value.toString() : "";
    }

    public record RrhhExportResult(Path targetDir, int pacientesCount, int cuestionariosCount) {
    }
}
