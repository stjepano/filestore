package com.stjepano.filestore.client;

import java.net.URI;

/**
 * A file store factory ...
 */
public interface FileStoreFactory {

    FileStore createFileStore(URI fileStoreServerUri);

}
