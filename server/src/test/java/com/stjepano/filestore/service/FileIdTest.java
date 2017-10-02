package com.stjepano.filestore.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test for {@link FileId}
 */
@RunWith(Parameterized.class)
public class FileIdTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"file.txt", true},
                {"File.png", true},
                {"some-file.txt", true},
                {"Spaces are allowed.jpeg", true},
                {"And_underscores.txt", true},
                {"Everything_that_you-can-im4gine.tar.gz", true},
                {".secret", false},
                {"trying_to\\..\\..\\go_outside", false},
                {"this/will/not/work.txt", false},
                {"\\//", false},
                {".", false},
                {"..", false}
        });
    }

    private String fileName;
    private boolean valid;

    public FileIdTest(String fileName, boolean valid) {
        this.fileName = fileName;
        this.valid = valid;
    }

    @Test
    public void from() throws Exception {
        if (valid) {
            FileId fileId = FileId.from("validbucketname", fileName);
            Assertions.assertThat(fileId.getFileName())
                    .isEqualTo(fileName);
        } else {
            try {
                FileId.from("validbucketname", fileName);
                fail("File name " + fileName + " should not be valid!");
            } catch (InvalidFileIdException e) {

            }
        }
    }
}