package com.prevengos.plug.hubbackend.controller;

import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rrhh/export")
public class RrhhExportController {

    private final RrhhCsvExportJob rrhhCsvExportJob;

    public RrhhExportController(RrhhCsvExportJob rrhhCsvExportJob) {
        this.rrhhCsvExportJob = rrhhCsvExportJob;
    }

    @PostMapping
    public ResponseEntity<?> triggerExport(@RequestBody(required = false) Map<String, String> body) throws Exception {
        String triggerType = body != null && body.containsKey("trigger_type")
                ? body.get("trigger_type") : "manual";
        RrhhCsvExportJob.RrhhExportResult result = rrhhCsvExportJob.runExport(triggerType);
        return ResponseEntity.ok(Map.of(
                "trace_id", result.traceId().toString(),
                "staging_dir", result.stagingDir().toString(),
                "archive_dir", result.archiveDir().toString(),
                "pacientes_count", result.pacientesCount(),
                "cuestionarios_count", result.cuestionariosCount()
        ));
    }
}
