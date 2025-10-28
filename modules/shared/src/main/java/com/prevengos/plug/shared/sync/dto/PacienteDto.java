package com.prevengos.plug.shared.sync.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Contrato unificado para los datos de pacientes intercambiados durante los procesos
 * de sincronización. Incluye los campos de control de versión utilizados por los
 * consumidores del hub y valida el formato de la información crítica.
 */
public record PacienteDto(
        UUID pacienteId,
        String nif,
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String sexo,
        String telefono,
        String email,
        UUID empresaId,
        UUID centroId,
        String externoRef,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime lastModified,
        Long syncToken
) {

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9A-Za-z]{5,16}$");
    private static final Pattern SEXO_PATTERN = Pattern.compile("^[MFX]$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\s]+@[^@\s]+\\.[^@\s]+$");

    public PacienteDto {
        Objects.requireNonNull(pacienteId, "pacienteId no puede ser nulo");
        Objects.requireNonNull(nombre, "nombre no puede ser nulo");
        Objects.requireNonNull(apellidos, "apellidos no puede ser nulo");

        if (nif != null && (!NIF_PATTERN.matcher(nif).matches())) {
            throw new IllegalArgumentException("El NIF debe tener entre 5 y 16 caracteres alfanuméricos");
        }
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (apellidos.isBlank()) {
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos");
        }
        if (sexo != null) {
            if (sexo.isBlank() || !SEXO_PATTERN.matcher(sexo).matches()) {
                throw new IllegalArgumentException("Sexo debe ser una sigla válida (M, F o X)");
            }
        }
        if (telefono != null && telefono.length() > 32) {
            throw new IllegalArgumentException("El teléfono no puede superar los 32 caracteres");
        }
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email proporcionado no es válido");
        }
        if (externoRef != null && externoRef.length() > 128) {
            throw new IllegalArgumentException("El identificador externo no puede superar los 128 caracteres");
        }
    }
}
