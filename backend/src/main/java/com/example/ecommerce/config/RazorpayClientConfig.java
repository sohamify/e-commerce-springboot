package com.example.ecommerce.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RazorpayClientConfig.class);

    @Bean
    RazorpayClient razorpayClient(RazorpayProperties properties) throws RazorpayException {
        // key_id isn't sensitive (it's exposed to the frontend anyway) so it's logged in full;
        // key_secret is logged only as a length, which is still enough to catch the classic
        // Secret Manager gotcha (a stray trailing newline making the value one character longer
        // than what's actually in the dashboard) without ever printing the secret itself.
        log.info("Configuring Razorpay client — key_id='{}' ({} chars), key_secret={} chars, webhook_secret={} chars",
            properties.keyId(),
            properties.keyId() == null ? 0 : properties.keyId().length(),
            properties.keySecret() == null ? 0 : properties.keySecret().length(),
            properties.webhookSecret() == null ? 0 : properties.webhookSecret().length());
        return new RazorpayClient(properties.keyId(), properties.keySecret());
    }
}
