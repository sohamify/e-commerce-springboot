package com.example.ecommerce.listing.dto;

import com.example.ecommerce.listing.ListingCategory;
import com.example.ecommerce.listing.ListingCondition;
import com.example.ecommerce.listing.ListingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListingDetailResponse(
    UUID id,
    String title,
    String description,
    BigDecimal price,
    ListingCondition condition,
    ListingCategory category,
    String location,
    ListingStatus status,
    List<String> photoUrls,
    List<String> tags,
    SellerSummaryResponse seller,
    Instant createdAt
) {
}
