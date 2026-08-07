package com.example.ecommerce.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Razorpay API credentials and the one place the platform commission rate lives. */
@ConfigurationProperties(prefix = "app.razorpay")
public record RazorpayProperties(String keyId, String keySecret, String webhookSecret, BigDecimal platformFeePercent) {

    public RazorpayProperties {
        // Secret Manager values are easy to corrupt with a trailing newline (e.g. `echo "x" |
        // gcloud secrets versions add ...` — echo appends \n) — Razorpay rejects the resulting
        // credential wholesale as "Authentication failed" rather than anything that points at
        // whitespace, so strip it defensively instead of requiring every secret to be pasted in
        // exactly right.
        keyId = keyId == null ? null : keyId.trim();
        keySecret = keySecret == null ? null : keySecret.trim();
        webhookSecret = webhookSecret == null ? null : webhookSecret.trim();
    }
}
