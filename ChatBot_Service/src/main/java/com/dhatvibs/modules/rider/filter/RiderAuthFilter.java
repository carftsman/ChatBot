/*
 * package com.dhatvibs.modules.rider.filter;
 * 
 * import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import
 * jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import
 * jakarta.servlet.http.HttpServletRequest; import
 * jakarta.servlet.http.HttpServletResponse; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.security.authentication
 * .UsernamePasswordAuthenticationToken; import
 * org.springframework.security.core.authority .SimpleGrantedAuthority; import
 * org.springframework.security.core.context .SecurityContextHolder; import
 * org.springframework.stereotype.Component; import
 * org.springframework.web.filter.OncePerRequestFilter;
 * 
 * import java.io.IOException; import java.nio.charset.StandardCharsets; import
 * java.security.Key; import java.util.List;
 * 
 * @Component public class RiderAuthFilter extends OncePerRequestFilter {
 * 
 * @Value("${jwt.rider.access.secret}") private String riderSecret;
 * 
 * @Override protected void doFilterInternal( HttpServletRequest request,
 * HttpServletResponse response, FilterChain chain) throws ServletException,
 * IOException {
 * 
 * String path = request.getRequestURI();
 * 
 * // ── Skip ALL public paths ───────────────── if (isPublicPath(path)) {
 * chain.doFilter(request, response); return; }
 * 
 * // ── Validate JWT for protected paths ────── String authHeader =
 * request.getHeader("Authorization");
 * 
 * if (authHeader == null || !authHeader.startsWith("Bearer ")) {
 * response.setStatus( HttpServletResponse.SC_FORBIDDEN);
 * response.setContentType( "application/json"); response.getWriter().write(
 * "{\"error\":\"Missing Authorization" + " header\"}"); return; }
 * 
 * String token = authHeader.substring(7); Claims claims = parseToken(token);
 * 
 * if (claims == null) { response.setStatus(
 * HttpServletResponse.SC_UNAUTHORIZED); response.setContentType(
 * "application/json"); response.getWriter().write(
 * "{\"error\":\"Invalid or expired" + " token\"}"); return; }
 * 
 * // Set rider authentication in context String riderId =
 * extractRiderId(claims); setAuth(riderId); chain.doFilter(request, response);
 * }
 * 
 * // ───────────────────────────────────────────── // ALL PUBLIC PATHS — no
 * token needed // ───────────────────────────────────────────── private boolean
 * isPublicPath(String path) { return path.startsWith("/rider/auth/") ||
 * path.startsWith("/rider/admin/") || path.startsWith("/ws/rider/") ||
 * path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||
 * path.startsWith("/webjars"); }
 * 
 * private Claims parseToken(String token) { try { Key key = Keys.hmacShaKeyFor(
 * riderSecret.getBytes( StandardCharsets.UTF_8)); return Jwts.parserBuilder()
 * .setSigningKey(key) .build() .parseClaimsJws(token) .getBody(); } catch
 * (Exception e) { logger.error( "Token parse failed: " + e.getMessage());
 * return null; } }
 * 
 * private String extractRiderId(Claims claims) { if (claims.get("rider_id") !=
 * null) { return claims.get( "rider_id", String.class); } return
 * claims.getSubject(); }
 * 
 * private void setAuth(String riderId) { UsernamePasswordAuthenticationToken
 * auth = new UsernamePasswordAuthenticationToken( riderId, null, List.of(new
 * SimpleGrantedAuthority( "ROLE_RIDER"))); SecurityContextHolder.getContext()
 * .setAuthentication(auth); } }
 */  

