package com.example.ecommerce.listing;

import com.example.ecommerce.listing.dto.ListingDetailResponse;
import com.example.ecommerce.listing.dto.ListingRequest;
import com.example.ecommerce.listing.dto.ListingSearchResponse;
import com.example.ecommerce.listing.dto.ListingSummaryResponse;
import com.example.ecommerce.listing.dto.OrderSummaryResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ListingService {

    ListingDetailResponse create(UUID sellerId, ListingRequest request);

    ListingDetailResponse update(UUID sellerId, UUID listingId, ListingRequest request);

    void remove(UUID sellerId, UUID listingId);

    ListingDetailResponse get(UUID listingId);

    List<ListingSummaryResponse> mine(UUID sellerId);

    ListingDetailResponse purchase(UUID buyerId, UUID listingId);

    List<OrderSummaryResponse> purchases(UUID buyerId);

    List<OrderSummaryResponse> sales(UUID sellerId);

    ListingSearchResponse search(
        String q,
        ListingCategory category,
        ListingCondition condition,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String location,
        String tag,
        int page,
        int size);

    /** status == null means "every listing regardless of status" — admin-only, bypasses the public ACTIVE-only view. */
    List<ListingSummaryResponse> adminList(ListingStatus status);

    void adminRemove(UUID listingId);

    void adminRestore(UUID listingId);
}
