package com.prevengos.plug.hubbackend.repository;

import com.prevengos.plug.hubbackend.domain.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, UUID> {
}
