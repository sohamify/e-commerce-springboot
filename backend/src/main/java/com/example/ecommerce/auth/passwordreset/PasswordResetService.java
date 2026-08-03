package com.example.ecommerce.auth.passwordreset;

import java.util.UUID;

public interface PasswordResetService {

    /** Invalidates any outstanding tokens for the user and issues a fresh one (the raw value for the email link). */
    String issueToken(UUID userId);

    /**
     * Validates and consumes the token. Does not itself change the password — that's the
     * caller's job (it needs the new password value); this only proves the token was valid
     * and returns which user it belonged to.
     */
    UUID consumeToken(String rawToken);
}
