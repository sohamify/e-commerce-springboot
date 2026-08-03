package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class ThreadNotFoundException extends ApiException {

    public ThreadNotFoundException() {
        super(HttpStatus.NOT_FOUND, "THREAD_NOT_FOUND", "Message thread not found");
    }
}
