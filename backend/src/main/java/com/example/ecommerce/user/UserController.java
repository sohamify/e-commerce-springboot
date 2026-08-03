package com.example.ecommerce.user;

import com.example.ecommerce.common.exception.UserNotFoundException;
import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import com.example.ecommerce.rating.RatingService;
import com.example.ecommerce.rating.dto.RatingResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public-facing profile info — the seller card wherever it appears, and the reviews behind it. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final RatingService ratingService;

    public UserController(UserRepository userRepository, RatingService ratingService) {
        this.userRepository = userRepository;
        this.ratingService = ratingService;
    }

    @GetMapping("/{id}")
    public SellerSummaryResponse get(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return SellerSummaryResponse.from(user);
    }

    @GetMapping("/{id}/ratings")
    public List<RatingResponse> ratings(@PathVariable UUID id) {
        return ratingService.listForUser(id);
    }
}
