package com.prevengos.plug.hubbackend.io;

import com.prevengos.plug.shared.csv.CsvRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader counterpart for {@link CsvFileWriter}. It understands the repository CSV
 * conventions (UTF-8, semicolon separator, double quote escaping) and returns
 * immutable {@link CsvRecord} instances preserving column order.
 */
@Component
public class CsvFileReader {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileReader.class);

    public List<CsvRecord> readCsv(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return List.of();
            }
            List<String> headers = parseLine(headerLine);
            List<CsvRecord> records = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseLine(line);
                records.add(CsvRecord.of(headers, values));
            }
            logger.info("CSV leído: {} registros desde {}", records.size(), file);
            return List.copyOf(records);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading CSV file " + file, e);
        }
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    current.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ';' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }
}
