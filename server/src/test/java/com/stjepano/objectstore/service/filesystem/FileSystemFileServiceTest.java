package com.stjepano.objectstore.service.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.stjepano.objectstore.service.FileService;
import com.stjepano.objectstore.service.FileServiceBaseTest;
import com.stjepano.objectstore.service.filesystem.FileSystemFileService;
import org.junit.Rule;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test for {@link FileSystemFileService}
 */
@SpringBootTest
public class FileSystemFileServiceTest extends FileServiceBaseTest {

    private static final String CONTENT_DIR = "/filestore";

    @Mock
    private ResourceLoader resourceLoader;

    private FileSystemFileService fileService;

    private FileSystem fileSystem;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Override
    protected FileService createFileService() {
        try {
            final String contentDir = CONTENT_DIR;
            fileSystem = Jimfs.newFileSystem(Configuration.unix());
            Path rootPath = fileSystem.getPath(contentDir);
            Files.createDirectory(rootPath);

            Path shadowFile = fileSystem.getPath("/shadow");
            Files.write(shadowFile, "abcdef".getBytes());

            fileService = new FileSystemFileService(contentDir, fileSystem, resourceLoader);
            return fileService;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void createSomeBuckets(String... buckets) {
        try {
            for (String bucket : buckets) {
                Path bucketPath = fileSystem.getPath(CONTENT_DIR, bucket);
                Files.createDirectory(bucketPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void createSomeFiles(String bucket, String[][] data) {
        try {
            Path bucketPath = fileSystem.getPath(CONTENT_DIR, bucket);
            for (String[] fileData : data) {
                String fileName = fileData[0];
                String fileContent = fileData[1];

                Path filePath = bucketPath.resolve(fileName);
                Files.write(filePath, fileContent.getBytes());

                Resource resource = createMockResource(fileName, fileContent);
                Mockito.when(resourceLoader.getResource(
                        Matchers.eq(filePath.toString())
                )).thenReturn(resource);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    private Resource createMockResource(String filename, String fileContent) {
        Resource mockResource = new Resource() {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public boolean isReadable() {
                return true;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public URL getURL() throws IOException {
                return null;
            }

            @Override
            public URI getURI() throws IOException {
                return null;
            }

            @Override
            public File getFile() throws IOException {
                return null;
            }

            @Override
            public long contentLength() throws IOException {
                return fileContent.getBytes().length;
            }

            @Override
            public long lastModified() throws IOException {
                return System.currentTimeMillis();
            }

            @Override
            public Resource createRelative(String relativePath) throws IOException {
                return null;
            }

            @Override
            public String getFilename() {
                return filename;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(fileContent.getBytes());
            }
        };
        return mockResource;
    }
}
