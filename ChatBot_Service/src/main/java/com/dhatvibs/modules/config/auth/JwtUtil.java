package com.dhatvibs.modules.config.auth;



import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry}")
    private long expiry;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates token.
     * When decoded in jwt.io you will see:
     * {
     *   "sub":      "USR_001",         ← userId
     *   "appId":    "USER",            ← appId
     *   "role":     "USER",
     *   "fullName": "Ravi Kumar",
     *   "iat":      1234567890,
     *   "exp":      1234654290
     * }
     */
    public String generateToken(String externalUserId,
                                 String appId,
                                 String role,
                                 String fullName) {
        return Jwts.builder()
            .setSubject(externalUserId)          // userId
            .claim("appId",    appId)            // appId
            .claim("role",     role)
            .claim("fullName", fullName)
            .setIssuedAt(new Date())
            .setExpiration(
                new Date(System.currentTimeMillis() + expiry))
            .signWith(getSigningKey(),
                      SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String extractUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String extractAppId(String token) {
        return parseToken(token).get("appId", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
}