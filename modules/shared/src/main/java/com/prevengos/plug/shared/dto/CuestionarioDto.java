package com.prevengos.plug.shared.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data transfer object used for questionnaire synchronization payloads. Arrays
 * of responses, signatures or attachments are represented as JSON-compatible
 * structures to keep the DTO close to the wire format consumed by clients.
 */
public record CuestionarioDto(
        @NotNull UUID cuestionarioId,
        @NotNull UUID pacienteId,
        @NotBlank @Size(max = 64) String plantillaCodigo,
        @NotBlank @Pattern(regexp = "^(borrador|completado|validado)$") String estado,
        @NotNull Map<String, Object> respuestas,
        List<@Valid Map<String, Object>> firmas,
        List<@Valid Map<String, Object>> adjuntos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
