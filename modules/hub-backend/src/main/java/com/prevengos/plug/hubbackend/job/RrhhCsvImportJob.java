package com.prevengos.plug.hubbackend.job;

import com.prevengos.plug.gateway.csv.CsvFileReader;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.hubbackend.config.RrhhImportProperties;
import com.prevengos.plug.shared.contracts.v1.Cuestionario;
import com.prevengos.plug.shared.contracts.v1.Paciente;
import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RrhhCsvImportJob {

    private static final Logger logger = LoggerFactory.getLogger(RrhhCsvImportJob.class);

    private final PacienteGateway pacienteGateway;
    private final CuestionarioGateway cuestionarioGateway;
    private final CsvFileReader csvFileReader;
    private final RrhhImportProperties properties;

    public RrhhCsvImportJob(PacienteGateway pacienteGateway,
                            CuestionarioGateway cuestionarioGateway,
                            CsvFileReader csvFileReader,
                            RrhhImportProperties properties) {
        this.pacienteGateway = pacienteGateway;
        this.cuestionarioGateway = cuestionarioGateway;
        this.csvFileReader = csvFileReader;
        this.properties = properties;
    }

    @Scheduled(cron = "${hub.jobs.rrhh-import.cron:0 30 3 * * *}")
    public void scheduledRun() {
        RrhhImportReport report = processInbox("scheduled");
        logger.info("Importación RRHH programada completada", report);
    }

    public RrhhImportReport processInbox(String trigger) {
        logger.info("Iniciando importación RRHH (trigger={}, inbox={})", trigger, properties.getInboxDir());
        if (!Files.exists(properties.getInboxDir())) {
            logger.warn("La ruta de entrada {} no existe", properties.getInboxDir());
            return new RrhhImportReport(0, 0, 0, List.of(), List.of());
        }

        List<Path> dropDirs = listDropDirectories();
        List<String> archived = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        int processedDrops = 0;
        int pacientesImported = 0;
        int cuestionariosImported = 0;

        for (Path dropDir : dropDirs) {
            if (processedDrops >= properties.getMaxDropsPerRun()) {
                logger.info("Límite de {} drops alcanzado", properties.getMaxDropsPerRun());
                break;
            }
            Path relative = properties.getInboxDir().relativize(dropDir);
            try {
                ImportCounters counters = importDrop(dropDir);
                pacientesImported += counters.pacientes();
                cuestionariosImported += counters.cuestionarios();
                moveDirectory(dropDir, properties.getArchiveDir().resolve(relative));
                archived.add(relative.toString());
                processedDrops++;
                logger.info("Drop {} procesado correctamente (pacientes={}, cuestionarios={})",
                        relative, counters.pacientes(), counters.cuestionarios());
            } catch (Exception e) {
                logger.error("Error procesando drop {}", dropDir, e);
                moveDirectorySilently(dropDir, properties.getErrorDir().resolve(relative));
                failed.add(relative + ": " + e.getMessage());
            }
        }

        return new RrhhImportReport(processedDrops, pacientesImported, cuestionariosImported,
                List.copyOf(archived), List.copyOf(failed));
    }

    private List<Path> listDropDirectories() {
        try {
            return Files.walk(properties.getInboxDir(), 2)
                    .filter(Files::isDirectory)
                    .filter(dir -> containsRrhhFiles(dir))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Error listing RRHH inbox", e);
        }
    }

    private boolean containsRrhhFiles(Path dir) {
        Path pacientes = dir.resolve("pacientes.csv");
        Path cuestionarios = dir.resolve("cuestionarios.csv");
        return Files.exists(pacientes) || Files.exists(cuestionarios);
    }

    private ImportCounters importDrop(Path dropDir) throws IOException, NoSuchAlgorithmException {
        int pacientesCount = 0;
        int cuestionariosCount = 0;
        Path pacientesFile = dropDir.resolve("pacientes.csv");
        if (Files.exists(pacientesFile)) {
            verifyChecksum(pacientesFile);
            pacientesCount = importPacientes(pacientesFile);
        }
        Path cuestionariosFile = dropDir.resolve("cuestionarios.csv");
        if (Files.exists(cuestionariosFile)) {
            verifyChecksum(cuestionariosFile);
            cuestionariosCount = importCuestionarios(cuestionariosFile);
        }
        return new ImportCounters(pacientesCount, cuestionariosCount);
    }

    private int importPacientes(Path file) {
        List<CsvRecord> records = csvFileReader.readCsv(file);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (CsvRecord record : records) {
            Paciente paciente = Paciente.fromCsvRecord(record);
            CsvRecord normalized = paciente.toCsvRecord();
            OffsetDateTime createdAt = paciente.createdAt().orElse(now);
            OffsetDateTime updatedAt = paciente.updatedAt().orElse(now);
            PacienteDto dto = new PacienteDto(
                    paciente.pacienteId(),
                    paciente.nif(),
                    paciente.nombre(),
                    paciente.apellidos(),
                    paciente.fechaNacimiento(),
                    paciente.sexo().code(),
                    normalized.optional("telefono").orElse(null),
                    normalized.optional("email").orElse(null),
                    paciente.empresaId().orElse(null),
                    paciente.centroId().orElse(null),
                    normalized.optional("externo_ref").orElse(null),
                    createdAt,
                    updatedAt,
                    updatedAt,
                    null
            );
            pacienteGateway.upsert(dto, updatedAt, 0L);
        }
        return records.size();
    }

    private int importCuestionarios(Path file) {
        List<CsvRecord> records = csvFileReader.readCsv(file);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (CsvRecord record : records) {
            Cuestionario cuestionario = Cuestionario.fromCsvRecord(record);
            CsvRecord normalized = cuestionario.toCsvRecord();
            OffsetDateTime createdAt = cuestionario.createdAt().orElse(now);
            OffsetDateTime updatedAt = cuestionario.updatedAt().orElse(now);
            CuestionarioDto dto = new CuestionarioDto(
                    cuestionario.cuestionarioId(),
                    cuestionario.pacienteId(),
                    cuestionario.plantillaCodigo(),
                    cuestionario.estado().code(),
                    normalized.require("respuestas"),
                    normalized.optional("firmas").orElse(null),
                    normalized.optional("adjuntos").orElse(null),
                    createdAt,
                    updatedAt,
                    updatedAt,
                    null
            );
            cuestionarioGateway.upsert(dto, updatedAt, 0L);
        }
        return records.size();
    }

    private void verifyChecksum(Path file) throws IOException, NoSuchAlgorithmException {
        Path checksumFile = file.resolveSibling(file.getFileName().toString() + ".sha256");
        if (!Files.exists(checksumFile)) {
            throw new IllegalStateException("Falta el checksum para " + file.getFileName());
        }
        String expected = Files.readString(checksumFile).trim();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(Files.readAllBytes(file));
        String actual = HexFormat.of().withUpperCase().formatHex(hash);
        if (!expected.equalsIgnoreCase(actual)) {
            throw new IllegalStateException("Checksum inválido para " + file.getFileName());
        }
    }

    private void moveDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void moveDirectorySilently(Path source, Path target) {
        try {
            moveDirectory(source, target);
        } catch (IOException ioException) {
            logger.error("No se pudo mover {} a {}", source, target, ioException);
        }
    }

    private record ImportCounters(int pacientes, int cuestionarios) {
    }

    public record RrhhImportReport(int processedDrops,
                                   int pacientesImported,
                                   int cuestionariosImported,
                                   List<String> archivedDrops,
                                   List<String> failedDrops) {
    }
}
