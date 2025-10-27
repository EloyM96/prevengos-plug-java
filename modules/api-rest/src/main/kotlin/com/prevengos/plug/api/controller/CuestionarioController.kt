package com.prevengos.plug.api.controller

import com.prevengos.plug.app.dto.GuardarCuestionarioCommand
import com.prevengos.plug.app.usecase.GuardarCuestionarioUseCase
import com.prevengos.plug.domain.model.Cuestionario
import com.prevengos.plug.domain.model.EstadoCuestionario
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/cuestionarios")
class CuestionarioController(
    private val guardarCuestionarioUseCase: GuardarCuestionarioUseCase
) {
    @PostMapping
    fun guardar(@Valid @RequestBody request: GuardarCuestionarioRequest): ResponseEntity<Cuestionario> {
        val command = GuardarCuestionarioCommand(
            pacienteId = request.pacienteId,
            plantillaCodigo = request.plantillaCodigo,
            estado = request.estado,
            respuestas = request.respuestas,
            fuente = "api-rest"
        )
        val cuestionario = guardarCuestionarioUseCase.execute(command)
        return ResponseEntity.accepted().body(cuestionario)
    }
}

data class GuardarCuestionarioRequest(
    val pacienteId: UUID,
    @field:NotBlank
    val plantillaCodigo: String,
    val estado: EstadoCuestionario,
    @field:NotEmpty
    val respuestas: Map<String, Any?>
)
