package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotRefundableException extends ApiException {

    public PaymentNotRefundableException() {
        super(HttpStatus.CONFLICT, "PAYMENT_NOT_REFUNDABLE", "Only a captured payment can be refunded");
    }
}
