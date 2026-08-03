package com.example.ecommerce.rating;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.rating.dto.RatingRequest;
import com.example.ecommerce.rating.dto.RatingResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings/{listingId}/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingResponse> submit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable UUID listingId,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ratingService.submitRating(principal.userId(), listingId, request));
    }
}
