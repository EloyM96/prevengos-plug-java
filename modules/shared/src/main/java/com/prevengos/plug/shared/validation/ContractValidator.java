package com.prevengos.plug.shared.validation;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility with reusable validation helpers aligned with the JSON Schema
 * constraints described in {@code contracts/json}.
 */
public final class ContractValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ContractValidator() {
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new ContractValidationException(field + " must not be null");
        }
        return value;
    }

    public static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ContractValidationException(field + " must not be blank");
        }
        return value;
    }

    public static String requirePattern(String value, Pattern pattern, String field, String message) {
        if (value == null) {
            throw new ContractValidationException(field + " must not be null");
        }
        if (!pattern.matcher(value).matches()) {
            throw new ContractValidationException(message);
        }
        return value;
    }

    public static String requireEmail(String value, String field) {
        if (value == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new ContractValidationException(field + " must be a valid email address");
        }
        return value;
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new ContractValidationException(message);
        }
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    public static boolean equalsAny(String value, String field, String... allowed) {
        if (value == null) {
            throw new ContractValidationException(field + " must not be null");
        }
        for (String candidate : allowed) {
            if (Objects.equals(value, candidate)) {
                return true;
            }
        }
        throw new ContractValidationException(field + " must be one of " + String.join(", ", allowed));
    }
}
