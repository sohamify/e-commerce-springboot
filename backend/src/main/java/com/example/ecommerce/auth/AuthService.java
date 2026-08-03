package com.example.ecommerce.auth;

import com.example.ecommerce.auth.dto.AuthResponse;
import com.example.ecommerce.auth.dto.LoginRequest;
import com.example.ecommerce.auth.dto.RegisterRequest;
import com.example.ecommerce.auth.dto.UserSummaryResponse;
import java.time.Duration;
import java.util.UUID;

public interface AuthService {

    void register(RegisterRequest request);

    AuthTokens login(LoginRequest request);

    AuthTokens refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);

    void verifyEmail(String rawToken);

    void resendVerification(String email);

    /** Always succeeds from the caller's perspective, whether or not the email is registered (no user enumeration). */
    void forgotPassword(String email);

    void resetPassword(String rawToken, String newPassword);

    UserSummaryResponse me(UUID userId);

    /** Everything the controller needs to build both the JSON body and the refresh cookie. */
    record AuthTokens(AuthResponse response, String rawRefreshToken, Duration refreshTokenTtl) {
    }
}
