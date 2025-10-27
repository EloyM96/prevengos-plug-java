package com.prevengos.plug.domain.ports;

import com.prevengos.plug.domain.model.Cuestionario;
import com.prevengos.plug.domain.model.PacienteId;

import java.time.Instant;
import java.util.List;

public interface CuestionariosPort {
    Cuestionario guardar(Cuestionario cuestionario);

    List<Cuestionario> obtenerPorPaciente(PacienteId pacienteId);

    List<Cuestionario> pendientesDeSincronizar(Instant desde);
}
