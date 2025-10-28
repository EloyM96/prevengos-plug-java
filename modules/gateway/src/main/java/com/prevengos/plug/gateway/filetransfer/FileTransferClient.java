package com.prevengos.plug.gateway.filetransfer;

import java.nio.file.Path;

/**
 * Contrato mínimo para entregar ficheros generados a un destino remoto.
 */
public interface FileTransferClient {

    void deliver(Path localFile, String remotePath) throws Exception;
}
