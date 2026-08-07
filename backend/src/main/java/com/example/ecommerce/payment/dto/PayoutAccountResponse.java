package com.example.ecommerce.payment.dto;

import com.example.ecommerce.payment.PayoutAccountStatus;

/** {@code status} is null when the current user has never started payout onboarding. */
public record PayoutAccountResponse(PayoutAccountStatus status) {
}
