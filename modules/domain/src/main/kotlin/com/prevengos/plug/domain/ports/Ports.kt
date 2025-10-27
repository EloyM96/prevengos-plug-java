package com.prevengos.plug.domain.ports

import com.prevengos.plug.domain.model.Cita
import com.prevengos.plug.domain.model.CursoMoodle
import com.prevengos.plug.domain.model.Cuestionario
import com.prevengos.plug.domain.model.Notificacion
import com.prevengos.plug.domain.model.Paciente
import com.prevengos.plug.domain.model.PacienteId
import com.prevengos.plug.domain.model.ResultadoAnalitico
import java.time.Instant

interface PacientesPort {
    fun registrar(paciente: Paciente): Paciente
    fun buscarPorId(id: PacienteId): Paciente?
    fun pacientesActualizadosDesde(desde: Instant): List<Paciente>
}

interface CuestionariosPort {
    fun guardar(cuestionario: Cuestionario): Cuestionario
    fun obtenerPorPaciente(pacienteId: PacienteId): List<Cuestionario>
    fun pendientesDeSincronizar(desde: Instant): List<Cuestionario>
}

interface CitasPort {
    fun programar(cita: Cita): Cita
    fun obtenerPorPaciente(pacienteId: PacienteId): List<Cita>
}

interface ResultadosPort {
    fun registrar(resultado: ResultadoAnalitico): ResultadoAnalitico
    fun obtenerPorPaciente(pacienteId: PacienteId): List<ResultadoAnalitico>
}

interface RRHHPort {
    fun exportarPlantillaTrabajadores(desde: Instant)
}

interface NotificacionPort {
    fun programar(notificacion: Notificacion)
}

interface MoodlePort {
    fun sincronizarCursos(cursos: List<CursoMoodle>)
}

interface PrevengosPort {
    fun publicarEventoDominio(payload: Any)
}
