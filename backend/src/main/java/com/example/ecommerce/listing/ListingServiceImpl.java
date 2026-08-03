package com.example.ecommerce.listing;

import com.example.ecommerce.common.exception.CannotBuyOwnListingException;
import com.example.ecommerce.common.exception.ListingAccessDeniedException;
import com.example.ecommerce.common.exception.ListingNotEditableException;
import com.example.ecommerce.common.exception.ListingNotFoundException;
import com.example.ecommerce.common.exception.ListingUnavailableException;
import com.example.ecommerce.listing.dto.ListingDetailResponse;
import com.example.ecommerce.listing.dto.ListingRequest;
import com.example.ecommerce.listing.dto.ListingSearchResponse;
import com.example.ecommerce.listing.dto.ListingSummaryResponse;
import com.example.ecommerce.listing.dto.OrderSummaryResponse;
import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import com.example.ecommerce.rating.RatingRepository;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final ListingPhotoRepository listingPhotoRepository;
    private final ListingTagRepository listingTagRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public ListingServiceImpl(
            ListingRepository listingRepository,
            ListingPhotoRepository listingPhotoRepository,
            ListingTagRepository listingTagRepository,
            UserRepository userRepository,
            RatingRepository ratingRepository) {
        this.listingRepository = listingRepository;
        this.listingPhotoRepository = listingPhotoRepository;
        this.listingTagRepository = listingTagRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional
    public ListingDetailResponse create(UUID sellerId, ListingRequest request) {
        Listing listing = Listing.builder()
            .sellerId(sellerId)
            .title(request.title())
            .description(request.description())
            .price(request.price())
            .condition(request.condition())
            .category(request.category())
            .location(request.location())
            .status(ListingStatus.ACTIVE)
            .build();
        // saveAndFlush, not save: @CreationTimestamp only populates createdAt/updatedAt on the
        // actual flush, and toDetailResponse() below reads them straight off this instance.
        listing = listingRepository.saveAndFlush(listing);

        savePhotos(listing.getId(), request.photoUrls());
        saveTags(listing.getId(), request.tags());

        return toDetailResponse(listing, request.photoUrls(), request.tags(), requireUser(sellerId));
    }

    @Override
    @Transactional
    public ListingDetailResponse update(UUID sellerId, UUID listingId, ListingRequest request) {
        Listing listing = requireListing(listingId);
        requireOwner(listing, sellerId);
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ListingNotEditableException();
        }

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setCondition(request.condition());
        listing.setCategory(request.category());
        listing.setLocation(request.location());
        listing = listingRepository.save(listing);

        listingPhotoRepository.deleteByListingId(listingId);
        savePhotos(listingId, request.photoUrls());
        listingTagRepository.deleteByListingId(listingId);
        saveTags(listingId, request.tags());

        return toDetailResponse(listing, request.photoUrls(), request.tags(), requireUser(sellerId));
    }

    @Override
    @Transactional
    public void remove(UUID sellerId, UUID listingId) {
        Listing listing = requireListing(listingId);
        requireOwner(listing, sellerId);
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ListingNotEditableException();
        }

        listing.setStatus(ListingStatus.REMOVED);
        listingRepository.save(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingDetailResponse get(UUID listingId) {
        Listing listing = requireListing(listingId);
        List<String> photoUrls = listingPhotoRepository.findByListingIdOrderByPositionAsc(listingId).stream()
            .map(ListingPhoto::getUrl)
            .toList();
        List<String> tags = listingTagRepository.findByListingId(listingId).stream()
            .map(ListingTag::getTag)
            .toList();
        return toDetailResponse(listing, photoUrls, tags, requireUser(listing.getSellerId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> mine(UUID sellerId) {
        List<Listing> listings = listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        return toSummaries(listings);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingSearchResponse search(
            String q,
            ListingCategory category,
            ListingCondition condition,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String location,
            String tag,
            int page,
            int size) {
        Page<Listing> results = listingRepository.search(
            blankToNull(q),
            category == null ? null : category.name(),
            condition == null ? null : condition.name(),
            minPrice,
            maxPrice,
            blankToNull(location),
            blankToNull(tag),
            PageRequest.of(page, size));

        return new ListingSearchResponse(
            toSummaries(results.getContent()),
            results.getNumber(),
            results.getSize(),
            results.getTotalElements(),
            results.getTotalPages());
    }

    @Override
    @Transactional
    public ListingDetailResponse purchase(UUID buyerId, UUID listingId) {
        Listing listing = requireListing(listingId);
        if (listing.getSellerId().equals(buyerId)) {
            throw new CannotBuyOwnListingException();
        }

        int updated = listingRepository.claim(listingId, buyerId);
        if (updated == 0) {
            throw new ListingUnavailableException();
        }

        return get(listingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> purchases(UUID buyerId) {
        return toOrderSummaries(listingRepository.findByBuyerIdOrderBySoldAtDesc(buyerId), false, buyerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> sales(UUID sellerId) {
        return toOrderSummaries(
            listingRepository.findBySellerIdAndStatusOrderBySoldAtDesc(sellerId, ListingStatus.SOLD), true, sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> adminList(ListingStatus status) {
        List<Listing> listings = status == null
            ? listingRepository.findAllByOrderByCreatedAtDesc()
            : listingRepository.findByStatusOrderByCreatedAtDesc(status);
        return toSummaries(listings);
    }

    @Override
    @Transactional
    public void adminRemove(UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);
        listing.setStatus(ListingStatus.REMOVED);
        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public void adminRestore(UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);
        listing.setStatus(ListingStatus.ACTIVE);
        listingRepository.save(listing);
    }

    private List<OrderSummaryResponse> toOrderSummaries(List<Listing> listings, boolean counterpartyIsBuyer, UUID viewerId) {
        if (listings.isEmpty()) {
            return List.of();
        }

        List<UUID> listingIds = listings.stream().map(Listing::getId).toList();
        List<UUID> counterpartyIds = listings.stream()
            .map(counterpartyIsBuyer ? Listing::getBuyerId : Listing::getSellerId)
            .distinct()
            .toList();

        Map<UUID, String> primaryPhotoByListingId = new HashMap<>();
        for (ListingPhoto photo : listingPhotoRepository.findByListingIdInOrderByListingIdAscPositionAsc(listingIds)) {
            primaryPhotoByListingId.putIfAbsent(photo.getListingId(), photo.getUrl());
        }

        Map<UUID, SellerSummaryResponse> counterpartyById = new HashMap<>();
        for (User user : userRepository.findAllById(counterpartyIds)) {
            counterpartyById.put(user.getId(), SellerSummaryResponse.from(user));
        }

        Set<UUID> ratedListingIds = new HashSet<>();
        for (var rating : ratingRepository.findByRaterIdAndListingIdIn(viewerId, listingIds)) {
            ratedListingIds.add(rating.getListingId());
        }

        return listings.stream()
            .map(listing -> new OrderSummaryResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getPrice(),
                primaryPhotoByListingId.get(listing.getId()),
                counterpartyById.get(counterpartyIsBuyer ? listing.getBuyerId() : listing.getSellerId()),
                listing.getSoldAt(),
                ratedListingIds.contains(listing.getId())))
            .toList();
    }

    private List<ListingSummaryResponse> toSummaries(List<Listing> listings) {
        if (listings.isEmpty()) {
            return List.of();
        }

        List<UUID> listingIds = listings.stream().map(Listing::getId).toList();
        List<UUID> sellerIds = listings.stream().map(Listing::getSellerId).distinct().toList();

        Map<UUID, String> primaryPhotoByListingId = new HashMap<>();
        for (ListingPhoto photo : listingPhotoRepository.findByListingIdInOrderByListingIdAscPositionAsc(listingIds)) {
            primaryPhotoByListingId.putIfAbsent(photo.getListingId(), photo.getUrl());
        }

        Map<UUID, SellerSummaryResponse> sellerById = new HashMap<>();
        for (User user : userRepository.findAllById(sellerIds)) {
            sellerById.put(user.getId(), SellerSummaryResponse.from(user));
        }

        return listings.stream()
            .map(listing -> new ListingSummaryResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getPrice(),
                listing.getCondition(),
                listing.getCategory(),
                listing.getLocation(),
                listing.getStatus(),
                primaryPhotoByListingId.get(listing.getId()),
                sellerById.get(listing.getSellerId()),
                listing.getCreatedAt()))
            .sorted(Comparator.comparing(ListingSummaryResponse::createdAt).reversed())
            .toList();
    }

    private void savePhotos(UUID listingId, List<String> photoUrls) {
        short position = 0;
        for (String url : photoUrls) {
            listingPhotoRepository.save(ListingPhoto.builder()
                .listingId(listingId)
                .url(url)
                .position(position++)
                .build());
        }
    }

    private void saveTags(UUID listingId, List<String> tags) {
        for (String tag : tags.stream().map(t -> t.trim().toLowerCase()).distinct().toList()) {
            if (!tag.isEmpty()) {
                listingTagRepository.save(ListingTag.builder().listingId(listingId).tag(tag).build());
            }
        }
    }

    private Listing requireListing(UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);
        if (listing.getStatus() == ListingStatus.REMOVED) {
            throw new ListingNotFoundException();
        }
        return listing;
    }

    private void requireOwner(Listing listing, UUID sellerId) {
        if (!listing.getSellerId().equals(sellerId)) {
            throw new ListingAccessDeniedException();
        }
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Listing references a user that no longer exists"));
    }

    private ListingDetailResponse toDetailResponse(Listing listing, List<String> photoUrls, List<String> tags, User seller) {
        return new ListingDetailResponse(
            listing.getId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getPrice(),
            listing.getCondition(),
            listing.getCategory(),
            listing.getLocation(),
            listing.getStatus(),
            photoUrls,
            tags,
            SellerSummaryResponse.from(seller),
            listing.getCreatedAt());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
