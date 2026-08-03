package com.example.ecommerce.messaging.dto;

import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import java.time.Instant;
import java.util.UUID;

public record ThreadSummaryResponse(
    UUID id,
    UUID listingId,
    String listingTitle,
    String listingPhotoUrl,
    SellerSummaryResponse counterparty,
    String lastMessagePreview,
    Instant lastMessageAt
) {
}
