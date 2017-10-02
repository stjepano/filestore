package com.stjepano.filestore.controller;

import com.stjepano.filestore.service.BucketId;
import com.stjepano.filestore.service.FileId;
import com.stjepano.filestore.service.FileInfo;
import com.stjepano.filestore.service.ObjectStoreException;
import com.stjepano.filestore.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * A file controller
 */
@RestController
@RequestMapping("/{bucket}")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/")
    public List<FileInfo> listFiles(@PathVariable("bucket") String bucket) throws ObjectStoreException {
        return fileService.getFiles(BucketId.from(bucket));
    }

    @DeleteMapping("/{filename:.+}")
    public void deleteFile(@PathVariable("bucket") String bucket,
                           @PathVariable("filename") String filename)
            throws ObjectStoreException {
        fileService.deleteFile(FileId.from(bucket, filename));
    }

    @PostMapping("/")
    public void uploadFile(@PathVariable("bucket") String bucket,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(value = "filename", required = false) String filename)
            throws ObjectStoreException, IOException {
        final String desiredFilename = (filename == null) ? file.getOriginalFilename() : filename;
        fileService.upload(FileId.from(bucket, desiredFilename), file.getInputStream());
    }

    @PutMapping("/{filename:.+}")
    public void overwriteFile(@PathVariable("bucket") String bucket,
                              @PathVariable("filename") String filename,
                              @RequestParam("file") MultipartFile file)
            throws ObjectStoreException, IOException {
        fileService.overwrite(FileId.from(bucket, filename), file.getInputStream());
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("bucket") String bucket,
                                                 @PathVariable("filename") String filename,
                                                 @RequestParam(value = "att", required = false, defaultValue = "true") boolean asAttachment)
            throws ObjectStoreException {
        Resource file = fileService.download(FileId.from(bucket, filename));
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();
        if (asAttachment) {
            bodyBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"");
        }
        return bodyBuilder.body(file);
    }
}
