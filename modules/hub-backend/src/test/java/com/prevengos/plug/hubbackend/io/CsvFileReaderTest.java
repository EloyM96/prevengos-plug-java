package com.prevengos.plug.hubbackend.io;

import com.prevengos.plug.gateway.csv.CsvFileReader;
import com.prevengos.plug.shared.csv.CsvRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvFileReaderTest {

    private final CsvFileReader reader = new CsvFileReader();

    @Test
    void parsesQuotedValues(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("sample.csv");
        Files.writeString(csv, "col1;col2\n\"value;1\";\"\"quoted\"\"\n");

        List<CsvRecord> records = reader.readCsv(csv);

        assertThat(records).hasSize(1);
        CsvRecord record = records.get(0);
        assertThat(record.require("col1")).isEqualTo("value;1");
        assertThat(record.require("col2")).isEqualTo("\"quoted\"");
    }
}
