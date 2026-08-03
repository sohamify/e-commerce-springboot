package com.example.ecommerce.auth.verification;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email-verification")
public record EmailVerificationProperties(Duration ttl) {

    public EmailVerificationProperties {
        if (ttl == null) {
            ttl = Duration.ofHours(24);
        }
    }
}
