package com.example.ecommerce.admin.dto;

import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRole;
import com.example.ecommerce.user.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record AdminUserDetailResponse(
    UUID id,
    String email,
    String displayName,
    String avatarUrl,
    String location,
    UserRole role,
    UserStatus status,
    boolean emailVerified,
    Double ratingAverage,
    int ratingCount,
    Instant createdAt,
    int listingsCount,
    int purchasesCount,
    int salesCount
) {

    public static AdminUserDetailResponse from(User user, int listingsCount, int purchasesCount, int salesCount) {
        return new AdminUserDetailResponse(
            user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.getLocation(),
            user.getRole(), user.getStatus(), user.isEmailVerified(), user.getRatingAverage(), user.getRatingCount(),
            user.getCreatedAt(), listingsCount, purchasesCount, salesCount);
    }
}
