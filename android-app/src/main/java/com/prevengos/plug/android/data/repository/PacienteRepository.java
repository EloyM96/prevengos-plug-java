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

    public PacienteEntity createPaciente(String nif, String nombre, String apellidos, String telefono, String email) {
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
                true
        );
        pacienteDao.upsert(paciente);
        return paciente;
    }
}
