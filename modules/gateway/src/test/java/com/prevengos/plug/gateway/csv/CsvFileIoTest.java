package com.prevengos.plug.gateway.csv;

import com.prevengos.plug.shared.csv.CsvRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvFileIoTest {

    private final CsvFileWriter writer = new CsvFileWriter();
    private final CsvFileReader reader = new CsvFileReader();

    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsCsvWithEscaping() throws IOException, NoSuchAlgorithmException {
        Path csvFile = tempDir.resolve("rrhh.csv");
        List<String> headers = List.of("id", "name;with;separator", "empty");
        List<List<String>> rows = List.of(
                List.of("123", "Ana;Prevengos", ""),
                List.of("456", "\"Quoted\" value", "LineBreak")
        );

        writer.writeCsv(csvFile, headers, rows);

        assertTrue(Files.exists(csvFile), "CSV file should be created");
        List<String> lines = Files.readAllLines(csvFile);
        assertEquals(3, lines.size());
        assertEquals("id;\"name;with;separator\";empty", lines.get(0));
        assertEquals("123;\"Ana;Prevengos\";", lines.get(1));
        assertEquals("456;\"\"\"Quoted\"\" value\";LineBreak", lines.get(2));

        String checksum = writer.writeChecksum(csvFile);
        Path checksumFile = csvFile.resolveSibling("rrhh.csv.sha256");
        assertTrue(Files.exists(checksumFile), "Checksum file should be created");

        String expectedChecksum = calculateChecksum(csvFile);
        assertEquals(expectedChecksum, checksum);
        assertEquals(expectedChecksum, Files.readString(checksumFile).trim());

        List<CsvRecord> records = reader.readCsv(csvFile);
        assertEquals(2, records.size());
        CsvRecord first = records.get(0);
        assertEquals("123", first.require("id"));
        assertTrue(first.optional("empty").isEmpty());

        CsvRecord second = records.get(1);
        assertEquals("456", second.require("id"));
        assertEquals("\"Quoted\" value", second.require("name;with;separator"));
        assertEquals("LineBreak", second.require("empty"));
    }

    private static String calculateChecksum(Path file) throws IOException, NoSuchAlgorithmException {
        byte[] bytes = Files.readAllBytes(file);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        return HexFormat.of().withUpperCase().formatHex(hash);
    }
}
