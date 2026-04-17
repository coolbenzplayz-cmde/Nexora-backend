package org.example.nexora.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_TYPE = "type";
    private static final String REFRESH = "refresh";

    private final Key secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration:604800000}") long refreshExpirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 bytes (UTF-8)");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /** Legacy helper: access token with email subject only (no uid claim). */
    public String generateToken(String email) {
        return buildAccessToken(null, email);
    }

    public String generateToken(Long userId, String email) {
        return buildAccessToken(userId, email);
    }

    private String buildAccessToken(Long userId, String email) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256);
        if (userId != null) {
            builder.claim(CLAIM_UID, String.valueOf(userId));
        }
        return builder.compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(CLAIM_TYPE, REFRESH)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the numeric user id as a string from either a refresh token (subject)
     * or an access token ({@code uid} claim).
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (REFRESH.equals(claims.get(CLAIM_TYPE))) {
            return claims.getSubject();
        }
        Object uid = claims.get(CLAIM_UID);
        if (uid != null) {
            return uid.toString();
        }
        return null;
    }

    public boolean isTokenValid(String token, String email) {
        try {
            String extractedEmail = extractEmail(token);
            return extractedEmail.equals(email) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
