package com.prevengos.plug.api.adapter.inmemory

import com.prevengos.plug.domain.model.PacienteId
import com.prevengos.plug.domain.model.ResultadoAnalitico
import com.prevengos.plug.domain.ports.ResultadosPort
import java.util.concurrent.ConcurrentHashMap

class InMemoryResultadosAdapter : ResultadosPort {
    private val resultados = ConcurrentHashMap<String, ResultadoAnalitico>()

    override fun registrar(resultado: ResultadoAnalitico): ResultadoAnalitico {
        resultados[resultado.id.value.toString()] = resultado
        return resultado
    }

    override fun obtenerPorPaciente(pacienteId: PacienteId): List<ResultadoAnalitico> =
        resultados.values.filter { it.pacienteId == pacienteId }
}
