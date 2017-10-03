package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stjepano.filestore.client.Bucket;
import com.stjepano.filestore.client.FileStoreServerException;
import com.stjepano.filestore.common.ErrorResponse;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for {@link OkHttpFileStore}
 */
public class OkHttpFileStoreTest {


    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockWebServer mockWebServer;

    private OkHttpFileStore fileStore;

    @Before
    public void before() throws IOException {
        mockWebServer = new MockWebServer();
        final HttpUrl mockWebServerUrl = mockWebServer.url("/");
        //mockWebServer.start();

        fileStore = new OkHttpFileStore(mockWebServerUrl.uri(), new OkHttpClient(), objectMapper);
    }

    @After
    public void after() throws IOException {
        mockWebServer.close();
        mockWebServer = null;
    }

    @Test
    public void getBuckets() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("[\"one\", \"two\"]");
        mockWebServer.enqueue(mockResponse);

        List<Bucket> buckets = fileStore.getBuckets();

        assertThat(buckets)
                .hasSize(2);

        assertThat(buckets.get(0).getName())
                .isEqualTo("one");

        assertThat(buckets.get(1).getName())
                .isEqualTo("two");
    }


    @Test
    public void getBucketsError() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": true, \"message\": \"some message\"}");
        mockWebServer.enqueue(mockResponse);

        try {
            fileStore.getBuckets();
            fail("expecting an FileStoreServerException here");
        } catch (FileStoreServerException e) {
            assertThat(e.getCode())
                    .isEqualTo(500);
            assertThat(e.getServerMessage())
                    .isEqualTo("some message");
        }
    }

    @Test
    public void getBucket() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("[\"one\", \"two\"]");
        mockWebServer.enqueue(mockResponse);

        Optional<Bucket> bucketOptional = fileStore.getBucket("two");
        assertThat(bucketOptional)
                .isNotEmpty();
        assertThat(bucketOptional.get().getName())
                .isEqualTo("two");
    }

    @Test
    public void getBucketDoesNotExist() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("[\"one\", \"two\"]");
        mockWebServer.enqueue(mockResponse);

        Optional<Bucket> bucketOptional = fileStore.getBucket("three");
        assertThat(bucketOptional)
                .isEmpty();
    }

    @Test
    public void getBucketException() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": true, \"message\": \"some message\"}");
        mockWebServer.enqueue(mockResponse);

        try {
            fileStore.getBucket("four");
            fail("expecting an FileStoreServerException here");
        } catch (FileStoreServerException e) {
            assertThat(e.getCode())
                    .isEqualTo(500);
            assertThat(e.getServerMessage())
                    .isEqualTo("some message");
        }
    }


    @Test
    public void createBucket() throws Exception {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        Bucket bucket = fileStore.createBucket("bucket");

        assertThat(bucket.getName())
                .isEqualTo("bucket");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/");
        assertThat(recordedRequest.getBody().readString(Charset.forName("utf-8")))
                .isEqualTo("bucket");
    }


    @Test(expected = FileStoreServerException.class)
    public void createBucketConflict() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Bucket already exists");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(409)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        fileStore.createBucket("bucket");
    }

    @Test(expected = FileStoreServerException.class)
    public void createBucketInvalidName() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Bucket name is not valid");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(400)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        fileStore.createBucket(".bucket$$");
    }

    @Test(expected = FileStoreServerException.class)
    public void createBucketServerError() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("Server error");
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .setBody(objectMapper.writeValueAsString(errorResponse));
        mockWebServer.enqueue(mockResponse);

        fileStore.createBucket("bucket");
    }
}