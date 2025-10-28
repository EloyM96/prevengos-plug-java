package com.prevengos.plug.desktop.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.service.RemoteSyncGateway;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Implementación HTTP del gateway de sincronización.
 */
public class HttpRemoteSyncGateway implements RemoteSyncGateway {

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
    public SyncPushResponse push(SyncPushRequest requestPayload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.baseUrl() + "/sincronizacion/push"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestPayload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Error al sincronizar lote: " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), SyncPushResponse.class);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Fallo en el envío del lote de sincronización", e);
        }
    }

    @Override
    public SyncPullResponse pull(Long syncToken, int limit) {
        try {
            StringBuilder uriBuilder = new StringBuilder(config.baseUrl())
                    .append("/sincronizacion/pull?limit=").append(limit);
            if (syncToken != null) {
                uriBuilder.append("&syncToken=").append(syncToken);
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
            return objectMapper.readValue(response.body(), SyncPullResponse.class);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Fallo en el pull de sincronización", e);
        }
    }
}
