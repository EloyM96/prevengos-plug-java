package com.prevengos.plug.gateway.filetransfer;

import java.nio.file.Path;

public interface FileTransferClient {

    void upload(Path localFile, FileTransferRequest request);
}
