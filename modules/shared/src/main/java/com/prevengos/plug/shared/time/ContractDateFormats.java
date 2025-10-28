package com.prevengos.plug.shared.time;

import com.prevengos.plug.shared.validation.ContractValidationException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Common date/time helpers used by contract models. They keep parsing and formatting
 * consistent across Android, desktop and backend runtimes.
 */
public final class ContractDateFormats {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private ContractDateFormats() {
    }

    public static LocalDate parseDate(String input, String field) {
        try {
            return LocalDate.parse(input, ISO_DATE);
        } catch (DateTimeParseException ex) {
            throw new ContractValidationException("Invalid ISO date for '" + field + "': " + input, ex);
        }
    }

    public static OffsetDateTime parseDateTime(String input, String field) {
        try {
            return OffsetDateTime.parse(input, ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new ContractValidationException("Invalid ISO-8601 date-time for '" + field + "': " + input, ex);
        }
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return ISO_DATE.format(date);
    }

    public static String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ISO_OFFSET_DATE_TIME.format(dateTime);
    }
}
