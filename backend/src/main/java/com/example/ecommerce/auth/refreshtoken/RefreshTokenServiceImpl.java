package com.example.ecommerce.auth.refreshtoken;

import com.example.ecommerce.common.exception.InvalidRefreshTokenException;
import com.example.ecommerce.common.security.SecureTokenGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final RefreshTokenProperties properties;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository, RefreshTokenProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public String issue(UUID userId) {
        String rawToken = SecureTokenGenerator.generate();
        RefreshToken entity = RefreshToken.builder()
            .userId(userId)
            .tokenHash(SecureTokenGenerator.hash(rawToken))
            .expiresAt(Instant.now().plus(properties.ttl()))
            .build();
        repository.save(entity);
        return rawToken;
    }

    @Override
    @Transactional
    public RotatedToken rotate(String rawToken) {
        RefreshToken existing = repository.findByTokenHash(SecureTokenGenerator.hash(rawToken))
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        if (existing.getRevokedAt() != null) {
            repository.revokeAllActiveForUser(existing.getUserId(), Instant.now());
            throw new InvalidRefreshTokenException("Refresh token already used; all sessions revoked");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        existing.setRevokedAt(Instant.now());
        repository.save(existing);

        return new RotatedToken(existing.getUserId(), issue(existing.getUserId()));
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(SecureTokenGenerator.hash(rawToken))
            .ifPresent(token -> {
                token.setRevokedAt(Instant.now());
                repository.save(token);
            });
    }

    @Override
    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllActiveForUser(userId, Instant.now());
    }

    @Override
    public Duration ttl() {
        return properties.ttl();
    }
}
