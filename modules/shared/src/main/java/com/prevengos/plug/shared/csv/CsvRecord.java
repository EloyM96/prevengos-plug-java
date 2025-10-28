package com.prevengos.plug.shared.csv;

import com.prevengos.plug.shared.validation.ContractValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable CSV record representation that keeps column ordering stable across
 * Android, desktop and backend modules.
 */
public final class CsvRecord {

    private final List<String> headers;
    private final List<String> values;
    private final Map<String, Integer> indexByHeader;

    private CsvRecord(List<String> headers, List<String> values) {
        if (headers.size() != values.size()) {
            throw new ContractValidationException("Headers and values length mismatch: " + headers.size() + " != " + values.size());
        }
        this.headers = List.copyOf(headers);
        this.values = List.copyOf(values);
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            index.put(headers.get(i), i);
        }
        this.indexByHeader = Collections.unmodifiableMap(index);
    }

    public static CsvRecord of(List<String> headers, List<String> values) {
        return new CsvRecord(headers, values);
    }

    public static CsvRecord of(Map<String, String> orderedValues) {
        List<String> headers = new ArrayList<>(orderedValues.size());
        List<String> values = new ArrayList<>(orderedValues.size());
        for (Map.Entry<String, String> entry : orderedValues.entrySet()) {
            headers.add(entry.getKey());
            values.add(entry.getValue());
        }
        return new CsvRecord(headers, values);
    }

    public String require(String column) {
        Integer index = indexByHeader.get(column);
        if (index == null) {
            throw new ContractValidationException("Column '" + column + "' not present in CSV record");
        }
        return values.get(index);
    }

    public Optional<String> optional(String column) {
        Integer index = indexByHeader.get(column);
        if (index == null) {
            return Optional.empty();
        }
        String value = values.get(index);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    public List<String> headers() {
        return headers;
    }

    public List<String> values() {
        return values;
    }

    public String[] toArray() {
        return values.toArray(new String[0]);
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.get(i), values.get(i));
        }
        return Collections.unmodifiableMap(result);
    }

    public CsvRecord with(String column, String value) {
        List<String> newHeaders = new ArrayList<>(headers);
        List<String> newValues = new ArrayList<>(values);
        Integer existing = indexByHeader.get(column);
        if (existing == null) {
            newHeaders.add(column);
            newValues.add(value);
        } else {
            newValues.set(existing, value);
        }
        return new CsvRecord(newHeaders, newValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CsvRecord other)) {
            return false;
        }
        return headers.equals(other.headers) && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, values);
    }

    @Override
    public String toString() {
        return "CsvRecord{" + "headers=" + headers + ", values=" + values + '}';
    }
}
