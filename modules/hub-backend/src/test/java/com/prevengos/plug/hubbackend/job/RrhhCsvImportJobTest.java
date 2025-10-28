package com.prevengos.plug.hubbackend.job;

import com.prevengos.plug.gateway.csv.CsvFileReader;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.hubbackend.config.RrhhImportProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RrhhCsvImportJobTest {

    @Mock
    private PacienteGateway pacienteGateway;

    @Mock
    private CuestionarioGateway cuestionarioGateway;

    private final CsvFileReader csvFileReader = new CsvFileReader();

    @Test
    void processesValidDropAndMovesToArchive(@TempDir Path tempDir) throws Exception {
        Path inbox = tempDir.resolve("inbox");
        Path archive = tempDir.resolve("archive");
        Path error = tempDir.resolve("error");
        Files.createDirectories(inbox);
        Files.createDirectories(archive);
        Files.createDirectories(error);

        Path dropDir = inbox.resolve("20240202").resolve("rrhh").resolve("prevengos");
        Files.createDirectories(dropDir);

        Path pacientesFile = dropDir.resolve("pacientes.csv");
        Files.writeString(pacientesFile, "paciente_id;nif;nombre;apellidos;fecha_nacimiento;sexo;telefono;email;empresa_id;centro_id;externo_ref;created_at;updated_at\n"
                + "11111111-2222-3333-4444-555555555555;12345678A;Ana;Prevengos;1990-05-12;F;+34911222333;ana.prevengos@example.com;aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee;ffffffff-1111-2222-3333-444444444444;EXT-ANA-001;2024-02-01T08:15:00Z;2024-02-02T09:45:00Z\n",
                StandardCharsets.UTF_8);
        writeChecksum(pacientesFile);

        Path cuestionariosFile = dropDir.resolve("cuestionarios.csv");
        Files.writeString(cuestionariosFile, "cuestionario_id;paciente_id;plantilla_codigo;estado;respuestas;firmas;adjuntos;created_at;updated_at\n"
                + "66666666-7777-8888-9999-000000000000;11111111-2222-3333-4444-555555555555;CS-REVISION;validado;\"[{\\\"pregunta_codigo\\\":\\\"VISION\\\",\\\"valor\\\":{\\\"ojo_derecho\\\":\\\"1.0\\\"}}]\";[];\"[\\\"informe-revision.pdf\\\"]\";2024-02-02T09:45:00Z;2024-02-02T09:55:00Z\n",
                StandardCharsets.UTF_8);
        writeChecksum(cuestionariosFile);

        RrhhImportProperties properties = new RrhhImportProperties();
        properties.setInboxDir(inbox);
        properties.setArchiveDir(archive);
        properties.setErrorDir(error);

        RrhhCsvImportJob job = new RrhhCsvImportJob(pacienteGateway, cuestionarioGateway, csvFileReader, properties);
        RrhhCsvImportJob.RrhhImportReport report = job.processInbox("test");

        verify(pacienteGateway).upsert(any(), any(), anyLong());
        verify(cuestionarioGateway).upsert(any(), any(), anyLong());
        assertThat(report.pacientesImported()).isEqualTo(1);
        assertThat(report.cuestionariosImported()).isEqualTo(1);
        assertThat(archive.resolve("20240202/rrhh/prevengos")).exists();
        assertThat(Files.exists(dropDir)).isFalse();
        assertThat(report.failedDrops()).isEmpty();
    }

    @Test
    void movesDropToErrorOnChecksumFailure(@TempDir Path tempDir) throws Exception {
        Path inbox = tempDir.resolve("inbox");
        Path archive = tempDir.resolve("archive");
        Path error = tempDir.resolve("error");
        Files.createDirectories(inbox);
        Files.createDirectories(archive);
        Files.createDirectories(error);

        Path dropDir = inbox.resolve("20240203").resolve("rrhh").resolve("prevengos");
        Files.createDirectories(dropDir);

        Path pacientesFile = dropDir.resolve("pacientes.csv");
        Files.writeString(pacientesFile, "paciente_id;nif;nombre;apellidos;fecha_nacimiento;sexo;telefono;email;empresa_id;centro_id;externo_ref;created_at;updated_at\n"
                + "11111111-2222-3333-4444-555555555555;12345678A;Ana;Prevengos;1990-05-12;F;+34911222333;ana.prevengos@example.com;aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee;ffffffff-1111-2222-3333-444444444444;EXT-ANA-001;2024-02-01T08:15:00Z;2024-02-02T09:45:00Z\n",
                StandardCharsets.UTF_8);
        // No checksum para forzar error

        RrhhImportProperties properties = new RrhhImportProperties();
        properties.setInboxDir(inbox);
        properties.setArchiveDir(archive);
        properties.setErrorDir(error);

        RrhhCsvImportJob job = new RrhhCsvImportJob(pacienteGateway, cuestionarioGateway, csvFileReader, properties);
        RrhhCsvImportJob.RrhhImportReport report = job.processInbox("test");

        verifyNoInteractions(pacienteGateway, cuestionarioGateway);
        assertThat(report.processedDrops()).isEqualTo(0);
        assertThat(report.failedDrops()).hasSize(1);
        assertThat(error.resolve("20240203/rrhh/prevengos")).exists();
        assertThat(Files.exists(dropDir)).isFalse();
    }

    private void writeChecksum(Path csvFile) throws Exception {
        byte[] bytes = Files.readAllBytes(csvFile);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String checksum = HexFormat.of().withUpperCase().formatHex(digest.digest(bytes));
        Files.writeString(csvFile.resolveSibling(csvFile.getFileName().toString() + ".sha256"), checksum, StandardCharsets.UTF_8);
    }
}
