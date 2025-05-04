package com.thesis.receiptify.config;

import com.thesis.receiptify.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    CommandLineRunner initMinioStorage(FileStorageService fileStorageService) {
        return args -> {
            fileStorageService.init();
        };
    }
}
