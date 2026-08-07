package com.example.ecommerce.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerPayoutAccountRepository extends JpaRepository<SellerPayoutAccount, UUID> {

    Optional<SellerPayoutAccount> findByUserId(UUID userId);
}
