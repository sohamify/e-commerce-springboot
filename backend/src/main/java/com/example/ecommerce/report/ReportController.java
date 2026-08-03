package com.example.ecommerce.report;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.report.dto.ReportRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Void> submit(@AuthenticationPrincipal JwtPrincipal principal, @Valid @RequestBody ReportRequest request) {
        reportService.submit(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
