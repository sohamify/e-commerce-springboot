package com.example.ecommerce.admin;

import com.example.ecommerce.admin.dto.AdminDashboardResponse;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.report.ReportRepository;
import com.example.ecommerce.report.ReportStatus;
import com.example.ecommerce.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final ListingRepository listingRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public AdminDashboardController(ListingRepository listingRepository, ReportRepository reportRepository,
                                     UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public AdminDashboardResponse get() {
        return new AdminDashboardResponse(
            listingRepository.countByStatus(ListingStatus.FLAGGED),
            reportRepository.countByStatus(ReportStatus.OPEN),
            userRepository.count(),
            listingRepository.count(),
            listingRepository.countByStatusAndSoldAtAfter(ListingStatus.SOLD, Instant.now().minus(7, ChronoUnit.DAYS)));
    }
}
