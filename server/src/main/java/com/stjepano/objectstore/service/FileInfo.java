package com.stjepano.objectstore.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * File info
 */
public class FileInfo {

    private final String name;
    private final long size;
    private final FileType type;
    private final String mimeType;
    private final LocalDateTime dateCreated;


    public FileInfo(String name, long size, FileType type, String mimeType, LocalDateTime dateCreated) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.mimeType = mimeType;
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public FileType getType() {
        return type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public static FileInfo from(Path path) throws IOException {
        final String filename = path.getFileName().toString();
        long size = 0;
        if (Files.isRegularFile(path)) {
            size = Files.size(path);
        }
        BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime creationTime = basicFileAttributes.creationTime();
        FileType fileType = FileType.REGULAR_FILE;
        if (Files.isDirectory(path)) {
            fileType = FileType.DIRECTORY;
        }
        LocalDateTime dateCreated = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneOffset.systemDefault());
        String mimeType = Files.probeContentType(path);
        return new FileInfo(filename, size, fileType, mimeType, dateCreated);
    }
}
