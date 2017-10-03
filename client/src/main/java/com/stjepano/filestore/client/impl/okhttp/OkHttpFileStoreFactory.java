package com.stjepano.filestore.client.impl.okhttp;

import com.stjepano.filestore.client.FileStore;
import com.stjepano.filestore.client.FileStoreFactory;

import java.net.URI;

/**
 * Implementation of {@link FileStoreFactory} that creates {@link OkHttpFileStore} objects
 */
public class OkHttpFileStoreFactory implements FileStoreFactory {

    @Override
    public FileStore createFileStore(URI fileStoreServerUri) {
        return new OkHttpFileStore(fileStoreServerUri);
    }

}
