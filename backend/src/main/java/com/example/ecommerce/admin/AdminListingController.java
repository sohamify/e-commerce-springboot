package com.example.ecommerce.admin;

import com.example.ecommerce.listing.ListingService;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.listing.dto.ListingSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/listings")
public class AdminListingController {

    private final ListingService listingService;

    public AdminListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public List<ListingSummaryResponse> list(@RequestParam(required = false) ListingStatus status) {
        return listingService.adminList(status);
    }

    @PostMapping("/{id}/remove")
    public void remove(@PathVariable UUID id) {
        listingService.adminRemove(id);
    }

    @PostMapping("/{id}/restore")
    public void restore(@PathVariable UUID id) {
        listingService.adminRestore(id);
    }
}
