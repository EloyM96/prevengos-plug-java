package com.prevengos.plug.gateway.filetransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Implementación que sólo registra la entrega.
 */
public class NoOpFileTransferClient implements FileTransferClient {

    private static final Logger log = LoggerFactory.getLogger(NoOpFileTransferClient.class);

    @Override
    public void deliver(Path localFile, String remotePath) {
        log.info("Entrega simulada de {} a {}", localFile, remotePath);
    }
}