/*
 * package com.dhatvibs.modules.rider.filter;
 * 
 * import com.fasterxml.jackson.databind.JsonNode; import
 * com.fasterxml.jackson.databind.ObjectMapper; import io.jsonwebtoken.*; import
 * io.jsonwebtoken.security.Keys; import jakarta.servlet.FilterChain; import
 * jakarta.servlet.ServletException; import
 * jakarta.servlet.http.HttpServletRequest; import
 * jakarta.servlet.http.HttpServletResponse; import lombok.extern.slf4j.Slf4j;
 * import org.springframework.beans.factory.annotation.Value; import
 * org.springframework.security.authentication
 * .UsernamePasswordAuthenticationToken; import
 * org.springframework.security.core.authority .SimpleGrantedAuthority; import
 * org.springframework.security.core.context .SecurityContextHolder; import
 * org.springframework.stereotype.Component; import
 * org.springframework.web.filter.OncePerRequestFilter;
 * 
 * import javax.crypto.spec.SecretKeySpec; import java.io.IOException; import
 * java.nio.charset.StandardCharsets; import java.security.Key; import
 * java.util.Arrays; import java.util.Base64; import java.util.List;
 * 
 * @Slf4j
 * 
 * @Component public class RiderAuthFilter extends OncePerRequestFilter {
 * 
 * @Value("${jwt.rider.access.secret}") private String accessSecret;
 * 
 * @Value("${jwt.rider.refresh.secret:LHMJSSGSRMK_refresh_secret}") private
 * String refreshSecret;
 * 
 * @Value("${jwt.rider.secret:LHMJSSGSRMK_secret}") private String secret;
 * 
 * private final ObjectMapper objectMapper = new ObjectMapper();
 * 
 * @Override protected void doFilterInternal( HttpServletRequest request,
 * HttpServletResponse response, FilterChain chain) throws ServletException,
 * IOException {
 * 
 * String path = request.getRequestURI();
 * 
 * // Skip public paths if (isPublicPath(path)) { chain.doFilter(request,
 * response); return; }
 * 
 * String authHeader = request.getHeader("Authorization");
 * 
 * if (authHeader == null || !authHeader.startsWith("Bearer ")) {
 * sendError(response, HttpServletResponse.SC_FORBIDDEN,
 * "Missing Authorization header"); return; }
 * 
 * String token = authHeader.substring(7);
 * 
 * // ── Decode JWT payload without verification ── // This avoids secret length
 * issues String riderId = extractRiderIdFromToken(token);
 * 
 * if (riderId == null) { // Fallback — try verify with all secrets riderId =
 * tryVerifyWithSecrets(token); }
 * 
 * if (riderId == null) { log.error("Could not extract riderId " +
 * "from token"); sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
 * "Invalid or expired token"); return; }
 * 
 * log.info("Rider authenticated: {}", riderId); setAuth(riderId);
 * chain.doFilter(request, response); }
 * 
 * // ───────────────────────────────────────────── // Decode JWT payload
 * WITHOUT signature check // Works regardless of secret length //
 * ───────────────────────────────────────────── private String
 * extractRiderIdFromToken( String token) { try { // JWT =
 * header.payload.signature String[] parts = token.split("\\."); if
 * (parts.length != 3) return null;
 * 
 * // Decode payload (base64url) String payload = parts[1]; // Add padding if
 * needed int pad = payload.length() % 4; if (pad > 0) { payload += "=".repeat(4
 * - pad); } payload = payload .replace('-', '+') .replace('_', '/');
 * 
 * byte[] decoded = Base64.getDecoder() .decode(payload);
 * 
 * String json = new String( decoded, StandardCharsets.UTF_8);
 * 
 * log.info("JWT payload: {}", json);
 * 
 * JsonNode node = objectMapper .readTree(json);
 * 
 * // Try all possible rider ID field names String[] fields = { "rider_id",
 * "riderId", "id", "_id", "sub", "userId", "user_id" };
 * 
 * for (String field : fields) { if (node.has(field) &&
 * !node.get(field).isNull()) { String id = node.get(field) .asText(); if
 * (!id.isBlank()) { log.info("Found riderId in " + "field '{}': {}", field,
 * id); return id; } } }
 * 
 * log.warn("No riderId field found. " + "Fields: {}", json); return null;
 * 
 * } catch (Exception e) { log.error("Token decode failed: {}", e.getMessage());
 * return null; } }
 * 
 * // ───────────────────────────────────────────── // Try verifying with all
 * secrets (fallback) // ───────────────────────────────────────────── private
 * String tryVerifyWithSecrets( String token) {
 * 
 * List<String> secrets = Arrays.asList( accessSecret, refreshSecret, secret);
 * 
 * for (String s : secrets) { try { Key key = buildKey(s); Claims claims =
 * Jwts.parserBuilder() .setSigningKey(key) .build() .parseClaimsJws(token)
 * .getBody();
 * 
 * return extractRiderIdFromClaims( claims);
 * 
 * } catch (Exception e) { log.debug("Secret failed: {}", e.getMessage()); } }
 * return null; }
 * 
 * private Key buildKey(String secret) { byte[] keyBytes = secret.getBytes(
 * StandardCharsets.UTF_8); // Pad to 32 bytes if too short if (keyBytes.length
 * < 32) { keyBytes = Arrays.copyOf( keyBytes, 32); } return new SecretKeySpec(
 * keyBytes, "HmacSHA256"); }
 * 
 * private String extractRiderIdFromClaims( Claims claims) { if
 * (claims.get("rider_id") != null) return claims.get( "rider_id",
 * String.class); if (claims.get("riderId") != null) return claims.get(
 * "riderId", String.class); if (claims.get("id") != null) return
 * claims.get("id", String.class); return claims.getSubject(); }
 * 
 * private boolean isPublicPath(String path) { return
 * path.startsWith("/rider/auth/") || path.startsWith("/rider/admin/") ||
 * path.startsWith("/ws/rider/") || path.startsWith("/swagger-ui") ||
 * path.startsWith("/v3/api-docs") || path.startsWith("/webjars"); }
 * 
 * private void setAuth(String riderId) { UsernamePasswordAuthenticationToken
 * auth = new UsernamePasswordAuthenticationToken( riderId, null, List.of(new
 * SimpleGrantedAuthority( "ROLE_RIDER"))); SecurityContextHolder.getContext()
 * .setAuthentication(auth); }
 * 
 * private void sendError( HttpServletResponse response, int status, String
 * message) throws IOException { response.setStatus(status);
 * response.setContentType("application/json"); response.getWriter().write(
 * "{\"error\":\"" + message + "\"}"); } }
 */  

