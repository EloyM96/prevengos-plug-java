package com.prevengos.plug.hubbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "hub.jobs.rrhh-import")
public class RrhhImportProperties {

    private Path inboxDir = Path.of("/var/prevengos/oficial/incoming");
    private Path archiveDir = Path.of("/var/prevengos/oficial/archive");
    private Path errorDir = Path.of("/var/prevengos/oficial/error");
    private String processName = "rrhh";
    private String cron = "0 30 3 * * *";
    private int maxDropsPerRun = 4;

    public Path getInboxDir() {
        return inboxDir;
    }

    public void setInboxDir(Path inboxDir) {
        this.inboxDir = inboxDir;
    }

    public Path getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(Path archiveDir) {
        this.archiveDir = archiveDir;
    }

    public Path getErrorDir() {
        return errorDir;
    }

    public void setErrorDir(Path errorDir) {
        this.errorDir = errorDir;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getMaxDropsPerRun() {
        return maxDropsPerRun;
    }

    public void setMaxDropsPerRun(int maxDropsPerRun) {
        this.maxDropsPerRun = maxDropsPerRun;
    }
}
