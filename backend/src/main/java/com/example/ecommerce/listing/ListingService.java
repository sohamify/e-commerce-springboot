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

    /** Atomically claims the listing for the buyer (only succeeds while still ACTIVE) and
     * returns the updated listing — the one place a listing is ever marked SOLD. Called only
     * after payment has actually been confirmed (by {@code PaymentService}), never directly
     * from a controller anymore. */
    ListingDetailResponse claimForBuyer(UUID buyerId, UUID listingId);

    /** Reverts a SOLD listing back to ACTIVE for resale — used when a captured payment is
     * refunded (either the losing side of a claim race, or an admin-initiated refund). */
    void revertToActive(UUID listingId, UUID buyerId);

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
