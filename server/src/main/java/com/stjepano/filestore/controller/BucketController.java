package com.stjepano.filestore.controller;

import com.stjepano.filestore.service.BucketAlreadyExistsException;
import com.stjepano.filestore.service.BucketDoesNotExistException;
import com.stjepano.filestore.service.BucketId;
import com.stjepano.filestore.service.InvalidBucketIdException;
import com.stjepano.filestore.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * File controller
 */
@RestController
@RequestMapping("/files")
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
