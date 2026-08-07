package com.example.ecommerce.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Razorpay API credentials and the one place the platform commission rate lives. */
@ConfigurationProperties(prefix = "app.razorpay")
public record RazorpayProperties(String keyId, String keySecret, String webhookSecret, BigDecimal platformFeePercent) {
}
