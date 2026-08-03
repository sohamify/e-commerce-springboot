package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ListingNotFoundException extends ApiException {

    public ListingNotFoundException() {
        super(HttpStatus.NOT_FOUND, "LISTING_NOT_FOUND", "Listing not found");
    }
}
