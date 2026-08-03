package com.example.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Refresh-cookie attributes, environment-aware: dev (same registrable domain, different ports)
 * only needs SameSite=Lax over plain HTTP; prod (frontend and backend on different domains)
 * needs SameSite=None and Secure for the browser to send it cross-site at all.
 */
@ConfigurationProperties(prefix = "app.security.refresh-cookie")
public record CookieProperties(boolean secure, String sameSite) {

    public CookieProperties {
        if (sameSite == null) {
            sameSite = "Lax";
        }
    }
}
