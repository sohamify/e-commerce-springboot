package com.example.ecommerce.report;

import com.example.ecommerce.report.dto.ReportRequest;
import com.example.ecommerce.report.dto.ReportSummaryResponse;
import java.util.List;
import java.util.UUID;

public interface ReportService {

    void submit(UUID reporterId, ReportRequest request);

    List<ReportSummaryResponse> listOpen();

    void dismiss(UUID adminId, UUID reportId);

    void resolve(UUID adminId, UUID reportId);
}
