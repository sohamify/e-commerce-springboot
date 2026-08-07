package com.example.ecommerce.payment;

import com.example.ecommerce.payment.dto.PurchaseInitiationResponse;
import com.example.ecommerce.payment.dto.VerifyPaymentRequest;
import com.example.ecommerce.payment.dto.VerifyPaymentResponse;
import java.util.UUID;

public interface PaymentService {

    PurchaseInitiationResponse initiatePurchase(UUID buyerId, UUID listingId);

    VerifyPaymentResponse verifyPayment(UUID buyerId, VerifyPaymentRequest request);

    /** Invoked only by the payment.captured webhook handler — the authoritative path. */
    void handleCaptured(String razorpayOrderId, String razorpayPaymentId);

    /** Invoked only by the payment.failed webhook handler. */
    void handleFailed(String razorpayOrderId);

    void refund(UUID paymentId);
}
