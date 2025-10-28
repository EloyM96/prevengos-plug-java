package com.prevengos.plug.hubbackend;

import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.filetransfer.FileTransferClient;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.gateway.sqlserver.RrhhAuditGateway;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SynchronizationFlowTest {

    @TempDir
    Path tempDir;

    @Test
    void rrhhExportJobGeneratesCsvFiles() throws Exception {
        PacienteGateway pacienteGateway = Mockito.mock(PacienteGateway.class);
        CuestionarioGateway cuestionarioGateway = Mockito.mock(CuestionarioGateway.class);

        OffsetDateTime now = OffsetDateTime.now();
        when(pacienteGateway.fetchForRrhhExport(Mockito.any(OffsetDateTime.class)))
                .thenReturn(List.of(new PacienteCsvRow(
                        UUID.randomUUID(),
                        "12345A",
                        "Ana",
                        "Prevengos",
                        now.toLocalDate(),
                        "F",
                        "+34123456789",
                        "ana.prevengos@example.com",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "EXT-1",
                        now.minusDays(1),
                        now)));

        when(cuestionarioGateway.fetchForRrhhExport(Mockito.any(OffsetDateTime.class)))
                .thenReturn(List.of(new CuestionarioCsvRow(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "CS-01",
                        "completado",
                        "{\"ok\":true}",
                        null,
                        null,
                        now.minusDays(1),
                        now)));

        RrhhExportProperties properties = new RrhhExportProperties();
        properties.setBaseDir(tempDir);
        properties.setLookbackHours(24);
        properties.setArchiveDir(tempDir.resolve("archive"));
        properties.getDelivery().setEnabled(false);

        CsvFileWriter csvFileWriter = new CsvFileWriter();
        FileTransferClient transferClient = Mockito.mock(FileTransferClient.class);
        RrhhAuditGateway auditGateway = Mockito.mock(RrhhAuditGateway.class);
        RrhhCsvExportJob job = new RrhhCsvExportJob(
                pacienteGateway,
                cuestionarioGateway,
                csvFileWriter,
                transferClient,
                auditGateway,
                properties);

        RrhhCsvExportJob.RrhhExportResult result = job.runExport("test");

        Path pacientesCsv = result.stagingDir().resolve("pacientes.csv");
        Path cuestionariosCsv = result.stagingDir().resolve("cuestionarios.csv");

        assertThat(Files.exists(pacientesCsv)).isTrue();
        assertThat(Files.exists(pacientesCsv.resolveSibling("pacientes.csv.sha256"))).isTrue();
        assertThat(Files.exists(cuestionariosCsv)).isTrue();
        assertThat(Files.readString(pacientesCsv)).contains("paciente_id");
        assertThat(Files.readString(cuestionariosCsv)).contains("cuestionario_id");
        assertThat(Files.exists(result.archiveDir().resolve("pacientes.csv"))).isTrue();
        assertThat(Files.exists(result.archiveDir().resolve("cuestionarios.csv"))).isTrue();
    }

}
