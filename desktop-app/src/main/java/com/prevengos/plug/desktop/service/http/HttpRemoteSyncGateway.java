package com.prevengos.plug.desktop.service.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.service.RemoteSyncGateway;
import com.prevengos.plug.desktop.service.dto.PullResponse;
import com.prevengos.plug.desktop.service.dto.SyncBatch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Implementación HTTP del gateway de sincronización.
 */
public class HttpRemoteSyncGateway implements RemoteSyncGateway {

    private static final TypeReference<PullResponse> PULL_RESPONSE_TYPE = new TypeReference<>() { };

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AppConfig config;

    public HttpRemoteSyncGateway(AppConfig config) {
        this(config, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    public HttpRemoteSyncGateway(AppConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = config.objectMapper();
    }

    @Override
    public SyncBatch pushBatch(SyncBatch batch) {
        try {
            Map<String, Object> payload = Map.of(
                    "patients", batch.getPatients(),
                    "questionnaires", batch.getQuestionnaires(),
                    "sourceSystem", batch.getSourceSystem(),
                    "generatedAt", batch.getGeneratedAt()
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.baseUrl() + "/sincronizacion/lotes"))
                    .header("Content-Type", "application/json")
                    .header("X-Source-System", batch.getSourceSystem())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Error al sincronizar lote: " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), SyncBatch.class);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Fallo en el envío del lote de sincronización", e);
        }
    }

    @Override
    public PullResponse pull(String syncToken, String since, int limit) {
        try {
            StringBuilder uriBuilder = new StringBuilder(config.baseUrl()).append("/sincronizacion/pull?limit=").append(limit);
            if (syncToken != null) {
                uriBuilder.append("&syncToken=").append(syncToken);
            }
            if (since != null) {
                uriBuilder.append("&since=").append(since);
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriBuilder.toString()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Error en pull: " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), PULL_RESPONSE_TYPE);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Fallo en el pull de sincronización", e);
        }
    }
}
