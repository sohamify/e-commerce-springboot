package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidReportTargetException extends ApiException {

    public InvalidReportTargetException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_REPORT_TARGET", "A report must target exactly one listing or user");
    }
}
