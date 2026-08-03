package com.example.ecommerce.listing.dto;

import java.util.List;

public record ListingSearchResponse(
    List<ListingSummaryResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
