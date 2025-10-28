package com.prevengos.plug.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prevengos.plug.desktop.config.DesktopConfiguration;
import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.repository.CuestionarioRepository;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PacienteRepository;
import com.prevengos.plug.desktop.repository.SyncEventRepository;
import com.prevengos.plug.desktop.service.JsonExportService;
import com.prevengos.plug.desktop.sync.SyncService;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

public class AppContainer {

    private final DesktopConfiguration configuration;
    private final DatabaseManager databaseManager;
    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final SyncEventRepository syncEventRepository;
    private final MetadataRepository metadataRepository;
    private final SyncService syncService;
    private final JsonExportService jsonExportService;

    public AppContainer(DesktopConfiguration configuration) {
        this.configuration = configuration;
        try {
            this.databaseManager = new DatabaseManager(configuration.databasePath());
            this.databaseManager.initialize();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", e);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        this.pacienteRepository = new PacienteRepository(databaseManager);
        this.cuestionarioRepository = new CuestionarioRepository(databaseManager);
        this.syncEventRepository = new SyncEventRepository(databaseManager, mapper);
        this.metadataRepository = new MetadataRepository(databaseManager);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(configuration.requestTimeoutSeconds()))
                .build();

        this.syncService = new SyncService(configuration, httpClient, mapper,
                pacienteRepository, cuestionarioRepository, metadataRepository, syncEventRepository);
        this.jsonExportService = new JsonExportService(mapper, pacienteRepository, cuestionarioRepository, metadataRepository);
    }

    public DesktopConfiguration configuration() {
        return configuration;
    }

    public PacienteRepository pacienteRepository() {
        return pacienteRepository;
    }

    public CuestionarioRepository cuestionarioRepository() {
        return cuestionarioRepository;
    }

    public SyncEventRepository syncEventRepository() {
        return syncEventRepository;
    }

    public MetadataRepository metadataRepository() {
        return metadataRepository;
    }

    public SyncService syncService() {
        return syncService;
    }

    public JsonExportService jsonExportService() {
        return jsonExportService;
    }
}
