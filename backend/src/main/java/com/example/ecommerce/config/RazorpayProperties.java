package com.example.ecommerce.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Razorpay API credentials and the one place the platform commission rate lives.
 *
 * {@code routeEnabled} is a temporary switch: Route (Linked Accounts / transfers) requires
 * Razorpay support to enable it on the account, which hasn't happened yet. While it's false,
 * seller payout onboarding is refused up front and orders are created without a transfers
 * array — plain payment collection into the platform's own balance — so the rest of the flow
 * (order creation, Checkout, signature verification, the webhook) can be built and tested now
 * instead of blocking on Route approval. Flip to true once Route is live.
 */
@ConfigurationProperties(prefix = "app.razorpay")
public record RazorpayProperties(
        String keyId, String keySecret, String webhookSecret, BigDecimal platformFeePercent, boolean routeEnabled) {

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
