package com.prevengos.plug.api.adapter.inmemory

import com.prevengos.plug.domain.model.Cuestionario
import com.prevengos.plug.domain.model.PacienteId
import com.prevengos.plug.domain.ports.CuestionariosPort
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemoryCuestionariosAdapter : CuestionariosPort {
    private val cuestionarios = ConcurrentHashMap<String, Cuestionario>()

    override fun guardar(cuestionario: Cuestionario): Cuestionario {
        val key = cuestionario.id.value.toString()
        cuestionarios[key] = cuestionario
        return cuestionario
    }

    override fun obtenerPorPaciente(pacienteId: PacienteId): List<Cuestionario> =
        cuestionarios.values.filter { it.pacienteId == pacienteId }

    override fun pendientesDeSincronizar(desde: Instant): List<Cuestionario> =
        cuestionarios.values.filter { cuestionario ->
            cuestionario.completadoEn?.let { it.isAfter(desde) || it == desde } ?: true
        }
}
