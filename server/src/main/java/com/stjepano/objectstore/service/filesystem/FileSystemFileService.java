package com.stjepano.objectstore.service.filesystem;

import com.stjepano.objectstore.service.BucketAlreadyExistsException;
import com.stjepano.objectstore.service.BucketDoesNotExistException;
import com.stjepano.objectstore.service.BucketId;
import com.stjepano.objectstore.service.FileAlreadyExistException;
import com.stjepano.objectstore.service.FileDoesNotExistException;
import com.stjepano.objectstore.service.FileId;
import com.stjepano.objectstore.service.FileInfo;
import com.stjepano.objectstore.service.FileService;
import com.stjepano.objectstore.service.InvalidBucketIdException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link FileService} that reads buckets and files from given path.
 *
 * The {@link FileSystemFileService} uses directories under contentDir as buckets and files in each bucket are files of that bucket.
 *
 * It is not possible to access anything else. If attempt to access anything else would happen a BucketAccessViolationException is raised.
 */
public class FileSystemFileService implements FileService {

    private final FileSystem fileSystem;
    private final ResourceLoader resourceLoader;
    private final Path contentDirPath;

    /**
     * Initialize {@link FileSystemFileService}.
     *  @param contentDir root of content directory
     * @param fileSystem fileSystem to use
     * @param resourceLoader resource loader to use in order for loading files as spring resources
     */
    public FileSystemFileService(String contentDir,
                                 FileSystem fileSystem,
                                 ResourceLoader resourceLoader) {
        this.fileSystem = fileSystem;
        this.resourceLoader = resourceLoader;
        this.contentDirPath = this.fileSystem.getPath(contentDir);
    }

    private Path bucketPath(BucketId bucketId) {
        return contentDirPath.resolve(bucketId.getId());
    }

    private boolean bucketExists(BucketId bucketId) {
        final Path bucketPath = bucketPath(bucketId);
        return Files.exists(bucketPath) && Files.isDirectory(bucketPath);
    }

    @Override
    public List<String> getBuckets() {
        try (Stream<Path> files = Files.walk(contentDirPath, 1)) {
            return files
                    .filter(p -> {
                        try {
                            return !Files.isSameFile(p, contentDirPath);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .filter(Files::isReadable)
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateBucketUnderContentDir(Path bucketDir) throws BucketAccessViolationException {
        try {
            if (!Files.isSameFile(this.contentDirPath, bucketDir.normalize().getParent())) {
                throw new BucketAccessViolationException();
            }
        } catch (IOException e) {
            throw new BucketAccessViolationException(e);
        }
    }

    private void validateFileUnderBucket(Path bucketDir, Path filePath) throws BucketAccessViolationException {
        validateBucketUnderContentDir(bucketDir);
        try {
            if (!Files.isSameFile(bucketDir, filePath.normalize().getParent())) {
                throw new BucketAccessViolationException();
            }
        } catch (IOException e) {
            throw new BucketAccessViolationException(e);
        }
    }

    @Override
    public void createBucket(BucketId bucketId) throws BucketAlreadyExistsException, InvalidBucketIdException {
        Path bucketPath = bucketPath(bucketId);
        validateBucketUnderContentDir(bucketPath);
        if (bucketExists(bucketId)) {
            throw new BucketAlreadyExistsException(bucketId);
        }

        try {
            Files.createDirectory(bucketPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteBucket(BucketId bucketId) throws BucketDoesNotExistException, InvalidBucketIdException {
        final Path directory = bucketPath(bucketId);
        validateBucketUnderContentDir(directory);

        if (!bucketExists(bucketId)) {
            throw new BucketDoesNotExistException(bucketId);
        }


        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
                {
                    // try to delete the file anyway, even if its attributes
                    // could not be read, since delete-only access is
                    // theoretically possible
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FileInfo> getFiles(BucketId bucketId) throws BucketDoesNotExistException {
        Path bucketPath = bucketPath(bucketId);
        validateBucketUnderContentDir(bucketPath);

        if (!bucketExists(bucketId)) {
            throw new BucketDoesNotExistException(bucketId);
        }

        if (!Files.isReadable(bucketPath)) {
            throw new RuntimeException(String.format("Bucket path '%s' is not readable!", bucketPath.toAbsolutePath().toString()));
        }

        try (Stream<Path> files = Files.walk(bucketPath, 1)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .sorted(Comparator.comparing(a -> a.getFileName().toString()))
                    .map(path -> {
                        try {
                            return FileInfo.from(path);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path filePath(FileId fileId) {
        Path bucketPath = bucketPath(fileId.getBucketId());
        return bucketPath.resolve(fileId.getFileName());
    }

    private boolean fileExists(FileId fileId) {
        final Path filePath = filePath(fileId);
        return Files.exists(filePath) && Files.isRegularFile(filePath) && Files.isReadable(filePath);
    }

    @Override
    public void deleteFile(FileId fileId) throws BucketDoesNotExistException, FileDoesNotExistException {

        if (!bucketExists(fileId.getBucketId())) {
            throw new BucketDoesNotExistException(fileId.getBucketId());
        }
        if (!fileExists(fileId)) {
            throw new FileDoesNotExistException(fileId);
        }

        final Path bucketPath = bucketPath(fileId.getBucketId());
        final Path filePath = filePath(fileId);

        validateFileUnderBucket(bucketPath, filePath);

        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void upload(FileId fileId, InputStream dataStream) throws BucketDoesNotExistException, FileAlreadyExistException {
        if (!bucketExists(fileId.getBucketId())) {
            throw new BucketDoesNotExistException(fileId.getBucketId());
        }

        if (fileExists(fileId)) {
            throw new FileAlreadyExistException(fileId);
        }

        final Path bucketPath = bucketPath(fileId.getBucketId());
        final Path filePath = filePath(fileId);

        validateFileUnderBucket(bucketPath, filePath);

        try {
            Files.copy(dataStream, filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void overwrite(FileId fileId, InputStream dataStream) throws BucketDoesNotExistException, FileDoesNotExistException {
        if (!bucketExists(fileId.getBucketId())) {
            throw new BucketDoesNotExistException(fileId.getBucketId());
        }
        if (!fileExists(fileId)) {
            throw new FileDoesNotExistException(fileId);
        }

        final Path bucketPath = bucketPath(fileId.getBucketId());
        final Path filePath = filePath(fileId);

        validateFileUnderBucket(bucketPath, filePath);

        try {
            Files.copy(dataStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource download(FileId fileId) throws BucketDoesNotExistException, FileDoesNotExistException {

        if (!bucketExists(fileId.getBucketId())) {
            throw new BucketDoesNotExistException(fileId.getBucketId());
        }
        if (!fileExists(fileId)) {
            throw new FileDoesNotExistException(fileId);
        }

        final Path bucketPath = bucketPath(fileId.getBucketId());
        final Path filePath = filePath(fileId);

        validateFileUnderBucket(bucketPath, filePath);

        return resourceLoader.getResource(filePath.toAbsolutePath().toString());
    }
}
