package com.prevengos.plug.domain.ports;

import com.prevengos.plug.domain.model.CursoMoodle;

import java.util.List;

public interface MoodlePort {
    void sincronizarCursos(List<CursoMoodle> cursos);
}
