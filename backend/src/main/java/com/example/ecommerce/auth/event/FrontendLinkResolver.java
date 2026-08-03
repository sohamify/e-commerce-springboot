package com.example.ecommerce.auth.event;

import com.example.ecommerce.config.FrontendProperties;
import com.example.ecommerce.user.UserRole;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/** Builds the frontend links embedded in verification/reset emails, routing admins and customers to their own app. */
@Component
public class FrontendLinkResolver {

    private final FrontendProperties properties;

    public FrontendLinkResolver(FrontendProperties properties) {
        this.properties = properties;
    }

    public String verificationLink(UserRole role, String rawToken) {
        return baseUrlFor(role) + "/verify-email?token=" + encode(rawToken);
    }

    public String passwordResetLink(UserRole role, String rawToken) {
        return baseUrlFor(role) + "/reset-password?token=" + encode(rawToken);
    }

    private String baseUrlFor(UserRole role) {
        return role == UserRole.ADMIN ? properties.adminBaseUrl() : properties.storefrontBaseUrl();
    }

    private String encode(String token) {
        return URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
