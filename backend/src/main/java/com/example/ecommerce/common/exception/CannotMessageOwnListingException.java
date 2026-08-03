package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class CannotMessageOwnListingException extends ApiException {

    public CannotMessageOwnListingException() {
        super(HttpStatus.BAD_REQUEST, "CANNOT_MESSAGE_OWN_LISTING", "You can't start a message thread on your own listing");
    }
}
