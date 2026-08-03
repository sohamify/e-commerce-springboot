package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ReportNotFoundException extends ApiException {

    public ReportNotFoundException() {
        super(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found");
    }
}
