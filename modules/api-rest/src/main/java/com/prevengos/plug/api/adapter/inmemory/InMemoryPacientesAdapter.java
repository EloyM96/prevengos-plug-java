package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.model.Paciente;
import com.prevengos.plug.domain.model.PacienteId;
import com.prevengos.plug.domain.ports.PacientesPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPacientesAdapter implements PacientesPort {
    private final Map<PacienteId, Paciente> pacientes = new ConcurrentHashMap<>();

    @Override
    public Paciente registrar(Paciente paciente) {
        Paciente actualizado = new Paciente(
                paciente.id(),
                paciente.nombre(),
                paciente.apellidos(),
                paciente.documentoIdentidad(),
                paciente.fechaNacimiento(),
                paciente.empresa(),
                paciente.centroTrabajo(),
                Instant.now()
        );
        pacientes.put(actualizado.id(), actualizado);
        return actualizado;
    }

    @Override
    public Paciente buscarPorId(PacienteId id) {
        return pacientes.get(id);
    }

    @Override
    public List<Paciente> pacientesActualizadosDesde(Instant desde) {
        return pacientes.values().stream()
                .filter(paciente -> !paciente.actualizadoEn().isBefore(desde))
                .collect(Collectors.toList());
    }
}
