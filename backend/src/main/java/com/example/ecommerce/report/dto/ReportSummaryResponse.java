package com.example.ecommerce.report.dto;

import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import com.example.ecommerce.report.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public record ReportSummaryResponse(
    UUID id,
    SellerSummaryResponse reporter,
    UUID reportedListingId,
    String reportedListingTitle,
    SellerSummaryResponse reportedUser,
    String reason,
    ReportStatus status,
    Instant createdAt
) {
}
