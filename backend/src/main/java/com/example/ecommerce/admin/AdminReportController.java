package com.example.ecommerce.admin;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.report.ReportService;
import com.example.ecommerce.report.dto.ReportSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<ReportSummaryResponse> list() {
        return reportService.listOpen();
    }

    @PostMapping("/{id}/dismiss")
    public void dismiss(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable UUID id) {
        reportService.dismiss(principal.userId(), id);
    }

    @PostMapping("/{id}/resolve")
    public void resolve(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable UUID id) {
        reportService.resolve(principal.userId(), id);
    }
}
