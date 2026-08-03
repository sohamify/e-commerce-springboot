package com.example.ecommerce.listing;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingTagRepository extends JpaRepository<ListingTag, UUID> {

    List<ListingTag> findByListingId(UUID listingId);

    void deleteByListingId(UUID listingId);
}
