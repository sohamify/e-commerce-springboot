package com.example.ecommerce.auth.passwordreset;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(Duration ttl) {

    public PasswordResetProperties {
        if (ttl == null) {
            ttl = Duration.ofHours(1);
        }
    }
}
