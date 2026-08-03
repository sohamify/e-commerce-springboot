package com.example.ecommerce.rating.dto;

import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
    UUID id,
    UUID listingId,
    String listingTitle,
    SellerSummaryResponse rater,
    int score,
    String comment,
    Instant createdAt
) {
}
