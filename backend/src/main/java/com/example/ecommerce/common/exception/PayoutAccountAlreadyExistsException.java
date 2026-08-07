package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class PayoutAccountAlreadyExistsException extends ApiException {

    public PayoutAccountAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "PAYOUT_ACCOUNT_ALREADY_EXISTS", "A payout account has already been set up");
    }
}
