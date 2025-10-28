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
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Generates CSV exports for the RRHH integration.
 */
public class RrhhCsvExportJob {

    private static final DateTimeFormatter DIRECTORY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withLocale(Locale.ROOT);

    private final PacienteGateway pacienteGateway;
    private final CuestionarioGateway cuestionarioGateway;
    private final CsvFileWriter csvFileWriter;
    private final FileTransferClient transferClient;
    private final RrhhAuditGateway auditGateway;
    private final RrhhExportProperties properties;

    public RrhhCsvExportJob(
            PacienteGateway pacienteGateway,
            CuestionarioGateway cuestionarioGateway,
            CsvFileWriter csvFileWriter,
            FileTransferClient transferClient,
            RrhhAuditGateway auditGateway,
            RrhhExportProperties properties
    ) {
        this.pacienteGateway = Objects.requireNonNull(pacienteGateway, "pacienteGateway");
        this.cuestionarioGateway = Objects.requireNonNull(cuestionarioGateway, "cuestionarioGateway");
        this.csvFileWriter = Objects.requireNonNull(csvFileWriter, "csvFileWriter");
        this.transferClient = Objects.requireNonNull(transferClient, "transferClient");
        this.auditGateway = Objects.requireNonNull(auditGateway, "auditGateway");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public RrhhExportResult runExport(String exportLabel) throws IOException {
        OffsetDateTime exportedAt = OffsetDateTime.now();
        OffsetDateTime since = exportedAt.minusHours(Math.max(0, properties.getLookbackHours()));

        List<PacienteCsvRow> pacientes = pacienteGateway.fetchForRrhhExport(since);
        List<CuestionarioCsvRow> cuestionarios = cuestionarioGateway.fetchForRrhhExport(since);

        Path stagingDir = prepareStagingDirectory(exportLabel, exportedAt);
        Path pacientesFile = stagingDir.resolve("pacientes.csv");
        csvFileWriter.writeCsv(
                pacientesFile,
                PacienteCsvRow.CSV_HEADERS,
                pacientes.stream().map(PacienteCsvRow::toCsvRow).toList()
        );
        csvFileWriter.writeChecksum(pacientesFile);

        Path cuestionariosFile = stagingDir.resolve("cuestionarios.csv");
        csvFileWriter.writeCsv(
                cuestionariosFile,
                CuestionarioCsvRow.CSV_HEADERS,
                cuestionarios.stream().map(CuestionarioCsvRow::toCsvRow).toList()
        );
        csvFileWriter.writeChecksum(cuestionariosFile);

        Path archiveDir = archiveFiles(stagingDir);

        deliverFiles(stagingDir);

        auditGateway.recordSuccessfulExport(
                exportLabel,
                since,
                exportedAt,
                pacientes.size(),
                cuestionarios.size(),
                archiveDir,
                properties.getDelivery().getRemoteDir(),
                properties.getDelivery().getProtocol() != null ? properties.getDelivery().getProtocol().name() : null,
                properties.getProcessName()
        );

        return new RrhhExportResult(stagingDir, archiveDir, since, exportedAt, pacientes.size(), cuestionarios.size());
    }

    private Path prepareStagingDirectory(String exportLabel, OffsetDateTime exportedAt) throws IOException {
        Path baseDir = properties.getBaseDir();
        if (baseDir == null) {
            baseDir = Files.createTempDirectory("rrhh-export");
        } else {
            Files.createDirectories(baseDir);
        }

        String safeLabel = (exportLabel == null || exportLabel.isBlank())
                ? "export"
                : exportLabel.replaceAll("[^A-Za-z0-9-_]", "-");
        String directoryName = DIRECTORY_FORMATTER.format(exportedAt) + "-" + safeLabel;
        return Files.createDirectories(baseDir.resolve(directoryName));
    }

    private Path archiveFiles(Path stagingDir) throws IOException {
        Path archiveBase = properties.getArchiveDir();
        if (archiveBase == null) {
            archiveBase = Files.createTempDirectory("rrhh-export-archive");
        }
        Files.createDirectories(archiveBase);

        for (String fileName : List.of("pacientes.csv", "pacientes.csv.sha256", "cuestionarios.csv", "cuestionarios.csv.sha256")) {
            Path source = stagingDir.resolve(fileName);
            if (Files.exists(source)) {
                Files.copy(source, archiveBase.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return archiveBase;
    }

    private void deliverFiles(Path stagingDir) {
        var delivery = properties.getDelivery();
        if (delivery == null || !delivery.isEnabled()) {
            return;
        }
        FileTransferProtocol protocol = delivery.getProtocol();
        if (protocol == null) {
            throw new IllegalStateException("Delivery protocol must be configured when delivery is enabled");
        }

        SftpConnectionDetails sftpDetails = null;
        if (protocol == FileTransferProtocol.SFTP) {
            var sftp = delivery.getSftp();
            sftpDetails = new SftpConnectionDetails(
                    sftp.getHost(),
                    sftp.getPort(),
                    sftp.getUsername(),
                    sftp.getPassword(),
                    sftp.isStrictHostKeyChecking(),
                    sftp.getKnownHosts()
            );
        }

        SmbConnectionDetails smbDetails = null;
        if (protocol == FileTransferProtocol.SMB) {
            var smb = delivery.getSmb();
            smbDetails = new SmbConnectionDetails(
                    smb.getHost(),
                    smb.getShare(),
                    smb.getUsername(),
                    smb.getPassword(),
                    smb.getDomain()
            );
        }

        for (String fileName : List.of("pacientes.csv", "pacientes.csv.sha256", "cuestionarios.csv", "cuestionarios.csv.sha256")) {
            Path file = stagingDir.resolve(fileName);
            if (Files.exists(file)) {
                FileTransferRequest request = new FileTransferRequest(
                        protocol,
                        delivery.getRemoteDir(),
                        file.getFileName().toString(),
                        sftpDetails,
                        smbDetails
                );
                transferClient.upload(file, request);
            }
        }
    }

    public record RrhhExportResult(
            Path stagingDir,
            Path archiveDir,
            OffsetDateTime since,
            OffsetDateTime exportedAt,
            int pacientesCount,
            int cuestionariosCount
    ) {
    }
}
