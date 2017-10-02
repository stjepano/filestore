package com.stjepano.objectstore.controller;

import com.stjepano.objectstore.service.BucketAlreadyExistsException;
import com.stjepano.objectstore.service.BucketDoesNotExistException;
import com.stjepano.objectstore.service.BucketId;
import com.stjepano.objectstore.service.InvalidBucketIdException;
import com.stjepano.objectstore.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * File controller
 */
@RestController
public class BucketController {

    @Autowired
    private FileService fileService;

    @GetMapping("/")
    public List<String> listBuckets() {
        return fileService.getBuckets();
    }

    @PostMapping("/")
    public void createBucket(@RequestBody String bucketName) throws BucketAlreadyExistsException, InvalidBucketIdException {
        fileService.createBucket(BucketId.from(bucketName));
    }

    @DeleteMapping("/{bucket}")
    public void deleteBucket(@PathVariable("bucket") String bucket) throws BucketDoesNotExistException, InvalidBucketIdException {
        fileService.deleteBucket(BucketId.from(bucket));
    }


}
