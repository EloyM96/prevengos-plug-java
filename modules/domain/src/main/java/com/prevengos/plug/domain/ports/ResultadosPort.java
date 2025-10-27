package com.prevengos.plug.domain.ports;

import com.prevengos.plug.domain.model.PacienteId;
import com.prevengos.plug.domain.model.ResultadoAnalitico;

import java.util.List;

public interface ResultadosPort {
    ResultadoAnalitico registrar(ResultadoAnalitico resultado);

    List<ResultadoAnalitico> obtenerPorPaciente(PacienteId pacienteId);
}
