package com.prevengos.plug.desktop.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.config.DesktopConfiguration;
import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;
import com.prevengos.plug.desktop.repository.CuestionarioRepository;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PacienteRepository;
import com.prevengos.plug.desktop.repository.SyncEventRepository;
import com.prevengos.plug.desktop.sync.dto.SyncBatchRequest;
import com.prevengos.plug.desktop.sync.dto.SyncBatchResponse;
import com.prevengos.plug.desktop.sync.dto.SyncPullResponse;
import com.prevengos.plug.desktop.sync.dto.SyncChange;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.List;

public class SyncService {

    private final DesktopConfiguration configuration;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final MetadataRepository metadataRepository;
    private final SyncEventRepository syncEventRepository;

    public SyncService(DesktopConfiguration configuration,
                       HttpClient httpClient,
                       ObjectMapper mapper,
                       PacienteRepository pacienteRepository,
                       CuestionarioRepository cuestionarioRepository,
                       MetadataRepository metadataRepository,
                       SyncEventRepository syncEventRepository) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.pacienteRepository = pacienteRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.metadataRepository = metadataRepository;
        this.syncEventRepository = syncEventRepository;
    }

    public SyncBatchResponse pushDirtyEntities() throws IOException {
        List<Paciente> pacientes = pacienteRepository.findDirty();
        List<Cuestionario> cuestionarios = cuestionarioRepository.findDirty();
        if (pacientes.isEmpty() && cuestionarios.isEmpty()) {
            Long lastToken = metadataRepository.readMetadata().lastSyncToken();
            return new SyncBatchResponse(0, 0, lastToken == null ? 0 : lastToken);
        }

        SyncBatchRequest requestPayload = new SyncBatchRequest(configuration.sourceSystem(), pacientes, cuestionarios);
        String payload;
        try {
            payload = mapper.writeValueAsString(requestPayload);
        } catch (JsonProcessingException e) {
            throw new IOException("No se pudo serializar el lote de sincronización", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(configuration.normalisedBaseUrl() + "/sincronizacion/lotes"))
                .header("Content-Type", "application/json")
                .header("X-Source-System", configuration.sourceSystem())
                .timeout(java.time.Duration.ofSeconds(configuration.requestTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("La petición de sincronización fue interrumpida", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("El Hub devolvió un estado inesperado: " + response.statusCode());
        }

        SyncBatchResponse batchResponse = mapper.readValue(response.body(), SyncBatchResponse.class);
        long syncToken = batchResponse.lastSyncToken();
        for (Paciente paciente : pacientes) {
            pacienteRepository.markAsClean(paciente.pacienteId(), syncToken);
            syncEventRepository.append("paciente", paciente.pacienteId().toString(), "paciente-upserted", paciente, configuration.sourceSystem(), syncToken);
        }
        for (Cuestionario cuestionario : cuestionarios) {
            cuestionarioRepository.markAsClean(cuestionario.cuestionarioId(), syncToken);
            syncEventRepository.append("cuestionario", cuestionario.cuestionarioId().toString(), "cuestionario-upserted", cuestionario, configuration.sourceSystem(), syncToken);
        }

        metadataRepository.updateLastPush(OffsetDateTime.now());
        metadataRepository.updateLastToken(syncToken);
        return batchResponse;
    }

    public SyncPullResponse pullUpdates() throws IOException {
        Long lastToken = metadataRepository.readMetadata().lastSyncToken();
        long token = lastToken != null ? lastToken : 0L;
        URI uri = URI.create(configuration.normalisedBaseUrl() + "/sincronizacion/pull?token=" + token + "&limit=" + configuration.syncPageSize());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(configuration.requestTimeoutSeconds()))
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("La petición de pull fue interrumpida", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("El Hub devolvió un estado inesperado en el pull: " + response.statusCode());
        }

        SyncPullResponse pullResponse = mapper.readValue(response.body(), SyncPullResponse.class);
        if (pullResponse.changes() != null) {
            for (SyncChange change : pullResponse.changes()) {
                long syncToken = change.syncToken();
                if (change.isPaciente() && change.paciente() != null) {
                    pacienteRepository.upsertFromRemote(change.paciente());
                    syncEventRepository.append("paciente", change.paciente().pacienteId().toString(), "paciente-updated", change.paciente(), "hub", syncToken);
                } else if (change.isCuestionario() && change.cuestionario() != null) {
                    cuestionarioRepository.upsertFromRemote(change.cuestionario());
                    syncEventRepository.append("cuestionario", change.cuestionario().cuestionarioId().toString(), "cuestionario-updated", change.cuestionario(), "hub", syncToken);
                }
            }
        }
        metadataRepository.updateLastToken(pullResponse.nextToken());
        metadataRepository.updateLastPull(OffsetDateTime.now());
        return pullResponse;
    }
}
