package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class SellerPayoutNotReadyException extends ApiException {

    public SellerPayoutNotReadyException() {
        super(HttpStatus.CONFLICT, "SELLER_PAYOUT_NOT_READY",
            "The seller hasn't finished setting up payouts yet, so this listing can't be bought right now");
    }
}
