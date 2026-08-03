package com.example.ecommerce.auth.jwt;

import com.example.ecommerce.user.UserRole;
import java.time.Duration;
import java.util.UUID;

/** Issues and parses access tokens. Swappable signing scheme without touching callers. */
public interface JwtService {

    String issueAccessToken(UUID userId, String email, UserRole role);

    /** @throws InvalidAccessTokenException if the token is malformed, unsigned, or expired */
    JwtPrincipal parseAccessToken(String token);

    Duration accessTokenTtl();
}
