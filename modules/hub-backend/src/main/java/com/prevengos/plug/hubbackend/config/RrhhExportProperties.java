package com.prevengos.plug.hubbackend.config;

import com.prevengos.plug.gateway.filetransfer.FileTransferProtocol;
import com.prevengos.plug.gateway.filetransfer.FileTransferRequest;
import com.prevengos.plug.gateway.filetransfer.SftpConnectionDetails;
import com.prevengos.plug.gateway.filetransfer.SmbConnectionDetails;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.Objects;

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
        private FileTransferProtocol protocol;
        private String remoteDir;
        private final Sftp sftp = new Sftp();
        private final Smb smb = new Smb();

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

        public Sftp getSftp() {
            return sftp;
        }

        public Smb getSmb() {
            return smb;
        }

        public FileTransferRequest toRequest(String remoteFileName) {
            if (!enabled) {
                throw new IllegalStateException("La entrega remota está deshabilitada");
            }
            Objects.requireNonNull(remoteFileName, "remoteFileName no puede ser nulo");
            if (protocol == null) {
                throw new IllegalStateException("No se configuró el protocolo de entrega remota");
            }
            return switch (protocol) {
                case SFTP -> new FileTransferRequest(protocol, remoteDir, remoteFileName, sftp.toDetails(), null);
                case SMB -> new FileTransferRequest(protocol, remoteDir, remoteFileName, null, smb.toDetails());
            };
        }

        private static String requireNonBlank(String value, String property) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("La propiedad de entrega '" + property + "' es obligatoria");
            }
            return value;
        }

        public static class Sftp {
            private String host;
            private int port = 22;
            private String username;
            private String password;
            private boolean strictHostKeyChecking = true;
            private Path knownHostsPath;

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

            public Path getKnownHostsPath() {
                return knownHostsPath;
            }

            public void setKnownHostsPath(Path knownHostsPath) {
                this.knownHostsPath = knownHostsPath;
            }

            private SftpConnectionDetails toDetails() {
                return new SftpConnectionDetails(
                        requireNonBlank(host, "sftp.host"),
                        port,
                        requireNonBlank(username, "sftp.username"),
                        password,
                        strictHostKeyChecking,
                        knownHostsPath
                );
            }
        }

        public static class Smb {
            private String host;
            private String share;
            private String username;
            private String password;
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

            private SmbConnectionDetails toDetails() {
                return new SmbConnectionDetails(
                        requireNonBlank(host, "smb.host"),
                        requireNonBlank(share, "smb.share"),
                        requireNonBlank(username, "smb.username"),
                        password,
                        domain
                );
            }
        }
    }
}
