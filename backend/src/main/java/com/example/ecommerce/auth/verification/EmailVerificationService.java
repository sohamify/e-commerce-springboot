package com.example.ecommerce.auth.verification;

import java.util.UUID;

public interface EmailVerificationService {

    /** Invalidates any outstanding tokens for the user and issues a fresh one (the raw value for the email link). */
    String issueToken(UUID userId);

    /** Marks the owning user's email verified and consumes the token; returns that user's id. */
    UUID consumeToken(String rawToken);
}
