package com.example.ecommerce.config;

import com.google.cloud.NoCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    @Bean
    Storage storage(StorageProperties properties) {
        if (properties.emulatorHost() != null && !properties.emulatorHost().isBlank()) {
            return StorageOptions.newBuilder()
                .setHost(properties.emulatorHost())
                .setProjectId(projectId)
                .setCredentials(NoCredentials.getInstance())
                .build()
                .getService();
        }
        return StorageOptions.getDefaultInstance().getService();
    }
}
