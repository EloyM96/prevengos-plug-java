package com.prevengos.plug.api.adapter.inmemory

import com.prevengos.plug.domain.model.Cita
import com.prevengos.plug.domain.model.PacienteId
import com.prevengos.plug.domain.ports.CitasPort
import java.util.concurrent.ConcurrentHashMap

class InMemoryCitasAdapter : CitasPort {
    private val citas = ConcurrentHashMap<String, Cita>()

    override fun programar(cita: Cita): Cita {
        citas[cita.id.value.toString()] = cita
        return cita
    }

    override fun obtenerPorPaciente(pacienteId: PacienteId): List<Cita> =
        citas.values.filter { it.pacienteId == pacienteId }
}
