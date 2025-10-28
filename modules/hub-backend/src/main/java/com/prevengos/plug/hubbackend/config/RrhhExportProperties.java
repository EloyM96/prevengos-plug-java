package com.prevengos.plug.hubbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "hub.jobs.rrhh-export")
public class RrhhExportProperties {

    private Path baseDir;
    private Path archiveDir;
    private String processName = "rrhh";
    private String origin = "hub";
    private String operator = "hub-system";
    private int lookbackHours = 24;
    private Delivery delivery = new Delivery();

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    public Path getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(Path archiveDir) {
        this.archiveDir = archiveDir;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getLookbackHours() {
        return lookbackHours;
    }

    public void setLookbackHours(int lookbackHours) {
        this.lookbackHours = lookbackHours;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public static class Delivery {
        private boolean enabled;
        private String protocol;
        private String remoteDir;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getRemoteDir() {
            return remoteDir;
        }

        public void setRemoteDir(String remoteDir) {
            this.remoteDir = remoteDir;
        }
    }
}
