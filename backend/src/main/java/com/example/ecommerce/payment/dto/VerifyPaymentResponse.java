package com.example.ecommerce.payment.dto;

import java.util.UUID;

/** {@code sold} is false in the (rare) case this payment won a legitimate signature check but
 * lost the race to claim the listing — the frontend shows a distinct "refunded, sold to someone
 * else" message rather than treating it as a normal success. */
public record VerifyPaymentResponse(boolean sold, UUID listingId) {
}
