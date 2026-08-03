package com.example.ecommerce.listing.dto;

import com.example.ecommerce.listing.ListingCategory;
import com.example.ecommerce.listing.ListingCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ListingRequest(
    @NotBlank @Size(max = 140) String title,
    @NotBlank String description,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price,
    @NotNull ListingCondition condition,
    @NotNull ListingCategory category,
    @Size(max = 120) String location,
    @NotEmpty List<@NotBlank String> photoUrls,
    List<@Size(max = 40) String> tags
) {
    public ListingRequest {
        tags = tags == null ? List.of() : tags;
    }
}
