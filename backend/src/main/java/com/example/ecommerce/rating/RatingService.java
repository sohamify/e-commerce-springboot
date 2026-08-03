package com.example.ecommerce.rating;

import com.example.ecommerce.rating.dto.RatingRequest;
import com.example.ecommerce.rating.dto.RatingResponse;
import java.util.List;
import java.util.UUID;

public interface RatingService {

    RatingResponse submitRating(UUID raterId, UUID listingId, RatingRequest request);

    List<RatingResponse> listForUser(UUID userId);
}
