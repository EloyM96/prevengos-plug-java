package com.prevengos.plug.hubbackend.repository;

import com.prevengos.plug.hubbackend.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    List<Paciente> findByLastModifiedGreaterThanEqualOrderByLastModifiedAsc(OffsetDateTime lastModified);
}
