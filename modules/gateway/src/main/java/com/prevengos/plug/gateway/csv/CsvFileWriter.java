package com.prevengos.plug.gateway.csv;

import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilidad ligera para generar CSV compatibles con RRHH.
 */
public class CsvFileWriter {

    public Path writePacientes(Path directory, List<PacienteCsvRow> rows) {
        Path output = directory.resolve("pacientes.csv");
        writeCsv(output, PacienteCsvRow.headers(), rows.stream().map(PacienteCsvRow::values).toList());
        writeChecksum(output);
        return output;
    }

    public Path writeCuestionarios(Path directory, List<CuestionarioCsvRow> rows) {
        Path output = directory.resolve("cuestionarios.csv");
        writeCsv(output, CuestionarioCsvRow.headers(), rows.stream().map(CuestionarioCsvRow::values).toList());
        writeChecksum(output);
        return output;
    }

    private void writeCsv(Path path, List<String> headers, List<List<String>> rows) {
        try {
            Files.createDirectories(path.getParent());
            StringBuilder builder = new StringBuilder();
            builder.append(String.join(",", headers)).append('\n');
            for (List<String> row : rows) {
                builder.append(row.stream()
                        .map(this::escape)
                        .collect(Collectors.joining(",")))
                        .append('\n');
            }
            Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir el CSV " + path, e);
        }
    }

    private void writeChecksum(Path path) {
        Path checksum = path.resolveSibling(path.getFileName().toString() + ".sha256");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(path);
            byte[] hash = digest.digest(data);
            String hex = HexFormat.of().formatHex(hash);
            Files.writeString(checksum, hex + "  " + path.getFileName() + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir checksum", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
