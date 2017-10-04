package com.stjepano.filestore.controller;

import com.stjepano.filestore.service.BucketDoesNotExistException;
import com.stjepano.filestore.service.BucketId;
import com.stjepano.filestore.service.FileAlreadyExistException;
import com.stjepano.filestore.service.FileDoesNotExistException;
import com.stjepano.filestore.service.FileId;
import com.stjepano.filestore.common.FileInfo;
import com.stjepano.filestore.service.FileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link FileController}
 */
@RunWith(SpringRunner.class)
@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Mock
    private Resource resource;

    @Test
    public void testListFiles() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        when(fileService.getFiles(BucketId.from("bucket")))
                .thenReturn(Arrays.asList(
                        new FileInfo("filea.png", 5000, "image/png", now)
                ));

        String response = mockMvc.perform(get("/store/bucket/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedJson = "[{\"name\": \"filea.png\", \"size\": 5000, \"mimeType\": \"image/png\", \"dateCreated\": \""+nowStr+"\"}]";

        JSONAssert.assertEquals(expectedJson, response, true);

        verify(fileService, times(1))
                .getFiles(BucketId.from("bucket"));
    }

    @Test
    public void testListFiles_InvalidBucketName() throws Exception {
        mockMvc.perform(get("/store/.\\..\\..\\shadow/"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListFiles_BucketDoesNotExist() throws Exception {
        doThrow(new BucketDoesNotExistException(BucketId.from("bucket")))
                .when(fileService)
                .getFiles(BucketId.from("bucket"));

        mockMvc.perform(get("/store/bucket/"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteFile() throws Exception {
        doNothing()
                .when(fileService)
                .deleteFile(FileId.from("bucket", "fileA.png"));

        mockMvc.perform(delete("/store/bucket/fileA.png"))
                .andExpect(status().isOk());

        verify(fileService, times(1))
                .deleteFile(FileId.from("bucket", "fileA.png"));
    }

    @Test
    public void testDeleteFile_BucketDoesNotExist() throws Exception {
        doThrow(new BucketDoesNotExistException(BucketId.from("bucket")))
                .when(fileService)
                .deleteFile(FileId.from("bucket", "fileA.png"));

        mockMvc.perform(delete("/store/bucket/fileA.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteFile_InvalidBucketName() throws Exception {
        mockMvc.perform(delete("/store/.\\..\\..\\someDir/sensitive_data"))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void testDeleteFile_InvalidFileName() throws Exception {
        mockMvc.perform(delete("/store/bucket/fileA\\..\\..\\shadow.png"))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void testDeleteFile_FileDoesNotExist() throws Exception {
        doThrow(new FileDoesNotExistException(FileId.from("bucket", "fileA.png")))
                .when(fileService)
                .deleteFile(FileId.from("bucket", "fileA.png"));

        mockMvc.perform(delete("/store/bucket/fileA.png"))
                .andExpect(status().isNotFound());

        verify(fileService, times(1))
                .deleteFile(FileId.from("bucket", "fileA.png"));
    }


    @Test
    public void testUploadFile() throws Exception {
        doNothing()
                .when(fileService)
                .upload(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/").file(file))
                .andExpect(status().isOk());

        verify(fileService, times(1))
                .upload(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

    }

    @Test
    public void testUploadFile_InvalidBucketName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());
        mockMvc.perform(fileUpload("/store/bucket$$$$$../").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUploadFile_InvalidFileName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "..\\..\\fileA.png", "image/png", "abcd".getBytes());
        mockMvc.perform(fileUpload("/store/bucket/").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUploadFile_DifferentFilename() throws Exception {
        doNothing()
                .when(fileService)
                .upload(any(FileId.class), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/?filename=something.png").file(file))
                .andExpect(status().isOk());

        verify(fileService, times(1))
                .upload(eq(FileId.from("bucket", "something.png")), any(InputStream.class));
    }

    @Test
    public void testUploadFile__DifferentFileName_InvalidFileName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());
        mockMvc.perform(fileUpload("/store/bucket/?filename=..\\..\\something.php").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUploadFile_BucketDoesNotExist() throws Exception {
        doThrow(new BucketDoesNotExistException(BucketId.from("bucket")))
                .when(fileService)
                .upload(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/").file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUploadFile_FileAlreadyExist() throws Exception {
        doThrow(new FileAlreadyExistException(FileId.from("bucket", "fileA.png")))
                .when(fileService)
                .upload(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/").file(file))
                .andExpect(status().isConflict());
    }


    @Test
    public void testOverwriteFile() throws Exception {
        doNothing()
                .when(fileService)
                .overwrite(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/fileA.png").file(file).with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());

        verify(fileService, times(1))
                .overwrite(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

    }

    @Test
    public void testOverwriteFile_InvalidBucketName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());
        mockMvc.perform(fileUpload("/store/bucket$$$$$../fileA.png").file(file).with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testOverwriteFile_InvalidFileName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());
        mockMvc.perform(fileUpload("/store/bucket/fileA..\\..\\.png").file(file).with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testOverwriteFile_BucketDoesNotExist() throws Exception {
        doThrow(new BucketDoesNotExistException(BucketId.from("bucket")))
                .when(fileService)
                .overwrite(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/fileA.png").file(file).with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testOverwriteFile_FileDoesNotExist() throws Exception {
        doThrow(new FileDoesNotExistException(FileId.from("bucket", "fileA.png")))
                .when(fileService)
                .overwrite(eq(FileId.from("bucket", "fileA.png")), any(InputStream.class));

        MockMultipartFile file = new MockMultipartFile("file", "fileA.png", "image/png", "abcd".getBytes());

        mockMvc.perform(fileUpload("/store/bucket/fileA.png").file(file).with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testDownloadFile() throws Exception {
        doReturn(resource)
                .when(fileService)
                .download(eq(FileId.from("bucket", "fileA.png")));

        byte[] bytes = "abcd".getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        when(resource.getFilename())
                .thenReturn("fileA.png");
        when(resource.getInputStream())
                .thenReturn(inputStream);

        mockMvc.perform(get("/store/bucket/fileA.png"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fileA.png\""));
    }

    @Test
    public void testDownloadFile_InvalidBucketName() throws Exception {
        mockMvc.perform(get("/store/~~~bucket$$/fileA.png"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDownloadFile_InvalidFileName() throws Exception {
        mockMvc.perform(get("/store/bucket/fileA\\.png"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDownloadFile_NotAttachment() throws Exception {
        doReturn(resource)
                .when(fileService)
                .download(eq(FileId.from("bucket", "fileA.png")));

        byte[] bytes = "abcd".getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        when(resource.getFilename())
                .thenReturn("fileA.png");
        when(resource.getInputStream())
                .thenReturn(inputStream);

        mockMvc.perform(get("/store/bucket/fileA.png?att=false"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes))
                .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));
    }


    @Test
    public void testDownloadFile_BucketDoesNotExist() throws Exception {
        doThrow(new BucketDoesNotExistException(BucketId.from("bucket")))
                .when(fileService)
                .download(eq(FileId.from("bucket", "fileA.png")));

        mockMvc.perform(get("/store/bucket/fileA.png?att=false"))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testDownloadFile_FileDoesNotExist() throws Exception {
        doThrow(new FileDoesNotExistException(FileId.from("bucket", "fileA.png")))
                .when(fileService)
                .download(eq(FileId.from("bucket", "fileA.png")));

        mockMvc.perform(get("/store/bucket/fileA.png?att=false"))
                .andExpect(status().isNotFound());
    }


}