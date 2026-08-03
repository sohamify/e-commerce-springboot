package com.example.ecommerce.auth;

import com.example.ecommerce.auth.dto.AuthResponse;
import com.example.ecommerce.auth.dto.LoginRequest;
import com.example.ecommerce.auth.dto.RegisterRequest;
import com.example.ecommerce.auth.dto.UserSummaryResponse;
import com.example.ecommerce.auth.event.PasswordResetRequestedEvent;
import com.example.ecommerce.auth.event.UserRegisteredEvent;
import com.example.ecommerce.auth.jwt.JwtService;
import com.example.ecommerce.auth.passwordreset.PasswordResetService;
import com.example.ecommerce.auth.refreshtoken.RefreshTokenService;
import com.example.ecommerce.auth.verification.EmailVerificationService;
import com.example.ecommerce.common.exception.AccountNotVerifiedException;
import com.example.ecommerce.common.exception.AccountSuspendedException;
import com.example.ecommerce.common.exception.EmailAlreadyRegisteredException;
import com.example.ecommerce.common.exception.InvalidCredentialsException;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import com.example.ecommerce.user.UserRole;
import com.example.ecommerce.user.UserStatus;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthServiceImpl(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService,
                            RefreshTokenService refreshTokenService,
                            EmailVerificationService emailVerificationService,
                            PasswordResetService passwordResetService,
                            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(UserRole.CUSTOMER)
            .emailVerified(false)
            .displayName(email.substring(0, email.indexOf('@')))
            .status(UserStatus.ACTIVE)
            .build();
        user = userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole()));
    }

    @Override
    @Transactional
    public AuthTokens login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isEmailVerified()) {
            throw new AccountNotVerifiedException();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountSuspendedException();
        }
        return issueSession(user);
    }

    @Override
    @Transactional
    public AuthTokens refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedToken rotated = refreshTokenService.rotate(rawRefreshToken);
        User user = userRepository.findById(rotated.userId())
            .orElseThrow(() -> new IllegalStateException("Refresh token references a user that no longer exists"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountSuspendedException();
        }

        String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), user.getRole());
        AuthResponse response = new AuthResponse(
            accessToken, "Bearer", jwtService.accessTokenTtl().toSeconds(), UserSummaryResponse.from(user));
        return new AuthTokens(response, rotated.newRawToken(), refreshTokenService.ttl());
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Override
    @Transactional
    public void verifyEmail(String rawToken) {
        emailVerificationService.consumeToken(rawToken);
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(normalize(email))
            .filter(user -> !user.isEmailVerified())
            .ifPresent(user -> eventPublisher.publishEvent(
                new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole())));
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(normalize(email))
            .ifPresent(user -> eventPublisher.publishEvent(
                new PasswordResetRequestedEvent(user.getId(), user.getEmail(), user.getRole())));
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        UUID userId = passwordResetService.consumeToken(rawToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Reset token references a user that no longer exists"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse me(UUID userId) {
        return userRepository.findById(userId)
            .map(UserSummaryResponse::from)
            .orElseThrow(InvalidCredentialsException::new);
    }

    private AuthTokens issueSession(User user) {
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), user.getRole());
        String rawRefreshToken = refreshTokenService.issue(user.getId());
        AuthResponse response = new AuthResponse(
            accessToken, "Bearer", jwtService.accessTokenTtl().toSeconds(), UserSummaryResponse.from(user));
        return new AuthTokens(response, rawRefreshToken, refreshTokenService.ttl());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
