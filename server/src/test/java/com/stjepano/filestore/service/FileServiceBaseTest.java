package com.stjepano.filestore.service;

import com.stjepano.filestore.common.FileInfo;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Base test class for {@link FileService}.
 *
 * All implementations should pass this test
 */
public abstract class FileServiceBaseTest {

    protected abstract FileService createFileService();

    protected abstract void createSomeBuckets(String... buckets);

    protected abstract void createSomeFiles(String bucket, String[][] data);

    private FileService fileService;

    @Before
    public void beforeBaseTest() {
        fileService = createFileService();
    }

    @Test
    public void getBuckets() {
        Assertions.assertThat(fileService.getBuckets())
                .isEmpty();
    }


    @Test
    public void getBucketsReturnsBucketsSortedAlphabetically() {
        createSomeBuckets("bucketB", "bucketA", "bucketC");

        List<String> buckets = fileService.getBuckets();
        Assertions.assertThat(buckets)
                .isSorted()
                .contains("bucketA", "bucketB", "bucketC")
                .hasSize(3);
    }

    @Test
    public void createBucket() throws BucketAlreadyExistsException, InvalidBucketIdException {
        fileService.createBucket(BucketId.from("bucketA"));

        Assertions.assertThat(fileService.getBuckets())
                .hasSize(1)
                .contains("bucketA");
    }

    @Test(expected = BucketAlreadyExistsException.class)
    public void createDuplicateBucket() throws BucketAlreadyExistsException, InvalidBucketIdException {
        try {
            fileService.createBucket(BucketId.from("bucketA"));
        } catch (BucketAlreadyExistsException e) {
            Assert.fail();
        } catch (InvalidBucketIdException e) {
            throw e;
        }

        fileService.createBucket(BucketId.from("bucketA"));
    }

    @Test
    public void deleteBucket() throws Exception {
        fileService.createBucket(BucketId.from("bucketA"));

        Assertions.assertThat(fileService.getBuckets())
                .hasSize(1)
                .contains("bucketA");

        fileService.deleteBucket(BucketId.from("bucketA"));
    }

    @Test(expected = BucketDoesNotExistException.class)
    public void deleteNonExistingBucket() throws Exception {
        Assertions.assertThat(fileService.getBuckets())
                .doesNotContain("bucketA");

        fileService.deleteBucket(BucketId.from("bucketA"));
    }

    @Test
    public void getFiles() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Thread.sleep(20);  // sleep some time before we create files
        createSomeBuckets("bucket");
        createSomeFiles("bucket", new String[][] {
                new String[] {"cfile.zip", "abcdef"},
                new String[] {"afile.txt", "abcdefg"},
                new String[] {"bfile.png", "abcdefgh"}
        });

        List<FileInfo> fileInfoList = fileService.getFiles(BucketId.from("bucket"));
        Assertions.assertThat(fileInfoList)
                .isNotNull()
                .hasSize(3);
        FileInfo first = fileInfoList.get(0);
        FileInfo second = fileInfoList.get(1);
        FileInfo third = fileInfoList.get(2);

        Assertions.assertThat(first.getName())
                .isEqualTo("afile.txt");

        Assertions.assertThat(first.getDateCreated())
                .isAfterOrEqualTo(now);
        Assertions.assertThat(first.getSize())
                .isEqualTo("abcdefg".getBytes().length);
        Assertions.assertThat(first.getMimeType())
                .isEqualTo("text/plain");

        Assertions.assertThat(second.getName())
                .isEqualTo("bfile.png");
        Assertions.assertThat(second.getDateCreated())
                .isAfterOrEqualTo(now);
        Assertions.assertThat(second.getSize())
                .isEqualTo("abcdefgh".getBytes().length);
        Assertions.assertThat(second.getMimeType())
                .isEqualTo("image/png");

