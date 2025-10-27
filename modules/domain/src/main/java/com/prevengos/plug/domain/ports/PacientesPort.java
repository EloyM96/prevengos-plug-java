package com.prevengos.plug.domain.ports;

import com.prevengos.plug.domain.model.Paciente;
import com.prevengos.plug.domain.model.PacienteId;

import java.time.Instant;
import java.util.List;

public interface PacientesPort {
    Paciente registrar(Paciente paciente);

    Paciente buscarPorId(PacienteId id);

    List<Paciente> pacientesActualizadosDesde(Instant desde);
}
