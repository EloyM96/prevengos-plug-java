package com.prevengos.plug.desktop.sync.dto;

import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;

public record SyncChange(
        String entityType,
        Paciente paciente,
        Cuestionario cuestionario,
        long syncToken
) {
    public boolean isPaciente() {
        return "paciente".equalsIgnoreCase(entityType);
    }

    public boolean isCuestionario() {
        return "cuestionario".equalsIgnoreCase(entityType);
    }
}
