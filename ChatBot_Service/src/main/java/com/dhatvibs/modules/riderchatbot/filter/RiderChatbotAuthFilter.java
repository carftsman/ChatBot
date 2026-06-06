package com.dhatvibs.modules.riderchatbot.filter;

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
public class RiderChatbotAuthFilter
        extends OncePerRequestFilter {

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/consumer/")
            || path.startsWith("/vendor/")
            || path.startsWith("/rider/");
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

    private String decodeRiderId(String token) {
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

            log.info("Rider JWT payload: {}", json);

            JsonNode node =
                objectMapper.readTree(json);

            // Check expiry
            if (node.has("exp")) {
                long exp = node.get("exp").asLong();
                long now =
                    System.currentTimeMillis() / 1000;
                if (now > exp) {
                    log.warn("Token expired");
                    return null;
                }
            }

            // Try rider id fields
            for (String field : List.of(
                    "riderId", "rider_id",
                    "sub", "id", "userId")) {
                if (node.has(field)
                        && !node.get(field).isNull()
                        && !node.get(field).asText()
                                .isBlank()) {
                    log.info("Found riderId in '{}': {}",
                             field,
                             node.get(field).asText());
                    return node.get(field).asText();
                }
            }
            return null;

        } catch (Exception e) {
            log.error("Token decode failed: {}",
                      e.getMessage());
            return null;
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/riderchatbot/auth/")
            || path.startsWith("/riderchatbot/admin/")
            || path.startsWith("/ws/riderchatbot/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/webjars");
    }

    private void setAuth(String riderId) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                riderId, null,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_RIDER")));
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
            .write("{\"error\":\"" + message + "\"}");
    }
}