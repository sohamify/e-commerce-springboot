package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ThreadAccessDeniedException extends ApiException {

    public ThreadAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "THREAD_ACCESS_DENIED", "You're not a participant in this message thread");
    }
}
