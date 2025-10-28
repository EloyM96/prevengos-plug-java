package com.prevengos.plug.hubbackend.config;

import com.prevengos.plug.gateway.filetransfer.FileTransferProtocol;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "hub.jobs.rrhh-export")
public class RrhhExportProperties {

    private Path baseDir = Path.of("/var/prevengos/oficial/outgoing");
    private Path archiveDir = Path.of("/var/prevengos/oficial/archive");
    private String processName = "rrhh";
    private String origin = "hub";
    private String operator = "hub-system";
    private String cron = "0 0 3 * * *";
    private int lookbackHours = 24;
    private DeliveryProperties delivery = new DeliveryProperties();

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

    public DeliveryProperties getDelivery() {
        return delivery;
    }

    public void setDelivery(DeliveryProperties delivery) {
        this.delivery = delivery;
    }

    public static class DeliveryProperties {
        private boolean enabled = true;
        private FileTransferProtocol protocol = FileTransferProtocol.SFTP;
        private String remoteDir = "/prevengos/oficial/rrhh";
        private SftpProperties sftp = new SftpProperties();
        private SmbProperties smb = new SmbProperties();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public FileTransferProtocol getProtocol() {
            return protocol;
        }

        public void setProtocol(FileTransferProtocol protocol) {
            this.protocol = protocol;
        }

        public String getRemoteDir() {
            return remoteDir;
        }

        public void setRemoteDir(String remoteDir) {
            this.remoteDir = remoteDir;
        }

        public SftpProperties getSftp() {
            return sftp;
        }

        public void setSftp(SftpProperties sftp) {
            this.sftp = sftp;
        }

        public SmbProperties getSmb() {
            return smb;
        }

        public void setSmb(SmbProperties smb) {
            this.smb = smb;
        }
    }

    public static class SftpProperties {
        private String host = "localhost";
        private int port = 22;
        private String username = "prevengos";
        private String password = "changeit";
        private boolean strictHostKeyChecking = false;
        private Path knownHosts;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isStrictHostKeyChecking() {
            return strictHostKeyChecking;
        }

        public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
            this.strictHostKeyChecking = strictHostKeyChecking;
        }

        public Path getKnownHosts() {
            return knownHosts;
        }

        public void setKnownHosts(Path knownHosts) {
            this.knownHosts = knownHosts;
        }
    }

    public static class SmbProperties {
        private String host = "localhost";
        private String share = "rrhh";
        private String username = "prevengos";
        private String password = "changeit";
        private String domain;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getShare() {
            return share;
        }

        public void setShare(String share) {
            this.share = share;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }
}
