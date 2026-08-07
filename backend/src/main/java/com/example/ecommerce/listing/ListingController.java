package com.example.ecommerce.listing;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.listing.dto.ListingDetailResponse;
import com.example.ecommerce.listing.dto.ListingPhotoUploadResponse;
import com.example.ecommerce.listing.dto.ListingRequest;
import com.example.ecommerce.listing.dto.ListingSearchResponse;
import com.example.ecommerce.listing.dto.ListingSummaryResponse;
import com.example.ecommerce.listing.dto.OrderSummaryResponse;
import com.example.ecommerce.messaging.MessagingService;
import com.example.ecommerce.messaging.dto.MessageRequest;
import com.example.ecommerce.messaging.dto.ThreadSummaryResponse;
import com.example.ecommerce.payment.PaymentService;
import com.example.ecommerce.payment.dto.PurchaseInitiationResponse;
import com.example.ecommerce.storage.PhotoStorageService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;
    private final PhotoStorageService photoStorageService;
    private final MessagingService messagingService;
    private final PaymentService paymentService;

    public ListingController(
            ListingService listingService,
            PhotoStorageService photoStorageService,
            MessagingService messagingService,
            PaymentService paymentService) {
        this.listingService = listingService;
        this.photoStorageService = photoStorageService;
        this.messagingService = messagingService;
        this.paymentService = paymentService;
    }

    @PostMapping("/photos")
    public ListingPhotoUploadResponse uploadPhoto(@RequestPart("file") MultipartFile file) {
        return new ListingPhotoUploadResponse(photoStorageService.upload(file));
    }

    @PostMapping
    public ResponseEntity<ListingDetailResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal, @Valid @RequestBody ListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.create(principal.userId(), request));
    }

    @PutMapping("/{id}")
    public ListingDetailResponse update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ListingRequest request) {
        return listingService.update(principal.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable UUID id) {
        listingService.remove(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public List<ListingSummaryResponse> mine(@AuthenticationPrincipal JwtPrincipal principal) {
        return listingService.mine(principal.userId());
    }

    /** Kicks off a purchase — creates a Razorpay order (with the seller's Route transfer) rather
     * than completing the sale synchronously; the listing is only actually marked SOLD once
     * payment is confirmed via /api/payments/verify or the payment.captured webhook. */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<PurchaseInitiationResponse> purchase(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePurchase(principal.userId(), id));
    }

    @GetMapping("/purchases")
    public List<OrderSummaryResponse> purchases(@AuthenticationPrincipal JwtPrincipal principal) {
        return listingService.purchases(principal.userId());
    }

    @GetMapping("/sales")
    public List<OrderSummaryResponse> sales(@AuthenticationPrincipal JwtPrincipal principal) {
        return listingService.sales(principal.userId());
    }

    @GetMapping("/{id}")
    public ListingDetailResponse get(@PathVariable UUID id) {
        return listingService.get(id);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ThreadSummaryResponse> startThread(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messagingService.startThread(principal.userId(), id, request));
    }

    @GetMapping
    public ListingSearchResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ListingCategory category,
            @RequestParam(required = false) ListingCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return listingService.search(q, category, condition, minPrice, maxPrice, location, tag, page, Math.min(size, 50));
    }
}
