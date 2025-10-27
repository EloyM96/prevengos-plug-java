package com.prevengos.plug.api.controller;

import com.prevengos.plug.app.dto.RegistrarPacienteCommand;
import com.prevengos.plug.app.usecase.RegistrarPacienteUseCase;
import com.prevengos.plug.domain.model.Paciente;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
    private static final String SOURCE = "api-rest";

    private final RegistrarPacienteUseCase registrarPacienteUseCase;

    public PacienteController(RegistrarPacienteUseCase registrarPacienteUseCase) {
        this.registrarPacienteUseCase = Objects.requireNonNull(registrarPacienteUseCase, "registrarPacienteUseCase");
    }

    @PostMapping
    public ResponseEntity<Paciente> registrar(@Valid @RequestBody RegistrarPacienteRequest request) {
        RegistrarPacienteCommand command = new RegistrarPacienteCommand(
                request.nombre,
                request.apellidos,
                request.documentoIdentidad,
                request.fechaNacimiento,
                request.empresa,
                request.centroTrabajo,
                SOURCE
        );
        Paciente paciente = registrarPacienteUseCase.execute(command);
        return ResponseEntity.accepted().body(paciente);
    }

    public static class RegistrarPacienteRequest {
        @NotBlank
        private final String nombre;
        @NotBlank
        private final String apellidos;
        @NotBlank
        @Size(min = 6, max = 32)
        private final String documentoIdentidad;
        private final LocalDate fechaNacimiento;
        private final String empresa;
        private final String centroTrabajo;

        @JsonCreator
        public RegistrarPacienteRequest(
                @JsonProperty("nombre") String nombre,
                @JsonProperty("apellidos") String apellidos,
                @JsonProperty("documentoIdentidad") String documentoIdentidad,
                @JsonProperty("fechaNacimiento") LocalDate fechaNacimiento,
                @JsonProperty("empresa") String empresa,
                @JsonProperty("centroTrabajo") String centroTrabajo
        ) {
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.documentoIdentidad = documentoIdentidad;
            this.fechaNacimiento = fechaNacimiento;
            this.empresa = empresa;
            this.centroTrabajo = centroTrabajo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getApellidos() {
            return apellidos;
        }

        public String getDocumentoIdentidad() {
            return documentoIdentidad;
        }

        public LocalDate getFechaNacimiento() {
            return fechaNacimiento;
        }

        public String getEmpresa() {
            return empresa;
        }

        public String getCentroTrabajo() {
            return centroTrabajo;
        }
    }
}
