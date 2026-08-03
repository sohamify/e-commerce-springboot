package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the refresh cookie is missing, expired, already-revoked, or reused after rotation. */
public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", message);
    }
}
