package com.prevengos.plug.desktop.config;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public record DesktopConfiguration(
        Path databasePath,
        String apiBaseUrl,
        String sourceSystem,
        int syncPageSize,
        int requestTimeoutSeconds
) {
    private static final String DEFAULT_DB = "prevengos-desktop.db";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_SOURCE = "desktop-app";

    public static DesktopConfiguration fromSystemProperties() {
        try {
            String dbPath = System.getProperty("database.path", DEFAULT_DB);
            String baseUrl = System.getProperty("api.baseUrl", DEFAULT_BASE_URL);
            String sourceSystem = System.getProperty("api.sourceSystem", DEFAULT_SOURCE);
            int pageSize = Integer.parseInt(System.getProperty("sync.pageSize", "200"));
            int timeoutSeconds = Integer.parseInt(System.getProperty("api.timeoutSeconds", "30"));

            if (baseUrl.isBlank()) {
                throw new ConfigurationException("api.baseUrl no puede estar vacío");
            }

            if (pageSize <= 0) {
                throw new ConfigurationException("sync.pageSize debe ser mayor que cero");
            }

            if (timeoutSeconds <= 0) {
                throw new ConfigurationException("api.timeoutSeconds debe ser mayor que cero");
            }

            return new DesktopConfiguration(Path.of(dbPath).toAbsolutePath(), baseUrl, sourceSystem, pageSize, timeoutSeconds);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Las propiedades numéricas de sincronización no son válidas", e);
        }
    }

    public String normalisedBaseUrl() {
        Objects.requireNonNull(apiBaseUrl);
        if (apiBaseUrl.endsWith("/")) {
            return apiBaseUrl.substring(0, apiBaseUrl.length() - 1);
        }
        return apiBaseUrl;
    }

    public Locale locale() {
        return Locale.getDefault();
    }
}
