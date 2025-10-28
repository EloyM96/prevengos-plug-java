package com.prevengos.plug.desktop.pacientes;

import com.prevengos.plug.shared.contracts.v1.Paciente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository {

    List<Paciente> findAll();

    List<Paciente> search(String filter);

    Optional<Paciente> findById(UUID pacienteId);

    Paciente save(Paciente paciente);

    void delete(UUID pacienteId);
}
