package com.prevengos.plug.hubbackend.web;

import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.shared.dto.CuestionarioDto;
import com.prevengos.plug.shared.dto.PacienteDto;
import com.prevengos.plug.hubbackend.dto.SyncPullResponse;
import com.prevengos.plug.hubbackend.service.CuestionarioService;
import com.prevengos.plug.hubbackend.service.PacienteService;
import com.prevengos.plug.hubbackend.service.SyncEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/sincronizacion")
@Validated
public class SynchronizationController {

    private final PacienteService pacienteService;
    private final CuestionarioService cuestionarioService;
    private final SyncEventService syncEventService;

    public SynchronizationController(PacienteService pacienteService,
                                      CuestionarioService cuestionarioService,
                                      SyncEventService syncEventService) {
        this.pacienteService = pacienteService;
        this.cuestionarioService = cuestionarioService;
        this.syncEventService = syncEventService;
    }

    @PostMapping("/pacientes")
    public ResponseEntity<BatchSyncResponse> syncPacientes(@RequestBody @Valid List<@Valid PacienteDto> pacientes,
                                                           @RequestHeader(value = "X-Source-System", required = false) String source) {
        List<PacienteDto> payload = pacientes != null ? pacientes : Collections.emptyList();
        BatchSyncResponse response = pacienteService.upsertPacientes(payload, source);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cuestionarios")
    public ResponseEntity<BatchSyncResponse> syncCuestionarios(@RequestBody @Valid List<@Valid CuestionarioDto> cuestionarios,
                                                               @RequestHeader(value = "X-Source-System", required = false) String source) {
        List<CuestionarioDto> payload = cuestionarios != null ? cuestionarios : Collections.emptyList();
        BatchSyncResponse response = cuestionarioService.upsertCuestionarios(payload, source);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pull")
    public ResponseEntity<SyncPullResponse> pull(@RequestParam(name = "syncToken", required = false) Long syncToken,
                                                 @RequestParam(name = "since", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
                                                 @RequestParam(name = "limit", defaultValue = "100")
                                                 @Min(1) @Max(500) int limit) {
        SyncPullResponse response = syncEventService.pull(syncToken, since, limit);
        return ResponseEntity.ok(response);
    }
}
