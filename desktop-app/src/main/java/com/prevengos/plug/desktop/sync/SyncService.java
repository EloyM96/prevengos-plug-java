package com.prevengos.plug.desktop.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.config.DesktopConfiguration;
import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;
import com.prevengos.plug.desktop.repository.CuestionarioRepository;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PacienteRepository;
import com.prevengos.plug.desktop.repository.SyncEventRepository;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public SyncPushResponse pushDirtyEntities() throws IOException {
        List<Paciente> pacientes = pacienteRepository.findDirty();
        List<Cuestionario> cuestionarios = cuestionarioRepository.findDirty();
        if (pacientes.isEmpty() && cuestionarios.isEmpty()) {
            Long lastToken = metadataRepository.readMetadata().lastSyncToken();
            long resolvedToken = lastToken != null ? lastToken : 0L;
            return new SyncPushResponse(0, 0, resolvedToken, List.of());
        }

        SyncPushRequest requestPayload = new SyncPushRequest(
                configuration.sourceSystem(),
                UUID.randomUUID(),
                toRemotePacientes(pacientes),
                toRemoteCuestionarios(cuestionarios)
        );
        String payload = mapper.writeValueAsString(requestPayload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(configuration.normalisedBaseUrl() + "/sincronizacion/push"))
                .header("Content-Type", "application/json")
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

        SyncPushResponse pushResponse = mapper.readValue(response.body(), SyncPushResponse.class);
        long syncToken = pushResponse.lastSyncToken();
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
        return pushResponse;
    }

    public SyncPullResponse pullUpdates() throws IOException {
        Long lastToken = metadataRepository.readMetadata().lastSyncToken();
        long token = lastToken != null ? lastToken : 0L;
        URI uri = URI.create(configuration.normalisedBaseUrl() + "/sincronizacion/pull?syncToken=" + token + "&limit=" + configuration.syncPageSize());
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
        for (PacienteDto remoto : pullResponse.pacientes()) {
            Paciente paciente = toDomainPaciente(remoto);
            pacienteRepository.upsertFromRemote(paciente);
            syncEventRepository.append("paciente", paciente.pacienteId().toString(), "paciente-updated", paciente, "hub", remoto.syncToken() != null ? remoto.syncToken() : 0L);
        }
        for (CuestionarioDto remoto : pullResponse.cuestionarios()) {
            Cuestionario cuestionario = toDomainCuestionario(remoto);
            cuestionarioRepository.upsertFromRemote(cuestionario);
            syncEventRepository.append("cuestionario", cuestionario.cuestionarioId().toString(), "cuestionario-updated", cuestionario, "hub", remoto.syncToken() != null ? remoto.syncToken() : 0L);
        }
        metadataRepository.updateLastToken(pullResponse.nextSyncToken());
        metadataRepository.updateLastPull(OffsetDateTime.now());
        return pullResponse;
    }

    private List<PacienteDto> toRemotePacientes(List<Paciente> pacientes) {
        List<PacienteDto> remote = new ArrayList<>(pacientes.size());
        for (Paciente paciente : pacientes) {
            remote.add(new PacienteDto(
                    paciente.pacienteId(),
                    paciente.nif(),
                    paciente.nombre(),
                    paciente.apellidos(),
                    paciente.fechaNacimiento(),
                    paciente.sexo(),
                    paciente.telefono(),
                    paciente.email(),
                    paciente.empresaId(),
                    paciente.centroId(),
                    paciente.externoRef(),
                    paciente.createdAt(),
                    paciente.updatedAt(),
                    toOffsetDateTime(paciente.lastModified()),
                    paciente.syncToken() > 0 ? paciente.syncToken() : null
            ));
        }
        return remote;
    }

    private List<CuestionarioDto> toRemoteCuestionarios(List<Cuestionario> cuestionarios) {
        List<CuestionarioDto> remote = new ArrayList<>(cuestionarios.size());
        for (Cuestionario cuestionario : cuestionarios) {
            remote.add(new CuestionarioDto(
                    cuestionario.cuestionarioId(),
                    cuestionario.pacienteId(),
                    cuestionario.plantillaCodigo(),
                    cuestionario.estado(),
                    cuestionario.respuestas(),
                    cuestionario.firmas(),
                    cuestionario.adjuntos(),
                    cuestionario.createdAt(),
                    cuestionario.updatedAt(),
                    toOffsetDateTime(cuestionario.lastModified()),
                    cuestionario.syncToken() > 0 ? cuestionario.syncToken() : null
            ));
        }
        return remote;
    }

    private Paciente toDomainPaciente(PacienteDto dto) {
        long lastModified = dto.lastModified() != null ? dto.lastModified().toInstant().toEpochMilli() : DatabaseManager.nowEpochMillis();
        long syncToken = dto.syncToken() != null ? dto.syncToken() : 0L;
        return new Paciente(
                dto.pacienteId(),
                dto.nif(),
                dto.nombre(),
                dto.apellidos(),
                dto.fechaNacimiento(),
                dto.sexo(),
                dto.telefono(),
                dto.email(),
                dto.empresaId(),
                dto.centroId(),
                dto.externoRef(),
                dto.createdAt(),
                dto.updatedAt(),
                lastModified,
                syncToken,
                false
        );
    }

    private Cuestionario toDomainCuestionario(CuestionarioDto dto) {
        long lastModified = dto.lastModified() != null ? dto.lastModified().toInstant().toEpochMilli() : DatabaseManager.nowEpochMillis();
        long syncToken = dto.syncToken() != null ? dto.syncToken() : 0L;
        return new Cuestionario(
                dto.cuestionarioId(),
                dto.pacienteId(),
                dto.plantillaCodigo(),
                dto.estado(),
                dto.respuestas(),
                dto.firmas(),
                dto.adjuntos(),
                dto.createdAt(),
                dto.updatedAt(),
                lastModified,
                syncToken,
                false
        );
    }

    private OffsetDateTime toOffsetDateTime(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
