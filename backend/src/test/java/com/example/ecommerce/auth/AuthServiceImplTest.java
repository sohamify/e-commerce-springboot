package com.example.ecommerce.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ecommerce.auth.dto.LoginRequest;
import com.example.ecommerce.auth.dto.RegisterRequest;
import com.example.ecommerce.auth.event.PasswordResetRequestedEvent;
import com.example.ecommerce.auth.event.UserRegisteredEvent;
import com.example.ecommerce.auth.jwt.JwtService;
import com.example.ecommerce.auth.passwordreset.PasswordResetService;
import com.example.ecommerce.auth.refreshtoken.RefreshTokenService;
import com.example.ecommerce.auth.verification.EmailVerificationService;
import com.example.ecommerce.common.exception.AccountNotVerifiedException;
import com.example.ecommerce.common.exception.EmailAlreadyRegisteredException;
import com.example.ecommerce.common.exception.InvalidCredentialsException;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import com.example.ecommerce.user.UserRole;
import com.example.ecommerce.user.UserStatus;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private PasswordResetService passwordResetService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, refreshTokenService,
            emailVerificationService, passwordResetService, eventPublisher);
    }

    @Test
    void register_newEmail_createsUnverifiedCustomerAndPublishesEvent() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        authService.register(new RegisterRequest("new@example.com", "password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(UserRole.CUSTOMER, saved.getRole());
        assertFalse(saved.isEmailVerified());
        assertEquals("hashed", saved.getPasswordHash());

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("new@example.com", eventCaptor.getValue().email());
    }

    @Test
    void register_existingEmail_throws() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
            () -> authService.register(new RegisterRequest("taken@example.com", "password123")));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void login_correctPasswordAndVerified_issuesSession() {
        User user = verifiedUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.issueAccessToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("access-token");
        when(jwtService.accessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(refreshTokenService.issue(user.getId())).thenReturn("raw-refresh-token");
        when(refreshTokenService.ttl()).thenReturn(Duration.ofDays(30));

        AuthService.AuthTokens tokens = authService.login(new LoginRequest(user.getEmail(), "password123"));

        assertEquals("access-token", tokens.response().accessToken());
        assertEquals("raw-refresh-token", tokens.rawRefreshToken());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        User user = verifiedUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(new LoginRequest(user.getEmail(), "wrong")));
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentialsNotEnumeration() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(new LoginRequest("ghost@example.com", "whatever1")));
    }

    @Test
    void login_unverifiedAccount_throwsAccountNotVerified() {
        User user = verifiedUser();
        user.setEmailVerified(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        assertThrows(AccountNotVerifiedException.class,
            () -> authService.login(new LoginRequest(user.getEmail(), "password123")));
    }

    @Test
    void forgotPassword_unknownEmail_doesNotPublishEvent() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword("ghost@example.com");

        verify(eventPublisher, never()).publishEvent(any(PasswordResetRequestedEvent.class));
    }

    @Test
    void forgotPassword_knownEmail_publishesEvent() {
        User user = verifiedUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        authService.forgotPassword(user.getEmail());

        ArgumentCaptor<PasswordResetRequestedEvent> captor = ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(user.getId(), captor.getValue().userId());
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndRevokesAllSessions() {
        User user = verifiedUser();
        when(passwordResetService.consumeToken("raw-token")).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword1")).thenReturn("new-hashed");

        authService.resetPassword("raw-token", "newpassword1");

        verify(userRepository).save(user);
        assertEquals("new-hashed", user.getPasswordHash());
        verify(refreshTokenService, times(1)).revokeAllForUser(user.getId());
    }

    private User verifiedUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .email("verified@example.com")
            .passwordHash("hashed")
            .role(UserRole.CUSTOMER)
            .emailVerified(true)
            .status(UserStatus.ACTIVE)
            .build();
    }
}
