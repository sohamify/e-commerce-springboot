package com.example.ecommerce.auth.passwordreset;

import com.example.ecommerce.common.exception.InvalidOrExpiredTokenException;
import com.example.ecommerce.common.security.SecureTokenGenerator;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetProperties properties;

    public PasswordResetServiceImpl(PasswordResetTokenRepository tokenRepository, PasswordResetProperties properties) {
        this.tokenRepository = tokenRepository;
        this.properties = properties;
    }

    @Override
    // REQUIRES_NEW: this is invoked from an AFTER_COMMIT transactional event listener, where the
    // publishing transaction's resources are still thread-bound but already committed — joining
    // it (the REQUIRED default) fails with "No active transaction" on the bulk-update query below.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String issueToken(UUID userId) {
        tokenRepository.invalidateAllActiveForUser(userId, Instant.now());

        String rawToken = SecureTokenGenerator.generate();
        PasswordResetToken token = PasswordResetToken.builder()
            .userId(userId)
            .tokenHash(SecureTokenGenerator.hash(rawToken))
            .expiresAt(Instant.now().plus(properties.ttl()))
            .build();
        tokenRepository.save(token);
        return rawToken;
    }

    @Override
    @Transactional
    public UUID consumeToken(String rawToken) {
        PasswordResetToken token = tokenRepository.findByTokenHash(SecureTokenGenerator.hash(rawToken))
            .filter(PasswordResetToken::isActive)
            .orElseThrow(() -> new InvalidOrExpiredTokenException("Reset link is invalid or has expired"));

        token.setConsumedAt(Instant.now());
        tokenRepository.save(token);
        return token.getUserId();
    }
}
