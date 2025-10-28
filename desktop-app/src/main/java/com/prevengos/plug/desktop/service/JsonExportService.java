package com.prevengos.plug.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;
import com.prevengos.plug.desktop.model.SyncMetadata;
import com.prevengos.plug.desktop.repository.CuestionarioRepository;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PacienteRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonExportService {

    private final ObjectMapper mapper;
    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final MetadataRepository metadataRepository;

    public JsonExportService(ObjectMapper mapper,
                             PacienteRepository pacienteRepository,
                             CuestionarioRepository cuestionarioRepository,
                             MetadataRepository metadataRepository) {
        this.mapper = mapper;
        this.pacienteRepository = pacienteRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.metadataRepository = metadataRepository;
    }

    public void exportTo(Path target) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        List<Paciente> pacientes = pacienteRepository.findAll();
        root.set("pacientes", mapper.valueToTree(pacientes));
        Map<UUID, List<Cuestionario>> cuestionariosPorPaciente = new HashMap<>();
        for (Paciente paciente : pacientes) {
            cuestionariosPorPaciente.put(paciente.pacienteId(), cuestionarioRepository.findByPaciente(paciente.pacienteId()));
        }
        root.set("cuestionarios", mapper.valueToTree(cuestionariosPorPaciente));
        root.set("metadata", mapper.valueToTree(metadataRepository.readMetadata()));

        Path parent = target.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (OutputStream os = Files.newOutputStream(target)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(os, root);
        }
    }

    public void importFrom(Path source) throws IOException {
        try (InputStream is = Files.newInputStream(source)) {
            ObjectNode root = (ObjectNode) mapper.readTree(is);
            Paciente[] pacientes = mapper.treeToValue(root.get("pacientes"), Paciente[].class);
            if (pacientes != null) {
                for (Paciente paciente : pacientes) {
                    pacienteRepository.upsertFromRemote(paciente);
                }
            }
            ObjectNode cuestionariosNode = (ObjectNode) root.get("cuestionarios");
            if (cuestionariosNode != null) {
                cuestionariosNode.fields().forEachRemaining(entry -> {
                    try {
                        Cuestionario[] cuestionarios = mapper.treeToValue(entry.getValue(), Cuestionario[].class);
                        for (Cuestionario cuestionario : cuestionarios) {
                            cuestionarioRepository.upsertFromRemote(cuestionario);
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException("No se pudo importar cuestionarios", e);
                    }
                });
            }
            if (root.has("metadata")) {
                SyncMetadata metadata = mapper.treeToValue(root.get("metadata"), SyncMetadata.class);
                if (metadata.lastSyncToken() != null) {
                    metadataRepository.updateLastToken(metadata.lastSyncToken());
                }
                if (metadata.lastPullAt() != null) {
                    metadataRepository.updateLastPull(metadata.lastPullAt());
                }
                if (metadata.lastPushAt() != null) {
                    metadataRepository.updateLastPush(metadata.lastPushAt());
                }
            } else {
                metadataRepository.updateLastPull(OffsetDateTime.now());
            }
        }
    }
}
