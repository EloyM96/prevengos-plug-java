package com.prevengos.plug.hubbackend.job;

import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.filetransfer.FileTransferClient;
import com.prevengos.plug.gateway.filetransfer.FileTransferProtocol;
import com.prevengos.plug.gateway.filetransfer.FileTransferRequest;
import com.prevengos.plug.gateway.filetransfer.SftpConnectionDetails;
import com.prevengos.plug.gateway.filetransfer.SmbConnectionDetails;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.gateway.sqlserver.RrhhAuditGateway;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.shared.contracts.v1.Cuestionario;
import com.prevengos.plug.shared.contracts.v1.Paciente;
import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.FileDropLogRecord;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.RrhhExportRecord;
import com.prevengos.plug.shared.time.ContractDateFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class RrhhCsvExportJob {

    private static final Logger logger = LoggerFactory.getLogger(RrhhCsvExportJob.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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

    @Scheduled(cron = "${hub.jobs.rrhh-export.cron:0 0 3 * * *}")
    public void scheduledRun() {
        RrhhExportResult result = runExport("scheduled");
        logger.info("Exportación RRHH programada completada (traceId={}, pacientes={}, cuestionarios={}, remoto={})",
                result.traceId(), result.pacientesCount(), result.cuestionariosCount(), result.remotePath());
    }

    public RrhhExportResult runExport(String trigger) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime since = now.minusHours(properties.getLookbackHours());
        UUID traceId = UUID.randomUUID();
        String relativePath = buildRelativePath(now);
        Path stagingDir = resolveTargetDir(now);
        logger.info("Iniciando exportación RRHH (trigger={}, traceId={}, since={}, staging={})",
                trigger, traceId, since, stagingDir);

        ensureDirectory(stagingDir);

        ExportedCsv pacientesExport = exportPacientes(stagingDir.resolve("pacientes.csv"), since);
        ExportedCsv cuestionariosExport = exportCuestionarios(stagingDir.resolve("cuestionarios.csv"), since);
        List<ExportedCsv> exports = List.of(pacientesExport, cuestionariosExport);

        Path archiveDir = copyToArchive(stagingDir, relativePath, traceId);
        String remoteDir = joinRemote(properties.getDelivery().getRemoteDir(), relativePath);

        int pacientesCount = pacientesExport.rowCount();
        int cuestionariosCount = cuestionariosExport.rowCount();

        try {
            if (properties.getDelivery().isEnabled()) {
                deliver(traceId, remoteDir, exports);
            } else {
                logger.info("Entrega remota deshabilitada para exportación RRHH (traceId={})", traceId);
            }

            auditGateway.recordExport(new RrhhExportRecord(
                    UUID.randomUUID(),
                    traceId,
                    trigger,
                    properties.getProcessName(),
                    properties.getOrigin(),
                    properties.getOperator(),
                    remoteDir,
                    archiveDir.toString(),
                    pacientesCount,
                    cuestionariosCount,
                    "SUCCESS",
                    null,
                    now
            ));

            logger.info("Exportación RRHH completada (trigger={}, traceId={}, pacientes={}, cuestionarios={}, remoto={}, archivo={})",
                    trigger, traceId, pacientesCount, cuestionariosCount, remoteDir, archiveDir);
            return new RrhhExportResult(traceId, remoteDir, stagingDir, archiveDir, pacientesCount, cuestionariosCount);
        } catch (Exception exception) {
            auditGateway.recordExport(new RrhhExportRecord(
                    UUID.randomUUID(),
                    traceId,
                    trigger,
                    properties.getProcessName(),
                    properties.getOrigin(),
                    properties.getOperator(),
                    remoteDir,
                    archiveDir.toString(),
                    pacientesCount,
                    cuestionariosCount,
                    "FAILED",
                    exception.getMessage(),
                    now
            ));
            logger.error("Error durante exportación RRHH (traceId={})", traceId, exception);
            throw exception;
        }
    }

    private ExportedCsv exportPacientes(Path file, OffsetDateTime since) {
        List<PacienteCsvRow> rows = pacienteGateway.fetchForRrhhExport(since);
        List<List<String>> values = new ArrayList<>(rows.size());
        for (PacienteCsvRow row : rows) {
            Paciente paciente = Paciente.builder()
                    .pacienteId(row.pacienteId())
                    .nif(row.nif())
                    .nombre(row.nombre())
                    .apellidos(row.apellidos())
                    .fechaNacimiento(row.fechaNacimiento())
                    .sexo(Paciente.Sexo.fromCode(row.sexo()))
                    .telefono(row.telefono())
                    .email(row.email())
                    .empresaId(row.empresaId())
                    .centroId(row.centroId())
                    .externoRef(row.externoRef())
                    .createdAt(row.createdAt())
                    .updatedAt(row.updatedAt())
                    .build();
            CsvRecord record = paciente.toCsvRecord();
            values.add(new ArrayList<>(record.values()));
        }
        csvFileWriter.writeCsv(file, Paciente.CSV_HEADERS, values);
        String checksum = csvFileWriter.writeChecksum(file);
        logger.info("CSV pacientes exportado (file={}, registros={})", file, rows.size());
        return new ExportedCsv(file, rows.size(), checksum);
    }

    private ExportedCsv exportCuestionarios(Path file, OffsetDateTime since) {
        List<CuestionarioCsvRow> rows = cuestionarioGateway.fetchForRrhhExport(since);
        List<List<String>> values = new ArrayList<>(rows.size());
        for (CuestionarioCsvRow row : rows) {
            Map<String, String> ordered = new LinkedHashMap<>();
            ordered.put("cuestionario_id", row.cuestionarioId().toString());
            ordered.put("paciente_id", row.pacienteId().toString());
            ordered.put("plantilla_codigo", row.plantillaCodigo());
            ordered.put("estado", row.estado());
            ordered.put("respuestas", defaultJson(row.respuestas(), "[]"));
            ordered.put("firmas", blankToNull(row.firmas()));
            ordered.put("adjuntos", blankToNull(row.adjuntos()));
            ordered.put("created_at", ContractDateFormats.formatDateTime(row.createdAt()));
            ordered.put("updated_at", ContractDateFormats.formatDateTime(row.updatedAt()));
            CsvRecord record = CsvRecord.of(ordered);
            CsvRecord normalized = Cuestionario.fromCsvRecord(record).toCsvRecord();
            values.add(new ArrayList<>(normalized.values()));
        }
        csvFileWriter.writeCsv(file, Cuestionario.CSV_HEADERS, values);
        String checksum = csvFileWriter.writeChecksum(file);
        logger.info("CSV cuestionarios exportado (file={}, registros={})", file, rows.size());
        return new ExportedCsv(file, rows.size(), checksum);
    }

    private void deliver(UUID traceId, String remoteDir, List<ExportedCsv> exports) {
        RrhhExportProperties.DeliveryProperties delivery = properties.getDelivery();
        FileTransferProtocol protocol = delivery.getProtocol();
        SftpConnectionDetails sftpDetails = protocol == FileTransferProtocol.SFTP
                ? toSftpDetails(delivery.getSftp())
                : null;
        SmbConnectionDetails smbDetails = protocol == FileTransferProtocol.SMB
                ? toSmbDetails(delivery.getSmb())
                : null;

        for (ExportedCsv export : exports) {
            if (export.rowCount() == 0) {
                logger.info("Se omite subida de {} porque no contiene registros", export.path().getFileName());
                continue;
            }
            String fileName = export.path().getFileName().toString();
            FileTransferRequest request = new FileTransferRequest(protocol, remoteDir, fileName, sftpDetails, smbDetails);
            OffsetDateTime eventTime = OffsetDateTime.now(ZoneOffset.UTC);
            String remotePath = joinRemote(remoteDir, fileName);
            try {
                fileTransferClient.upload(export.path(), request);
                auditGateway.recordFileDrop(new FileDropLogRecord(
                        UUID.randomUUID(),
                        traceId,
                        properties.getProcessName(),
                        protocol.name(),
                        remotePath,
                        fileName,
                        export.checksum(),
                        "SUCCESS",
                        null,
                        eventTime
                ));
            } catch (Exception uploadError) {
                auditGateway.recordFileDrop(new FileDropLogRecord(
                        UUID.randomUUID(),
                        traceId,
                        properties.getProcessName(),
                        protocol.name(),
                        remotePath,
                        fileName,
                        export.checksum(),
                        "FAILED",
                        uploadError.getMessage(),
                        eventTime
                ));
                throw uploadError;
            }
        }
    }

    private Path copyToArchive(Path sourceDir, String relativePath, UUID traceId) {
        Path archiveDir = properties.getArchiveDir()
                .resolve(relativePath)
                .resolve(traceId.toString());
        try {
            try (Stream<Path> stream = Files.walk(sourceDir)) {
                for (Path source : stream.toList()) {
                    Path target = archiveDir.resolve(sourceDir.relativize(source).toString());
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            logger.info("Copia de archivo RRHH almacenada en {}", archiveDir);
            return archiveDir;
        } catch (IOException e) {
            throw new IllegalStateException("Error copiando exportación RRHH a archivo " + archiveDir, e);
        }
    }

    private void ensureDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo preparar directorio de exportación " + directory, e);
        }
    }

    private Path resolveTargetDir(OffsetDateTime now) {
        return properties.getBaseDir()
                .resolve(DATE_FORMATTER.format(now))
                .resolve(properties.getProcessName())
                .resolve(properties.getOrigin());
    }

    private String buildRelativePath(OffsetDateTime now) {
        return DATE_FORMATTER.format(now) + "/" + properties.getProcessName() + "/" + properties.getOrigin();
    }

    private SftpConnectionDetails toSftpDetails(RrhhExportProperties.SftpProperties properties) {
        if (properties == null) {
            return null;
        }
        return new SftpConnectionDetails(
                properties.getHost(),
                properties.getPort(),
                properties.getUsername(),
                properties.getPassword(),
                properties.isStrictHostKeyChecking(),
                properties.getKnownHosts()
        );
    }

    private SmbConnectionDetails toSmbDetails(RrhhExportProperties.SmbProperties properties) {
        if (properties == null) {
            return null;
        }
        return new SmbConnectionDetails(
                properties.getHost(),
                properties.getShare(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getDomain()
        );
    }

    private String joinRemote(String base, String relative) {
        String normalizedBase = normalizeBase(base);
        String normalizedRelative = normalizeRelative(relative);
        if (normalizedBase.isEmpty()) {
            return normalizedRelative;
        }
        if (normalizedRelative.isEmpty()) {
            return normalizedBase;
        }
        return normalizedBase + "/" + normalizedRelative;
    }

    private String normalizeBase(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replace('\\', '/');
        normalized = normalized.replaceAll("/+", "/");
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String normalizeRelative(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replace('\\', '/');
        normalized = normalized.replaceAll("/+", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String defaultJson(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private record ExportedCsv(Path path, int rowCount, String checksum) {
    }

    public record RrhhExportResult(UUID traceId,
                                   String remotePath,
                                   Path stagingDir,
                                   Path archiveDir,
                                   int pacientesCount,
                                   int cuestionariosCount) {
    }
}
