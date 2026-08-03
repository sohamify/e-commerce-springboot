package com.example.ecommerce.admin.dto;

import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRole;
import com.example.ecommerce.user.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record AdminUserSummaryResponse(
    UUID id,
    String email,
    String displayName,
    UserRole role,
    UserStatus status,
    boolean emailVerified,
    Instant createdAt
) {

    public static AdminUserSummaryResponse from(User user) {
        return new AdminUserSummaryResponse(
            user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(),
            user.getStatus(), user.isEmailVerified(), user.getCreatedAt());
    }
}
