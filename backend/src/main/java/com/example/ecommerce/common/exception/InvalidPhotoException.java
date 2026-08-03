package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidPhotoException extends ApiException {

    public InvalidPhotoException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_PHOTO", message);
    }
}
