package com.prevengos.plug.hubbackend.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Component
public class CsvFileWriter {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileWriter.class);

    public void writeCsv(Path file, List<String> headers, List<List<String>> rows) {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(String.join(";", headers.stream().map(this::escape).toList()));
                writer.newLine();
                for (List<String> row : rows) {
                    writer.write(String.join(";", row.stream().map(this::escape).toList()));
                    writer.newLine();
                }
            }
            logger.info("CSV generado en {}", file);
        } catch (IOException e) {
            throw new IllegalStateException("Error writing CSV file " + file, e);
        }
    }

    public void writeChecksum(Path file) {
        Path checksumFile = file.resolveSibling(file.getFileName().toString() + ".sha256");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file);
            byte[] hash = digest.digest(bytes);
            String value = HexFormat.of().withUpperCase().formatHex(hash);
            Files.writeString(checksumFile, value, StandardCharsets.UTF_8);
            logger.info("Checksum generado en {}", checksumFile);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("Error generating checksum for " + file, e);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(";") || value.contains("\"") || value.contains("\n");
        String sanitized = value.replace("\"", "\"\"");
        if (needsQuotes) {
            return "\"" + sanitized + "\"";
        }
        return sanitized;
    }
}
