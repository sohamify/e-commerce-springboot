package com.example.ecommerce.rating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    Optional<Rating> findByListingIdAndRaterId(UUID listingId, UUID raterId);

    boolean existsByListingIdAndRaterId(UUID listingId, UUID raterId);

    List<Rating> findByRateeIdOrderByCreatedAtDesc(UUID rateeId);

    List<Rating> findByRaterIdAndListingIdIn(UUID raterId, Collection<UUID> listingIds);
}
