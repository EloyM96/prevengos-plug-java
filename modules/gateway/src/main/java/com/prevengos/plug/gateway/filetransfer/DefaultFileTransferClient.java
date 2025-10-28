package com.prevengos.plug.gateway.filetransfer;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Component
public class DefaultFileTransferClient implements FileTransferClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileTransferClient.class);

    @Override
    public void upload(Path localFile, FileTransferRequest request) {
        Objects.requireNonNull(localFile, "localFile must not be null");
        if (!Files.exists(localFile)) {
            throw new IllegalArgumentException("Local file does not exist: " + localFile);
        }
        if (request.protocol() == FileTransferProtocol.SFTP) {
            uploadViaSftp(localFile, request);
        } else if (request.protocol() == FileTransferProtocol.SMB) {
            uploadViaSmb(localFile, request);
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + request.protocol());
        }
    }

    private void uploadViaSftp(Path localFile, FileTransferRequest request) {
        SftpConnectionDetails details = request.sftp();
        if (details == null) {
            throw new IllegalArgumentException("SFTP connection details are required");
        }
        try (SSHClient client = new SSHClient()) {
            if (details.strictHostKeyChecking()) {
                if (details.knownHostsPath() != null) {
                    client.loadKnownHosts(details.knownHostsPath().toFile());
                } else {
                    throw new IllegalStateException("Known hosts path required when strict host key checking is enabled");
                }
            } else {
                client.addHostKeyVerifier(new PromiscuousVerifier());
            }
            client.connect(details.host(), details.port());
            if (details.password() != null) {
                client.authPassword(details.username(), details.password());
            } else {
                client.authPublickey(details.username());
            }
            try (SFTPClient sftp = client.newSFTPClient()) {
                String remoteDirectory = normalizeUnixPath(request.remoteDirectory());
                createSftpDirectories(sftp, remoteDirectory);
                String remotePath = remoteDirectory.isEmpty()
                        ? request.remoteFileName()
                        : remoteDirectory + "/" + request.remoteFileName();
                sftp.put(localFile.toString(), remotePath);
                logger.info("Archivo {} subido por SFTP a {}", localFile, remotePath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error uploading file via SFTP", e);
        }
    }

    private void createSftpDirectories(SFTPClient client, String remoteDirectory) throws IOException {
        if (remoteDirectory == null || remoteDirectory.isBlank()) {
            return;
        }
        String[] parts = remoteDirectory.split("/");
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (current.length() > 0) {
                current.append('/');
            }
            current.append(part);
            String path = current.toString();
            try {
                client.stat(path);
            } catch (IOException ex) {
                client.mkdirs(path);
            }
        }
    }

    private void uploadViaSmb(Path localFile, FileTransferRequest request) {
        SmbConnectionDetails details = request.smb();
        if (details == null) {
            throw new IllegalArgumentException("SMB connection details are required");
        }
        SMBClient client = new SMBClient();
        try (Connection connection = client.connect(details.host())) {
            AuthenticationContext context = new AuthenticationContext(
                    details.username(),
                    details.password() != null ? details.password().toCharArray() : new char[0],
                    details.domain() == null ? "" : details.domain());
            Session session = connection.authenticate(context);
            try (DiskShare share = (DiskShare) session.connectShare(details.share())) {
                String directory = normalizeWindowsPath(request.remoteDirectory());
                ensureSmbDirectories(share, directory);
                String remotePath = directory.isEmpty()
                        ? request.remoteFileName()
                        : directory + "\\" + request.remoteFileName();
                try (File file = share.openFile(remotePath,
                        EnumSet.of(AccessMask.GENERIC_WRITE),
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_OVERWRITE_IF,
                        EnumSet.noneOf(SMB2CreateOptions.class));
                     OutputStream outputStream = file.getOutputStream()) {
                    Files.copy(localFile, outputStream);
                    logger.info("Archivo {} subido por SMB a {}", localFile, remotePath);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error uploading file via SMB", e);
        }
    }

    private void ensureSmbDirectories(DiskShare share, String directory) {
        if (directory == null || directory.isBlank()) {
            return;
        }
        List<String> parts = Arrays.stream(directory.split("\\\\|/"))
                .filter(part -> !part.isBlank())
                .toList();
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (current.length() > 0) {
                current.append('\\');
            }
            current.append(part);
            String path = current.toString();
            if (!share.folderExists(path)) {
                share.mkdir(path);
            }
        }
    }

    private String normalizeUnixPath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.replace('\\', '/');
        normalized = normalized.replaceAll("/+", "/");
        if (normalized.equals("/")) {
            return "";
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.startsWith("/")) {
            return normalized;
        }
        return normalized;
    }

    private String normalizeWindowsPath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.replace('/', '\\');
        normalized = normalized.replaceAll("\\\\+", "\\\\");
        if (normalized.equals("\\")) {
            return "";
        }
        if (normalized.endsWith("\\")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
