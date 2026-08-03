package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class RatingNotAllowedException extends ApiException {

    public RatingNotAllowedException() {
        super(HttpStatus.FORBIDDEN, "RATING_NOT_ALLOWED",
            "Only the buyer and seller of a completed transaction can rate each other");
    }
}
