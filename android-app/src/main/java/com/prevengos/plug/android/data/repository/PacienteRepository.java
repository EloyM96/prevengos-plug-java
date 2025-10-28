package com.prevengos.plug.android.data.repository;

import androidx.lifecycle.LiveData;

import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;

import java.util.List;
import java.util.UUID;

public class PacienteRepository {
    private final PacienteDao pacienteDao;

    public PacienteRepository(PacienteDao pacienteDao) {
        this.pacienteDao = pacienteDao;
    }

    public LiveData<List<PacienteEntity>> observePacientes() {
        return pacienteDao.observePacientes();
    }

    public PacienteEntity findById(String pacienteId) {
        return pacienteDao.findById(pacienteId);
    }

    public PacienteEntity createPaciente(String nif,
                                         String nombre,
                                         String apellidos,
                                         String telefono,
                                         String email) {
        long now = System.currentTimeMillis();
        PacienteEntity paciente = new PacienteEntity(
                UUID.randomUUID().toString(),
                nif,
                nombre,
                apellidos,
                null,
                null,
                telefono,
                email,
                null,
                null,
                null,
                null,
                null,
                now,
                null,
                true);
        pacienteDao.upsert(paciente);
        return paciente;
    }

    public PacienteEntity updatePaciente(String pacienteId,
                                         String nif,
                                         String nombre,
                                         String apellidos,
                                         String telefono,
                                         String email) {
        PacienteEntity existing = pacienteDao.findById(pacienteId);
        if (existing == null) {
            throw new IllegalArgumentException("Paciente no encontrado");
        }
        long now = System.currentTimeMillis();
        PacienteEntity actualizado = new PacienteEntity(
                existing.getPacienteId(),
                nif,
                nombre,
                apellidos,
                existing.getFechaNacimiento(),
                existing.getSexo(),
                telefono,
                email,
                existing.getEmpresaId(),
                existing.getCentroId(),
                existing.getExternoRef(),
                existing.getCreatedAt(),
                existing.getUpdatedAt(),
                now,
                existing.getSyncToken(),
                true);
        pacienteDao.upsert(actualizado);
        return actualizado;
    }
}
