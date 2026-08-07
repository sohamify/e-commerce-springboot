package com.example.ecommerce.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Minimum viable set of fields to create a Razorpay Route Linked Account + Stakeholder + bank
 * settlement config in one onboarding submission. Razorpay's Linked Account is the system of
 * record for the rest of the KYC/bank details — we only persist the resulting account id. */
public record PayoutAccountRequest(
    @NotBlank @Size(min = 4, max = 200) String legalBusinessName,
    @NotBlank @Pattern(regexp = "individual|proprietorship|partnership|other",
        message = "must be one of individual, proprietorship, partnership, other")
    String businessType,
    @NotBlank @Size(min = 4, max = 255) String contactName,
    @NotBlank @Size(min = 8, max = 15) String phone,
    @NotBlank @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "must be a valid PAN") String pan,
    @NotBlank @Size(min = 5, max = 20) String bankAccountNumber,
    @NotBlank @Pattern(regexp = "[A-Z]{4}0[A-Z0-9]{6}", message = "must be a valid IFSC code") String ifscCode,
    @NotBlank @Size(min = 4, max = 120) String beneficiaryName
) {
}
