package com.stjepano.filestore.client;

import com.stjepano.filestore.client.impl.okhttp.OkHttpFileStore;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * test for {@link FileStores} object
 */
public class FileStoresTest {

    @Test
    public void get() {
        FileStore fileStore = FileStores.get(URI.create("http://localhost:8080"));

        Assertions.assertThat(fileStore)
                .isNotNull();

        Assertions.assertThat(fileStore.getClass())
                .isEqualTo(OkHttpFileStore.class);
    }


    @Test
    public void get_customFactory() {
        try {
            System.setProperty("com.stjepano.filestore.client.factory",
                    "com.stjepano.filestore.client.FileStoresTest$TestFileStoreFactory");
            FileStore fileStore = FileStores.get(URI.create("http://localhost:8080"));

            Assertions.assertThat(fileStore)
                    .isNotNull();

            Assertions.assertThat(fileStore.getClass())
                    .isEqualTo(TestFileStore.class);
        } finally {
            System.clearProperty("com.stjepano.filestore.client.factory");
        }
    }

    public static class TestFileStore implements FileStore {

        @Override
        public List<Bucket> getBuckets() {
            return null;
        }

        @Override
        public Optional<Bucket> getBucket(String bucketName) {
            return null;
        }

        @Override
        public Bucket createBucket(String bucketName) {
            return null;
        }
        
    }

    public static class TestFileStoreFactory implements FileStoreFactory {

        @Override
        public FileStore createFileStore(URI fileStoreServerUri) {
            return new TestFileStore();
        }
    }
}