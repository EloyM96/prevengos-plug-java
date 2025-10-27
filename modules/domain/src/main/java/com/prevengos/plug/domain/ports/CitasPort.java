package com.prevengos.plug.domain.ports;

import com.prevengos.plug.domain.model.Cita;
import com.prevengos.plug.domain.model.PacienteId;

import java.util.List;

public interface CitasPort {
    Cita programar(Cita cita);

    List<Cita> obtenerPorPaciente(PacienteId pacienteId);
}
