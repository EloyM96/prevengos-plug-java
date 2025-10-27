package com.prevengos.plug.app.dto

import com.prevengos.plug.domain.model.CanalNotificacion
import com.prevengos.plug.domain.model.CitaId
import com.prevengos.plug.domain.model.Cita as CitaModel
import com.prevengos.plug.domain.model.Cuestionario
import com.prevengos.plug.domain.model.CuestionarioId
import com.prevengos.plug.domain.model.CursoId
import com.prevengos.plug.domain.model.CursoMoodle
import com.prevengos.plug.domain.model.EstadoCuestionario
import com.prevengos.plug.domain.model.Notificacion
import com.prevengos.plug.domain.model.NotificacionId
import com.prevengos.plug.domain.model.Paciente
import com.prevengos.plug.domain.model.PacienteId
import com.prevengos.plug.domain.model.ResultadoAnalitico
import com.prevengos.plug.domain.model.ResultadoId
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class RegistrarPacienteCommand(
    val nombre: String,
    val apellidos: String,
    val documentoIdentidad: String,
    val fechaNacimiento: LocalDate,
    val empresa: String?,
    val centroTrabajo: String?,
    val fuente: String
) {
    fun toPaciente(): Paciente = Paciente(
        id = PacienteId(UUID.randomUUID()),
        nombre = nombre,
        apellidos = apellidos,
        documentoIdentidad = documentoIdentidad,
        fechaNacimiento = fechaNacimiento,
        empresa = empresa,
        centroTrabajo = centroTrabajo,
        actualizadoEn = Instant.now()
    )
}

data class GuardarCuestionarioCommand(
    val pacienteId: UUID,
    val plantillaCodigo: String,
    val estado: EstadoCuestionario,
    val respuestas: Map<String, Any?>,
    val fuente: String
) {
    fun toCuestionario(): Cuestionario = Cuestionario(
        id = CuestionarioId(UUID.randomUUID()),
        pacienteId = PacienteId(pacienteId),
        plantillaCodigo = plantillaCodigo,
        estado = estado,
        respuestas = respuestas,
        completadoEn = if (estado == EstadoCuestionario.COMPLETADO || estado == EstadoCuestionario.VALIDADO) Instant.now() else null
    )
}

data class ProgramarCitaCommand(
    val pacienteId: UUID,
    val fechaHora: Instant,
    val motivo: String,
    val localizacion: String,
    val fuente: String
) {
    fun toCita(): CitaModel = CitaModel(
        id = CitaId(UUID.randomUUID()),
        pacienteId = PacienteId(pacienteId),
        fechaHora = fechaHora,
        motivo = motivo,
        localizacion = localizacion
    )
}

data class RegistrarResultadoCommand(
    val pacienteId: UUID,
    val cuestionarioId: UUID?,
    val tipo: String,
    val valor: String,
    val fuente: String
) {
    fun toResultado(): ResultadoAnalitico = ResultadoAnalitico(
        id = ResultadoId(UUID.randomUUID()),
        pacienteId = PacienteId(pacienteId),
        cuestionarioId = cuestionarioId?.let(::CuestionarioId),
        tipo = tipo,
        valor = valor,
        registradoEn = Instant.now()
    )
}

data class ProgramarNotificacionCommand(
    val canal: CanalNotificacion,
    val destino: String,
    val plantilla: String,
    val datos: Map<String, Any?>
) {
    fun toNotificacion(): Notificacion = Notificacion(
        id = NotificacionId(UUID.randomUUID()),
        canal = canal,
        destino = destino,
        plantilla = plantilla,
        datos = datos
    )
}

data class SincronizarCursosCommand(
    val cursos: List<Curso>
) {
    data class Curso(
        val nombre: String,
        val codigo: String,
        val url: String
    ) {
        fun toCursoMoodle(): CursoMoodle = CursoMoodle(
            id = CursoId(UUID.randomUUID()),
            nombre = nombre,
            codigo = codigo,
            url = url
        )
    }

    fun toCursos(): List<CursoMoodle> = cursos.map { it.toCursoMoodle() }
}
