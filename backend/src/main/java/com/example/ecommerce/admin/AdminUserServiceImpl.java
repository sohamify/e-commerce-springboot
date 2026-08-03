package com.example.ecommerce.admin;

import com.example.ecommerce.admin.dto.AdminUserDetailResponse;
import com.example.ecommerce.admin.dto.AdminUserSummaryResponse;
import com.example.ecommerce.common.exception.UserNotFoundException;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import com.example.ecommerce.user.UserStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public AdminUserServiceImpl(UserRepository userRepository, ListingRepository listingRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserSummaryResponse> search(String q) {
        String normalized = (q == null || q.isBlank()) ? null : q.trim();
        return userRepository.search(normalized, PageRequest.of(0, 50)).stream()
            .map(AdminUserSummaryResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getDetail(UUID userId) {
        User user = requireUser(userId);
        int listingsCount = listingRepository.findBySellerIdOrderByCreatedAtDesc(userId).size();
        int purchasesCount = listingRepository.findByBuyerIdOrderBySoldAtDesc(userId).size();
        int salesCount = listingRepository.findBySellerIdAndStatusOrderBySoldAtDesc(userId, ListingStatus.SOLD).size();
        return AdminUserDetailResponse.from(user, listingsCount, purchasesCount, salesCount);
    }

    @Override
    @Transactional
    public void suspend(UUID userId) {
        setStatus(userId, UserStatus.SUSPENDED);
    }

    @Override
    @Transactional
    public void ban(UUID userId) {
        setStatus(userId, UserStatus.BANNED);
    }

    @Override
    @Transactional
    public void reactivate(UUID userId) {
        setStatus(userId, UserStatus.ACTIVE);
    }

    private void setStatus(UUID userId, UserStatus status) {
        User user = requireUser(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}
