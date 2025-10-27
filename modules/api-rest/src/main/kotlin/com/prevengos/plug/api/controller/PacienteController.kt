package com.prevengos.plug.api.controller

import com.prevengos.plug.app.dto.RegistrarPacienteCommand
import com.prevengos.plug.app.usecase.RegistrarPacienteUseCase
import com.prevengos.plug.domain.model.Paciente
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/pacientes")
class PacienteController(
    private val registrarPacienteUseCase: RegistrarPacienteUseCase
) {
    @PostMapping
    fun registrar(@Valid @RequestBody request: RegistrarPacienteRequest): ResponseEntity<Paciente> {
        val command = RegistrarPacienteCommand(
            nombre = request.nombre,
            apellidos = request.apellidos,
            documentoIdentidad = request.documentoIdentidad,
            fechaNacimiento = request.fechaNacimiento,
            empresa = request.empresa,
            centroTrabajo = request.centroTrabajo,
            fuente = "api-rest"
        )
        val paciente = registrarPacienteUseCase.execute(command)
        return ResponseEntity.accepted().body(paciente)
    }
}

data class RegistrarPacienteRequest(
    @field:NotBlank
    val nombre: String,
    @field:NotBlank
    val apellidos: String,
    @field:NotBlank
    @field:Size(min = 6, max = 32)
    val documentoIdentidad: String,
    val fechaNacimiento: LocalDate,
    val empresa: String?,
    val centroTrabajo: String?
)
