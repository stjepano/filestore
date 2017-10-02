package com.stjepano.filestore.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test for BucketId
 */
@RunWith(Parameterized.class)
public class BucketIdTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"good", true},
                {"bucket123", true},
                {"some-bucket", true},
                {"UPPERCASE", true},
                {"with_underscore", true},
                {"A_combination-of_every1234567890", true},
                {"../../shadow", false},
                {".git", false},
                {"blabla.txt", false},
                {"\\aaa", false}
        });
    }

    private String bucketName;
    private boolean valid;

    public BucketIdTest(String bucketName, boolean valid) {
        this.bucketName = bucketName;
        this.valid = valid;
    }

    @Test
    public void from() throws Exception {
        if (valid) {
            BucketId bucketId = BucketId.from(bucketName);
            Assertions.assertThat(bucketId.getId())
                    .isEqualTo(bucketName);
        } else {
            try {
                BucketId.from(bucketName);
                fail("Bucket id " + bucketName + " should not be valid!");
            } catch (InvalidBucketIdException e) {

            }
        }
    }
}