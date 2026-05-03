package com.arteisis.service;

import com.arteisis.model.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(
            @Value("${arteisis.jwt.secret}") String secret,
            @Value("${arteisis.jwt.expiration-seconds:86400}") long expirationSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("arteisis.jwt.secret must be at least 32 characters (UTF-8) for HS256.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(AppUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
