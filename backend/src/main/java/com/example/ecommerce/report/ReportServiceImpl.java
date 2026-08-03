package com.example.ecommerce.report;

import com.example.ecommerce.common.exception.InvalidReportTargetException;
import com.example.ecommerce.common.exception.ListingNotFoundException;
import com.example.ecommerce.common.exception.ReportNotFoundException;
import com.example.ecommerce.common.exception.UserNotFoundException;
import com.example.ecommerce.listing.Listing;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import com.example.ecommerce.report.dto.ReportRequest;
import com.example.ecommerce.report.dto.ReportSummaryResponse;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(ReportRepository reportRepository, ListingRepository listingRepository,
                              UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void submit(UUID reporterId, ReportRequest request) {
        boolean hasListing = request.listingId() != null;
        boolean hasUser = request.userId() != null;
        if (hasListing == hasUser) {
            throw new InvalidReportTargetException();
        }

        if (hasListing) {
            Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(ListingNotFoundException::new);
            if (listing.getStatus() == ListingStatus.ACTIVE) {
                listing.setStatus(ListingStatus.FLAGGED);
                listingRepository.save(listing);
            }
        } else {
            if (!userRepository.existsById(request.userId())) {
                throw new UserNotFoundException();
            }
        }

        Report report = Report.builder()
            .reporterId(reporterId)
            .reportedListingId(request.listingId())
            .reportedUserId(request.userId())
            .reason(request.reason())
            .status(ReportStatus.OPEN)
            .build();
        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportSummaryResponse> listOpen() {
        return toSummaries(reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN));
    }

    @Override
    @Transactional
    public void dismiss(UUID adminId, UUID reportId) {
        Report report = requireReport(reportId);
        report.setStatus(ReportStatus.DISMISSED);
        report.setResolvedBy(adminId);
        report.setResolvedAt(Instant.now());
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void resolve(UUID adminId, UUID reportId) {
        Report report = requireReport(reportId);
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(adminId);
        report.setResolvedAt(Instant.now());
        reportRepository.save(report);
    }

    private Report requireReport(UUID reportId) {
        return reportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
    }

    private List<ReportSummaryResponse> toSummaries(List<Report> reports) {
        if (reports.isEmpty()) {
            return List.of();
        }

        List<UUID> reporterIds = reports.stream().map(Report::getReporterId).distinct().toList();
        List<UUID> reportedUserIds = reports.stream()
            .map(Report::getReportedUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<UUID> reportedListingIds = reports.stream()
            .map(Report::getReportedListingId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        Map<UUID, SellerSummaryResponse> usersById = new HashMap<>();
        for (User user : userRepository.findAllById(concatIds(reporterIds, reportedUserIds))) {
            usersById.put(user.getId(), SellerSummaryResponse.from(user));
        }

        Map<UUID, String> listingTitleById = new HashMap<>();
        for (Listing listing : listingRepository.findAllById(reportedListingIds)) {
            listingTitleById.put(listing.getId(), listing.getTitle());
        }

        return reports.stream()
            .map(report -> new ReportSummaryResponse(
                report.getId(),
                usersById.get(report.getReporterId()),
                report.getReportedListingId(),
                report.getReportedListingId() == null ? null : listingTitleById.get(report.getReportedListingId()),
                report.getReportedUserId() == null ? null : usersById.get(report.getReportedUserId()),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()))
            .toList();
    }

    private List<UUID> concatIds(List<UUID> a, List<UUID> b) {
        return Stream.concat(a.stream(), b.stream()).distinct().toList();
    }
}
