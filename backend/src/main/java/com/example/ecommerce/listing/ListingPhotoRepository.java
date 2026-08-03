package com.example.ecommerce.listing;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, UUID> {

    List<ListingPhoto> findByListingIdOrderByPositionAsc(UUID listingId);

    List<ListingPhoto> findByListingIdInOrderByListingIdAscPositionAsc(Collection<UUID> listingIds);

    void deleteByListingId(UUID listingId);
}
