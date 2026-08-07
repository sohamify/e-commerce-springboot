package com.example.ecommerce.payment.dto;

import java.math.BigDecimal;

/** Everything the frontend needs to open Razorpay Checkout for this purchase. {@code amount} is
 * in rupees (matches the listing's price display); the paise conversion happens only at the
 * Razorpay API boundary inside RazorpayService. */
public record PurchaseInitiationResponse(String orderId, String keyId, BigDecimal amount, String currency) {
}
