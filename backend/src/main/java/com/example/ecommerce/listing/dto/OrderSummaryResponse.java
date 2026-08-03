package com.example.ecommerce.listing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One completed transaction, from either the buyer's or the seller's point of view. */
public record OrderSummaryResponse(
    UUID listingId,
    String title,
    BigDecimal price,
    String primaryPhotoUrl,
    SellerSummaryResponse counterparty,
    Instant completedAt,
    boolean ratedByMe
) {
}
