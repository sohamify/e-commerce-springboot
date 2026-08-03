package com.example.ecommerce.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserSummaryResponse user) {
}
