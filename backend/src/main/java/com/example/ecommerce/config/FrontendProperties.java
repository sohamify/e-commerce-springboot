package com.example.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Base URLs of the two frontends, used to build links embedded in transactional emails. */
@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(String storefrontBaseUrl, String adminBaseUrl) {
}
