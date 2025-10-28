package com.prevengos.plug.hubbackend;

import com.prevengos.plug.gateway.sqlserver.CuestionarioCsvRow;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteCsvRow;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.hubbackend.io.CsvFileWriter;
import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
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

        when(pacienteGateway.fetchForRrhhExport(Mockito.any(OffsetDateTime.class)))
                .thenReturn(List.of(new PacienteCsvRow(
                        UUID.randomUUID(),
                        "12345A",
                        "Ana",
                        "Prevengos",
                        "F",
                        OffsetDateTime.now(),
                        "+34123456789",
                        "ana.prevengos@example.com",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "EXT-1")));

        when(cuestionarioGateway.fetchForRrhhExport(Mockito.any(OffsetDateTime.class)))
                .thenReturn(List.of(new CuestionarioCsvRow(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "CS-01",
                        "completado",
                        OffsetDateTime.now())));

        RrhhExportProperties properties = new RrhhExportProperties();
        properties.setBaseDir(tempDir);
        properties.setLookbackHours(24);
        CsvFileWriter csvFileWriter = new CsvFileWriter();
        RrhhCsvExportJob job = new RrhhCsvExportJob(pacienteGateway, cuestionarioGateway, csvFileWriter, properties);

        job.runExport("test");

        Path dayDir;
        try (var stream = Files.list(tempDir)) {
            dayDir = stream.findFirst().orElseThrow();
        }
        Path processDir;
        try (var stream = Files.list(dayDir)) {
            processDir = stream.findFirst().orElseThrow();
        }
        Path pacientesCsv = processDir.resolve("pacientes.csv");
        Path cuestionariosCsv = processDir.resolve("cuestionarios.csv");

        assertThat(Files.exists(pacientesCsv)).isTrue();
        assertThat(Files.exists(pacientesCsv.resolveSibling("pacientes.csv.sha256"))).isTrue();
        assertThat(Files.exists(cuestionariosCsv)).isTrue();
        assertThat(Files.readString(pacientesCsv)).contains("paciente_id");
        assertThat(Files.readString(cuestionariosCsv)).contains("cuestionario_id");
    }

}
