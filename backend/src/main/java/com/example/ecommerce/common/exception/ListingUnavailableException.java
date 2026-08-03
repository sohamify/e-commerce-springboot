package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ListingUnavailableException extends ApiException {

    public ListingUnavailableException() {
        super(HttpStatus.CONFLICT, "LISTING_UNAVAILABLE", "This item is no longer available");
    }
}
