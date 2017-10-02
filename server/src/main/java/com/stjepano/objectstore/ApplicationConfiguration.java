package com.stjepano.objectstore;

import com.stjepano.objectstore.service.FileService;
import com.stjepano.objectstore.service.filesystem.FileSystemFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.nio.file.FileSystems;

/**
 * Application configuration
 */
@Configuration
public class ApplicationConfiguration {

    @Value("${app.files.path}")
    private String appFilesPathString;

    @Bean
    public FileService fileService() {
        return new FileSystemFileService(appFilesPathString,
                FileSystems.getDefault(),
                new FileSystemResourceLoader()
        );
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
