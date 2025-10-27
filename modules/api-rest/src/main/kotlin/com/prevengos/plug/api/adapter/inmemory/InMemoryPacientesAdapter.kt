package com.prevengos.plug.api.adapter.inmemory

import com.prevengos.plug.domain.model.Paciente
import com.prevengos.plug.domain.model.PacienteId
import com.prevengos.plug.domain.ports.PacientesPort
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemoryPacientesAdapter : PacientesPort {
    private val pacientes = ConcurrentHashMap<PacienteId, Paciente>()

    override fun registrar(paciente: Paciente): Paciente {
        pacientes[paciente.id] = paciente.copy(actualizadoEn = Instant.now())
        return pacientes.getValue(paciente.id)
    }

    override fun buscarPorId(id: PacienteId): Paciente? = pacientes[id]

    override fun pacientesActualizadosDesde(desde: Instant): List<Paciente> =
        pacientes.values.filter { it.actualizadoEn.isAfter(desde) || it.actualizadoEn == desde }
}
