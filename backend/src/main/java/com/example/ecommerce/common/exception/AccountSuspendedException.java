package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class AccountSuspendedException extends ApiException {

    public AccountSuspendedException() {
        super(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", "This account has been suspended. Contact support for details.");
    }
}
