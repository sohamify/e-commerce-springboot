package com.example.ecommerce.listing.dto;

import com.example.ecommerce.user.User;
import java.time.Instant;
import java.util.UUID;

public record SellerSummaryResponse(
    UUID id,
    String displayName,
    String avatarUrl,
    String location,
    Double ratingAverage,
    int ratingCount,
    Instant memberSince
) {

    public static SellerSummaryResponse from(User user) {
        return new SellerSummaryResponse(
            user.getId(),
            user.getDisplayName(),
            user.getAvatarUrl(),
            user.getLocation(),
            user.getRatingAverage(),
            user.getRatingCount(),
            user.getCreatedAt());
    }
}
