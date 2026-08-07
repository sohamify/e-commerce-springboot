package com.example.ecommerce.payment.dto;

/** Lets the frontend know whether Route (seller payouts) is actually usable right now, so it can
 * skip the "set up payouts first" gate while Razorpay support hasn't enabled it yet. */
public record PaymentsConfigResponse(boolean routeEnabled) {
}
