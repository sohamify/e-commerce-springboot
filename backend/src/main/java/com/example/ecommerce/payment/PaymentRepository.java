package com.example.ecommerce.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Conditional update: only succeeds (returns 1) if this payment is still CREATED, so a
     * concurrent /verify call and the payment.captured webhook for the same order can't both
     * "win" and double-process it — mirrors {@code ListingRepository.claim()}.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Payment p SET p.status = com.example.ecommerce.payment.PaymentStatus.CAPTURED,
            p.razorpayPaymentId = :razorpayPaymentId
        WHERE p.razorpayOrderId = :razorpayOrderId AND p.status = com.example.ecommerce.payment.PaymentStatus.CREATED
        """)
    int markCaptured(@Param("razorpayOrderId") String razorpayOrderId, @Param("razorpayPaymentId") String razorpayPaymentId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Payment p SET p.status = com.example.ecommerce.payment.PaymentStatus.FAILED
        WHERE p.razorpayOrderId = :razorpayOrderId AND p.status = com.example.ecommerce.payment.PaymentStatus.CREATED
        """)
    int markFailed(@Param("razorpayOrderId") String razorpayOrderId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Payment p SET p.status = com.example.ecommerce.payment.PaymentStatus.REFUNDED
        WHERE p.id = :id AND p.status = com.example.ecommerce.payment.PaymentStatus.CAPTURED
        """)
    int markRefunded(@Param("id") UUID id);
}
