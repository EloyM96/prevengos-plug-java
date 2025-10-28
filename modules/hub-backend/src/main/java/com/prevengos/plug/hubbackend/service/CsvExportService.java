package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.prevengos.plug.hubbackend.domain.Cuestionario;
import com.prevengos.plug.hubbackend.domain.Paciente;
import com.prevengos.plug.hubbackend.repository.CuestionarioRepository;
import com.prevengos.plug.hubbackend.repository.PacienteRepository;
import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.json.ContractJsonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for transforming persisted entities into the CSV
 * representation defined in {@code contracts/csv}.
 */
@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private static final OffsetDateTime DEFAULT_CURSOR = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    public CsvExportService(PacienteRepository pacienteRepository,
                            CuestionarioRepository cuestionarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.cuestionarioRepository = cuestionarioRepository;
    }

    public List<CsvRecord> exportPacientesSince(OffsetDateTime lastModifiedInclusive) {
        OffsetDateTime cursor = lastModifiedInclusive != null ? lastModifiedInclusive : DEFAULT_CURSOR;
        return pacienteRepository
                .findByLastModifiedGreaterThanEqualOrderByLastModifiedAsc(cursor)
                .stream()
                .map(this::toPacienteCsvRecord)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<CsvRecord> exportCuestionariosSince(OffsetDateTime lastModifiedInclusive) {
        OffsetDateTime cursor = lastModifiedInclusive != null ? lastModifiedInclusive : DEFAULT_CURSOR;
        return cuestionarioRepository
                .findByLastModifiedGreaterThanEqualOrderByLastModifiedAsc(cursor)
                .stream()
                .map(this::toCuestionarioCsvRecord)
                .collect(Collectors.toUnmodifiableList());
    }

    private CsvRecord toPacienteCsvRecord(Paciente entity) {
        com.prevengos.plug.shared.contracts.v1.Paciente.Builder builder =
                com.prevengos.plug.shared.contracts.v1.Paciente.builder()
                        .pacienteId(entity.getPacienteId())
                        .nif(entity.getNif())
                        .nombre(entity.getNombre())
                        .apellidos(entity.getApellidos())
                        .fechaNacimiento(entity.getFechaNacimiento())
                        .sexo(com.prevengos.plug.shared.contracts.v1.Paciente.Sexo.fromCode(entity.getSexo()));

        if (entity.getTelefono() != null) {
            builder.telefono(entity.getTelefono());
        }
        if (entity.getEmail() != null) {
            builder.email(entity.getEmail());
        }
        if (entity.getEmpresaId() != null) {
            builder.empresaId(entity.getEmpresaId());
        }
        if (entity.getCentroId() != null) {
            builder.centroId(entity.getCentroId());
        }
        if (entity.getExternoRef() != null) {
            builder.externoRef(entity.getExternoRef());
        }
        if (entity.getCreatedAt() != null) {
            builder.createdAt(entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() != null) {
            builder.updatedAt(entity.getUpdatedAt());
        }

        return builder.build().toCsvRecord();
    }

    private CsvRecord toCuestionarioCsvRecord(Cuestionario entity) {
        com.prevengos.plug.shared.contracts.v1.Cuestionario.Builder builder =
                com.prevengos.plug.shared.contracts.v1.Cuestionario.builder()
                        .cuestionarioId(entity.getCuestionarioId())
                        .pacienteId(entity.getPacienteId())
                        .plantillaCodigo(entity.getPlantillaCodigo())
                        .estado(com.prevengos.plug.shared.contracts.v1.Cuestionario.Estado.fromCode(entity.getEstado()))
                        .respuestas(parseRespuestas(entity.getRespuestas()));

        List<String> firmas = parseStringArray(entity.getFirmas());
        if (!firmas.isEmpty()) {
            builder.firmas(firmas);
        }
        List<String> adjuntos = parseStringArray(entity.getAdjuntos());
        if (!adjuntos.isEmpty()) {
            builder.adjuntos(adjuntos);
        }
        if (entity.getCreatedAt() != null) {
            builder.createdAt(entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() != null) {
            builder.updatedAt(entity.getUpdatedAt());
        }

        return builder.build().toCsvRecord();
    }

    private List<com.prevengos.plug.shared.contracts.v1.Cuestionario.Respuesta> parseRespuestas(String json) {
        JsonNode node = ContractJsonMapper.parseNode(json);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw new IllegalStateException("respuestas must be a JSON array");
        }
        List<com.prevengos.plug.shared.contracts.v1.Cuestionario.Respuesta> result = new ArrayList<>();
        for (JsonNode element : node) {
            result.add(com.prevengos.plug.shared.contracts.v1.Cuestionario.Respuesta.fromJson(element));
        }
        return List.copyOf(result);
    }

    private List<String> parseStringArray(String json) {
        JsonNode node = ContractJsonMapper.parseNode(json);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw new IllegalStateException("Expected JSON array of strings");
        }
        List<String> result = new ArrayList<>();
        for (JsonNode element : node) {
            if (!element.isTextual()) {
                throw new IllegalStateException("Array elements must be strings");
            }
            result.add(element.asText());
        }
        return List.copyOf(result);
    }
}
