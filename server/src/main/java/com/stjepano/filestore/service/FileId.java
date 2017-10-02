package com.stjepano.filestore.service;

/**
 * The id of the file which is composed of bucket name and file name.
 *
 * Bucket name must be valid, see details in {@link BucketId} for rules.
 *
 * File name must be valid according to following regex ^[^.][^\\/]*$
 */
public final class FileId {

    private static final String REGEX = "^[^.][^\\\\/]*$";

    private final BucketId bucketId;
    private final String fileName;

    private FileId(BucketId bucketId, String fileName) {
        this.bucketId = bucketId;
        this.fileName = fileName;
    }

    public static FileId from(String bucketName, String fileName) {
        if (!isValid(fileName)) {
            throw new InvalidFileIdException(fileName);
        }
        BucketId bucketId = BucketId.from(bucketName);
        return new FileId(bucketId, fileName);
    }

    public BucketId getBucketId() {
        return bucketId;
    }

    public String getFileName() {
        return fileName;
    }

    private static boolean isValid(String value) {
        if (value == null) return false;
        return value.matches(REGEX);
    }

    @Override
    public String toString() {
        return String.format("%s/%s", bucketId, fileName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileId fileId = (FileId) o;

        if (!bucketId.equals(fileId.bucketId)) return false;
        return fileName.equals(fileId.fileName);
    }

    @Override
    public int hashCode() {
        int result = bucketId.hashCode();
        result = 31 * result + fileName.hashCode();
        return result;
    }
}
