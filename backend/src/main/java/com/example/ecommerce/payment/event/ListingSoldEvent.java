package com.example.ecommerce.payment.event;

import java.util.UUID;

/** Published once a payment is CAPTURED and the listing has been claimed for the buyer. */
public record ListingSoldEvent(UUID listingId, UUID buyerId, UUID sellerId, UUID paymentId) {
}
