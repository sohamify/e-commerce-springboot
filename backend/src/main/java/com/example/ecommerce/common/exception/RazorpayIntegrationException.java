package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Wraps the checked {@code RazorpayException} the SDK throws on any failed API call, so callers
 * higher up don't need to know about a third-party checked exception type. */
public class RazorpayIntegrationException extends ApiException {

    public RazorpayIntegrationException(String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, "RAZORPAY_ERROR", message);
        initCause(cause);
    }
}
