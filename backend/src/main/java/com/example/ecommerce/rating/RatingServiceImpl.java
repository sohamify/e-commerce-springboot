package com.example.ecommerce.rating;

import com.example.ecommerce.common.exception.DuplicateRatingException;
import com.example.ecommerce.common.exception.ListingNotFoundException;
import com.example.ecommerce.common.exception.RatingNotAllowedException;
import com.example.ecommerce.listing.Listing;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import com.example.ecommerce.rating.dto.RatingRequest;
import com.example.ecommerce.rating.dto.RatingResponse;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, ListingRepository listingRepository,
                              UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RatingResponse submitRating(UUID raterId, UUID listingId, RatingRequest request) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);
        if (listing.getStatus() != ListingStatus.SOLD) {
            throw new RatingNotAllowedException();
        }

        UUID rateeId;
        if (raterId.equals(listing.getSellerId())) {
            rateeId = listing.getBuyerId();
        } else if (raterId.equals(listing.getBuyerId())) {
            rateeId = listing.getSellerId();
        } else {
            throw new RatingNotAllowedException();
        }

        if (ratingRepository.existsByListingIdAndRaterId(listingId, raterId)) {
            throw new DuplicateRatingException();
        }

        Rating rating = Rating.builder()
            .listingId(listingId)
            .raterId(raterId)
            .rateeId(rateeId)
            .score(request.score().shortValue())
            .comment(request.comment())
            .build();
        rating = ratingRepository.saveAndFlush(rating);

        applyRatingToRatee(rateeId, request.score());

        User rater = requireUser(raterId);
        return new RatingResponse(
            rating.getId(), listingId, listing.getTitle(), SellerSummaryResponse.from(rater),
            rating.getScore(), rating.getComment(), rating.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> listForUser(UUID userId) {
        List<Rating> ratings = ratingRepository.findByRateeIdOrderByCreatedAtDesc(userId);
        if (ratings.isEmpty()) {
            return List.of();
        }

        List<UUID> listingIds = ratings.stream().map(Rating::getListingId).distinct().toList();
        List<UUID> raterIds = ratings.stream().map(Rating::getRaterId).distinct().toList();

        Map<UUID, String> titleByListingId = new HashMap<>();
        for (Listing listing : listingRepository.findAllById(listingIds)) {
            titleByListingId.put(listing.getId(), listing.getTitle());
        }

        Map<UUID, SellerSummaryResponse> raterById = new HashMap<>();
        for (User user : userRepository.findAllById(raterIds)) {
            raterById.put(user.getId(), SellerSummaryResponse.from(user));
        }

        return ratings.stream()
            .map(rating -> new RatingResponse(
                rating.getId(),
                rating.getListingId(),
                titleByListingId.getOrDefault(rating.getListingId(), ""),
                raterById.get(rating.getRaterId()),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()))
            .toList();
    }

    private void applyRatingToRatee(UUID rateeId, int score) {
        User ratee = requireUser(rateeId);
        double currentAverage = ratee.getRatingAverage() == null ? 0 : ratee.getRatingAverage();
        int currentCount = ratee.getRatingCount();
        double newAverage = (currentAverage * currentCount + score) / (double) (currentCount + 1);
        ratee.setRatingAverage(newAverage);
        ratee.setRatingCount(currentCount + 1);
        userRepository.save(ratee);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Rating references a user that no longer exists"));
    }
}
