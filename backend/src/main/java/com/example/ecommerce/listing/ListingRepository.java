package com.example.ecommerce.listing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    List<Listing> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);

    List<Listing> findByBuyerIdOrderBySoldAtDesc(UUID buyerId);

    List<Listing> findBySellerIdAndStatusOrderBySoldAtDesc(UUID sellerId, ListingStatus status);

    List<Listing> findAllByOrderByCreatedAtDesc();

    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    long countByStatus(ListingStatus status);

    long countByStatusAndSoldAtAfter(ListingStatus status, Instant soldAfter);

    /**
     * Conditional update: only succeeds (returns 1) if the listing is still ACTIVE, so two
     * concurrent buyers can't both "win" the same one-of-a-kind item.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Listing l SET l.status = com.example.ecommerce.listing.ListingStatus.SOLD,
            l.buyerId = :buyerId, l.soldAt = CURRENT_TIMESTAMP
        WHERE l.id = :id AND l.status = com.example.ecommerce.listing.ListingStatus.ACTIVE
        """)
    int claim(@Param("id") UUID id, @Param("buyerId") UUID buyerId);

    @Query(value = """
        SELECT l.id, l.seller_id, l.title, l.description, l.price, l.condition, l.category,
               l.location, l.status, l.buyer_id, l.sold_at, l.created_at, l.updated_at
        FROM listings l
        WHERE l.status = 'ACTIVE'
          AND (:q IS NULL OR l.search_vector @@ plainto_tsquery('english', :q))
          AND (:category IS NULL OR l.category = :category)
          AND (:condition IS NULL OR l.condition = :condition)
          AND (:minPrice IS NULL OR l.price >= :minPrice)
          AND (:maxPrice IS NULL OR l.price <= :maxPrice)
          AND (:location IS NULL OR l.location ILIKE '%' || :location || '%')
          AND (:tag IS NULL OR EXISTS (
              SELECT 1 FROM listing_tags lt WHERE lt.listing_id = l.id AND lt.tag = :tag))
        ORDER BY (CASE WHEN :q IS NULL THEN 0 ELSE ts_rank(l.search_vector, plainto_tsquery('english', :q)) END) DESC,
                 l.created_at DESC
        """,
        countQuery = """
        SELECT count(*)
        FROM listings l
        WHERE l.status = 'ACTIVE'
          AND (:q IS NULL OR l.search_vector @@ plainto_tsquery('english', :q))
          AND (:category IS NULL OR l.category = :category)
          AND (:condition IS NULL OR l.condition = :condition)
          AND (:minPrice IS NULL OR l.price >= :minPrice)
          AND (:maxPrice IS NULL OR l.price <= :maxPrice)
          AND (:location IS NULL OR l.location ILIKE '%' || :location || '%')
          AND (:tag IS NULL OR EXISTS (
              SELECT 1 FROM listing_tags lt WHERE lt.listing_id = l.id AND lt.tag = :tag))
        """,
        nativeQuery = true)
    Page<Listing> search(
        @Param("q") String q,
        @Param("category") String category,
        @Param("condition") String condition,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("location") String location,
        @Param("tag") String tag,
        Pageable pageable);
}
