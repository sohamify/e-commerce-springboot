package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentSignatureException extends ApiException {

    public InvalidPaymentSignatureException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_SIGNATURE", "Payment signature could not be verified");
    }
}
