package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.model.Cita;
import com.prevengos.plug.domain.model.PacienteId;
import com.prevengos.plug.domain.ports.CitasPort;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryCitasAdapter implements CitasPort {
    private final Map<String, Cita> citas = new ConcurrentHashMap<>();

    @Override
    public Cita programar(Cita cita) {
        citas.put(cita.id().value().toString(), cita);
        return cita;
    }

    @Override
    public List<Cita> obtenerPorPaciente(PacienteId pacienteId) {
        return citas.values().stream()
                .filter(cita -> cita.pacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }
}
