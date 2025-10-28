package com.prevengos.plug.desktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads configuration for the desktop application from bundled defaults,
 * optional overrides in {@code ~/.prevengos/desktop.properties} and
 * environment variables. The configuration defines how the application
 * connects to the local or hub database.
 */
public final class DesktopConfiguration {

    private static final String DEFAULTS_RESOURCE = "/com/prevengos/plug/desktop/application-defaults.properties";
    private static final String USER_PROPERTIES_FILE = "desktop.properties";

    private final DatabaseMode databaseMode;
    private final Path sqlitePath;
    private final String hubUrl;
    private final String hubUsername;
    private final String hubPassword;
    private final String environmentLabel;

    private DesktopConfiguration(DatabaseMode databaseMode,
                                 Path sqlitePath,
                                 String hubUrl,
                                 String hubUsername,
                                 String hubPassword,
                                 String environmentLabel) {
        this.databaseMode = Objects.requireNonNull(databaseMode, "databaseMode");
        this.sqlitePath = Objects.requireNonNull(sqlitePath, "sqlitePath");
        this.hubUrl = hubUrl;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.environmentLabel = environmentLabel != null ? environmentLabel : databaseMode.displayName();
    }

    public static DesktopConfiguration load() {
        Properties properties = new Properties();

        try (InputStream defaults = DesktopConfiguration.class.getResourceAsStream(DEFAULTS_RESOURCE)) {
            if (defaults != null) {
                properties.load(defaults);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load default configuration", ex);
        }

        Path userConfigDir = Paths.get(System.getProperty("user.home"), ".prevengos");
        Path userConfigFile = userConfigDir.resolve(USER_PROPERTIES_FILE);
        if (Files.isRegularFile(userConfigFile)) {
            try (InputStream in = Files.newInputStream(userConfigFile)) {
                properties.load(in);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load user configuration from " + userConfigFile, ex);
            }
        }

        overrideWithEnv(properties, "db.mode", "PREVENGOS_DB_MODE");
        overrideWithEnv(properties, "db.sqlite.path", "PREVENGOS_SQLITE_PATH");
        overrideWithEnv(properties, "db.hub.url", "PREVENGOS_HUB_URL");
        overrideWithEnv(properties, "db.hub.username", "PREVENGOS_HUB_USERNAME");
        overrideWithEnv(properties, "db.hub.password", "PREVENGOS_HUB_PASSWORD");
        overrideWithEnv(properties, "environment.label", "PREVENGOS_ENVIRONMENT_LABEL");

        DatabaseMode mode = parseMode(properties.getProperty("db.mode", "LOCAL"));
        Path sqlitePath = resolvePath(properties.getProperty("db.sqlite.path"));
        String hubUrl = trimToNull(properties.getProperty("db.hub.url"));
        String hubUsername = trimToNull(properties.getProperty("db.hub.username"));
        String hubPassword = trimToNull(properties.getProperty("db.hub.password"));
        String environmentLabel = trimToNull(properties.getProperty("environment.label"));

        return new DesktopConfiguration(mode, sqlitePath, hubUrl, hubUsername, hubPassword, environmentLabel);
    }

    private static void overrideWithEnv(Properties properties, String key, String envVar) {
        String value = System.getenv(envVar);
        if (value != null && !value.isBlank()) {
            properties.setProperty(key, value);
        }
    }

    private static DatabaseMode parseMode(String value) {
        String normalized = value == null ? "LOCAL" : value.trim().toUpperCase(Locale.ROOT);
        return DatabaseMode.valueOf(normalized);
    }

    private static Path resolvePath(String value) {
        String resolved = resolvePlaceholders(trimToNull(value));
        if (resolved == null) {
            throw new IllegalArgumentException("SQLite path configuration cannot be null");
        }
        return Paths.get(resolved).toAbsolutePath();
    }

    private static String resolvePlaceholders(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("${user.home}", System.getProperty("user.home"));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public DatabaseMode databaseMode() {
        return databaseMode;
    }

    public Path sqlitePath() {
        return sqlitePath;
    }

    public String hubUrl() {
        return hubUrl;
    }

    public String hubUsername() {
        return hubUsername;
    }

    public String hubPassword() {
        return hubPassword;
    }

    public String environmentLabel() {
        return environmentLabel;
    }
}
