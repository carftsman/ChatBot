/*
 * package com.dhatvibs.modules.consumer.filter;
 * 
 * import com.dhatvibs.modules.config.auth.JwtTokenValidator;
 * 
 * import io.jsonwebtoken.Claims; import jakarta.servlet.FilterChain; import
 * jakarta.servlet.ServletException; import
 * jakarta.servlet.http.HttpServletRequest; import
 * jakarta.servlet.http.HttpServletResponse; import
 * lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.security.authentication
 * .UsernamePasswordAuthenticationToken; import
 * org.springframework.security.core.authority .SimpleGrantedAuthority; import
 * org.springframework.security.core.context .SecurityContextHolder; import
 * org.springframework.stereotype.Component; import
 * org.springframework.web.filter .OncePerRequestFilter;
 * 
 * import java.io.IOException; import java.util.List;
 * 
 * @Slf4j
 * 
 * @Component
 * 
 * @RequiredArgsConstructor public class ConsumerAuthFilter extends
 * OncePerRequestFilter {
 * 
 * private final JwtTokenValidator jwtTokenValidator;
 * 
 * @Value("${jwt.swachvega.secret:${jwt.secret:}}") private String
 * swachvegaSecret;
 * 
 * @Override protected void doFilterInternal( HttpServletRequest request,
 * HttpServletResponse response, FilterChain chain) throws ServletException,
 * IOException {
 * 
 * String path = request.getRequestURI();
 * 
 * if (isPublicPath(path)) { chain.doFilter(request, response); return; }
 * 
 * String authHeader = request.getHeader("Authorization");
 * 
 * if (authHeader == null || !authHeader.startsWith("Bearer ")) {
 * sendError(response, HttpServletResponse.SC_FORBIDDEN,
 * "Missing Authorization header"); return; }
 * 
 * String token = authHeader.substring(7); String consumerId =
 * resolveConsumerId(token);
 * 
 * if (consumerId == null) { sendError(response,
 * HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token"); return; }
 * 
 * log.info("Consumer authenticated: {}", consumerId); setAuth(consumerId);
 * chain.doFilter(request, response); }
 * 
 * private String resolveConsumerId(String token) { Claims claims =
 * jwtTokenValidator.verifyAndGetClaims( token, List.of(swachvegaSecret)); if
 * (claims == null) { return null; } return
 * jwtTokenValidator.extractFirstPresent( claims, "sub", "userId", "id",
 * "consumerId", "user_id", "phoneNumber"); }
 * 
 * private boolean isPublicPath(String path) { return
 * path.startsWith("/consumer/auth/") || path.startsWith("/ws/consumer/") ||
 * path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||
 * path.startsWith("/webjars"); }
 * 
 * private void setAuth(String consumerId) { UsernamePasswordAuthenticationToken
 * auth = new UsernamePasswordAuthenticationToken( consumerId, null, List.of(new
 * SimpleGrantedAuthority( "ROLE_CONSUMER")));
 * SecurityContextHolder.getContext() .setAuthentication(auth); }
 * 
 * private void sendError( HttpServletResponse response, int status, String
 * message) throws IOException { response.setStatus(status);
 * response.setContentType("application/json"); response.getWriter()
 * .write("{\"error\":\"" + message + "\"}"); }
 * 
 * @Override protected boolean shouldNotFilter( HttpServletRequest request) {
 * String path = request.getRequestURI(); return path.startsWith("/rider/") ||
 * path.startsWith("/vendor/") || path.startsWith("/riderchatbot/") ||
 * path.contains("/admin/"); } }
 */


package com.dhatvibs.modules.consumer.filter;

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
import org.springframework.web.filter
        .OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class ConsumerAuthFilter
        extends OncePerRequestFilter {

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/rider/")
            || path.startsWith("/vendor/")
            || path.startsWith("/riderchatbot/")
            || path.contains("/admin/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

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
        String consumerId = decodeConsumerId(token);

        if (consumerId == null) {
            sendError(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token");
            return;
        }

        log.info("Consumer authenticated: {}",
                 consumerId);
        setAuth(consumerId);
        chain.doFilter(request, response);
    }

    private String decodeConsumerId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = parts[1];
            int pad = payload.length() % 4;
            if (pad > 0)
                payload += "=".repeat(4 - pad);
            payload = payload
                .replace('-', '+')
                .replace('_', '/');

            byte[] decoded = Base64.getDecoder()
                .decode(payload);
            String json = new String(
                decoded, StandardCharsets.UTF_8);

            log.info("Consumer JWT payload: {}",
                     json);

            JsonNode node =
                objectMapper.readTree(json);

            // Check expiry only — no signature check
            if (node.has("exp")) {
                long exp = node.get("exp").asLong();
                long now =
                    System.currentTimeMillis() / 1000;
                if (now > exp) {
                    log.warn("Token expired. "
                           + "exp={} now={}",
                             exp, now);
                    return null;
                }
            }

            for (String field : List.of(
                    "sub", "userId", "id",
                    "consumerId", "user_id",
                    "phoneNumber")) {
                if (node.has(field)
                        && !node.get(field).isNull()
                        && !node.get(field).asText()
                                .isBlank()) {
                    log.info("Found consumerId "
                           + "in '{}': {}",
                             field,
                             node.get(field).asText());
                    return node.get(field).asText();
                }
            }

            log.warn("No consumerId found in token. "
                   + "Payload: {}", json);
            return null;

        } catch (Exception e) {
            log.error("Token decode failed: {}",
                      e.getMessage());
            return null;
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/consumer/auth/")
            || path.startsWith("/ws/consumer/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/webjars");
    }

    private void setAuth(String consumerId) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                consumerId, null,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_CONSUMER")));
        SecurityContextHolder.getContext()
            .setAuthentication(auth);
    }

    private void sendError(
            HttpServletResponse response,
            int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter()
            .write("{\"error\":\""
                   + message + "\"}");
    }
}