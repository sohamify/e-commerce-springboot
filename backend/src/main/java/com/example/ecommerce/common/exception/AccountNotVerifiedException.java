package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class AccountNotVerifiedException extends ApiException {

    public AccountNotVerifiedException() {
        super(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_VERIFIED", "Please verify your email before logging in");
    }
}