        Assertions.assertThat(third.getName())
                .isEqualTo("cfile.zip");
        Assertions.assertThat(third.getDateCreated())
                .isAfterOrEqualTo(now);
        Assertions.assertThat(third.getSize())
                .isEqualTo("abcdef".getBytes().length);
        Assertions.assertThat(third.getMimeType())
                .isEqualTo("application/x-zip-compressed");
    }

    @Test(expected = BucketDoesNotExistException.class)
    public void getFilesForNonExistingBucket() throws Exception {
        Assertions.assertThat(fileService.getBuckets())
                .doesNotContain("bucket_does_not_exist");
        fileService.getFiles(BucketId.from("bucket_does_not_exist"));
    }

    @Test
    public void deleteFile() throws Exception {
        createSomeBuckets("bucket");
        createSomeFiles("bucket", new String[][] {
                new String[] {"file.txt", "abcdef"}
        });

        Assertions.assertThat(fileService.getBuckets())
                .contains("bucket");
        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")))
                .hasSize(1);

        fileService.deleteFile(FileId.from("bucket", "file.txt"));
    }


    @Test(expected = FileDoesNotExistException.class)
    public void deleteFileWhenFileDoesNotExist() throws Exception {
        createSomeBuckets("bucket");
        Assertions.assertThat(fileService.getBuckets())
                .contains("bucket");
        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")))
                .isEmpty();

        fileService.deleteFile(FileId.from("bucket", "file.txt"));
    }

    @Test(expected = BucketDoesNotExistException.class)
    public void deleteFileBucketDoesNotExist() throws Exception {
        Assertions.assertThat(fileService.getBuckets())
                .doesNotContain("bucket");

        fileService.deleteFile(FileId.from("bucket", "file.txt"));
    }


    @Test
    public void uploadFile() throws Exception {
        createSomeBuckets("bucket");

        Assertions.assertThat(fileService.getBuckets())
                .contains("bucket");

        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")))
                .isEmpty();

        InputStream inputStream = new ByteArrayInputStream("abcdef".getBytes());
        fileService.upload(FileId.from("bucket", "file.txt"), inputStream);

        List<FileInfo> fileInfoList = fileService.getFiles(BucketId.from("bucket"));
        Assertions.assertThat(fileInfoList)
                .hasSize(1);

        FileInfo fileInfo = fileInfoList.get(0);

        Assertions.assertThat(fileInfo.getName())
                .isEqualTo("file.txt");
        Assertions.assertThat(fileInfo.getSize())
                .isEqualTo("abcdef".getBytes().length);

    }

    @Test(expected = BucketDoesNotExistException.class)
    public void uploadFileWithBucketDoesNotExist() throws Exception {
        Assertions.assertThat(fileService.getBuckets())
                .doesNotContain("bucket");

        InputStream inputStream = new ByteArrayInputStream("abcdef".getBytes());
        fileService.upload(FileId.from("bucket", "file.txt"), inputStream);
    }

    @Test(expected = FileAlreadyExistException.class)
    public void uploadFileWithFileAlreadyExist() throws Exception {
        createSomeBuckets("bucket");
        createSomeFiles("bucket", new String[][] {
                new String[] {"file.txt", "abcdef"}
        });

        InputStream inputStream = new ByteArrayInputStream("abcdef".getBytes());
        fileService.upload(FileId.from("bucket", "file.txt"), inputStream);
    }


    @Test
    public void overwriteFile() throws Exception {
        createSomeBuckets("bucket");
        createSomeFiles("bucket", new String[][] {
                new String[] {"file.txt", "abcd"}
        });

        Assertions.assertThat(fileService.getBuckets())
                .contains("bucket");

        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")))
                .hasSize(1);

        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")).get(0).getSize())
                .isEqualTo("abcd".getBytes().length);

        InputStream inputStream = new ByteArrayInputStream("abcdef".getBytes());
        fileService.overwrite(FileId.from("bucket", "file.txt"), inputStream);

        List<FileInfo> fileInfoList = fileService.getFiles(BucketId.from("bucket"));
        Assertions.assertThat(fileInfoList)
                .hasSize(1);

        FileInfo fileInfo = fileInfoList.get(0);

        Assertions.assertThat(fileInfo.getName())
                .isEqualTo("file.txt");
        Assertions.assertThat(fileInfo.getSize())
                .isEqualTo("abcdef".getBytes().length);

    }

    @Test(expected = BucketDoesNotExistException.class)
    public void overwriteFileWithBucketDoesNotExist() throws Exception {
        Assertions.assertThat(fileService.getBuckets())
                .doesNotContain("bucket");

        InputStream inputStream = new ByteArrayInputStream("abcdef".getBytes());
        fileService.overwrite(FileId.from("bucket", "file.txt"), inputStream);
    }

    @Test(expected = FileDoesNotExistException.class)
    public void overwriteFileWithFileDoesNotExist() throws Exception {
        createSomeBuckets("bucket");
        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")))
                .isEmpty();

        InputStream inputStream = new ByteArrayInputStream("abcdef".getBytes());
        fileService.overwrite(FileId.from("bucket", "file.txt"), inputStream);
    }


    @Test
    public void download() throws Exception {
        createSomeBuckets("bucket");
        createSomeFiles("bucket", new String[][] {
                new String[] {"file.txt", "abcd"}
        });

        Resource resource = fileService.download(FileId.from("bucket", "file.txt"));
        Assertions.assertThat(resource)
                .isNotNull();
    }

    @Test(expected = BucketDoesNotExistException.class)
    public void downloadWithNonExistingBucket() throws Exception {
        fileService.download(FileId.from("bucket", "file.txt"));
    }

    @Test(expected = FileDoesNotExistException.class)
    public void downloadWithFileDoesNotExist() throws Exception {
        createSomeBuckets("bucket");

        Assertions.assertThat(fileService.getFiles(BucketId.from("bucket")))
                .isEmpty();

        fileService.download(FileId.from("bucket", "file.txt"));
    }


}
