package com.prevengos.plug.hubbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CuestionarioDto(
        @NotNull UUID cuestionarioId,
        @NotNull UUID pacienteId,
        @NotBlank String plantillaCodigo,
        String estado,
        @NotNull @Valid List<@Valid RespuestaDto> respuestas,
        List<String> firmas,
        List<String> adjuntos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public record RespuestaDto(
            @NotBlank String preguntaCodigo,
            @NotNull Object valor,
            String unidad,
            Map<String, Object> metadata
    ) {
    }
}
