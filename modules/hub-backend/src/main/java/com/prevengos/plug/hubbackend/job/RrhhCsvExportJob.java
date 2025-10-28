package com.prevengos.plug.hubbackend.job;

import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.filetransfer.FileTransferClient;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.gateway.sqlserver.RrhhAuditGateway;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.rrhh.FileDropRecord;
import com.prevengos.plug.shared.rrhh.RrhhExportRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class RrhhCsvExportJob {

    private static final Logger log = LoggerFactory.getLogger(RrhhCsvExportJob.class);

    private final PacienteGateway pacienteGateway;
    private final CuestionarioGateway cuestionarioGateway;
    private final CsvFileWriter csvFileWriter;
    private final FileTransferClient fileTransferClient;
    private final RrhhAuditGateway auditGateway;
    private final RrhhExportProperties properties;

    public RrhhCsvExportJob(PacienteGateway pacienteGateway,
                            CuestionarioGateway cuestionarioGateway,
                            CsvFileWriter csvFileWriter,
                            FileTransferClient fileTransferClient,
                            RrhhAuditGateway auditGateway,
                            RrhhExportProperties properties) {
        this.pacienteGateway = pacienteGateway;
        this.cuestionarioGateway = cuestionarioGateway;
        this.csvFileWriter = csvFileWriter;
        this.fileTransferClient = fileTransferClient;
        this.auditGateway = auditGateway;
        this.properties = properties;
    }

    public RrhhExportResult runExport(String triggerType) throws Exception {
        OffsetDateTime since = OffsetDateTime.now().minusHours(properties.getLookbackHours());
        List<PacienteCsvRow> pacientes = pacienteGateway.fetchForRrhhExport(since);
        List<CuestionarioCsvRow> cuestionarios = cuestionarioGateway.fetchForRrhhExport(since);
        UUID traceId = UUID.randomUUID();
        Path baseDir = ensureDirectory(properties.getBaseDir());
        Path stagingDir = baseDir.resolve(traceId.toString());
        Files.createDirectories(stagingDir);

        Path pacientesCsv = csvFileWriter.writePacientes(stagingDir, pacientes);
        Path cuestionariosCsv = csvFileWriter.writeCuestionarios(stagingDir, cuestionarios);

        Path archiveDir = ensureDirectory(properties.getArchiveDir()).resolve(traceId.toString());
        Files.createDirectories(archiveDir);
        Files.copy(pacientesCsv, archiveDir.resolve(pacientesCsv.getFileName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(pacientesCsv.resolveSibling(pacientesCsv.getFileName() + ".sha256"),
                archiveDir.resolve(pacientesCsv.getFileName() + ".sha256"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(cuestionariosCsv, archiveDir.resolve(cuestionariosCsv.getFileName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(cuestionariosCsv.resolveSibling(cuestionariosCsv.getFileName() + ".sha256"),
                archiveDir.resolve(cuestionariosCsv.getFileName() + ".sha256"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        String remoteDir = properties.getDelivery().getRemoteDir();
        if (properties.getDelivery().isEnabled() && remoteDir != null) {
            fileTransferClient.deliver(pacientesCsv, remoteDir + "/" + pacientesCsv.getFileName());
            fileTransferClient.deliver(cuestionariosCsv, remoteDir + "/" + cuestionariosCsv.getFileName());
            logFileDrop(traceId, pacientesCsv, remoteDir);
            logFileDrop(traceId, cuestionariosCsv, remoteDir);
        }

        UUID exportId = UUID.randomUUID();
        auditGateway.recordExport(new RrhhExportRecord(
                exportId,
                traceId,
                triggerType,
                properties.getProcessName(),
                properties.getOrigin(),
                properties.getOperator(),
                remoteDir,
                archiveDir.toString(),
                pacientes.size(),
                cuestionarios.size(),
                "COMPLETED",
                null,
                OffsetDateTime.now()
        ));

        return new RrhhExportResult(traceId, stagingDir, archiveDir, pacientes.size(), cuestionarios.size());
    }

    private void logFileDrop(UUID traceId, Path file, String remoteDir) {
        auditGateway.recordFileDrop(new FileDropRecord(
                UUID.randomUUID(),
                traceId,
                properties.getProcessName(),
                properties.getDelivery().getProtocol(),
                remoteDir,
                file.getFileName().toString(),
                null,
                "DELIVERED",
                null,
                OffsetDateTime.now()
        ));
    }

    private Path ensureDirectory(Path path) throws IOException {
        if (path == null) {
            throw new IllegalStateException("Directorio no configurado para exportación RRHH");
        }
        Files.createDirectories(path);
        return path;
    }

    public record RrhhExportResult(UUID traceId,
                                   Path stagingDir,
                                   Path archiveDir,
                                   int pacientesCount,
                                   int cuestionariosCount) {
    }
}
