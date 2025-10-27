package com.prevengos.plug.api.controller;

import com.prevengos.plug.app.dto.GuardarCuestionarioCommand;
import com.prevengos.plug.app.usecase.GuardarCuestionarioUseCase;
import com.prevengos.plug.domain.model.Cuestionario;
import com.prevengos.plug.domain.model.EstadoCuestionario;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/cuestionarios")
public class CuestionarioController {
    private static final String SOURCE = "api-rest";

    private final GuardarCuestionarioUseCase guardarCuestionarioUseCase;

    public CuestionarioController(GuardarCuestionarioUseCase guardarCuestionarioUseCase) {
        this.guardarCuestionarioUseCase = Objects.requireNonNull(guardarCuestionarioUseCase, "guardarCuestionarioUseCase");
    }

    @PostMapping
    public ResponseEntity<Cuestionario> guardar(@Valid @RequestBody GuardarCuestionarioRequest request) {
        GuardarCuestionarioCommand command = new GuardarCuestionarioCommand(
                request.pacienteId,
                request.plantillaCodigo,
                request.estado,
                request.respuestas,
                SOURCE
        );
        Cuestionario cuestionario = guardarCuestionarioUseCase.execute(command);
        return ResponseEntity.accepted().body(cuestionario);
    }

    public static class GuardarCuestionarioRequest {
        private final UUID pacienteId;
        @NotBlank
        private final String plantillaCodigo;
        private final EstadoCuestionario estado;
        @NotEmpty
        private final Map<String, Object> respuestas;

        @JsonCreator
        public GuardarCuestionarioRequest(
                @JsonProperty("pacienteId") UUID pacienteId,
                @JsonProperty("plantillaCodigo") String plantillaCodigo,
                @JsonProperty("estado") EstadoCuestionario estado,
                @JsonProperty("respuestas") Map<String, Object> respuestas
        ) {
            this.pacienteId = pacienteId;
            this.plantillaCodigo = plantillaCodigo;
            this.estado = estado;
            this.respuestas = respuestas;
        }

        public UUID getPacienteId() {
            return pacienteId;
        }

        public String getPlantillaCodigo() {
            return plantillaCodigo;
        }

        public EstadoCuestionario getEstado() {
            return estado;
        }

        public Map<String, Object> getRespuestas() {
            return respuestas;
        }
    }
}
