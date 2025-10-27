package com.prevengos.plug.hubbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PacienteDto(
        @NotNull UUID pacienteId,
        @NotBlank @Size(min = 5, max = 16) @Pattern(regexp = "^[0-9A-Za-z]{5,16}$") String nif,
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank @Pattern(regexp = "^[MFX]$") String sexo,
        String telefono,
        @Email String email,
        UUID empresaId,
        UUID centroId,
        String externoRef,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
