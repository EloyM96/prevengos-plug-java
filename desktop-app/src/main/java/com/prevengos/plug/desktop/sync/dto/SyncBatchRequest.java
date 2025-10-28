package com.prevengos.plug.desktop.sync.dto;

import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;

import java.util.List;

public record SyncBatchRequest(
        String sourceSystem,
        List<Paciente> pacientes,
        List<Cuestionario> cuestionarios
) {
}
