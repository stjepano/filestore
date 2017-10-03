package com.stjepano.filestore.client;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * Factory helper for obtaining handle to {@link FileStore} object
 */
public class FileStores {

    /**
     * Gets a {@link FileStore} object "connected" to a filestore server specified by uri.
     *
     * The FileStore object is created with a help from {@link FileStoreFactory} that is configured by created with com.stjepano.filestore.client.factory property.
     * The default factory is built in com.stjepano.filestore.client.impl.okhttp.HttpClientFileStoreFactory.
     *
     *
     * @param serverUri an URI of server to "connect" to
     * @return a {@link FileStore} object
     */
    public static FileStore get(URI serverUri) {

        try {
            Properties defaultProperties = new Properties();
            defaultProperties.load(FileStore.class.getResourceAsStream("/filestoreclient.properties"));

            String factoryClassName = defaultProperties.getProperty("com.stjepano.filestore.client.factory");
            final String configuredFactoryClassName = System.getProperty("com.stjepano.filestore.client.factory");
            if (configuredFactoryClassName != null) {
                factoryClassName = configuredFactoryClassName;
            }

            FileStoreFactory fileStoreFactory = (FileStoreFactory) Class.forName(factoryClassName).newInstance();

            return fileStoreFactory.createFileStore(serverUri);

        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

    }

}
