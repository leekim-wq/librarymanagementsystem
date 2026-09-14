package com.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for generating and validating JWT tokens.
 * Used by the mobile/API authentication flow.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:ChangeThisToALongRandomSecretKeyAtLeast32CharsLongForSecurity!}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long expiration;

    /**
     * Get the HMAC signing key derived from the configured secret.
     * Must be at least 32 characters for HS256.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate a JWT token for a given member.
     */
    public String generateToken(String email, String role, Long memberId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("memberId", memberId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract all claims from a signed token.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate a token's signature and expiry.
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public Long getMemberId(String token) {
        return extractClaims(token).get("memberId", Long.class);
    }
}