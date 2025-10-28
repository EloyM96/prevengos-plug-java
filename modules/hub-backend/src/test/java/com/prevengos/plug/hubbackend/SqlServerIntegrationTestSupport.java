package com.prevengos.plug.hubbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SqlServerIntegrationTestSupport {

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-CU12-ubuntu-22.04")
            .acceptLicense();

    private static final Path EXPORT_BASE = tempDirectory("hub-backend-export-base");
    private static final Path EXPORT_ARCHIVE = tempDirectory("hub-backend-export-archive");
    private static final Path IMPORT_INBOX = tempDirectory("hub-backend-import-inbox");
    private static final Path IMPORT_ARCHIVE = tempDirectory("hub-backend-import-archive");
    private static final Path IMPORT_ERROR = tempDirectory("hub-backend-import-error");

    @Autowired
    private NamedParameterJdbcTemplate cleaningJdbcTemplate;

    @DynamicPropertySource
    static void registerSqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("hub.sqlserver.url", SQL_SERVER::getJdbcUrl);
        registry.add("hub.sqlserver.username", SQL_SERVER::getUsername);
        registry.add("hub.sqlserver.password", SQL_SERVER::getPassword);
        registry.add("hub.sqlserver.driver-class-name", SQL_SERVER::getDriverClassName);

        registry.add("spring.flyway.url", SQL_SERVER::getJdbcUrl);
        registry.add("spring.flyway.user", SQL_SERVER::getUsername);
        registry.add("spring.flyway.password", SQL_SERVER::getPassword);

        registry.add("hub.jobs.rrhh-export.base-dir", () -> EXPORT_BASE.toString());
        registry.add("hub.jobs.rrhh-export.archive-dir", () -> EXPORT_ARCHIVE.toString());
        registry.add("hub.jobs.rrhh-export.delivery.enabled", () -> "false");

        registry.add("hub.jobs.rrhh-import.inbox-dir", () -> IMPORT_INBOX.toString());
        registry.add("hub.jobs.rrhh-import.archive-dir", () -> IMPORT_ARCHIVE.toString());
        registry.add("hub.jobs.rrhh-import.error-dir", () -> IMPORT_ERROR.toString());

        registry.add("prl.notifier.enabled", () -> "false");
    }

    @BeforeEach
    void cleanDatabase() {
        cleaningJdbcTemplate.update("DELETE FROM file_drop_log", new MapSqlParameterSource());
        cleaningJdbcTemplate.update("DELETE FROM rrhh_exports", new MapSqlParameterSource());
        cleaningJdbcTemplate.update("DELETE FROM sync_events", new MapSqlParameterSource());
        cleaningJdbcTemplate.update("DELETE FROM cuestionarios", new MapSqlParameterSource());
        cleaningJdbcTemplate.update("DELETE FROM pacientes", new MapSqlParameterSource());
    }

    private static Path tempDirectory(String prefix) {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"), prefix + "-" + UUID.randomUUID());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create temporary directory for tests: " + dir, e);
        }
        return dir;
    }
}
