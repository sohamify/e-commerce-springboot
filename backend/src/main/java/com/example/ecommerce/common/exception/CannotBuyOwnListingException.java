package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class CannotBuyOwnListingException extends ApiException {

    public CannotBuyOwnListingException() {
        super(HttpStatus.BAD_REQUEST, "CANNOT_BUY_OWN_LISTING", "You can't buy your own listing");
    }
}
