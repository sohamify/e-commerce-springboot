package com.example.ecommerce.auth.passwordreset;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.consumedAt = :now WHERE t.userId = :userId AND t.consumedAt IS NULL")
    void invalidateAllActiveForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
