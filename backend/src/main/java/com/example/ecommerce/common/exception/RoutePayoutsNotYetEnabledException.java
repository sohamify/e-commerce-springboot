package com.example.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Route (Linked Accounts) hasn't been approved on the Razorpay account yet — refuse payout
 * onboarding cleanly instead of letting the request reach Razorpay and fail with a confusing
 * "Authentication failed"-style error. */
public class RoutePayoutsNotYetEnabledException extends ApiException {

    public RoutePayoutsNotYetEnabledException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "ROUTE_PAYOUTS_NOT_YET_ENABLED",
            "Payouts aren't available yet — check back soon");
    }
}
