package com.dhatvibs.modules.config.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenValidator {

    public Claims verifyAndGetClaims(
            String token,
            List<String> secrets) {
        if (token == null || token.isBlank()) {
            return null;
        }
        for (String secret : secrets) {
            if (secret == null || secret.isBlank()) {
                continue;
            }
            try {
                Key key = signingKey(secret);
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                return claims;
            } catch (ExpiredJwtException e) {
                log.warn("JWT expired");
                return null;
            } catch (SignatureException | IllegalArgumentException e) {
                log.debug("JWT verify failed for one secret: {}",
                          e.getMessage());
            } catch (Exception e) {
                log.debug("JWT parse failed: {}", e.getMessage());
            }
        }
        return null;
    }

    public String extractFirstPresent(
            Claims claims,
            String... fields) {
        if (claims == null) {
            return null;
        }
        for (String field : fields) {
            Object value = claims.get(field);
            if (value != null) {
                String text = value.toString();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        String sub = claims.getSubject();
        return (sub != null && !sub.isBlank()) ? sub : null;
    }

    public String extractFromToken(
            String token,
            List<String> secrets,
            String... idFields) {
        Claims claims = verifyAndGetClaims(token, secrets);
        return extractFirstPresent(claims, idFields);
    }

    public <T> T extractFromToken(
            String token,
            List<String> secrets,
            Function<Claims, T> mapper) {
        Claims claims = verifyAndGetClaims(token, secrets);
        if (claims == null) {
            return null;
        }
        return mapper.apply(claims);
    }

    private static Key signingKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
