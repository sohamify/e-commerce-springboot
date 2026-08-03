package com.example.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** GCS bucket used for listing photos. emulatorHost is set only in dev, pointing at fake-gcs-server. */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String bucket, String emulatorHost) {
}
