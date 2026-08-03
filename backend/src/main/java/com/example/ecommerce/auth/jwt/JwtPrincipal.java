package com.example.ecommerce.auth.jwt;

import com.example.ecommerce.user.UserRole;
import java.util.UUID;

/** The claims carried by an access token, reconstructed on every authenticated request. */
public record JwtPrincipal(UUID userId, String email, UserRole role) {
}
