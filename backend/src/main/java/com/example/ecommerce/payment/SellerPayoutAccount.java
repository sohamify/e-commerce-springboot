package com.example.ecommerce.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Links a user to their Razorpay Route Linked Account. We deliberately store only the
 * account id and its status here — Razorpay's Linked Account is the system of record for the
 * actual bank/KYC details, not this table.
 */
@Entity
@Table(name = "seller_payout_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPayoutAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "razorpay_account_id", nullable = false)
    private String razorpayAccountId;

    @Column(name = "razorpay_product_id", nullable = false)
    private String razorpayProductId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutAccountStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
