package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown for a bad/expired/already-consumed email-verification or password-reset token. */
public class InvalidOrExpiredTokenException extends ApiException {

    public InvalidOrExpiredTokenException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_OR_EXPIRED_TOKEN", message);
    }
}
