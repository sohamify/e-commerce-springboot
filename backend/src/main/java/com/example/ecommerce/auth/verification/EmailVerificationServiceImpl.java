package com.example.ecommerce.auth.verification;

import com.example.ecommerce.common.exception.InvalidOrExpiredTokenException;
import com.example.ecommerce.common.security.SecureTokenGenerator;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailVerificationProperties properties;

    public EmailVerificationServiceImpl(EmailVerificationTokenRepository tokenRepository,
                                         UserRepository userRepository,
                                         EmailVerificationProperties properties) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
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
        EmailVerificationToken token = EmailVerificationToken.builder()
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
        EmailVerificationToken token = tokenRepository.findByTokenHash(SecureTokenGenerator.hash(rawToken))
            .filter(EmailVerificationToken::isActive)
            .orElseThrow(() -> new InvalidOrExpiredTokenException("Verification link is invalid or has expired"));

        token.setConsumedAt(Instant.now());
        tokenRepository.save(token);

        User user = userRepository.findById(token.getUserId())
            .orElseThrow(() -> new InvalidOrExpiredTokenException("Verification link is invalid or has expired"));
        user.setEmailVerified(true);
        userRepository.save(user);

        return user.getId();
    }
}
