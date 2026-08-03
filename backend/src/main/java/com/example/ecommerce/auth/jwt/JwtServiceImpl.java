package com.example.ecommerce.auth.jwt;

import com.example.ecommerce.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey signingKey;
    private final JwtProperties properties;

    public JwtServiceImpl(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issueAccessToken(UUID userId, String email, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role.name())
            .issuer(properties.issuer())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.accessTokenTtl())))
            .signWith(signingKey)
            .compact();
    }

    @Override
    public JwtPrincipal parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            return new JwtPrincipal(userId, email, role);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAccessTokenException();
        }
    }

    @Override
    public Duration accessTokenTtl() {
        return properties.accessTokenTtl();
    }
}
