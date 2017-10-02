package com.stjepano.filestore.service;

/**
 * Represents the ID of the bucket.
 *
 * Bucket ID must be in format ^[a-zA-Z0-9_-]+$
 *
 */
public final class BucketId {

    private static final String REGEX = "^[a-zA-Z0-9_-]+$";

    private final String id;

    private BucketId(String id) {
        this.id = id;
    }

    /**
     * Create bucket id from string.
     *
     * The string must match following regex to work: ^[a-zA-Z0-9_-]+
     *
     * @param value a string id of the bucket
     * @return an {@link BucketId} object
     * @throws InvalidBucketIdException if bucket name is not valid
     */
    public static BucketId from(String value) {
        if (!isValid(value)) {
            throw new InvalidBucketIdException(value);
        }
        return new BucketId(value);
    }

    public String getId() {
        return id;
    }

    private static boolean isValid(String value) {
        if (value == null) return false;
        return value.matches(REGEX);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BucketId bucketId = (BucketId) o;

        return id.equals(bucketId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
