package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.model.Cuestionario;
import com.prevengos.plug.domain.model.PacienteId;
import com.prevengos.plug.domain.ports.CuestionariosPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryCuestionariosAdapter implements CuestionariosPort {
    private final Map<String, Cuestionario> cuestionarios = new ConcurrentHashMap<>();

    @Override
    public Cuestionario guardar(Cuestionario cuestionario) {
        cuestionarios.put(cuestionario.id().value().toString(), cuestionario);
        return cuestionario;
    }

    @Override
    public List<Cuestionario> obtenerPorPaciente(PacienteId pacienteId) {
        return cuestionarios.values().stream()
                .filter(cuestionario -> cuestionario.pacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Cuestionario> pendientesDeSincronizar(Instant desde) {
        return cuestionarios.values().stream()
                .filter(cuestionario -> {
                    Instant completadoEn = cuestionario.completadoEn();
                    return completadoEn == null || !completadoEn.isBefore(desde);
                })
                .collect(Collectors.toList());
    }
}
