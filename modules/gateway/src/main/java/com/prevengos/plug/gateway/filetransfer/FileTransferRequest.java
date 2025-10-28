package com.prevengos.plug.gateway.filetransfer;

import java.util.Objects;

public record FileTransferRequest(
        FileTransferProtocol protocol,
        String remoteDirectory,
        String remoteFileName,
        SftpConnectionDetails sftp,
        SmbConnectionDetails smb
) {

    public FileTransferRequest {
        Objects.requireNonNull(protocol, "protocol must not be null");
        Objects.requireNonNull(remoteFileName, "remoteFileName must not be null");
    }
}
