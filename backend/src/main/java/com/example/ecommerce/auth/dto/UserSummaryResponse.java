package com.example.ecommerce.auth.dto;

import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRole;
import java.util.UUID;

public record UserSummaryResponse(
    UUID id,
    String email,
    UserRole role,
    boolean emailVerified,
    String displayName,
    String avatarUrl,
    String location,
    Double ratingAverage,
    int ratingCount
) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.isEmailVerified(),
            user.getDisplayName(),
            user.getAvatarUrl(),
            user.getLocation(),
            user.getRatingAverage(),
            user.getRatingCount());
    }
}
