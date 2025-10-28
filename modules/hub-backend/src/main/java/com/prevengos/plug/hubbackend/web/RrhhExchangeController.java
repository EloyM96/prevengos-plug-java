package com.prevengos.plug.hubbackend.web;

import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
import com.prevengos.plug.hubbackend.job.RrhhCsvImportJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rrhh")
public class RrhhExchangeController {

    private final RrhhCsvExportJob exportJob;
    private final RrhhCsvImportJob importJob;

    public RrhhExchangeController(RrhhCsvExportJob exportJob, RrhhCsvImportJob importJob) {
        this.exportJob = exportJob;
        this.importJob = importJob;
    }

    @PostMapping("/export")
    public ResponseEntity<RrhhExportResponse> triggerExport() throws Exception {
        RrhhCsvExportJob.RrhhExportResult result = exportJob.runExport("manual");
        RrhhExportResponse body = new RrhhExportResponse(
                result.traceId().toString(),
                result.remotePath(),
                result.stagingDir().toString(),
                result.archiveDir().toString(),
                result.pacientesCount(),
                result.cuestionariosCount()
        );
        return ResponseEntity.accepted().body(body);
    }

    @PostMapping("/import")
    public ResponseEntity<RrhhImportResponse> triggerImport() {
        RrhhCsvImportJob.RrhhImportReport report = importJob.processInbox("manual");
        RrhhImportResponse body = new RrhhImportResponse(
                report.processedDrops(),
                report.pacientesImported(),
                report.cuestionariosImported(),
                report.archivedDrops(),
                report.failedDrops()
        );
        return ResponseEntity.accepted().body(body);
    }

    public record RrhhExportResponse(String traceId,
                                     String remotePath,
                                     String stagingDir,
                                     String archiveDir,
                                     int pacientes,
                                     int cuestionarios) {
    }

    public record RrhhImportResponse(int processedDrops,
                                     int pacientesImported,
                                     int cuestionariosImported,
                                     java.util.List<String> archivedDrops,
                                     java.util.List<String> failedDrops) {
    }
}
