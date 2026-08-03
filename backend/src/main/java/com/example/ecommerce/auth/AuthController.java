package com.example.ecommerce.auth;

import com.example.ecommerce.auth.dto.AuthResponse;
import com.example.ecommerce.auth.dto.ForgotPasswordRequest;
import com.example.ecommerce.auth.dto.LoginRequest;
import com.example.ecommerce.auth.dto.MessageResponse;
import com.example.ecommerce.auth.dto.RegisterRequest;
import com.example.ecommerce.auth.dto.ResendVerificationRequest;
import com.example.ecommerce.auth.dto.ResetPasswordRequest;
import com.example.ecommerce.auth.dto.UserSummaryResponse;
import com.example.ecommerce.auth.dto.VerifyEmailRequest;
import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.common.exception.InvalidRefreshTokenException;
import com.example.ecommerce.config.CookieProperties;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final AuthService authService;
    private final CookieProperties cookieProperties;

    public AuthController(AuthService authService, CookieProperties cookieProperties) {
        this.authService = authService;
        this.cookieProperties = cookieProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse("Registration successful. Check your email to verify your account."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withRefreshCookie(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("No refresh token presented");
        }
        return withRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString())
            .body(new MessageResponse("Logged out"));
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return new MessageResponse("Email verified. You can now log in.");
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
        return new MessageResponse("If that account exists and isn't verified yet, a new verification email is on its way.");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return new MessageResponse("If that email is registered, a reset link is on its way.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return new MessageResponse("Password reset. You can now log in with your new password.");
    }

    @GetMapping("/me")
    public UserSummaryResponse me(@AuthenticationPrincipal JwtPrincipal principal) {
        return authService.me(principal.userId());
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthService.AuthTokens tokens) {
        ResponseCookie cookie = buildCookie(tokens.rawRefreshToken(), tokens.refreshTokenTtl());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(tokens.response());
    }

    private ResponseCookie buildCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite(cookieProperties.sameSite())
            .path(REFRESH_COOKIE_PATH)
            .maxAge(maxAge)
            .build();
    }
}
