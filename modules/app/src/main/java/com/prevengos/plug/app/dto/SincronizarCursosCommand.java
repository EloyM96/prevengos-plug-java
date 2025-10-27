package com.prevengos.plug.app.dto;

import com.prevengos.plug.domain.model.CursoId;
import com.prevengos.plug.domain.model.CursoMoodle;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public record SincronizarCursosCommand(List<Curso> cursos) {
    public SincronizarCursosCommand {
        Objects.requireNonNull(cursos, "cursos");
    }

    public List<CursoMoodle> toCursos() {
        return cursos.stream()
                .map(Curso::toCursoMoodle)
                .collect(Collectors.toList());
    }

    public record Curso(
            String nombre,
            String codigo,
            String url
    ) {
        public Curso {
            Objects.requireNonNull(nombre, "nombre");
            Objects.requireNonNull(codigo, "codigo");
            Objects.requireNonNull(url, "url");
        }

        public CursoMoodle toCursoMoodle() {
            return new CursoMoodle(
                    new CursoId(UUID.randomUUID()),
                    nombre,
                    codigo,
                    url
            );
        }
    }
}
