package com.prevengos.plug.gateway.filetransfer;

public record SmbConnectionDetails(
        String host,
        String share,
        String username,
        String password,
        String domain
) {
}
