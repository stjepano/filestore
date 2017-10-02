package com.stjepano.filestore.controller;


import com.stjepano.filestore.service.BucketAlreadyExistsException;
import com.stjepano.filestore.service.BucketDoesNotExistException;
import com.stjepano.filestore.service.BucketId;
import com.stjepano.filestore.service.FileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link BucketController}
 */
@RunWith(SpringRunner.class)
@WebMvcTest(BucketController.class)
public class BucketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;


    @Test
    public void testListBuckets() throws Exception {
        when(fileService.getBuckets())
                .thenReturn(Arrays.asList("bucket1", "bucket2"));

        String response = mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals("[\"bucket1\",\"bucket2\"]", response, true);
    }

    @Test
    public void testCreateBucket() throws Exception {
        doNothing().when(fileService).createBucket(any(BucketId.class));

        mockMvc.perform(post("/").content("bucket3"))
                .andExpect(status().isOk());

        verify(fileService, times(1))
                .createBucket(BucketId.from("bucket3"));
    }


    @Test
    public void testCreateBucket_BucketAlreadyExists() throws Exception {
        doThrow(new BucketAlreadyExistsException(BucketId.from("bucketAA")))
                .when(fileService)
                .createBucket(eq(BucketId.from("bucketAA")));

        mockMvc.perform(post("/").content("bucketAA"))
                .andExpect(status().isConflict());
    }


    @Test
    public void testDeleteBucket() throws Exception {
        doNothing()
                .when(fileService)
                .deleteBucket(BucketId.from("bucket1"));

        mockMvc.perform(delete("/bucket1/"))
                .andExpect(status().isOk());

        verify(fileService, times(1))
                .deleteBucket(BucketId.from("bucket1"));
    }


    @Test
    public void testDeleteBucket_BucketDoesNotExist() throws Exception {
        doThrow(new BucketDoesNotExistException(BucketId.from("bucket1")))
                .when(fileService)
                .deleteBucket(BucketId.from("bucket1"));

        mockMvc.perform(delete("/bucket1/"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateBucket_InvalidName() throws Exception {
        mockMvc.perform(post("/").content("..\\bucket"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteBucket_InvalidName() throws Exception {
        mockMvc.perform(delete("/bucket$$"))
                .andExpect(status().isBadRequest());
    }

}