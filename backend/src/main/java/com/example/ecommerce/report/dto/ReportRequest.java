package com.example.ecommerce.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ReportRequest(
    UUID listingId,
    UUID userId,
    @NotBlank @Size(max = 1000) String reason
) {
}
