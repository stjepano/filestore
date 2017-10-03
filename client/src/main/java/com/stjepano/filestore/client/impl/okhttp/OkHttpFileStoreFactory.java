package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stjepano.filestore.client.FileStore;
import com.stjepano.filestore.client.FileStoreFactory;
import okhttp3.OkHttpClient;

import java.net.URI;

/**
 * Implementation of {@link FileStoreFactory} that creates {@link OkHttpFileStore} objects
 */
public class OkHttpFileStoreFactory implements FileStoreFactory {

    @Override
    public FileStore createFileStore(URI fileStoreServerUri) {
        OkHttpClient okHttpClient = new OkHttpClient();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return new OkHttpFileStore(fileStoreServerUri, okHttpClient, objectMapper);
    }

}
