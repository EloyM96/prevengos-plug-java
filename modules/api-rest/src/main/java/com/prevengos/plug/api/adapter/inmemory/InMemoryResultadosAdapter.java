package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.model.PacienteId;
import com.prevengos.plug.domain.model.ResultadoAnalitico;
import com.prevengos.plug.domain.ports.ResultadosPort;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryResultadosAdapter implements ResultadosPort {
    private final Map<String, ResultadoAnalitico> resultados = new ConcurrentHashMap<>();

    @Override
    public ResultadoAnalitico registrar(ResultadoAnalitico resultado) {
        resultados.put(resultado.id().value().toString(), resultado);
        return resultado;
    }

    @Override
    public List<ResultadoAnalitico> obtenerPorPaciente(PacienteId pacienteId) {
        return resultados.values().stream()
                .filter(resultado -> resultado.pacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }
}
