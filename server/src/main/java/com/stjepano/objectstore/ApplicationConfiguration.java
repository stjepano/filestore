package com.stjepano.objectstore;

import com.stjepano.objectstore.service.FileService;
import com.stjepano.objectstore.service.filesystem.FileSystemFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application configuration
 */
@Configuration
public class ApplicationConfiguration {

    @Value("${filestore.contentDir}")
    private String contentDirStr;

    @Bean
    public FileService fileService() {
        FileSystem fileSystem = FileSystems.getDefault();
        Path contentDir = fileSystem.getPath(contentDirStr);

        if (!Files.exists(contentDir) || !Files.isDirectory(contentDir)) {
            throw new RuntimeException("Configured content directory '" + contentDir.toString() + "' does not exist!");
        }

        if (!Files.isReadable(contentDir) || !Files.isWritable(contentDir)) {
            throw new RuntimeException("Configured content directory '" + contentDir.toString() + "' is not readable and/or writable by application!");
        }

        return new FileSystemFileService(contentDirStr,
                fileSystem,
                new FileSystemResourceLoader()
        );
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
