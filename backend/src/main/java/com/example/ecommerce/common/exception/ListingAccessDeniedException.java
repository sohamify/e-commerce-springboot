package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ListingAccessDeniedException extends ApiException {

    public ListingAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "LISTING_ACCESS_DENIED", "You don't have access to modify this listing");
    }
}
