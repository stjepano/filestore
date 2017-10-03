package com.stjepano.filestore.common;

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

    private String name;
    private long size;
    private String mimeType;
    private LocalDateTime dateCreated;

    public FileInfo() { }

    public FileInfo(String name, long size, String mimeType, LocalDateTime dateCreated) {
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public static FileInfo from(Path path) throws IOException {
        final String filename = path.getFileName().toString();
        long size = 0;
        if (Files.isRegularFile(path)) {
            size = Files.size(path);
        }
        BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime creationTime = basicFileAttributes.creationTime();
        LocalDateTime dateCreated = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneOffset.systemDefault());
        String mimeType = Files.probeContentType(path);
        return new FileInfo(filename, size, mimeType, dateCreated);
    }
}
