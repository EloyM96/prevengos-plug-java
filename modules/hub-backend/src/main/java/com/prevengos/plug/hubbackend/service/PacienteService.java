package com.prevengos.plug.hubbackend.service;

import com.prevengos.plug.hubbackend.domain.Paciente;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.PacienteDto;
import com.prevengos.plug.hubbackend.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final SyncEventService syncEventService;

    public PacienteService(PacienteRepository pacienteRepository, SyncEventService syncEventService) {
        this.pacienteRepository = pacienteRepository;
        this.syncEventService = syncEventService;
    }

    @Transactional
    public BatchSyncResponse upsertPacientes(List<PacienteDto> pacientes, String source) {
        List<UUID> identifiers = new ArrayList<>();
        for (PacienteDto dto : pacientes) {
            Paciente entity = pacienteRepository.findById(dto.pacienteId())
                    .orElseGet(() -> new Paciente(dto.pacienteId()));
            boolean isNew = entity.getCreatedAt() == null;
            mapDtoToEntity(dto, entity);
            if (isNew) {
                entity.setCreatedAt(dto.createdAt() != null ? dto.createdAt() : OffsetDateTime.now(ZoneOffset.UTC));
            }
            entity.setUpdatedAt(dto.updatedAt() != null ? dto.updatedAt() : entity.getUpdatedAt());
            entity.setLastModified(dto.updatedAt() != null ? dto.updatedAt() : OffsetDateTime.now(ZoneOffset.UTC));
            pacienteRepository.save(entity);

            long syncToken = syncEventService
                    .registerEvent("paciente-upserted", dto, source, null, null, null)
                    .getSyncToken();
            entity.setSyncToken(syncToken);
            entity.setLastModified(OffsetDateTime.now(ZoneOffset.UTC));
            pacienteRepository.save(entity);
            identifiers.add(entity.getPacienteId());
        }
        return new BatchSyncResponse(identifiers.size(), identifiers);
    }

    private void mapDtoToEntity(PacienteDto dto, Paciente entity) {
        entity.setNif(dto.nif());
        entity.setNombre(dto.nombre());
        entity.setApellidos(dto.apellidos());
        entity.setFechaNacimiento(dto.fechaNacimiento());
        entity.setSexo(dto.sexo());
        entity.setTelefono(dto.telefono());
        entity.setEmail(dto.email());
        entity.setEmpresaId(dto.empresaId());
        entity.setCentroId(dto.centroId());
        entity.setExternoRef(dto.externoRef());
    }
}
