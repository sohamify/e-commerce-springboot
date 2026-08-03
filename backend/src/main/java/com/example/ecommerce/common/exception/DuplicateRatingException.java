package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRatingException extends ApiException {

    public DuplicateRatingException() {
        super(HttpStatus.CONFLICT, "DUPLICATE_RATING", "You've already rated this transaction");
    }
}
