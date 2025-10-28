package com.prevengos.plug.hubbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "hub.jobs.rrhh-export")
public class RrhhExportProperties {

    private Path baseDir = Path.of("/var/prevengos/oficial/outgoing");
    private String processName = "rrhh";
    private String cron = "0 0 3 * * *";
    private int lookbackHours = 24;

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
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

    public int getLookbackHours() {
        return lookbackHours;
    }

    public void setLookbackHours(int lookbackHours) {
        this.lookbackHours = lookbackHours;
    }
}
