package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.stjepano.filestore.client.FileStoreServerException;
import com.stjepano.filestore.common.ErrorResponse;
import com.stjepano.filestore.common.FileInfo;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Write short comment about this class/interface/whatever ... What is it's responsibility?
 */
public class OkHttpBucketTest {

    private static final String BUCKET_NAME = "bucket";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private MockWebServer mockWebServer;

    private OkHttpBucket okHttpBucket;

    private FileSystem testFileSystem;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        final HttpUrl mockWebServerUrl = mockWebServer.url("/");
        final OkHttpClient okHttpClient = new OkHttpClient();

        final OkHttpFileStore okHttpFileStore = new OkHttpFileStore(mockWebServerUrl.uri(), okHttpClient, objectMapper);
        okHttpBucket = okHttpFileStore.newBucket(BUCKET_NAME);

        mockTestFileSystem();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    private void mockTestFileSystem() throws IOException {
        testFileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path testDir = testFileSystem.getPath("/testdata");
        Files.createDirectory(testDir);

        Path fileA = testDir.resolve("fileA.dat");
        Files.write(fileA, "abcdef".getBytes());

        Path fileB = testDir.resolve("fileB.txt");
        Files.write(fileB, "ghijkl".getBytes());
    }

    @Test
    public void getFiles() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        FileInfo[] data = new FileInfo[] {
            new FileInfo("testa.txt", 500, "text/plain", now),
            new FileInfo("testb.txt", 500, "text/plain", now),
            new FileInfo("testc.txt", 500, "text/plain", now)
        };

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(data));
        mockWebServer.enqueue(mockResponse);

        List<FileInfo> fileInfos = okHttpBucket.getFiles();

        assertThat(fileInfos)
                .hasSize(3);
        assertThat(fileInfos.get(0).getName())
                .isEqualTo("testa.txt");
        assertThat(fileInfos.get(1).getName())
                .isEqualTo("testb.txt");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("GET");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/"+BUCKET_NAME+"/");
    }

    @Test(expected = FileStoreServerException.class)
    public void getFilesServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Bucket does not exist");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.getFiles();
    }

    @Test
    public void getFileInfoFileExists() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        FileInfo[] data = new FileInfo[] {
                new FileInfo("testa.txt", 500, "text/plain", now),
                new FileInfo("testb.txt", 500, "text/plain", now),
                new FileInfo("testc.txt", 500, "text/plain", now)
        };

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(data));
        mockWebServer.enqueue(mockResponse);

        Optional<FileInfo> fileInfoOptional = okHttpBucket.getFileInfo("testb.txt");
        assertThat(fileInfoOptional)
                .isNotEmpty();
        assertThat(fileInfoOptional.get().getName())
                .isEqualTo("testb.txt");

    }

    @Test
    public void getFileInfoFileDoesNotExist() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        FileInfo[] data = new FileInfo[] {
                new FileInfo("testa.txt", 500, "text/plain", now),
                new FileInfo("testb.txt", 500, "text/plain", now),
                new FileInfo("testc.txt", 500, "text/plain", now)
        };

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(data));
        mockWebServer.enqueue(mockResponse);

        Optional<FileInfo> fileInfoOptional = okHttpBucket.getFileInfo("bubu.txt");
        assertThat(fileInfoOptional)
                .isEmpty();
    }

    @Test(expected = FileStoreServerException.class)
    public void getFileInfoServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Bucket does not exist");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.getFileInfo("aaaa.txt");
    }


    @Test
    public void fileExistsFileExists() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        FileInfo[] data = new FileInfo[] {
                new FileInfo("testa.txt", 500, "text/plain", now),
                new FileInfo("testb.txt", 500, "text/plain", now),
                new FileInfo("testc.txt", 500, "text/plain", now)
        };

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(data));
        mockWebServer.enqueue(mockResponse);

        boolean result = okHttpBucket.fileExists("testc.txt");
        assertThat(result)
                .isTrue();
    }

    @Test
    public void fileExistsFileDoesNotExist() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        FileInfo[] data = new FileInfo[] {
                new FileInfo("testa.txt", 500, "text/plain", now),
                new FileInfo("testb.txt", 500, "text/plain", now),
                new FileInfo("testc.txt", 500, "text/plain", now)
        };

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(data));
        mockWebServer.enqueue(mockResponse);

        boolean result = okHttpBucket.fileExists("aaaaa.txt");
        assertThat(result)
                .isFalse();
    }

    @Test(expected = FileStoreServerException.class)
    public void fileExistsServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Bucket does not exist");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.fileExists("aaaa.txt");
    }


    @Test
    public void uploadFile() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.uploadFile(testFileSystem.getPath("/testdata/fileA.dat"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + BUCKET_NAME + "/");
        String bodyString = recordedRequest.getBody().readString(Charset.forName("utf-8"));
        assertThat(bodyString)
                .contains("Content-Disposition: form-data; name=\"file\"; filename=\"fileA.dat\"");
        assertThat(bodyString)
                .contains("Content-Type: application/octet-stream");
    }


    @Test(expected = FileStoreServerException.class)
    public void uploadFileServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("File already exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(409)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.uploadFile(testFileSystem.getPath("/testdata/fileA.dat"));
    }


    @Test
    public void uploadFileDifferentFilename() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.uploadFile(testFileSystem.getPath("/testdata/fileA.dat"), "newFile.dat");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + BUCKET_NAME + "/?filename=newFile.dat");
        String bodyString = recordedRequest.getBody().readString(Charset.forName("utf-8"));
        assertThat(bodyString)
                .contains("Content-Disposition: form-data; name=\"file\"; filename=\"fileA.dat\"");
        assertThat(bodyString)
                .contains("Content-Type: application/octet-stream");
    }

    @Test(expected = FileStoreServerException.class)
    public void uploadFileDifferentFilenameServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("File already exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(409)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.uploadFile(testFileSystem.getPath("/testdata/fileA.dat"), "newFile.dat");
    }


    @Test
    public void overwriteFile() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.overwriteFile(testFileSystem.getPath("/testdata/fileA.dat"), "fileA.dat");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("PUT");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + BUCKET_NAME + "/fileA.dat");
        String bodyString = recordedRequest.getBody().readString(Charset.forName("utf-8"));
        assertThat(bodyString)
                .contains("Content-Disposition: form-data; name=\"file\"; filename=\"fileA.dat\"");
        assertThat(bodyString)
                .contains("Content-Type: application/octet-stream");
    }

    @Test(expected = FileStoreServerException.class)
    public void overwriteFileDifferentFilenameServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("File does not exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.overwriteFile(testFileSystem.getPath("/testdata/fileA.dat"), "newFile.dat");
    }

    @Test
    public void downloadFile() throws Exception {
        String data = "uvwxyz";
        byte[] dataBytes = data.getBytes(Charset.forName("utf-8"));
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type: application/octet-stream; charset=utf-8")
                .addHeader("Content-Disposition: attachment; filename=\"download.dat\"")
                .addHeader("Content-Length: " + data.length())
                .setBody(data);

        mockWebServer.enqueue(mockResponse);

        final Path downloadPath = testFileSystem.getPath("/testdata/download.dat");

        okHttpBucket.downloadFile("testA.dat", downloadPath);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("GET");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + BUCKET_NAME + "/testA.dat");

        byte[] downloadedBytes = Files.readAllBytes(downloadPath);
        assertThat(dataBytes)
                .isEqualTo(downloadedBytes);
    }


    @Test(expected = FileStoreServerException.class)
    public void downloadFileServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("File does not exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        final Path downloadPath = testFileSystem.getPath("/testdata/download.dat");
        okHttpBucket.downloadFile("testA.dat", downloadPath);
    }


    @Test
    public void deleteFile() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        okHttpBucket.deleteFile("testA.dat");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("DELETE");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/"+BUCKET_NAME+"/testA.dat");
    }

    @Test(expected = FileStoreServerException.class)
    public void deleteFileServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("File does not exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.deleteFile("testA.dat");
    }

    @Test
    public void deleteBucketAndAllFiles() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        okHttpBucket.deleteBucketAndAllFiles();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("DELETE");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/"+BUCKET_NAME+"/");
    }


    @Test(expected = FileStoreServerException.class)
    public void deleteBucketAndAllFilesServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Bucket does not exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(404)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        okHttpBucket.deleteBucketAndAllFiles();
    }

}