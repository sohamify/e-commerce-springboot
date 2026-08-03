package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ListingNotEditableException extends ApiException {

    public ListingNotEditableException() {
        super(HttpStatus.CONFLICT, "LISTING_NOT_EDITABLE", "This listing can no longer be edited or removed");
    }
}
