package com.example.ecommerce.listing.dto;

import com.example.ecommerce.listing.ListingCategory;
import com.example.ecommerce.listing.ListingCondition;
import com.example.ecommerce.listing.ListingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingSummaryResponse(
    UUID id,
    String title,
    BigDecimal price,
    ListingCondition condition,
    ListingCategory category,
    String location,
    ListingStatus status,
    String primaryPhotoUrl,
    SellerSummaryResponse seller,
    Instant createdAt
) {
}
