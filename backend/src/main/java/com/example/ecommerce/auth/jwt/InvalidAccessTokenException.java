package com.example.ecommerce.auth.jwt;

/** Internal to the jwt package: signals a bad/expired token to {@link JwtAuthenticationFilter}, never to a client directly. */
class InvalidAccessTokenException extends RuntimeException {

    InvalidAccessTokenException() {
        super("Invalid or expired access token");
    }
}
