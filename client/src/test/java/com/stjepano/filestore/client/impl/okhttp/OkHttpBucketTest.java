package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stjepano.filestore.client.Bucket;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

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

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        final HttpUrl mockWebServerUrl = mockWebServer.url("/");
        final OkHttpClient okHttpClient = new OkHttpClient();

        final OkHttpFileStore okHttpFileStore = new OkHttpFileStore(mockWebServerUrl.uri(), okHttpClient, objectMapper);
        okHttpBucket = okHttpFileStore.newBucket(BUCKET_NAME);
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.close();
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
    }
}