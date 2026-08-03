package com.example.ecommerce.messaging;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageThreadRepository extends JpaRepository<MessageThread, UUID> {

    Optional<MessageThread> findByListingIdAndBuyerId(UUID listingId, UUID buyerId);

    List<MessageThread> findByBuyerIdOrSellerIdOrderByUpdatedAtDesc(UUID buyerId, UUID sellerId);
}
