package com.prevengos.plug.gateway.filetransfer;

import java.nio.file.Path;

public record SftpConnectionDetails(
        String host,
        int port,
        String username,
        String password,
        boolean strictHostKeyChecking,
        Path knownHostsPath
) {
}
