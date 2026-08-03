package com.example.ecommerce.auth.refreshtoken;

import java.time.Duration;
import java.util.UUID;

public interface RefreshTokenService {

    /** Issues a new refresh token for the user; returns the raw (unhashed) token to hand to the client. */
    String issue(UUID userId);

    /**
     * Validates the presented token and rotates it: the old row is revoked and a new one issued.
     * Presenting an already-revoked token is treated as possible theft and revokes every active
     * token for that user.
     */
    RotatedToken rotate(String rawToken);

    void revoke(String rawToken);

    void revokeAllForUser(UUID userId);

    Duration ttl();

    record RotatedToken(UUID userId, String newRawToken) {
    }
}
