package com.prevengos.plug.desktop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuración central de la aplicación cargada desde {@code application.properties}.
 */
public final class AppConfig {

    private static final String DEFAULT_PROPERTIES = "/com/prevengos/plug/desktop/application.properties";

    private final Properties properties;
    private final ObjectMapper objectMapper;

    private AppConfig(Properties properties) {
        this.properties = properties;
        this.objectMapper = createObjectMapper();
    }

    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(DEFAULT_PROPERTIES)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar application.properties", e);
        }

        properties.putAll(System.getProperties());
        return new AppConfig(properties);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    public String baseUrl() {
        return properties.getProperty("api.baseUrl", "http://localhost:8080");
    }

    public String sourceSystem() {
        return properties.getProperty("sync.sourceSystem", "prevengos-desktop");
    }

    public Duration syncBackoff() {
        long seconds = Long.parseLong(properties.getProperty("sync.retryBackoffSeconds", "30"));
        return Duration.ofSeconds(seconds);
    }

    public int syncBatchSize() {
        return Integer.parseInt(properties.getProperty("sync.batchSize", "100"));
    }

    public Path resolveDatabasePath() {
        String configuredPath = properties.getProperty("database.path", defaultDatabasePath());
        if (configuredPath.startsWith("~")) {
            String home = System.getProperty("user.home");
            configuredPath = home + configuredPath.substring(1);
        }
        Path path = Paths.get(configuredPath).toAbsolutePath();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de la base de datos", e);
        }
        return path;
    }

    public Optional<String> readMetadata(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    private String defaultDatabasePath() {
        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome, ".prevengos", "desktop-app.db").toString();
    }
}