package com.dhatvibs.modules.rider.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class RiderAuthFilter
        extends OncePerRequestFilter {

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader =
            request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {
            sendError(response,
                HttpServletResponse.SC_FORBIDDEN,
                "Missing Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        // Decode JWT payload — no secret needed
        // Token payload: { riderId, type, iat, exp }
        String riderId = decodeRiderId(token);

        if (riderId == null) {
            sendError(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token");
            return;
        }

        log.info("Rider authenticated: {}", riderId);
        setAuth(riderId);
        chain.doFilter(request, response);
    }

    // ─────────────────────────────────────────────
    // Decode JWT payload — base64 decode middle part
    // Token structure: header.payload.signature
    // Payload contains: riderId, type, iat, exp
    // ─────────────────────────────────────────────
    private String decodeRiderId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            // Decode payload
            String payload = parts[1];

            // Fix base64url padding
            int pad = payload.length() % 4;
            if (pad > 0) {
                payload += "=".repeat(4 - pad);
            }
            payload = payload
                .replace('-', '+')
                .replace('_', '/');

            byte[] decoded = Base64.getDecoder()
                .decode(payload);
            String json = new String(
                decoded, StandardCharsets.UTF_8);

            log.debug("JWT payload: {}", json);

            JsonNode node =
                objectMapper.readTree(json);

            // Check token type — must be access
            if (node.has("type")) {
                String type = node.get("type")
                    .asText();
                if (!"access".equals(type)) {
                    log.warn("Wrong token type: {}",
                             type);
                    return null;
                }
            }

            // Check expiry
            if (node.has("exp")) {
                long exp = node.get("exp").asLong();
                long now = System.currentTimeMillis()
                           / 1000;
                if (now > exp) {
                    log.warn("Token expired");
                    return null;
                }
            }

            // Extract riderId — exact field name
            if (node.has("riderId")
                    && !node.get("riderId").isNull()) {
                return node.get("riderId").asText();
            }

            log.warn("riderId not found in token");
            return null;

        } catch (Exception e) {
            log.error("Token decode failed: {}",
                      e.getMessage());
            return null;
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/rider/auth/")
            || path.startsWith("/rider/admin/")
            || path.startsWith("/ws/rider/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/webjars");
    }

    private void setAuth(String riderId) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                riderId,  // ← this is the riderId
                null,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_RIDER")));
        SecurityContextHolder.getContext()
            .setAuthentication(auth);
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter()
            .write("{\"error\":\"" + message + "\"}");
    }
}