package com.prevengos.plug.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@JvmInline
value class PacienteId(val value: UUID)

@JvmInline
value class CuestionarioId(val value: UUID)

@JvmInline
value class CitaId(val value: UUID)

@JvmInline
value class ResultadoId(val value: UUID)

@JvmInline
value class CursoId(val value: UUID)

@JvmInline
value class NotificacionId(val value: UUID)

data class Paciente(
    val id: PacienteId,
    val nombre: String,
    val apellidos: String,
    val documentoIdentidad: String,
    val fechaNacimiento: LocalDate,
    val empresa: String?,
    val centroTrabajo: String?,
    val actualizadoEn: Instant
)

data class Cuestionario(
    val id: CuestionarioId,
    val pacienteId: PacienteId,
    val plantillaCodigo: String,
    val estado: EstadoCuestionario,
    val respuestas: Map<String, Any?>,
    val completadoEn: Instant?
)

enum class EstadoCuestionario {
    BORRADOR,
    COMPLETADO,
    VALIDADO
}

data class Cita(
    val id: CitaId,
    val pacienteId: PacienteId,
    val fechaHora: Instant,
    val motivo: String,
    val localizacion: String
)

data class ResultadoAnalitico(
    val id: ResultadoId,
    val pacienteId: PacienteId,
    val cuestionarioId: CuestionarioId?,
    val tipo: String,
    val valor: String,
    val registradoEn: Instant
)

data class CursoMoodle(
    val id: CursoId,
    val nombre: String,
    val codigo: String,
    val url: String
)

data class Notificacion(
    val id: NotificacionId,
    val canal: CanalNotificacion,
    val destino: String,
    val plantilla: String,
    val datos: Map<String, Any?>
)

enum class CanalNotificacion {
    EMAIL,
    SMS,
    WHATSAPP
}
